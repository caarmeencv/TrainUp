package com.carmen.trainup.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.carmen.trainup.auth.LoginActivity;
import com.carmen.trainup.R;

public class AdminMainActivity extends AppCompatActivity {

    private TextView txtSaludo;
    private TextView btnMenu;

    private LinearLayout btnGestionClases;
    private LinearLayout btnGestionReservas;
    private LinearLayout btnGestionGimnasio;
    private LinearLayout btnGestionUsuarios;

    private String nombreUsuario = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        txtSaludo = findViewById(R.id.txtSaludo);
        btnMenu = findViewById(R.id.btnMenu);

        btnGestionClases = findViewById(R.id.btnGestionClases);
        btnGestionReservas = findViewById(R.id.btnGestionReservas);
        btnGestionGimnasio = findViewById(R.id.btnGestionGimnasio);
        btnGestionUsuarios = findViewById(R.id.btnGestionUsuarios);

        SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);
        nombreUsuario = prefs.getString("nombreUsuario", "");

        if (!nombreUsuario.isEmpty()) {
            txtSaludo.setText("¡Hola, " + nombreUsuario + "!");
        } else {
            txtSaludo.setText("¡Hola, admin!");
        }

        btnGestionClases.setOnClickListener(v -> abrirClases());
        btnGestionReservas.setOnClickListener(v -> abrirReservas());
        btnGestionGimnasio.setOnClickListener(v -> abrirGimnasio());
        btnGestionUsuarios.setOnClickListener(v -> abrirUsuarios());

        btnMenu.setOnClickListener(v -> mostrarMenu());
    }

    private void mostrarMenu() {
        PopupMenu popupMenu = new PopupMenu(AdminMainActivity.this, btnMenu);

        popupMenu.getMenu().add("Editar perfil");
        popupMenu.getMenu().add("Clases");
        popupMenu.getMenu().add("Reservas");
        popupMenu.getMenu().add("Gimnasio");
        popupMenu.getMenu().add("Usuarios");
        popupMenu.getMenu().add("Cerrar sesión");

        popupMenu.setOnMenuItemClickListener(item -> {
            String opcion = item.getTitle().toString();

            if (opcion.equals("Editar perfil")) {
                abrirEditarPerfil();
            } else if (opcion.equals("Clases")) {
                abrirClases();
            } else if (opcion.equals("Reservas")) {
                abrirReservas();
            } else if (opcion.equals("Gimnasio")) {
                abrirGimnasio();
            } else if (opcion.equals("Usuarios")) {
                abrirUsuarios();
            } else if (opcion.equals("Cerrar sesión")) {
                cerrarSesion();
            }

            return true;
        });

        popupMenu.show();
    }

    private void abrirEditarPerfil() {
        Intent intent = new Intent(AdminMainActivity.this, AdminAjustesActivity.class);
        startActivity(intent);
    }

    private void abrirClases() {
        Intent intent = new Intent(AdminMainActivity.this, AdminClasesActivity.class);
        startActivity(intent);
    }

    private void abrirReservas() {
        Intent intent = new Intent(AdminMainActivity.this, AdminReservasActivity.class);
        startActivity(intent);
    }

    private void abrirGimnasio() {
        Intent intent = new Intent(AdminMainActivity.this, AdminGimnasioActivity.class);
        startActivity(intent);
    }

    private void abrirUsuarios() {
        Intent intent = new Intent(AdminMainActivity.this, AdminUsuariosActivity.class);
        startActivity(intent);
    }

    private void cerrarSesion() {
        SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(AdminMainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}