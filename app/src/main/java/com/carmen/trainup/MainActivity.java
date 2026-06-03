package com.carmen.trainup;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView txtSaludo;
    private TextView txtClaseNombre, txtClaseGimnasio, txtClaseHorario;
    private TextView txtRutinaNombre, txtRutinaInfo;

    private String accessToken;
    private String emailUsuario;
    private long idUsuario = -1;

    private final OkHttpClient client = new OkHttpClient();

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
        txtClaseNombre = findViewById(R.id.txtClaseNombre);
        txtClaseGimnasio = findViewById(R.id.txtClaseGimnasio);
        txtClaseHorario = findViewById(R.id.txtClaseHorario);
        txtRutinaNombre = findViewById(R.id.txtRutinaNombre);
        txtRutinaInfo = findViewById(R.id.txtRutinaInfo);

        txtSaludo.setText("¡Hola! 👋");

        cargarUsuario();
        cargarClase();
    }

    private void cargarUsuario() {
        if (emailUsuario == null || emailUsuario.isEmpty()) {
            return;
        }

        String emailCodificado = URLEncoder.encode(emailUsuario, StandardCharsets.UTF_8);

        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL
                        + "/rest/v1/Usuario?select=*&Email_Usuario=eq."
                        + emailCodificado
                        + "&limit=1")
                .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("USUARIO_ERROR", "Error cargando usuario", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respuesta = response.body() != null ? response.body().string() : "";

                Log.d("USUARIO_RESPONSE", respuesta);

                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(respuesta);

                        if (array.length() > 0) {
                            JSONObject usuario = array.getJSONObject(0);

                            idUsuario = usuario.optLong("ID_Usuario", -1);
                            String nombre = usuario.optString("Nombre_Usuario", "Usuario");

                            runOnUiThread(() ->
                                    txtSaludo.setText("¡Hola, " + nombre + "! 👋")
                            );

                            cargarRutinaUsuario();
                        }

                    } catch (Exception e) {
                        Log.e("USUARIO_ERROR", "Error leyendo usuario", e);
                    }
                } else {
                    Log.e("USUARIO_ERROR", "Código: " + response.code() + " Respuesta: " + respuesta);
                }

                response.close();
            }
        });
    }

    private void cargarClase() {
        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL
                        + "/rest/v1/Clase?select=*,Gimnasio(Nombre_Gimnasio)&limit=1")
                .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("CLASE_ERROR", "Error cargando clase", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respuesta = response.body() != null ? response.body().string() : "";

                Log.d("CLASE_RESPONSE", respuesta);

                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(respuesta);

                        if (array.length() > 0) {
                            JSONObject clase = array.getJSONObject(0);

                            String nombreClase = clase.optString("Nombre_Clase", "Clase");
                            String fecha = clase.optString("Fecha_Clase", "");
                            String hora = clase.optString("Hora_Clase", "");
                            String monitor = clase.optString("Monitor", "");

                            JSONObject gimnasioObj = clase.optJSONObject("Gimnasio");
                            String nombreGimnasio = "Gimnasio";

                            if (gimnasioObj != null) {
                                nombreGimnasio = gimnasioObj.optString("Nombre_Gimnasio", "Gimnasio");
                            }

                            String horario = fecha + " · " + hora;

                            if (!monitor.isEmpty()) {
                                horario = horario + " · Monitor: " + monitor;
                            }

                            String finalNombreGimnasio = nombreGimnasio;
                            String finalHorario = horario;

                            runOnUiThread(() -> {
                                txtClaseNombre.setText(nombreClase);
                                txtClaseGimnasio.setText(finalNombreGimnasio);
                                txtClaseHorario.setText(finalHorario);
                            });
                        }

                    } catch (Exception e) {
                        Log.e("CLASE_ERROR", "Error leyendo clase", e);
                    }
                } else {
                    Log.e("CLASE_ERROR", "Código: " + response.code() + " Respuesta: " + respuesta);
                }

                response.close();
            }
        });
    }

    private void cargarRutinaUsuario() {
        if (idUsuario == -1) {
            return;
        }

        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL
                        + "/rest/v1/Rutina?select=*&ID_Usuario=eq."
                        + idUsuario
                        + "&limit=1")
                .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("RUTINA_ERROR", "Error cargando rutina", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respuesta = response.body() != null ? response.body().string() : "";

                Log.d("RUTINA_RESPONSE", respuesta);

                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(respuesta);

                        if (array.length() > 0) {
                            JSONObject rutina = array.getJSONObject(0);

                            String nombreRutina = rutina.optString("Nombre_Rutina", "Rutina");
                            String objetivo = rutina.optString("Objetivo", "Sin objetivo");
                            int diasSemana = rutina.optInt("Dias_Semana", 0);

                            String info = diasSemana + " días por semana";

                            if (!objetivo.isEmpty()) {
                                info = info + " · " + objetivo;
                            }

                            String finalInfo = info;

                            runOnUiThread(() -> {
                                txtRutinaNombre.setText(nombreRutina);
                                txtRutinaInfo.setText(finalInfo);
                            });
                        } else {
                            runOnUiThread(() -> {
                                txtRutinaNombre.setText("Sin rutina asignada");
                                txtRutinaInfo.setText("Crea una rutina para empezar");
                            });
                        }

                    } catch (Exception e) {
                        Log.e("RUTINA_ERROR", "Error leyendo rutina", e);
                    }
                } else {
                    Log.e("RUTINA_ERROR", "Código: " + response.code() + " Respuesta: " + respuesta);
                }

                response.close();
            }
        });
    }
}