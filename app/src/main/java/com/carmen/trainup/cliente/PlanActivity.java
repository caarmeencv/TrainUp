package com.carmen.trainup.cliente;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.carmen.trainup.adapters.PlanAdapter;
import com.carmen.trainup.models.Plan;
import com.carmen.trainup.utils.SupabaseConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PlanActivity extends AppCompatActivity {

    private RecyclerView rvPlanes;
    private TextView txtNombreGimnasioPlan;

    private ArrayList<Plan> listaPlanes;
    private PlanAdapter planAdapter;

    private final OkHttpClient client = new OkHttpClient();

    private int idGimnasio;
    private long idUsuario;
    private String nombreGimnasio;
    private String accessToken;

    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_plan);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvPlanes = findViewById(R.id.rvPlanes);
        txtNombreGimnasioPlan = findViewById(R.id.txtNombreGimnasioPlan);

        SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);

        accessToken = prefs.getString("access_token", "");
        idUsuario = leerLongPrefs(prefs, "id_usuario", -1);

        idGimnasio = getIntent().getIntExtra("id_gimnasio", -1);
        nombreGimnasio = getIntent().getStringExtra("nombre_gimnasio");

        Log.d("PLAN", "ID Usuario: " + idUsuario);
        Log.d("PLAN", "ID Gimnasio: " + idGimnasio);

        if (idGimnasio == -1) {
            Toast.makeText(this, "No se encontró el gimnasio", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (nombreGimnasio == null || nombreGimnasio.isEmpty()) {
            nombreGimnasio = "este gimnasio";
        }

        txtNombreGimnasioPlan.setText("Planes disponibles de " + nombreGimnasio);

        listaPlanes = new ArrayList<>();
        planAdapter = new PlanAdapter(listaPlanes, plan -> actualizarUsuarioConPlan(plan));

        rvPlanes.setLayoutManager(new LinearLayoutManager(this));
        rvPlanes.setAdapter(planAdapter);

        cargarPlanes();
    }

    private long leerLongPrefs(SharedPreferences prefs, String clave, long valorDefecto) {
        try {
            return prefs.getLong(clave, valorDefecto);
        } catch (ClassCastException e) {
            return prefs.getInt(clave, (int) valorDefecto);
        }
    }

    private void cargarPlanes() {

        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/Plan?select=*"
                + "&ID_Gimnasio=eq." + idGimnasio;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.SUPABASE_API_KEY)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {

            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(PlanActivity.this, "Error cargando planes", Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws java.io.IOException {

                String respuesta = response.body() != null ? response.body().string() : "";

                Log.d("PLANES", "Código: " + response.code());
                Log.d("PLANES", "Respuesta: " + respuesta);

                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(respuesta);
                        listaPlanes.clear();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);

                            int idPlan = obj.getInt("ID_Plan");
                            String nombrePlan = obj.optString("Nombre_Plan");
                            String descripcion = obj.optString("Descripcion");
                            String precio = obj.optString("Precio");

                            listaPlanes.add(new Plan(idPlan, nombrePlan, descripcion, precio));
                        }

                        runOnUiThread(() -> planAdapter.notifyDataSetChanged());

                    } catch (Exception e) {
                        Log.e("PLAN_ERROR", "Error leyendo JSON", e);
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(PlanActivity.this, "Error Supabase: " + response.code(), Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }

    private void actualizarUsuarioConPlan(Plan plan) {

        if (idUsuario == -1) {
            Toast.makeText(this, "No se encontró el ID del usuario. Cierra sesión y vuelve a entrar.", Toast.LENGTH_LONG).show();
            return;
        }

        new Thread(() -> {
            try {
                int idPlan = plan.getIdPlan();

                JSONObject json = new JSONObject();
                json.put("ID_Gimnasio", idGimnasio);
                json.put("ID_Plan", idPlan);

                RequestBody body = RequestBody.create(json.toString(), JSON);

                String url = SupabaseConfig.SUPABASE_URL
                        + "/rest/v1/Usuario?ID_Usuario=eq." + idUsuario;

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                        .addHeader("Authorization", "Bearer " + SupabaseConfig.SUPABASE_API_KEY)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=representation")
                        .patch(body)
                        .build();

                Response response = client.newCall(request).execute();
                String respuesta = response.body() != null ? response.body().string() : "";

                Log.d("UPDATE_PLAN", "ID Usuario: " + idUsuario);
                Log.d("UPDATE_PLAN", "ID Gimnasio: " + idGimnasio);
                Log.d("UPDATE_PLAN", "ID Plan: " + idPlan);
                Log.d("UPDATE_PLAN", "JSON: " + json.toString());
                Log.d("UPDATE_PLAN", "Código: " + response.code());
                Log.d("UPDATE_PLAN", "Respuesta: " + respuesta);

                if (response.isSuccessful() && !respuesta.equals("[]")) {
                    runOnUiThread(() -> {

                        SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);

                        prefs.edit()
                                .putInt("id_gimnasio", idGimnasio)
                                .putInt("id_plan", idPlan)
                                .apply();

                        Toast.makeText(this, "Plan guardado", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(PlanActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "No se pudo actualizar: " + respuesta, Toast.LENGTH_LONG).show()
                    );
                }

            } catch (Exception e) {
                Log.e("UPDATE_PLAN", "Error", e);

                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
}