package com.carmen.trainup.cliente;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.carmen.trainup.R;
import com.carmen.trainup.utils.SupabaseConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InfoActivity2 extends AppCompatActivity {

    private ImageView imgGimnasioInfo;
    private TextView txtNombreGimnasioInfo, txtCiudadGimnasioInfo,
            txtDescripcionGimnasioInfo, txtDireccionGimnasioInfo,
            txtEmailGimnasioInfo, txtTelefonoGimnasioInfo;

    private Button btnDarseDeBaja;

    private final OkHttpClient client = new OkHttpClient();

    private String accessToken;
    private int idGimnasio;
    private long idUsuario;

    private String direccionGimnasio = "";
    private String emailGimnasio = "";
    private String telefonoGimnasio = "";
    private String nombreGimnasio = "";
    private String ciudadGimnasio = "";

    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info2);

        SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);

        accessToken = prefs.getString("access_token", "");
        idUsuario = leerLongPrefs(prefs, "id_usuario", -1);

        idGimnasio = getIntent().getIntExtra("id_gimnasio", -1);

        if (idGimnasio == -1) {
            idGimnasio = prefs.getInt("id_gimnasio", -1);
        }

        imgGimnasioInfo = findViewById(R.id.imgGimnasioInfo);
        txtNombreGimnasioInfo = findViewById(R.id.txtNombreGimnasioInfo);
        txtCiudadGimnasioInfo = findViewById(R.id.txtCiudadGimnasioInfo);
        txtDescripcionGimnasioInfo = findViewById(R.id.txtDescripcionGimnasioInfo);
        txtDireccionGimnasioInfo = findViewById(R.id.txtDireccionGimnasioInfo);
        txtEmailGimnasioInfo = findViewById(R.id.txtEmailGimnasioInfo);
        txtTelefonoGimnasioInfo = findViewById(R.id.txtTelefonoGimnasioInfo);
        btnDarseDeBaja = findViewById(R.id.btnDarseDeBaja);

        txtDireccionGimnasioInfo.setOnClickListener(v -> abrirGoogleMaps());
        txtEmailGimnasioInfo.setOnClickListener(v -> abrirEmail());
        txtTelefonoGimnasioInfo.setOnClickListener(v -> abrirTelefono());

        Log.d("INFO_GYM", "ID Usuario: " + idUsuario);
        Log.d("INFO_GYM", "ID Gimnasio: " + idGimnasio);

        if (idGimnasio == -1) {
            Toast.makeText(this, "No se encontró el gimnasio del usuario", Toast.LENGTH_LONG).show();
            return;
        }

        cargarDatosGimnasio();

        btnDarseDeBaja.setOnClickListener(v -> mostrarDialogoBaja());
    }

    private long leerLongPrefs(SharedPreferences prefs, String clave, long valorDefecto) {
        try {
            return prefs.getLong(clave, valorDefecto);
        } catch (ClassCastException e) {
            return prefs.getInt(clave, (int) valorDefecto);
        }
    }

    private void abrirGoogleMaps() {
        if (direccionGimnasio.isEmpty() || direccionGimnasio.equalsIgnoreCase("Sin dirección")) {
            Toast.makeText(this, "No hay dirección disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String busqueda = direccionGimnasio + " " + ciudadGimnasio;
            String direccionCodificada = URLEncoder.encode(busqueda, "UTF-8");

            Uri uri = Uri.parse("geo:0,0?q=" + direccionCodificada);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.maps");

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Intent navegador = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps/search/?api=1&query=" + direccionCodificada)
                );
                startActivity(navegador);
            }

        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir Google Maps", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirEmail() {
        if (emailGimnasio.isEmpty() || emailGimnasio.equalsIgnoreCase("Sin email")) {
            Toast.makeText(this, "No hay email disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + emailGimnasio));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Consulta sobre " + nombreGimnasio);

        try {
            startActivity(Intent.createChooser(intent, "Enviar correo"));
        } catch (Exception e) {
            Toast.makeText(this, "No se encontró una app de correo", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirTelefono() {
        if (telefonoGimnasio.isEmpty() || telefonoGimnasio.equalsIgnoreCase("Sin teléfono")) {
            Toast.makeText(this, "No hay teléfono disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + telefonoGimnasio));

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir la app de teléfono", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogoBaja() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar baja")
                .setMessage("¿Estás segura de que quieres darte de baja de este gimnasio?\n\nPerderás el plan asociado y dejarás de pertenecer al gimnasio.")
                .setPositiveButton("Sí, darme de baja", (dialog, which) -> darseDeBaja())
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void cargarDatosGimnasio() {
        new Thread(() -> {
            try {
                String url = SupabaseConfig.SUPABASE_URL
                        + "/rest/v1/Gimnasio?select=*"
                        + "&ID_Gimnasio=eq." + idGimnasio;

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .addHeader("Accept", "application/json")
                        .get()
                        .build();

                Response response = client.newCall(request).execute();

                String body = response.body() != null ? response.body().string() : "";
                Log.d("INFO_GYM", "Código: " + response.code());
                Log.d("INFO_GYM", "Respuesta: " + body);

                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error Supabase: " + response.code(), Toast.LENGTH_LONG).show());
                    return;
                }

                JSONArray array = new JSONArray(body);

                if (array.length() == 0) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "No se encontró el gimnasio", Toast.LENGTH_LONG).show());
                    return;
                }

                JSONObject gimnasio = array.getJSONObject(0);

                nombreGimnasio = gimnasio.optString("Nombre_Gimnasio", "Sin nombre");
                ciudadGimnasio = gimnasio.optString("Ciudad", "Sin ciudad");
                String descripcion = gimnasio.optString("Descripcion", "Sin descripción");
                direccionGimnasio = gimnasio.optString("Direccion", "Sin dirección");
                emailGimnasio = gimnasio.optString("Email", "Sin email");
                telefonoGimnasio = gimnasio.optString("Telefono", "Sin teléfono");
                String imagen = gimnasio.optString("Imagen_Gimnasio", "");

                runOnUiThread(() -> {
                    txtNombreGimnasioInfo.setText(nombreGimnasio);
                    txtCiudadGimnasioInfo.setText(ciudadGimnasio);
                    txtDescripcionGimnasioInfo.setText(descripcion);
                    txtDireccionGimnasioInfo.setText("Dirección: " + direccionGimnasio);
                    txtEmailGimnasioInfo.setText("Email: " + emailGimnasio);
                    txtTelefonoGimnasioInfo.setText("Teléfono: " + telefonoGimnasio);
                });

                if (!imagen.isEmpty() && !imagen.equals("null")) {
                    cargarImagen(imagen);
                } else {
                    runOnUiThread(() -> imgGimnasioInfo.setImageResource(R.drawable.logo_trainup));
                }

            } catch (Exception e) {
                Log.e("INFO_GYM", "Error", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void darseDeBaja() {
        new Thread(() -> {
            try {
                if (idUsuario == -1) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "No se encontró el usuario", Toast.LENGTH_LONG).show());
                    return;
                }

                JSONObject json = new JSONObject();
                json.put("ID_Gimnasio", JSONObject.NULL);
                json.put("ID_Plan", JSONObject.NULL);

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

                Log.d("BAJA_GYM", "Código: " + response.code());

                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error al darse de baja", Toast.LENGTH_LONG).show());
                    return;
                }

                getSharedPreferences("TrainUpPrefs", MODE_PRIVATE)
                        .edit()
                        .remove("id_gimnasio")
                        .remove("id_plan")
                        .apply();

                runOnUiThread(() -> {
                    Toast.makeText(this, "Te has dado de baja del gimnasio", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(InfoActivity2.this, GymListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                Log.e("BAJA_GYM", "Error", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void cargarImagen(String imageUrl) {
        new Thread(() -> {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                runOnUiThread(() -> imgGimnasioInfo.setImageBitmap(bitmap));

            } catch (Exception e) {
                Log.e("INFO_GYM_IMG", "Error cargando imagen", e);
                runOnUiThread(() ->
                        imgGimnasioInfo.setImageResource(R.drawable.logo_trainup));
            }
        }).start();
    }
}