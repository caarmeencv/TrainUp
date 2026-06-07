package com.carmen.trainup;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class InfoActivity extends AppCompatActivity {

    private ImageView imgGimnasioInfo;

    private TextView txtNombreGimnasioInfo;
    private TextView txtCiudadGimnasioInfo;
    private TextView txtDescripcionGimnasioInfo;

    private TextView txtDireccionGimnasioInfo;
    private TextView txtEmailGimnasioInfo;
    private TextView txtTelefonoGimnasioInfo;

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

        String nombre = getIntent().getStringExtra("nombre_gimnasio");
        String ciudad = getIntent().getStringExtra("ciudad_gimnasio");
        String descripcion = getIntent().getStringExtra("descripcion_gimnasio");
        String imagen = getIntent().getStringExtra("imagen_gimnasio");

        String direccion = getIntent().getStringExtra("direccion_gimnasio");
        String email = getIntent().getStringExtra("email_gimnasio");
        String telefono = getIntent().getStringExtra("telefono_gimnasio");

        txtNombreGimnasioInfo.setText(nombre);
        txtCiudadGimnasioInfo.setText(ciudad);
        txtDescripcionGimnasioInfo.setText(descripcion);

        txtDireccionGimnasioInfo.setText(direccion);
        txtEmailGimnasioInfo.setText(email);
        txtTelefonoGimnasioInfo.setText(telefono);

        Glide.with(this)
                .load(imagen)
                .placeholder(R.drawable.logo_trainup)
                .error(R.drawable.logo_trainup)
                .into(imgGimnasioInfo);
    }
}