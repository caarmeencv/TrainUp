package com.carmen.trainup.cliente;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.carmen.trainup.R;
import com.carmen.trainup.auth.LoginActivity;
import com.carmen.trainup.utils.SupabaseConfig;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingsActivity extends AppCompatActivity {

    private TextView txtEmailAjustes, txtRolAjustes;
    private Button btnEditarPerfil, btnCambiarGimnasio, btnCerrarSesion;

    private final OkHttpClient client = new OkHttpClient();

    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        txtEmailAjustes = findViewById(R.id.txtEmailAjustes);
        txtRolAjustes = findViewById(R.id.txtRolAjustes);

        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        btnCambiarGimnasio = findViewById(R.id.btnCambiarGimnasio);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);

        String email = prefs.getString("email", "Sin email");
        String rol = prefs.getString("rol", "cliente");

        txtEmailAjustes.setText(email);

        if (!rol.isEmpty()) {
            txtRolAjustes.setText(rol.substring(0, 1).toUpperCase() + rol.substring(1));
        } else {
            txtRolAjustes.setText("Cliente");
        }

        btnEditarPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        btnCambiarGimnasio.setOnClickListener(v -> mostrarDialogoCambiarGimnasio());

        btnCerrarSesion.setOnClickListener(v -> mostrarDialogoCerrarSesion());
    }

    private void mostrarDialogoCambiarGimnasio() {
        new AlertDialog.Builder(SettingsActivity.this)
                .setTitle("Cambiar gimnasio")
                .setMessage("¿Seguro que quieres cambiar de gimnasio? Se eliminará tu gimnasio actual y tendrás que seleccionar uno nuevo.")
                .setPositiveButton("Sí, cambiar", (dialog, which) -> cambiarGimnasio())
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void mostrarDialogoCerrarSesion() {
        new AlertDialog.Builder(SettingsActivity.this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que quieres cerrar sesión?")
                .setPositiveButton("Sí, cerrar sesión", (dialog, which) -> cerrarSesion())
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void cerrarSesion() {
        SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void cambiarGimnasio() {
        new Thread(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);

                int idUsuario = prefs.getInt("id_usuario", -1);
                String accessToken = prefs.getString("access_token", "");

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
                            Toast.makeText(this, "Error al cambiar de gimnasio", Toast.LENGTH_LONG).show());
                    return;
                }

                prefs.edit()
                        .remove("id_gimnasio")
                        .apply();

                runOnUiThread(() -> {
                    Toast.makeText(this, "Selecciona un nuevo gimnasio", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(SettingsActivity.this, GymListActivity.class);
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
}