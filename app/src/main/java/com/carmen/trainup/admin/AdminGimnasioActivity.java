package com.carmen.trainup.admin;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.carmen.trainup.R;
import com.carmen.trainup.utils.SupabaseConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class AdminGimnasioActivity extends AppCompatActivity {

    private ImageView imgGimnasio;
    private EditText etNombre, etDireccion, etCiudad, etEmail, etTelefono, etDescripcion;
    private Button btnGuardar, btnCambiarImagen;

    private final OkHttpClient client = new OkHttpClient();

    private String accessToken;
    private String emailUsuario;
    private int idGimnasio = -1;
    private String imagenActualUrl = "";

    private Uri imagenSeleccionadaUri = null;

    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    private static final MediaType IMAGE =
            MediaType.get("image/jpeg");

    private final ActivityResultLauncher<String> seleccionarImagenLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imagenSeleccionadaUri = uri;
                    imgGimnasio.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_gimnasio);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imgGimnasio = findViewById(R.id.imgGimnasio);
        etNombre = findViewById(R.id.etNombre);
        etDireccion = findViewById(R.id.etDireccion);
        etCiudad = findViewById(R.id.etCiudad);
        etEmail = findViewById(R.id.etEmail);
        etTelefono = findViewById(R.id.etTelefono);
        etDescripcion = findViewById(R.id.etDescripcion);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCambiarImagen = findViewById(R.id.btnCambiarImagen);

        SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
        emailUsuario = prefs.getString("email", "");

        if (accessToken.isEmpty() || emailUsuario.isEmpty()) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnCambiarImagen.setOnClickListener(v ->
                seleccionarImagenLauncher.launch("image/*")
        );

        btnGuardar.setOnClickListener(v -> {
            if (imagenSeleccionadaUri != null) {
                subirImagenYGuardarCambios();
            } else {
                guardarCambios(imagenActualUrl);
            }
        });

        cargarIdGimnasioDelUsuario();
    }

    private void cargarIdGimnasioDelUsuario() {
        try {
            String emailEncoded = URLEncoder.encode(emailUsuario, "UTF-8");

            String url = SupabaseConfig.SUPABASE_URL
                    + "/rest/v1/Usuario?Email_Usuario=eq."
                    + emailEncoded
                    + "&select=ID_Gimnasio";

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Accept", "application/json")
                    .get()
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, java.io.IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(AdminGimnasioActivity.this, "Error cargando usuario", Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                    String respuesta = response.body() != null ? response.body().string() : "";

                    Log.d("ADMIN_GIM_USER", "Código: " + response.code());
                    Log.d("ADMIN_GIM_USER", "Respuesta: " + respuesta);

                    if (response.isSuccessful()) {
                        try {
                            JSONArray array = new JSONArray(respuesta);

                            if (array.length() > 0 && !array.getJSONObject(0).isNull("ID_Gimnasio")) {
                                idGimnasio = array.getJSONObject(0).getInt("ID_Gimnasio");
                                cargarDatosGimnasio();
                            } else {
                                runOnUiThread(() ->
                                        Toast.makeText(AdminGimnasioActivity.this, "Este usuario no tiene gimnasio asignado", Toast.LENGTH_LONG).show()
                                );
                            }

                        } catch (Exception e) {
                            runOnUiThread(() ->
                                    Toast.makeText(AdminGimnasioActivity.this, "Error leyendo usuario", Toast.LENGTH_LONG).show()
                            );
                        }
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(AdminGimnasioActivity.this, "Error Supabase usuario: " + respuesta, Toast.LENGTH_LONG).show()
                        );
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void cargarDatosGimnasio() {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/Gimnasio?ID_Gimnasio=eq."
                + idGimnasio
                + "&select=*";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(AdminGimnasioActivity.this, "Error cargando gimnasio", Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                String respuesta = response.body() != null ? response.body().string() : "";

                Log.d("ADMIN_GIMNASIO", "Código: " + response.code());
                Log.d("ADMIN_GIMNASIO", "Respuesta: " + respuesta);

                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(respuesta);

                        if (array.length() > 0) {
                            JSONObject obj = array.getJSONObject(0);

                            String nombre = obj.optString("Nombre_Gimnasio", "");
                            String direccion = obj.optString("Direccion", "");
                            String ciudad = obj.optString("Ciudad", "");
                            String email = obj.optString("Email", "");
                            String telefono = obj.optString("Telefono", "");
                            String descripcion = obj.optString("Descripcion", "");

                            imagenActualUrl = obj.optString("Imagen_Gimnasio", "");

                            runOnUiThread(() -> {
                                etNombre.setText(nombre);
                                etDireccion.setText(direccion);
                                etCiudad.setText(ciudad);
                                etEmail.setText(email);
                                etTelefono.setText(telefono);
                                etDescripcion.setText(descripcion);

                                if (!imagenActualUrl.isEmpty()) {
                                    cargarImagenDesdeUrl(imagenActualUrl);
                                }
                            });
                        }

                    } catch (Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(AdminGimnasioActivity.this, "Error leyendo gimnasio", Toast.LENGTH_LONG).show()
                        );
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(AdminGimnasioActivity.this, "Error Supabase gimnasio: " + respuesta, Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }

    private void cargarImagenDesdeUrl(String urlImagen) {
        Request request = new Request.Builder()
                .url(urlImagen)
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                Log.e("IMG_GIMNASIO", "Error cargando imagen", e);
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    byte[] bytes = response.body().bytes();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    runOnUiThread(() -> imgGimnasio.setImageBitmap(bitmap));
                }
            }
        });
    }

    private void subirImagenYGuardarCambios() {
        if (imagenSeleccionadaUri == null) {
            guardarCambios(imagenActualUrl);
            return;
        }

        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imagenSeleccionadaUri);

                if (inputStream == null) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "No se pudo leer la imagen", Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[4096];
                int bytesRead;

                while ((bytesRead = inputStream.read(data)) != -1) {
                    buffer.write(data, 0, bytesRead);
                }

                inputStream.close();

                byte[] imagenBytes = buffer.toByteArray();

                String nombreArchivo = "gimnasio_" + idGimnasio + "_" + System.currentTimeMillis() + ".jpg";
                String rutaStorage = "gimnasios/" + nombreArchivo;

                String uploadUrl = SupabaseConfig.SUPABASE_URL
                        + "/storage/v1/object/Imagenes/"
                        + rutaStorage;

                RequestBody body = RequestBody.create(imagenBytes, IMAGE);

                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .addHeader("Content-Type", "image/jpeg")
                        .addHeader("x-upsert", "true")
                        .post(body)
                        .build();

                okhttp3.Response response = client.newCall(request).execute();
                String respuesta = response.body() != null ? response.body().string() : "";

                Log.d("UPLOAD_GIM_IMG", "Código: " + response.code());
                Log.d("UPLOAD_GIM_IMG", "Respuesta: " + respuesta);

                if (response.isSuccessful()) {
                    String nuevaUrlImagen = SupabaseConfig.SUPABASE_URL
                            + "/storage/v1/object/public/Imagenes/"
                            + rutaStorage;

                    guardarCambios(nuevaUrlImagen);
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error subiendo imagen: " + respuesta, Toast.LENGTH_LONG).show()
                    );
                }

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error imagen: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private void guardarCambios(String imagenUrlFinal) {
        if (idGimnasio == -1) {
            Toast.makeText(this, "No se encontró el gimnasio", Toast.LENGTH_LONG).show();
            return;
        }

        String nombre = etNombre.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        String ciudad = etCiudad.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();

        if (nombre.isEmpty() || direccion.isEmpty() || ciudad.isEmpty()) {
            Toast.makeText(this, "Nombre, dirección y ciudad son obligatorios", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("Nombre_Gimnasio", nombre);
            json.put("Direccion", direccion);
            json.put("Ciudad", ciudad);
            json.put("Email", email);
            json.put("Telefono", telefono);
            json.put("Descripcion", descripcion);
            json.put("Imagen_Gimnasio", imagenUrlFinal);

            RequestBody body = RequestBody.create(json.toString(), JSON);

            String url = SupabaseConfig.SUPABASE_URL
                    + "/rest/v1/Gimnasio?ID_Gimnasio=eq."
                    + idGimnasio;

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=minimal")
                    .patch(body)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, java.io.IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(AdminGimnasioActivity.this, "Error guardando cambios", Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                    String respuesta = response.body() != null ? response.body().string() : "";

                    Log.d("ADMIN_GIM_SAVE", "Código: " + response.code());
                    Log.d("ADMIN_GIM_SAVE", "Respuesta: " + respuesta);

                    if (response.isSuccessful()) {
                        imagenActualUrl = imagenUrlFinal;
                        imagenSeleccionadaUri = null;

                        runOnUiThread(() ->
                                Toast.makeText(AdminGimnasioActivity.this, "Gimnasio actualizado", Toast.LENGTH_LONG).show()
                        );
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(AdminGimnasioActivity.this, "Error Supabase: " + respuesta, Toast.LENGTH_LONG).show()
                        );
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}