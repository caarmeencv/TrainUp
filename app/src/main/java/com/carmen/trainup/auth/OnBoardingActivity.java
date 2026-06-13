package com.carmen.trainup.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.carmen.trainup.R;
import com.carmen.trainup.admin.AdminMainActivity;
import com.carmen.trainup.cliente.GymListActivity;
import com.carmen.trainup.cliente.MainActivity;

public class OnBoardingActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_on_boarding);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            SharedPreferences preferences = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);

            boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);
            String rol = preferences.getString("rol", "cliente");
            long idUsuario = leerLongPrefs(preferences, "id_usuario", -1);
            int idGimnasio = preferences.getInt("id_gimnasio", -1);
            int idPlan = preferences.getInt("id_plan", -1);

            Log.d("ONBOARDING", "isLoggedIn: " + isLoggedIn);
            Log.d("ONBOARDING", "rol: " + rol);
            Log.d("ONBOARDING", "idUsuario: " + idUsuario);
            Log.d("ONBOARDING", "idGimnasio: " + idGimnasio);
            Log.d("ONBOARDING", "idPlan: " + idPlan);

            Intent intent;

            if (isLoggedIn) {
                if (rol.equalsIgnoreCase("administrador")) {
                    intent = new Intent(OnBoardingActivity.this, AdminMainActivity.class);
                } else {
                    if (idUsuario != -1 && idGimnasio != -1 && idPlan != -1) {
                        intent = new Intent(OnBoardingActivity.this, MainActivity.class);
                    } else {
                        intent = new Intent(OnBoardingActivity.this, GymListActivity.class);
                    }
                }
            } else {
                intent = new Intent(OnBoardingActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();

        }, SPLASH_TIME);
    }

    private long leerLongPrefs(SharedPreferences prefs, String clave, long valorDefecto) {
        try {
            return prefs.getLong(clave, valorDefecto);
        } catch (ClassCastException e) {
            return prefs.getInt(clave, (int) valorDefecto);
        }
    }
}