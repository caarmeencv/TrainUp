package com.carmen.trainup.auth;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.carmen.trainup.R;
import com.carmen.trainup.cliente.GymListActivity;
import com.carmen.trainup.utils.SupabaseConfig;

import org.json.JSONObject;

import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNombre, etApellidos, etTelefono, etFechaNacimiento, etEmail, etPassword, etConfirmPassword;
    private Spinner spGenero;
    private Button btnRegister;
    private TextView txtLogin;

    private final OkHttpClient client = new OkHttpClient();

    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    private String nombre, apellidos, telefono, fechaNacimiento, genero, email, password;
    private String accessToken = "";
    private String authId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etNombre = findViewById(R.id.etNombre);
        etApellidos = findViewById(R.id.etApellidos);
        etTelefono = findViewById(R.id.etTelefono);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        spGenero = findViewById(R.id.spGenero);
        btnRegister = findViewById(R.id.btnRegister);
        txtLogin = findViewById(R.id.txtLogin);

        etFechaNacimiento.setOnClickListener(v -> abrirCalendario());
        btnRegister.setOnClickListener(v -> registrarUsuario());

        txtLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void abrirCalendario() {
        Calendar calendario = Calendar.getInstance();

        int anio = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String fecha = dayOfMonth + "/" + (month + 1) + "/" + year;
                    etFechaNacimiento.setText(fecha);
                },
                anio,
                mes,
                dia
        );

        datePickerDialog.show();
    }

    private void registrarUsuario() {
        nombre = etNombre.getText().toString().trim();
        apellidos = etApellidos.getText().toString().trim();
        telefono = etTelefono.getText().toString().trim();
        fechaNacimiento = etFechaNacimiento.getText().toString().trim();
        genero = spGenero.getSelectedItem().toString();
        email = etEmail.getText().toString().trim().toLowerCase();
        password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (nombre.isEmpty() || apellidos.isEmpty() || telefono.isEmpty()
                || fechaNacimiento.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {

            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (genero.equals("Selecciona género")) {
            Toast.makeText(this, "Selecciona un género", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.contains("@")) {
            etEmail.setError("Correo no válido");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            return;
        }

        crearUsuarioAuth();
    }

    private void crearUsuarioAuth() {
        try {
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("password", password);

            RequestBody body = RequestBody.create(json.toString(), JSON);

            Request request = new Request.Builder()
                    .url(SupabaseConfig.SUPABASE_URL + "/auth/v1/signup")
                    .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.SUPABASE_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {

                @Override
                public void onFailure(okhttp3.Call call, java.io.IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(RegisterActivity.this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                    String respuesta = response.body() != null ? response.body().string() : "";

                    Log.d("SUPABASE_REGISTER", "Código: " + response.code());
                    Log.d("SUPABASE_REGISTER", "Respuesta: " + respuesta);

                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonRespuesta = new JSONObject(respuesta);

                            if (jsonRespuesta.has("access_token") && !jsonRespuesta.isNull("access_token")) {
                                accessToken = jsonRespuesta.getString("access_token");
                            }

                            if (jsonRespuesta.has("user") && !jsonRespuesta.isNull("user")) {
                                JSONObject user = jsonRespuesta.getJSONObject("user");
                                authId = user.getString("id");
                            } else if (jsonRespuesta.has("id")) {
                                authId = jsonRespuesta.getString("id");
                            }

                            if (accessToken.isEmpty()) {
                                runOnUiThread(() ->
                                        Toast.makeText(RegisterActivity.this, "No se recibió token de acceso", Toast.LENGTH_LONG).show());
                                return;
                            }

                            if (authId.isEmpty()) {
                                runOnUiThread(() ->
                                        Toast.makeText(RegisterActivity.this, "No se recibió ID del usuario", Toast.LENGTH_LONG).show());
                                return;
                            }

                            insertarUsuarioTabla();

                        } catch (Exception e) {
                            runOnUiThread(() ->
                                    Toast.makeText(RegisterActivity.this, "Error leyendo Auth: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        }
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(RegisterActivity.this, "Error registro: " + respuesta, Toast.LENGTH_LONG).show());
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error al crear usuario: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void insertarUsuarioTabla() {
        try {
            JSONObject json = new JSONObject();

            json.put("Nombre_Usuario", nombre);
            json.put("Apellidos_Usuario", apellidos);
            json.put("Email_Usuario", email);
            json.put("Auth_id", authId);
            json.put("Rol", "cliente");
            json.put("Telefono", telefono);
            json.put("genero", genero);

            String[] partes = fechaNacimiento.split("/");
            String dia = partes[0];
            String mes = partes[1];
            String anio = partes[2];

            if (dia.length() == 1) dia = "0" + dia;
            if (mes.length() == 1) mes = "0" + mes;

            String fechaSupabase = anio + "-" + mes + "-" + dia;
            json.put("Fecha_Nacimiento", fechaSupabase);

            RequestBody body = RequestBody.create(json.toString(), JSON);

            Request request = new Request.Builder()
                    .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/Usuario")
                    .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {

                @Override
                public void onFailure(okhttp3.Call call, java.io.IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(RegisterActivity.this, "Error guardando usuario: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                    String respuesta = response.body() != null ? response.body().string() : "";

                    Log.d("SUPABASE_USUARIO", "Código: " + response.code());
                    Log.d("SUPABASE_USUARIO", "Respuesta: " + respuesta);

                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {

                            SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);

                            prefs.edit()
                                    .putBoolean("isLoggedIn", true)
                                    .putString("access_token", accessToken)
                                    .putString("email", email)
                                    .putString("auth_id", authId)
                                    .putString("rol", "cliente")
                                    .remove("id_gimnasio")
                                    .apply();

                            Toast.makeText(RegisterActivity.this, "Usuario registrado correctamente", Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(RegisterActivity.this, GymListActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(RegisterActivity.this, "Error tabla Usuario: " + respuesta, Toast.LENGTH_LONG).show());
                    }
                }
            });

        } catch (Exception e) {
            runOnUiThread(() ->
                    Toast.makeText(RegisterActivity.this, "Error insertando usuario: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }
}