package com.carmen.trainup.cliente;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.carmen.trainup.R;
import com.carmen.trainup.adapters.RutinaAdapter;
import com.carmen.trainup.utils.SupabaseConfig;
import com.carmen.trainup.models.Rutina;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class RutinaActivity extends AppCompatActivity {

    private RecyclerView rvRutinas;
    private TextView txtSinRutinas;
    private Button btnMisRutinas;

    private ArrayList<Rutina> listaRutinas;
    private RutinaAdapter rutinaAdapter;

    private final OkHttpClient client = new OkHttpClient();

    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rutina);

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

        rvRutinas = findViewById(R.id.rvRutinas);
        txtSinRutinas = findViewById(R.id.txtSinRutinas);
        btnMisRutinas = findViewById(R.id.btnMisRutinas);

        listaRutinas = new ArrayList<>();

            rutinaAdapter = new RutinaAdapter(listaRutinas, rutina -> {
                Intent intent = new Intent(RutinaActivity.this, VerRutinaActivity.class);
                intent.putExtra("id_rutina", rutina.getIdRutina());
                intent.putExtra("nombre_rutina", rutina.getNombreRutina());
                intent.putExtra("descripcion_rutina", rutina.getDescripcion());
                startActivity(intent);
            });

        rvRutinas.setLayoutManager(new LinearLayoutManager(this));
        rvRutinas.setAdapter(rutinaAdapter);

        btnMisRutinas.setOnClickListener(v -> {
            Intent intent = new Intent(RutinaActivity.this, MisRutinasActivity.class);
            startActivity(intent);
        });

        cargarRutinasRecomendadas();
    }

    private void cargarRutinasRecomendadas() {

        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/Rutina?select=*&ID_Usuario=is.null";

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
                Log.e("SUPABASE_RUTINAS", "Error conexión", e);

                runOnUiThread(() ->
                        Toast.makeText(RutinaActivity.this, "Error cargando rutinas", Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {

                String respuesta = response.body() != null ? response.body().string() : "";

                Log.d("SUPABASE_RUTINAS", "Código: " + response.code());
                Log.d("SUPABASE_RUTINAS", "Respuesta: " + respuesta);

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

                            Rutina rutina = new Rutina(
                                    idRutina,
                                    nombre,
                                    descripcion,
                                    diasSemana,
                                    objetivo,
                                    nivel,
                                    imagen
                            );

                            listaRutinas.add(rutina);
                        }

                        runOnUiThread(() -> {
                            rutinaAdapter.notifyDataSetChanged();

                            if (listaRutinas.isEmpty()) {
                                txtSinRutinas.setVisibility(View.VISIBLE);
                            } else {
                                txtSinRutinas.setVisibility(View.GONE);
                            }
                        });

                    } catch (Exception e) {
                        Log.e("SUPABASE_RUTINAS", "Error leyendo JSON", e);

                        runOnUiThread(() ->
                                Toast.makeText(RutinaActivity.this, "Error leyendo rutinas", Toast.LENGTH_LONG).show()
                        );
                    }

                } else {
                    Log.e("SUPABASE_RUTINAS", "Error Supabase: " + respuesta);

                    runOnUiThread(() ->
                            Toast.makeText(RutinaActivity.this, "Error Supabase: " + respuesta, Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }
}