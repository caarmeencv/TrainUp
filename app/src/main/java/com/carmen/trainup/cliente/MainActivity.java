package com.carmen.trainup.cliente;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.carmen.trainup.R;
import com.carmen.trainup.utils.MenuHelper;
import com.carmen.trainup.utils.SupabaseConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView txtSaludo, btnMenu;
    private TextView txtClaseNombre, txtClaseGimnasio, txtClaseHorario, btnVerClases;
    private View btnRutinas, btnClases, btnGimnasios;

    private String accessToken;
    private String emailUsuario;
    private int idGimnasio;
    private long idUsuario = -1;

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);

        accessToken = prefs.getString("access_token", "");
        emailUsuario = prefs.getString("email", "");
        idGimnasio = prefs.getInt("id_gimnasio", -1);

        txtSaludo = findViewById(R.id.txtSaludo);
        btnMenu = findViewById(R.id.btnMenu);

        txtClaseNombre = findViewById(R.id.txtClaseNombre);
        txtClaseGimnasio = findViewById(R.id.txtClaseGimnasio);
        txtClaseHorario = findViewById(R.id.txtClaseHorario);
        btnVerClases = findViewById(R.id.btnVerClases);

        btnRutinas = findViewById(R.id.btnRutinas);
        btnClases = findViewById(R.id.btnClases);
        btnGimnasios = findViewById(R.id.btnGimnasios);

        MenuHelper.configurarMenu(this, btnMenu);

        txtSaludo.setText("¡Hola!");
        txtClaseNombre.setText("Cargando...");
        txtClaseGimnasio.setText("");
        txtClaseHorario.setText("");

        btnRutinas.setOnClickListener(v -> abrirRutinas());
        btnClases.setOnClickListener(v -> abrirClases());
        btnGimnasios.setOnClickListener(v -> abrirInfoGimnasio());
        btnVerClases.setOnClickListener(v -> abrirReservas());

        cargarUsuario();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarUsuario();
    }

    private void cargarUsuario() {
        new Thread(() -> {
            try {
                String emailEncoded = URLEncoder.encode(emailUsuario, "UTF-8");

                String url = SupabaseConfig.SUPABASE_URL +
                        "/rest/v1/Usuario?select=ID_Usuario,Nombre_Usuario,ID_Gimnasio&Email_Usuario=eq." + emailEncoded;

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .build();

                Response response = client.newCall(request).execute();
                String body = response.body() != null ? response.body().string() : "";

                Log.d("MAIN_RESERVA", "USUARIO: " + body);

                if (!response.isSuccessful()) {
                    mostrarError("Error usuario", body);
                    return;
                }

                JSONArray usuarios = new JSONArray(body);

                if (usuarios.length() > 0) {
                    JSONObject usuario = usuarios.getJSONObject(0);

                    idUsuario = usuario.getLong("ID_Usuario");
                    idGimnasio = usuario.optInt("ID_Gimnasio", idGimnasio);

                    getSharedPreferences("TrainUpPrefs", MODE_PRIVATE)
                            .edit()
                            .putInt("id_usuario", (int) idUsuario)
                            .putInt("id_gimnasio", idGimnasio)
                            .apply();

                    cargarProximaReserva();

                } else {
                    mostrarError("Sin usuario", "No se encontró el usuario");
                }

            } catch (Exception e) {
                Log.e("MAIN_RESERVA", "Error usuario", e);
                mostrarError("Error usuario", e.getMessage());
            }
        }).start();
    }

    private void cargarProximaReserva() {
        new Thread(() -> {
            try {
                String urlReservas = SupabaseConfig.SUPABASE_URL +
                        "/rest/v1/Reserva?select=ID_Reserva,ID_Clase,Estado" +
                        "&ID_Usuario=eq." + idUsuario;

                Request requestReservas = new Request.Builder()
                        .url(urlReservas)
                        .get()
                        .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .build();

                Response responseReservas = client.newCall(requestReservas).execute();
                String bodyReservas = responseReservas.body() != null ? responseReservas.body().string() : "";

                Log.d("MAIN_RESERVA", "RESERVAS: " + bodyReservas);

                if (!responseReservas.isSuccessful()) {
                    mostrarError("Error reserva", bodyReservas);
                    return;
                }

                JSONArray reservas = new JSONArray(bodyReservas);

                JSONObject proximaClase = null;
                Date fechaProxima = null;

                SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date ahora = new Date();

                for (int i = 0; i < reservas.length(); i++) {
                    JSONObject reserva = reservas.getJSONObject(i);

                    String estado = reserva.optString("Estado", "");

                    if (estado.equalsIgnoreCase("Cancelada")) {
                        continue;
                    }

                    long idClase = reserva.getLong("ID_Clase");
                    JSONObject clase = obtenerClasePorId(idClase);

                    if (clase == null) {
                        continue;
                    }

                    String fecha = clase.getString("Fecha_Clase");
                    String hora = clase.getString("Hora_Clase");

                    if (hora.length() == 5) {
                        hora = hora + ":00";
                    }

                    Date fechaClase = formato.parse(fecha + " " + hora);

                    if (fechaClase != null && fechaClase.after(ahora)) {
                        if (fechaProxima == null || fechaClase.before(fechaProxima)) {
                            fechaProxima = fechaClase;
                            proximaClase = clase;
                        }
                    }
                }

                JSONObject finalProximaClase = proximaClase;

                runOnUiThread(() -> {
                    if (finalProximaClase != null) {
                        try {
                            String nombre = finalProximaClase.getString("Nombre_Clase");
                            String hora = finalProximaClase.getString("Hora_Clase");
                            String sala = finalProximaClase.optString("Sala", "");

                            txtClaseNombre.setText(nombre);
                            txtClaseGimnasio.setText("Mi gimnasio");
                            txtClaseHorario.setText(hora + " · " + sala);

                        } catch (Exception e) {
                            mostrarError("Error mostrando reserva", e.getMessage());
                        }
                    } else {
                        txtClaseNombre.setText("Sin próximas reservas");
                        txtClaseGimnasio.setText("No tienes reservas próximas");
                        txtClaseHorario.setText("Haz una reserva.");
                    }
                });

            } catch (Exception e) {
                Log.e("MAIN_RESERVA", "Error reserva", e);
                mostrarError("Error reserva", e.getMessage());
            }
        }).start();
    }

    private JSONObject obtenerClasePorId(long idClase) {
        try {
            String urlClase = SupabaseConfig.SUPABASE_URL +
                    "/rest/v1/Clase?select=ID_Clase,Nombre_Clase,Fecha_Clase,Hora_Clase,Sala,Monitor" +
                    "&ID_Clase=eq." + idClase;

            Request requestClase = new Request.Builder()
                    .url(urlClase)
                    .get()
                    .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();

            Response responseClase = client.newCall(requestClase).execute();
            String bodyClase = responseClase.body() != null ? responseClase.body().string() : "";

            Log.d("MAIN_RESERVA", "CLASE: " + bodyClase);

            if (!responseClase.isSuccessful()) {
                return null;
            }

            JSONArray clases = new JSONArray(bodyClase);

            if (clases.length() == 0) {
                return null;
            }

            return clases.getJSONObject(0);

        } catch (Exception e) {
            Log.e("MAIN_RESERVA", "Error clase", e);
            return null;
        }
    }

    private void mostrarError(String titulo, String detalle) {
        runOnUiThread(() -> {
            txtClaseNombre.setText(titulo);
            txtClaseGimnasio.setText(detalle == null ? "" : detalle);
            txtClaseHorario.setText("");
        });
    }

    private void abrirRutinas() {
        startActivity(new Intent(MainActivity.this, RutinaActivity.class));
    }

    private void abrirClases() {
        startActivity(new Intent(MainActivity.this, ClassListActivity.class));
    }

    private void abrirReservas() {
        startActivity(new Intent(MainActivity.this, ReservasActivity.class));
    }

    private void abrirInfoGimnasio() {
        Intent intent = new Intent(MainActivity.this, InfoActivity2.class);
        intent.putExtra("id_gimnasio", idGimnasio);
        startActivity(intent);
    }
}