package com.carmen.trainup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView txtRegister;

    private final OkHttpClient client = new OkHttpClient();

    private String accessToken = "";
    private String emailUsuario = "";
    private String authId = "";

    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtRegister = findViewById(R.id.txtRegister);

        txtRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        btnLogin.setOnClickListener(v -> iniciarSesion());
    }

    private void iniciarSesion() {

        String email = etEmail.getText().toString().trim().toLowerCase();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Introduce email y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        try {

            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("password", password);

            RequestBody body = RequestBody.create(json.toString(), JSON);

            Request request = new Request.Builder()
                    .url(SupabaseConfig.SUPABASE_URL + "/auth/v1/token?grant_type=password")
                    .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.SUPABASE_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {

                @Override
                public void onFailure(okhttp3.Call call, java.io.IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {

                    String respuesta = response.body() != null ? response.body().string() : "";

                    if (response.isSuccessful()) {

                        try {

                            JSONObject jsonRespuesta = new JSONObject(respuesta);

                            accessToken = jsonRespuesta.getString("access_token");

                            JSONObject user = jsonRespuesta.getJSONObject("user");

                            emailUsuario = user.getString("email");
                            authId = user.getString("id");

                            consultarRol();

                        } catch (Exception e) {

                            runOnUiThread(() ->
                                    Toast.makeText(LoginActivity.this, "Error leyendo usuario", Toast.LENGTH_SHORT).show());
                        }

                    } else {

                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show());
                    }
                }
            });

        } catch (Exception e) {

            Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
        }
    }

    private void consultarRol() {

        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/Usuario?select=Rol,Auth_id&Auth_id=eq."
                + authId;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {

            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(LoginActivity.this, "Error obteniendo rol", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {

                String respuesta = response.body() != null ? response.body().string() : "";

                try {
                    JSONArray array = new JSONArray(respuesta);

                    if (array.length() == 0) {
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this, "Usuario no encontrado", Toast.LENGTH_LONG).show());
                        return;
                    }

                    String rol = array.getJSONObject(0).getString("Rol");
                    abrirPantalla(rol);

                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this, "Error leyendo rol", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void abrirPantalla(String rol) {

        runOnUiThread(() -> {

            SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);

            prefs.edit()
                    .putString("access_token", accessToken)
                    .putString("email", emailUsuario)
                    .putString("rol", rol)
                    .apply();

            Intent intent;

            if (rol.equalsIgnoreCase("administrador")) {
                intent = new Intent(LoginActivity.this, AdminMainActivity.class);
            } else {
                intent = new Intent(LoginActivity.this, MainActivity.class);
            }

            startActivity(intent);
            finish();
        });
    }
}