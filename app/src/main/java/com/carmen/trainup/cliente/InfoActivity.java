package com.carmen.trainup.cliente;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.carmen.trainup.R;

import java.net.URLEncoder;

public class InfoActivity extends AppCompatActivity {

    private ImageView imgGimnasioInfo;

    private TextView txtNombreGimnasioInfo;
    private TextView txtCiudadGimnasioInfo;
    private TextView txtDescripcionGimnasioInfo;
    private TextView txtDireccionGimnasioInfo;
    private TextView txtEmailGimnasioInfo;
    private TextView txtTelefonoGimnasioInfo;

    private Button btnApuntarse;

    private String nombre = "";
    private String ciudad = "";
    private String direccion = "";
    private String email = "";
    private String telefono = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_info);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imgGimnasioInfo = findViewById(R.id.imgGimnasioInfo);

        txtNombreGimnasioInfo = findViewById(R.id.txtNombreGimnasioInfo);
        txtCiudadGimnasioInfo = findViewById(R.id.txtCiudadGimnasioInfo);
        txtDescripcionGimnasioInfo = findViewById(R.id.txtDescripcionGimnasioInfo);
        txtDireccionGimnasioInfo = findViewById(R.id.txtDireccionGimnasioInfo);
        txtEmailGimnasioInfo = findViewById(R.id.txtEmailGimnasioInfo);
        txtTelefonoGimnasioInfo = findViewById(R.id.txtTelefonoGimnasioInfo);

        btnApuntarse = findViewById(R.id.btnApuntarse);

        int idGimnasio = getIntent().getIntExtra("id_gimnasio", -1);
        nombre = getIntent().getStringExtra("nombre_gimnasio");
        ciudad = getIntent().getStringExtra("ciudad_gimnasio");
        String descripcion = getIntent().getStringExtra("descripcion_gimnasio");
        String imagen = getIntent().getStringExtra("imagen_gimnasio");
        direccion = getIntent().getStringExtra("direccion_gimnasio");
        email = getIntent().getStringExtra("email_gimnasio");
        telefono = getIntent().getStringExtra("telefono_gimnasio");
        String emailUsuario = getIntent().getStringExtra("email_usuario");

        if (nombre == null) nombre = "";
        if (ciudad == null) ciudad = "";
        if (descripcion == null) descripcion = "";
        if (imagen == null) imagen = "";
        if (direccion == null) direccion = "";
        if (email == null) email = "";
        if (telefono == null) telefono = "";

        txtNombreGimnasioInfo.setText(nombre);
        txtCiudadGimnasioInfo.setText(ciudad);
        txtDescripcionGimnasioInfo.setText(descripcion);
        txtDireccionGimnasioInfo.setText(direccion);
        txtEmailGimnasioInfo.setText(email);
        txtTelefonoGimnasioInfo.setText(telefono);

        txtDireccionGimnasioInfo.setOnClickListener(v -> abrirGoogleMaps());
        txtEmailGimnasioInfo.setOnClickListener(v -> abrirEmail());
        txtTelefonoGimnasioInfo.setOnClickListener(v -> abrirTelefono());

        Glide.with(this)
                .load(imagen)
                .placeholder(R.drawable.logo_trainup)
                .error(R.drawable.logo_trainup)
                .into(imgGimnasioInfo);

        btnApuntarse.setOnClickListener(v -> {
            Intent intent = new Intent(InfoActivity.this, PlanActivity.class);
            intent.putExtra("id_gimnasio", idGimnasio);
            intent.putExtra("nombre_gimnasio", nombre);
            intent.putExtra("email_usuario", emailUsuario);
            startActivity(intent);
        });
    }

    private void abrirGoogleMaps() {
        if (direccion.isEmpty()) {
            Toast.makeText(this, "No hay dirección disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String busqueda = direccion + " " + ciudad;
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
        if (email.isEmpty()) {
            Toast.makeText(this, "No hay email disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Consulta sobre " + nombre);

        try {
            startActivity(Intent.createChooser(intent, "Enviar correo"));
        } catch (Exception e) {
            Toast.makeText(this, "No se encontró una app de correo", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirTelefono() {
        if (telefono.isEmpty()) {
            Toast.makeText(this, "No hay teléfono disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + telefono));

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir la app de teléfono", Toast.LENGTH_SHORT).show();
        }
    }
}