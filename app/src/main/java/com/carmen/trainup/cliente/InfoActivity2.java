package com.carmen.trainup.cliente;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    private int idUsuario;

    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info2);

        SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);

        accessToken = prefs.getString("access_token", "");
        idUsuario = prefs.getInt("id_usuario", -1);

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

        if (idGimnasio == -1) {
            Toast.makeText(this, "No se encontró el gimnasio del usuario", Toast.LENGTH_LONG).show();
            return;
        }

        cargarDatosGimnasio();

        btnDarseDeBaja.setOnClickListener(v -> mostrarDialogoBaja());
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
                        .build();

                Response response = client.newCall(request).execute();

                String body = response.body() != null ? response.body().string() : "";
                Log.d("INFO_GYM", body);

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

                String nombre = gimnasio.optString("Nombre_Gimnasio", "Sin nombre");
                String ciudad = gimnasio.optString("Ciudad", "Sin ciudad");
                String descripcion = gimnasio.optString("Descripcion", "Sin descripción");
                String direccion = gimnasio.optString("Direccion", "Sin dirección");
                String email = gimnasio.optString("Email", "Sin email");
                String telefono = gimnasio.optString("Telefono", "Sin teléfono");
                String imagen = gimnasio.optString("Imagen_Gimnasio", "");

                runOnUiThread(() -> {
                    txtNombreGimnasioInfo.setText(nombre);
                    txtCiudadGimnasioInfo.setText(ciudad);
                    txtDescripcionGimnasioInfo.setText(descripcion);
                    txtDireccionGimnasioInfo.setText("Dirección: " + direccion);
                    txtEmailGimnasioInfo.setText("Email: " + email);
                    txtTelefonoGimnasioInfo.setText("Teléfono: " + telefono);
                });

                if (!imagen.isEmpty()) {
                    cargarImagen(imagen);
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

                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error al darse de baja", Toast.LENGTH_LONG).show());
                    return;
                }

                getSharedPreferences("TrainUpPrefs", MODE_PRIVATE)
                        .edit()
                        .remove("id_gimnasio")
                        .apply();

                runOnUiThread(() -> {
                    Toast.makeText(this, "Te has dado de baja del gimnasio", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(InfoActivity2.this, GymListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
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
                runOnUiThread(() ->
                        imgGimnasioInfo.setImageResource(R.drawable.logo_trainup));
            }
        }).start();
    }
}