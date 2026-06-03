package com.carmen.trainup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView txtRegister;

    private final OkHttpClient client = new OkHttpClient();

    private String accessTokenGlobal = "";
    private String emailUsuarioGlobal = "";

    public static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.etEmail);
        edtPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtRegister = findViewById(R.id.txtRegister);

        txtRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> iniciarSesion());
    }

    private void iniciarSesion() {
        String email = edtEmail.getText().toString().trim().toLowerCase();
        String password = edtPassword.getText().toString().trim();

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

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("LOGIN_ERROR", "Error de conexión", e);

                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String respuesta = response.body() != null ? response.body().string() : "";

                    Log.d("LOGIN_CODE", String.valueOf(response.code()));
                    Log.d("LOGIN_RESPONSE", respuesta);

                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonRespuesta = new JSONObject(respuesta);

                            accessTokenGlobal = jsonRespuesta.getString("access_token");

                            JSONObject user = jsonRespuesta.getJSONObject("user");
                            emailUsuarioGlobal = user.getString("email").trim().toLowerCase();

                            Log.d("EMAIL_LOGIN", emailUsuarioGlobal);

                            consultarRolUsuario(emailUsuarioGlobal);

                        } catch (Exception e) {
                            Log.e("LOGIN_ERROR", "Error leyendo respuesta", e);

                            runOnUiThread(() ->
                                    Toast.makeText(LoginActivity.this, "Error al leer usuario", Toast.LENGTH_SHORT).show()
                            );
                        }

                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                        );
                    }

                    response.close();
                }
            });

        } catch (Exception e) {
            Log.e("LOGIN_ERROR", "Error al iniciar sesión", e);
            Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
        }
    }

    private void consultarRolUsuario(String emailUsuario) {
        try {
            String emailCodificado = URLEncoder.encode(emailUsuario, "UTF-8");

            String url = SupabaseConfig.SUPABASE_URL
                    + "/rest/v1/Usuario?select=Rol,Email_Usuario&Email_Usuario=ilike."
                    + emailCodificado;

            Log.d("URL_ROL", url);

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                    .addHeader("Authorization", "Bearer " + accessTokenGlobal)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .get()
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("ROL_ERROR", "Error consultando rol", e);

                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this, "Error obteniendo rol", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String respuesta = response.body() != null ? response.body().string() : "";

                    Log.d("ROL_CODE", String.valueOf(response.code()));
                    Log.d("ROL_RESPONSE", respuesta);

                    if (response.isSuccessful()) {
                        try {
                            JSONArray array = new JSONArray(respuesta);

                            if (array.length() > 0) {
                                JSONObject usuario = array.getJSONObject(0);
                                String rol = usuario.optString("Rol", "cliente");

                                abrirPantallaSegunRol(rol);
                            } else {
                                runOnUiThread(() ->
                                        Toast.makeText(LoginActivity.this,
                                                "Usuario no encontrado en tabla Usuario",
                                                Toast.LENGTH_LONG).show()
                                );
                            }

                        } catch (Exception e) {
                            Log.e("ROL_ERROR", "Error leyendo rol", e);

                            runOnUiThread(() ->
                                    Toast.makeText(LoginActivity.this, "Error al leer rol", Toast.LENGTH_SHORT).show()
                            );
                        }
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this, "No se pudo consultar el rol", Toast.LENGTH_SHORT).show()
                        );
                    }

                    response.close();
                }
            });

        } catch (Exception e) {
            Log.e("ROL_ERROR", "Error preparando consulta de rol", e);
        }
    }

    private void abrirPantallaSegunRol(String rol) {
        runOnUiThread(() -> {
            SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);
            prefs.edit()
                    .putString("access_token", accessTokenGlobal)
                    .putString("email", emailUsuarioGlobal)
                    .putString("rol", rol)
                    .apply();

            Intent intent;

            if (rol.equalsIgnoreCase("administrador")) {
                intent = new Intent(LoginActivity.this, AdminMainActivity.class);
            } else {
                intent = new Intent(LoginActivity.this, MainActivity.class);
            }

            intent.putExtra("access_token", accessTokenGlobal);
            intent.putExtra("email", emailUsuarioGlobal);
            intent.putExtra("rol", rol);

            startActivity(intent);
            finish();
        });
    }
}