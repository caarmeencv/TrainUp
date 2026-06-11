package com.carmen.trainup.cliente;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.carmen.trainup.utils.SupabaseConfig;
import com.carmen.trainup.adapters.EjercicioRutinaAdapter;
import com.carmen.trainup.models.EjercicioRutina;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class VerRutinaActivity extends AppCompatActivity {

    private TextView txtNombreRutinaDetalle, txtDescripcionRutinaDetalle, txtSinEjercicios;
    private RecyclerView rvEjerciciosRutina;

    private ArrayList<EjercicioRutina> listaEjercicios;
    private EjercicioRutinaAdapter ejercicioAdapter;

    private final OkHttpClient client = new OkHttpClient();

    private String accessToken;
    private long idRutina;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ver_rutina);

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

        idRutina = getIntent().getLongExtra("id_rutina", -1);
        String nombreRutina = getIntent().getStringExtra("nombre_rutina");
        String descripcionRutina = getIntent().getStringExtra("descripcion_rutina");

        txtNombreRutinaDetalle = findViewById(R.id.txtNombreRutinaDetalle);
        txtDescripcionRutinaDetalle = findViewById(R.id.txtDescripcionRutinaDetalle);
        txtSinEjercicios = findViewById(R.id.txtSinEjercicios);
        rvEjerciciosRutina = findViewById(R.id.rvEjerciciosRutina);

        txtNombreRutinaDetalle.setText(nombreRutina != null ? nombreRutina : "Rutina");
        txtDescripcionRutinaDetalle.setText(descripcionRutina != null ? descripcionRutina : "");

        listaEjercicios = new ArrayList<>();
        ejercicioAdapter = new EjercicioRutinaAdapter(listaEjercicios);

        rvEjerciciosRutina.setLayoutManager(new LinearLayoutManager(this));
        rvEjerciciosRutina.setAdapter(ejercicioAdapter);

        if (idRutina != -1) {
            cargarEjerciciosRutina();
        } else {
            Toast.makeText(this, "No se encontró la rutina", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarEjerciciosRutina() {

        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/Rutina_Ejercicio?select=Series,Repeticiones,Peso,Descanso,Orden_Ejercicio,Ejercicio(ID_Ejercicio,Nombre_Ejercicio,Descripcion,Grupo_Muscular,Imagen_Ejercicio)"
                + "&ID_Rutina=eq." + idRutina
                + "&order=Orden_Ejercicio.asc";

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
                Log.e("SUPABASE_EJERCICIOS", "Error conexión", e);

                runOnUiThread(() ->
                        Toast.makeText(VerRutinaActivity.this, "Error cargando ejercicios", Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {

                String respuesta = response.body() != null ? response.body().string() : "";

                Log.d("SUPABASE_EJERCICIOS", "Código: " + response.code());
                Log.d("SUPABASE_EJERCICIOS", "Respuesta: " + respuesta);

                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(respuesta);

                        listaEjercicios.clear();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);

                            int series = obj.optInt("Series", 0);
                            int repeticiones = obj.optInt("Repeticiones", 0);
                            double peso = obj.optDouble("Peso", 0);
                            int descanso = obj.optInt("Descanso", 0);
                            int orden = obj.optInt("Orden_Ejercicio", 0);

                            JSONObject ejercicioObj = obj.getJSONObject("Ejercicio");

                            long idEjercicio = ejercicioObj.optLong("ID_Ejercicio", 0);
                            String nombre = ejercicioObj.optString("Nombre_Ejercicio", "Sin nombre");
                            String descripcion = ejercicioObj.optString("Descripcion", "Sin descripción");
                            String grupo = ejercicioObj.optString("Grupo_Muscular", "Sin grupo");
                            String imagen = ejercicioObj.optString("Imagen_Ejercicio", "");

                            listaEjercicios.add(new EjercicioRutina(
                                    idEjercicio,
                                    nombre,
                                    descripcion,
                                    grupo,
                                    imagen,
                                    series,
                                    repeticiones,
                                    peso,
                                    descanso,
                                    orden
                            ));
                        }

                        runOnUiThread(() -> {
                            ejercicioAdapter.notifyDataSetChanged();

                            if (listaEjercicios.isEmpty()) {
                                txtSinEjercicios.setVisibility(View.VISIBLE);
                            } else {
                                txtSinEjercicios.setVisibility(View.GONE);
                            }
                        });

                    } catch (Exception e) {
                        Log.e("SUPABASE_EJERCICIOS", "Error leyendo JSON", e);

                        runOnUiThread(() ->
                                Toast.makeText(VerRutinaActivity.this, "Error leyendo ejercicios", Toast.LENGTH_LONG).show()
                        );
                    }

                } else {
                    Log.e("SUPABASE_EJERCICIOS", "Error Supabase: " + respuesta);

                    runOnUiThread(() ->
                            Toast.makeText(VerRutinaActivity.this, "Error Supabase: " + respuesta, Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }
}