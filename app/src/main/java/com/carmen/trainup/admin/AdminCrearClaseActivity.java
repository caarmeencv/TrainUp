package com.carmen.trainup.admin;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.carmen.trainup.R;
import com.carmen.trainup.utils.SupabaseConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class AdminCrearClaseActivity extends AppCompatActivity {

    private EditText etNombreClase, etDescripcion, etFecha, etHora, etPlazas, etDuracion, etSala, etMonitor;
    private TextView btnGuardarClase, btnSeleccionarImagen;
    private ImageView imgClase;

    private final OkHttpClient client = new OkHttpClient();

    private String accessToken;
    private String emailUsuario;
    private int idGimnasio = -1;

    private Uri imagenUri;
    private String imagenUrlFinal = "";

    private static final int PICK_IMAGE = 100;

    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_crear_clase);

        SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
        emailUsuario = prefs.getString("email", "");

        etNombreClase = findViewById(R.id.etNombreClase);
        etDescripcion = findViewById(R.id.etDescripcion);
        etFecha = findViewById(R.id.etFecha);
        etHora = findViewById(R.id.etHora);
        etPlazas = findViewById(R.id.etPlazas);
        etDuracion = findViewById(R.id.etDuracion);
        etSala = findViewById(R.id.etSala);
        etMonitor = findViewById(R.id.etMonitor);

        imgClase = findViewById(R.id.imgClase);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        btnGuardarClase = findViewById(R.id.btnGuardarClase);

        etFecha.setFocusable(false);
        etFecha.setClickable(true);
        etFecha.setOnClickListener(v -> mostrarDatePicker());

        etHora.setFocusable(false);
        etHora.setClickable(true);
        etHora.setOnClickListener(v -> mostrarTimePicker());

        btnSeleccionarImagen.setOnClickListener(v -> abrirGaleria());

        btnGuardarClase.setOnClickListener(v -> {
            if (idGimnasio == -1) {
                Toast.makeText(this, "Gimnasio no cargado todavía", Toast.LENGTH_SHORT).show();
            } else {
                crearClase();
            }
        });

        cargarGimnasioUsuario();
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

                    etFecha.setText(fecha);
                },
                year,
                month,
                day
        );

        datePickerDialog.show();
    }

    private void mostrarTimePicker() {
        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    String hora = String.format("%02d:%02d", selectedHour, selectedMinute);
                    etHora.setText(hora);
                },
                hour,
                minute,
                true
        );

        timePickerDialog.show();
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            imagenUri = data.getData();
            imgClase.setImageURI(imagenUri);
        }
    }

    private void cargarGimnasioUsuario() {
        try {
            String emailCodificado = URLEncoder.encode(emailUsuario, "UTF-8");

            String url = SupabaseConfig.SUPABASE_URL
                    + "/rest/v1/Usuario?select=ID_Gimnasio&Email_Usuario=eq."
                    + emailCodificado;

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
                            Toast.makeText(AdminCrearClaseActivity.this, "Error cargando gimnasio", Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                    String respuesta = response.body() != null ? response.body().string() : "";

                    try {
                        JSONArray array = new JSONArray(respuesta);

                        if (array.length() > 0) {
                            JSONObject usuario = array.getJSONObject(0);
                            idGimnasio = usuario.optInt("ID_Gimnasio", -1);
                        }

                    } catch (Exception e) {
                        Log.e("CREAR_CLASE", "Error leyendo gimnasio: " + respuesta, e);
                    }
                }
            });

        } catch (Exception e) {
            Log.e("CREAR_CLASE", "Error codificando email", e);
        }
    }

    private void crearClase() {
        String nombre = etNombreClase.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String fecha = etFecha.getText().toString().trim();
        String hora = etHora.getText().toString().trim();
        String plazasTxt = etPlazas.getText().toString().trim();
        String duracionTxt = etDuracion.getText().toString().trim();
        String sala = etSala.getText().toString().trim();
        String monitor = etMonitor.getText().toString().trim();

        if (nombre.isEmpty() || fecha.isEmpty() || hora.isEmpty() || plazasTxt.isEmpty()
                || duracionTxt.isEmpty() || sala.isEmpty() || monitor.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                if (imagenUri != null) {
                    imagenUrlFinal = subirImagenClase(imagenUri);
                }

                JSONObject json = new JSONObject();
                json.put("Nombre_Clase", nombre);
                json.put("Descripcion", descripcion);
                json.put("Fecha_Clase", fecha);
                json.put("Hora_Clase", hora);
                json.put("Plazas_Maximas", Integer.parseInt(plazasTxt));
                json.put("Duracion", Integer.parseInt(duracionTxt));
                json.put("Sala", sala);
                json.put("Monitor", monitor);
                json.put("ID_Gimnasio", idGimnasio);

                if (!imagenUrlFinal.isEmpty()) {
                    json.put("Imagen_Clase", imagenUrlFinal);
                }

                RequestBody body = RequestBody.create(json.toString(), JSON);

                String url = SupabaseConfig.SUPABASE_URL + "/rest/v1/Clase";

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=representation")
                        .post(body)
                        .build();

                okhttp3.Response response = client.newCall(request).execute();

                String respuesta = "";
                if (response.body() != null) {
                    respuesta = response.body().string();
                }

                Log.e("CREAR_CLASE", "CODIGO=" + response.code() + " RESPUESTA=" + respuesta);

                String finalRespuesta = respuesta;

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(this, "Clase creada", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error " + response.code() + ": " + finalRespuesta, Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                Log.e("CREAR_CLASE", "Excepción creando clase", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private String subirImagenClase(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);

        if (inputStream == null) {
            throw new Exception("No se pudo abrir la imagen");
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int nRead;

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        inputStream.close();

        byte[] imagenBytes = buffer.toByteArray();

        String nombreArchivo = "clases/clase_" + System.currentTimeMillis() + ".jpg";

        RequestBody body = RequestBody.create(imagenBytes, MediaType.parse("image/jpeg"));

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

        okhttp3.Response response = client.newCall(request).execute();

        String respuesta = "";
        if (response.body() != null) {
            respuesta = response.body().string();
        }

        Log.e("SUBIR_IMAGEN_CLASE", "CODIGO=" + response.code() + " RESPUESTA=" + respuesta);

        if (!response.isSuccessful()) {
            throw new Exception("Error subiendo imagen: " + response.code());
        }

        return SupabaseConfig.SUPABASE_URL
                + "/storage/v1/object/public/Imagenes/"
                + nombreArchivo;
    }
}