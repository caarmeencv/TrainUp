package com.carmen.trainup.admin;

import android.app.Activity;
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

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class AdminEditarClaseActivity extends AppCompatActivity {

    private EditText etNombreClase, etDescripcion, etFecha, etHora, etPlazas, etDuracion, etSala, etMonitor;
    private TextView btnGuardarClase, btnSeleccionarImagen;
    private ImageView imgClase;

    private final OkHttpClient client = new OkHttpClient();

    private String accessToken;
    private long idClase = -1;

    private Uri imagenUri;
    private String imagenUrlFinal = "";

    private static final int PICK_IMAGE = 100;

    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_editar_clase);

        SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");

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

        idClase = getIntent().getLongExtra("idClase", -1);

        etNombreClase.setText(getIntent().getStringExtra("nombre"));
        etDescripcion.setText(getIntent().getStringExtra("descripcion"));
        etFecha.setText(getIntent().getStringExtra("fecha"));
        etHora.setText(getIntent().getStringExtra("hora"));
        etPlazas.setText(String.valueOf(getIntent().getIntExtra("plazas", 0)));
        etDuracion.setText(String.valueOf(getIntent().getIntExtra("duracion", 0)));
        etSala.setText(getIntent().getStringExtra("sala"));
        etMonitor.setText(getIntent().getStringExtra("monitor"));

        imagenUrlFinal = getIntent().getStringExtra("imagen");

        btnSeleccionarImagen.setOnClickListener(v -> abrirGaleria());
        btnGuardarClase.setOnClickListener(v -> editarClase());
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

    private void editarClase() {
        String nombre = etNombreClase.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String fecha = etFecha.getText().toString().trim();
        String hora = etHora.getText().toString().trim();
        String plazasTxt = etPlazas.getText().toString().trim();
        String duracionTxt = etDuracion.getText().toString().trim();
        String sala = etSala.getText().toString().trim();
        String monitor = etMonitor.getText().toString().trim();

        if (idClase == -1) {
            Toast.makeText(this, "No se encontró la clase", Toast.LENGTH_SHORT).show();
            return;
        }

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

                if (imagenUrlFinal != null && !imagenUrlFinal.isEmpty()) {
                    json.put("Imagen_Clase", imagenUrlFinal);
                }

                RequestBody body = RequestBody.create(json.toString(), JSON);

                String url = SupabaseConfig.SUPABASE_URL
                        + "/rest/v1/Clase?ID_Clase=eq."
                        + idClase;

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=representation")
                        .patch(body)
                        .build();

                okhttp3.Response response = client.newCall(request).execute();

                String respuesta = "";
                if (response.body() != null) {
                    respuesta = response.body().string();
                }

                Log.e("EDITAR_CLASE", "CODIGO=" + response.code() + " RESPUESTA=" + respuesta);

                String finalRespuesta = respuesta;

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(this, "Clase actualizada", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error " + response.code() + ": " + finalRespuesta, Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                Log.e("EDITAR_CLASE", "Excepción editando clase", e);
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

        Log.e("SUBIR_IMAGEN_EDITAR", "CODIGO=" + response.code() + " RESPUESTA=" + respuesta);

        if (!response.isSuccessful()) {
            throw new Exception("Error subiendo imagen: " + response.code());
        }

        return SupabaseConfig.SUPABASE_URL
                + "/storage/v1/object/public/Imagenes/"
                + nombreArchivo;
    }
}