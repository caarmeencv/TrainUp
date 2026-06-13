package com.carmen.trainup.cliente;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView imgPerfilEditar;
    private EditText etNombreEditar, etApellidosEditar, etEmailEditar,
            etTelefonoEditar, etFechaNacimientoEditar;
    private Spinner spGeneroEditar;
    private Button btnCambiarImagenPerfil, btnGuardarPerfil, btnCancelarEditarPerfil;

    private final OkHttpClient client = new OkHttpClient();

    private String accessToken;
    private int idUsuario;

    private static final int PICK_IMAGE_REQUEST = 1001;
    private Uri imageUri;

    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    private final String[] generos = {
            "Selecciona tu género",
            "Masculino",
            "Femenino",
            "Prefiero no decirlo"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
        idUsuario = prefs.getInt("id_usuario", -1);

        imgPerfilEditar = findViewById(R.id.imgPerfilEditar);
        etNombreEditar = findViewById(R.id.etNombreEditar);
        etApellidosEditar = findViewById(R.id.etApellidosEditar);
        etEmailEditar = findViewById(R.id.etEmailEditar);
        etTelefonoEditar = findViewById(R.id.etTelefonoEditar);
        etFechaNacimientoEditar = findViewById(R.id.etFechaNacimientoEditar);
        spGeneroEditar = findViewById(R.id.spGeneroEditar);
        btnCambiarImagenPerfil = findViewById(R.id.btnCambiarImagenPerfil);
        btnGuardarPerfil = findViewById(R.id.btnGuardarPerfil);
        btnCancelarEditarPerfil = findViewById(R.id.btnCancelarEditarPerfil);

        etFechaNacimientoEditar.setFocusable(false);
        etFechaNacimientoEditar.setClickable(true);
        etFechaNacimientoEditar.setOnClickListener(v -> mostrarDatePicker());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                generos
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGeneroEditar.setAdapter(adapter);

        if (idUsuario == -1) {
            Toast.makeText(this, "No se encontró el usuario", Toast.LENGTH_LONG).show();
            return;
        }

        cargarDatosUsuario();

        btnCambiarImagenPerfil.setOnClickListener(v -> abrirGaleria());
        btnGuardarPerfil.setOnClickListener(v -> guardarCambios());
        btnCancelarEditarPerfil.setOnClickListener(v -> finish());
    }

    private void mostrarDatePicker() {
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String fecha = selectedYear + "-"
                            + String.format("%02d", selectedMonth + 1)
                            + "-"
                            + String.format("%02d", selectedDay);

                    etFechaNacimientoEditar.setText(fecha);
                },
                year,
                month,
                day
        );

        datePickerDialog.show();
    }

    private void cargarDatosUsuario() {
        new Thread(() -> {
            try {
                String url = SupabaseConfig.SUPABASE_URL
                        + "/rest/v1/Usuario?select=*"
                        + "&ID_Usuario=eq." + idUsuario;

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .addHeader("Accept", "application/json")
                        .get()
                        .build();

                Response response = client.newCall(request).execute();
                String body = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_LONG).show());
                    return;
                }

                JSONArray array = new JSONArray(body);

                if (array.length() == 0) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_LONG).show());
                    return;
                }

                JSONObject usuario = array.getJSONObject(0);

                String nombre = usuario.optString("Nombre_Usuario", "");
                String apellidos = usuario.optString("Apellidos_Usuario", "");
                String email = usuario.optString("Email_Usuario", "");
                String telefono = usuario.optString("Telefono", "");
                String fechaNacimiento = usuario.optString("Fecha_Nacimiento", "");
                String genero = usuario.optString("genero", "");
                String imagen = usuario.optString("Imagen_Usuario", "");

                runOnUiThread(() -> {
                    ponerTextoSiExiste(etNombreEditar, nombre);
                    ponerTextoSiExiste(etApellidosEditar, apellidos);
                    ponerTextoSiExiste(etEmailEditar, email);
                    ponerTextoSiExiste(etTelefonoEditar, telefono);
                    ponerTextoSiExiste(etFechaNacimientoEditar, fechaNacimiento);
                    seleccionarGenero(genero);
                });

                if (!imagen.isEmpty() && !imagen.equals("null")) {
                    cargarImagen(imagen);
                }

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void guardarCambios() {
        new Thread(() -> {
            try {
                String nombre = etNombreEditar.getText().toString().trim();
                String apellidos = etApellidosEditar.getText().toString().trim();
                String email = etEmailEditar.getText().toString().trim();
                String telefono = etTelefonoEditar.getText().toString().trim();
                String fechaNacimiento = etFechaNacimientoEditar.getText().toString().trim();

                String genero = "";
                if (spGeneroEditar.getSelectedItemPosition() > 0) {
                    genero = spGeneroEditar.getSelectedItem().toString();
                }

                String urlImagenSubida = "";

                if (imageUri != null) {
                    urlImagenSubida = subirImagenSupabase();

                    if (urlImagenSubida.isEmpty()) {
                        return;
                    }
                }

                JSONObject json = new JSONObject();
                json.put("Nombre_Usuario", nombre.isEmpty() ? JSONObject.NULL : nombre);
                json.put("Apellidos_Usuario", apellidos.isEmpty() ? JSONObject.NULL : apellidos);
                json.put("Email_Usuario", email.isEmpty() ? JSONObject.NULL : email);
                json.put("Telefono", telefono.isEmpty() ? JSONObject.NULL : telefono);
                json.put("Fecha_Nacimiento", fechaNacimiento.isEmpty() ? JSONObject.NULL : fechaNacimiento);
                json.put("genero", genero.isEmpty() ? JSONObject.NULL : genero);

                if (!urlImagenSubida.isEmpty()) {
                    json.put("Imagen_Usuario", urlImagenSubida);
                }

                RequestBody body = RequestBody.create(json.toString(), JSON);

                String url = SupabaseConfig.SUPABASE_URL
                        + "/rest/v1/Usuario?ID_Usuario=eq." + idUsuario;

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=minimal")
                        .patch(body)
                        .build();

                Response response = client.newCall(request).execute();
                String error = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error al actualizar perfil: " + error, Toast.LENGTH_LONG).show());
                    return;
                }

                getSharedPreferences("TrainUpPrefs", MODE_PRIVATE)
                        .edit()
                        .putString("email", email)
                        .apply();

                runOnUiThread(() -> {
                    Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(EditProfileActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null) {

            imageUri = data.getData();
            imgPerfilEditar.setImageURI(imageUri);
        }
    }

    private String subirImagenSupabase() {
        try {
            if (imageUri == null) {
                return "";
            }

            InputStream inputStream = getContentResolver().openInputStream(imageUri);

            if (inputStream == null) {
                runOnUiThread(() ->
                        Toast.makeText(this, "No se pudo abrir la imagen", Toast.LENGTH_LONG).show());
                return "";
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();

            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            String nombreArchivo = "perfiles/" + idUsuario + "_" + UUID.randomUUID() + ".jpg";

            RequestBody body = RequestBody.create(
                    imageBytes,
                    MediaType.get("image/jpeg")
            );

            String url = SupabaseConfig.SUPABASE_URL
                    + "/storage/v1/object/Imagenes/"
                    + nombreArchivo;

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "image/jpeg")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            String error = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error subiendo imagen: " + error, Toast.LENGTH_LONG).show());
                return "";
            }

            return SupabaseConfig.SUPABASE_URL
                    + "/storage/v1/object/public/Imagenes/"
                    + nombreArchivo;

        } catch (Exception e) {
            runOnUiThread(() ->
                    Toast.makeText(this, "Error imagen: " + e.getMessage(), Toast.LENGTH_LONG).show());
            return "";
        }
    }

    private void ponerTextoSiExiste(EditText editText, String valor) {
        if (valor != null && !valor.isEmpty() && !valor.equals("null")) {
            editText.setText(valor);
        }
    }

    private void seleccionarGenero(String genero) {
        if (genero == null || genero.trim().isEmpty() || genero.equals("null")) {
            spGeneroEditar.setSelection(0);
            return;
        }

        genero = genero.trim();

        for (int i = 0; i < generos.length; i++) {
            if (generos[i].equals(genero)) {
                spGeneroEditar.setSelection(i);
                return;
            }
        }

        spGeneroEditar.setSelection(0);
    }

    private void cargarImagen(String imageUrl) {
        new Thread(() -> {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                runOnUiThread(() -> imgPerfilEditar.setImageBitmap(bitmap));

            } catch (Exception e) {
                runOnUiThread(() ->
                        imgPerfilEditar.setImageResource(R.drawable.logo_trainup));
            }
        }).start();
    }
}