package com.carmen.trainup.cliente;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.carmen.trainup.R;
import com.carmen.trainup.utils.SupabaseConfig;
import com.carmen.trainup.adapters.ClaseAdapter;
import com.carmen.trainup.models.Clase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ClassListActivity extends AppCompatActivity {

    private RecyclerView rvClases;
    private TextView txtSinClases;
    private Button btnMisReservas;

    private ArrayList<Clase> listaClases;
    private HashSet<Long> clasesReservadas;
    private ClaseAdapter claseAdapter;

    private final OkHttpClient client = new OkHttpClient();

    private String accessToken;
    private String emailUsuario;
    private long idUsuario = -1;
    private int idGimnasio = -1;

    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_list);

        SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
        emailUsuario = prefs.getString("email", "");

        rvClases = findViewById(R.id.rvClases);
        txtSinClases = findViewById(R.id.txtSinClases);
        btnMisReservas = findViewById(R.id.btnMisReservas);

        listaClases = new ArrayList<>();
        clasesReservadas = new HashSet<>();

        claseAdapter = new ClaseAdapter(listaClases, clasesReservadas, (clase, boton) -> {
            reservarClase(clase, boton);
        });

        rvClases.setLayoutManager(new LinearLayoutManager(this));
        rvClases.setAdapter(claseAdapter);

        btnMisReservas.setOnClickListener(v -> {
            Intent intent = new Intent(ClassListActivity.this, ReservasActivity.class);
            startActivity(intent);
        });

        cargarUsuario();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (idUsuario != -1 && idGimnasio != -1) {
            cargarReservasActivas();
        }
    }

    private void cargarUsuario() {
        try {
            String emailCodificado = URLEncoder.encode(emailUsuario, "UTF-8");

            String url = SupabaseConfig.SUPABASE_URL
                    + "/rest/v1/Usuario?select=ID_Usuario,ID_Gimnasio&Email_Usuario=eq."
                    + emailCodificado;

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
                            Toast.makeText(ClassListActivity.this, "Error cargando usuario", Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                    String respuesta = response.body() != null ? response.body().string() : "";

                    try {
                        JSONArray array = new JSONArray(respuesta);

                        if (array.length() > 0) {
                            JSONObject usuario = array.getJSONObject(0);

                            idUsuario = usuario.optLong("ID_Usuario", -1);
                            idGimnasio = usuario.optInt("ID_Gimnasio", -1);

                            cargarReservasActivas();

                        } else {
                            runOnUiThread(() -> {
                                txtSinClases.setVisibility(View.VISIBLE);
                                Toast.makeText(ClassListActivity.this, "No se encontró el usuario", Toast.LENGTH_LONG).show();
                            });
                        }

                    } catch (Exception e) {
                        Log.e("CLASES", "Error leyendo usuario", e);
                    }
                }
            });

        } catch (Exception e) {
            Log.e("CLASES", "Error codificando email", e);
        }
    }

    private void cargarReservasActivas() {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/Reserva?select=ID_Clase&ID_Usuario=eq."
                + idUsuario
                + "&Estado=eq.Activa";

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
                        Toast.makeText(ClassListActivity.this, "Error cargando reservas", Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                String respuesta = response.body() != null ? response.body().string() : "";

                try {
                    JSONArray array = new JSONArray(respuesta);

                    clasesReservadas.clear();

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        clasesReservadas.add(obj.optLong("ID_Clase", 0));
                    }

                    cargarClases();

                } catch (Exception e) {
                    Log.e("CLASES", "Error leyendo reservas activas", e);
                }
            }
        });
    }

    private void cargarClases() {

        if (idGimnasio == -1) {
            runOnUiThread(() -> {
                txtSinClases.setVisibility(View.VISIBLE);
                Toast.makeText(this, "El usuario no tiene gimnasio asignado", Toast.LENGTH_LONG).show();
            });
            return;
        }

        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/Clase?select=*&ID_Gimnasio=eq."
                + idGimnasio
                + "&order=Fecha_Clase.asc";

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
                        Toast.makeText(ClassListActivity.this, "Error cargando clases", Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                String respuesta = response.body() != null ? response.body().string() : "";

                try {
                    JSONArray array = new JSONArray(respuesta);

                    listaClases.clear();

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);

                        Clase clase = new Clase(
                                obj.optLong("ID_Clase", 0),
                                obj.optString("Nombre_Clase", "Sin nombre"),
                                obj.optString("Descripcion", ""),
                                obj.optString("Fecha_Clase", ""),
                                obj.optString("Hora_Clase", ""),
                                obj.optInt("Plazas_Maximas", 0),
                                obj.optInt("Duracion", 0),
                                obj.optString("Sala", ""),
                                obj.optString("Monitor", ""),
                                obj.optInt("ID_Gimnasio", 0),
                                obj.optString("Imagen_Clase", "")
                        );

                        listaClases.add(clase);
                    }

                    runOnUiThread(() -> {
                        claseAdapter.notifyDataSetChanged();
                        txtSinClases.setVisibility(listaClases.isEmpty() ? View.VISIBLE : View.GONE);
                    });

                } catch (Exception e) {
                    Log.e("CLASES", "Error leyendo clases", e);
                }
            }
        });
    }

    private void reservarClase(Clase clase, Button boton) {

        if (idUsuario == -1) {
            Toast.makeText(this, "Usuario no cargado", Toast.LENGTH_SHORT).show();
            return;
        }

        boton.setEnabled(false);

        String json = "{"
                + "\"ID_Usuario\":" + idUsuario + ","
                + "\"ID_Clase\":" + clase.getIdClase() + ","
                + "\"Estado\":\"Activa\""
                + "}";

        RequestBody body = RequestBody.create(json, JSON);

        String url = SupabaseConfig.SUPABASE_URL + "/rest/v1/Reserva";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> {
                    boton.setEnabled(true);
                    Toast.makeText(ClassListActivity.this, "Error al reservar", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                String respuesta = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        clasesReservadas.add(clase.getIdClase());
                        claseAdapter.notifyDataSetChanged();
                        Toast.makeText(ClassListActivity.this, "Clase reservada", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Log.e("RESERVAR", "Error: " + respuesta);

                    runOnUiThread(() -> {
                        boton.setEnabled(true);
                        Toast.makeText(ClassListActivity.this, "Error al reservar", Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
}