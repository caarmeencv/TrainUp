package com.carmen.trainup.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.carmen.trainup.cliente.EditProfileActivity;
import com.carmen.trainup.auth.LoginActivity;
import com.carmen.trainup.R;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public class AdminAjustesActivity extends AppCompatActivity {

    private TextView txtEmailAjustes, txtRolAjustes;
    private Button btnEditarPerfil, btnCerrarSesion;

    private final OkHttpClient client = new OkHttpClient();

    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_ajustes);

        txtEmailAjustes = findViewById(R.id.txtEmailAjustes);
        txtRolAjustes = findViewById(R.id.txtRolAjustes);

        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
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
            Intent intent = new Intent(AdminAjustesActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });


        btnCerrarSesion.setOnClickListener(v -> {
            prefs.edit().clear().apply();

            Intent intent = new Intent(AdminAjustesActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}