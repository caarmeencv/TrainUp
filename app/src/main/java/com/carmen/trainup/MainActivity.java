package com.carmen.trainup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private TextView txtSaludo, btnMenu;
    private Button btnRutinas, btnClases, btnAjustes;

    private String accessToken;
    private String emailUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top,
                    systemBars.right, systemBars.bottom);
            return insets;
        });

        accessToken = getIntent().getStringExtra("access_token");
        emailUsuario = getIntent().getStringExtra("email");

        txtSaludo = findViewById(R.id.txtSaludo);
        btnMenu = findViewById(R.id.btnMenu);

        btnRutinas = findViewById(R.id.btnRutinas);
        btnClases = findViewById(R.id.btnClases);
        btnAjustes = findViewById(R.id.btnAjustes);

        txtSaludo.setText("¡Hola! 👋");

        btnRutinas.setOnClickListener(v -> abrirRutinas());
        btnClases.setOnClickListener(v -> abrirClases());
        btnAjustes.setOnClickListener(v -> abrirAjustes());

        btnMenu.setOnClickListener(v -> mostrarMenu());
    }

    private void mostrarMenu() {
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, btnMenu);

        popupMenu.getMenu().add("Rutinas");
        popupMenu.getMenu().add("Clases");
        popupMenu.getMenu().add("Ajustes");
        popupMenu.getMenu().add("Cerrar sesión");

        popupMenu.setOnMenuItemClickListener(item -> {
            String opcion = item.getTitle().toString();

            if (opcion.equals("Rutinas")) {
                abrirRutinas();
            } else if (opcion.equals("Clases")) {
                abrirClases();
            } else if (opcion.equals("Ajustes")) {
                abrirAjustes();
            } else if (opcion.equals("Cerrar sesión")) {
                cerrarSesion();
            }

            return true;
        });

        popupMenu.show();
    }

    private void abrirRutinas() {
        Intent intent = new Intent(MainActivity.this, RutinaActivity.class);
        intent.putExtra("access_token", accessToken);
        intent.putExtra("email", emailUsuario);
        startActivity(intent);
    }

    private void abrirClases() {
        Intent intent = new Intent(MainActivity.this, ClassListActivity.class);
        intent.putExtra("access_token", accessToken);
        intent.putExtra("email", emailUsuario);
        startActivity(intent);
    }

    private void abrirAjustes() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        intent.putExtra("access_token", accessToken);
        intent.putExtra("email", emailUsuario);
        startActivity(intent);
    }

    private void cerrarSesion() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}