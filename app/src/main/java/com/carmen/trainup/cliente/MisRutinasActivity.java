package com.carmen.trainup.cliente;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.carmen.trainup.R;
import com.carmen.trainup.models.Rutina;
import com.carmen.trainup.adapters.RutinaAdapter;
import com.carmen.trainup.utils.SupabaseConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MisRutinasActivity extends AppCompatActivity {

    private RecyclerView rvMisRutinas;
    private TextView txtSinMisRutinas;
    private Button btnCrearRutina;

    private ArrayList<Rutina> listaRutinas;
    private RutinaAdapter rutinaAdapter;

    private final OkHttpClient client = new OkHttpClient();

    private String accessToken;
    private String emailUsuario;
    private int idUsuario = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mis_rutinas);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left + 28,
                    systemBars.top + 32,
                    systemBars.right + 28,
                    systemBars.bottom
            );
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
        emailUsuario = prefs.getString("email", "");

        rvMisRutinas = findViewById(R.id.rvMisRutinas);
        txtSinMisRutinas = findViewById(R.id.txtSinMisRutinas);
        btnCrearRutina = findViewById(R.id.btnCrearRutina);

        listaRutinas = new ArrayList<>();

        rutinaAdapter = new RutinaAdapter(listaRutinas, rutina -> {
            Intent intent = new Intent(MisRutinasActivity.this, VerRutinaActivity.class);
            intent.putExtra("id_rutina", rutina.getIdRutina());
            intent.putExtra("nombre_rutina", rutina.getNombreRutina());
            intent.putExtra("descripcion_rutina", rutina.getDescripcion());
            startActivity(intent);
        });

        rvMisRutinas.setLayoutManager(new LinearLayoutManager(this));
        rvMisRutinas.setAdapter(rutinaAdapter);

        btnCrearRutina.setOnClickListener(v -> {
            Intent intent = new Intent(MisRutinasActivity.this, CrearRutinaActivity.class);
            intent.putExtra("id_usuario", idUsuario);
            startActivity(intent);
        });

        obtenerIdUsuario();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (idUsuario != -1) {
            cargarMisRutinas();
        }
    }

    private void obtenerIdUsuario() {
        if (emailUsuario == null || emailUsuario.isEmpty()) {
            Toast.makeText(this, "No se encontró el email del usuario", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            String emailEncoded = URLEncoder.encode(emailUsuario, "UTF-8");

            String url = SupabaseConfig.SUPABASE_URL
                    + "/rest/v1/Usuario?select=ID_Usuario&Email_Usuario=eq."
                    + emailEncoded;

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
                    Log.e("MIS_RUTINAS", "Error obteniendo usuario", e);

                    runOnUiThread(() ->
                            Toast.makeText(MisRutinasActivity.this, "Error obteniendo usuario", Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                    String respuesta = response.body() != null ? response.body().string() : "";

                    Log.d("MIS_RUTINAS_USER", "Código: " + response.code());
                    Log.d("MIS_RUTINAS_USER", "Respuesta: " + respuesta);

                    if (response.isSuccessful()) {
                        try {
                            JSONArray array = new JSONArray(respuesta);

                            if (array.length() > 0) {
                                JSONObject obj = array.getJSONObject(0);
                                idUsuario = obj.getInt("ID_Usuario");

                                runOnUiThread(() -> cargarMisRutinas());
                            } else {
                                runOnUiThread(() ->
                                        Toast.makeText(MisRutinasActivity.this, "Usuario no encontrado", Toast.LENGTH_LONG).show()
                                );
                            }

                        } catch (Exception e) {
                            Log.e("MIS_RUTINAS", "Error leyendo usuario", e);
                        }
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(MisRutinasActivity.this, "Error Supabase: " + respuesta, Toast.LENGTH_LONG).show()
                        );
                    }
                }
            });

        } catch (Exception e) {
            Log.e("MIS_RUTINAS", "Error: " + e.getMessage());
        }
    }

    private void cargarMisRutinas() {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/Rutina?select=*&ID_Usuario=eq."
                + idUsuario;

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
                Log.e("MIS_RUTINAS", "Error cargando rutinas", e);

                runOnUiThread(() ->
                        Toast.makeText(MisRutinasActivity.this, "Error cargando mis rutinas", Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                String respuesta = response.body() != null ? response.body().string() : "";

                Log.d("MIS_RUTINAS", "Código: " + response.code());
                Log.d("MIS_RUTINAS", "Respuesta: " + respuesta);

                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(respuesta);

                        listaRutinas.clear();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);

                            long idRutina = obj.optLong("ID_Rutina", 0);
                            String nombre = obj.optString("Nombre_Rutina", "Sin nombre");
                            String descripcion = obj.optString("Descripcion", "Sin descripción");
                            int diasSemana = obj.optInt("Dias_Semana", 0);
                            String objetivo = obj.optString("Objetivo", "Sin objetivo");
                            String nivel = obj.optString("Nivel", "principiante");
                            String imagen = obj.optString("Imagen_Rutina", "");

                            listaRutinas.add(new Rutina(
                                    idRutina,
                                    nombre,
                                    descripcion,
                                    diasSemana,
                                    objetivo,
                                    nivel,
                                    imagen
                            ));
                        }

                        runOnUiThread(() -> {
                            rutinaAdapter.notifyDataSetChanged();

                            if (listaRutinas.isEmpty()) {
                                txtSinMisRutinas.setVisibility(View.VISIBLE);
                            } else {
                                txtSinMisRutinas.setVisibility(View.GONE);
                            }
                        });

                    } catch (Exception e) {
                        Log.e("MIS_RUTINAS", "Error leyendo rutinas", e);

                        runOnUiThread(() ->
                                Toast.makeText(MisRutinasActivity.this, "Error leyendo mis rutinas", Toast.LENGTH_LONG).show()
                        );
                    }

                } else {
                    runOnUiThread(() ->
                            Toast.makeText(MisRutinasActivity.this, "Error Supabase: " + respuesta, Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }
}