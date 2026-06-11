package com.carmen.trainup.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.carmen.trainup.cliente.MainActivity;
import com.carmen.trainup.R;
import com.carmen.trainup.utils.SupabaseConfig;
import com.carmen.trainup.admin.AdminMainActivity;
import com.carmen.trainup.cliente.GymListActivity;

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

                            consultarDatosUsuario();

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

    private void consultarDatosUsuario() {

        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/Usuario?select=ID_Usuario,Rol,Auth_id,ID_Gimnasio&Auth_id=eq."
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
                        Toast.makeText(LoginActivity.this, "Error obteniendo usuario", Toast.LENGTH_SHORT).show());
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

                    JSONObject usuario = array.getJSONObject(0);

                    int idUsuario = usuario.getInt("ID_Usuario");
                    String rol = usuario.optString("Rol", "cliente");

                    boolean tieneGimnasio =
                            usuario.has("ID_Gimnasio") && !usuario.isNull("ID_Gimnasio");

                    int idGimnasio = -1;

                    if (tieneGimnasio) {
                        idGimnasio = usuario.getInt("ID_Gimnasio");
                    }

                    abrirPantalla(rol, tieneGimnasio, idGimnasio, idUsuario);

                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this, "Error leyendo datos del usuario", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void abrirPantalla(String rol, boolean tieneGimnasio, int idGimnasio, int idUsuario) {

        runOnUiThread(() -> {

            SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);

            SharedPreferences.Editor editor = prefs.edit()
                    .putString("access_token", accessToken)
                    .putString("email", emailUsuario)
                    .putString("rol", rol)
                    .putInt("id_usuario", idUsuario);

            if (tieneGimnasio) {
                editor.putInt("id_gimnasio", idGimnasio);
            } else {
                editor.remove("id_gimnasio");
            }

            editor.apply();

            Intent intent;

            if (rol.equalsIgnoreCase("administrador")) {
                intent = new Intent(LoginActivity.this, AdminMainActivity.class);
            } else {
                if (tieneGimnasio) {
                    intent = new Intent(LoginActivity.this, MainActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, GymListActivity.class);
                }
            }

            startActivity(intent);
            finish();
        });
    }
}