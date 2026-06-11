package com.carmen.trainup.admin;

import android.app.AlertDialog;
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
import com.carmen.trainup.utils.SupabaseConfig;
import com.carmen.trainup.adapters.AdminClaseAdapter;
import com.carmen.trainup.models.AdminClase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class AdminClasesActivity extends AppCompatActivity {

    private TextView btnCrearClase;
    private RecyclerView rvClases;

    private ArrayList<AdminClase> listaClases;
    private AdminClaseAdapter adapter;

    private final OkHttpClient client = new OkHttpClient();

    private String accessToken;
    private String emailUsuario;
    private int idGimnasio = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_clases);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
        emailUsuario = prefs.getString("email", "");

        btnCrearClase = findViewById(R.id.btnCrearClase);
        rvClases = findViewById(R.id.rvClases);

        listaClases = new ArrayList<>();

        adapter = new AdminClaseAdapter(listaClases, new AdminClaseAdapter.OnClaseClickListener() {
            @Override
            public void onEditar(AdminClase clase) {
                Intent intent = new Intent(AdminClasesActivity.this, AdminEditarClaseActivity.class);

                intent.putExtra("idClase", clase.getIdClase());
                intent.putExtra("nombre", clase.getNombre());
                intent.putExtra("descripcion", clase.getDescripcion());
                intent.putExtra("fecha", clase.getFecha());
                intent.putExtra("hora", clase.getHora());
                intent.putExtra("plazas", clase.getPlazas());
                intent.putExtra("duracion", clase.getDuracion());
                intent.putExtra("sala", clase.getSala());
                intent.putExtra("monitor", clase.getMonitor());
                intent.putExtra("imagen", clase.getImagen());

                startActivity(intent);
            }

            @Override
            public void onEliminar(AdminClase clase) {
                confirmarEliminarClase(clase);
            }
        });

        rvClases.setLayoutManager(new LinearLayoutManager(this));
        rvClases.setAdapter(adapter);

        btnCrearClase.setOnClickListener(v -> {
            Intent intent = new Intent(AdminClasesActivity.this, AdminCrearClaseActivity.class);
            startActivity(intent);
        });

        cargarGimnasioUsuario();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (idGimnasio != -1) {
            cargarClases();
        }
    }

    private void confirmarEliminarClase(AdminClase clase) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar clase")
                .setMessage("¿Seguro que quieres eliminar \"" + clase.getNombre() + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarClase(clase))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarClase(AdminClase clase) {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/Clase?ID_Clase=eq."
                + clase.getIdClase();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Accept", "application/json")
                .addHeader("Prefer", "return=representation")
                .delete()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(AdminClasesActivity.this, "Error eliminando clase", Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                String respuesta = response.body() != null ? response.body().string() : "";

                Log.e("ELIMINAR_CLASE", "CODIGO=" + response.code() + " RESPUESTA=" + respuesta);

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        listaClases.remove(clase);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(AdminClasesActivity.this, "Clase eliminada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AdminClasesActivity.this, "Error " + response.code() + ": " + respuesta, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void cargarGimnasioUsuario() {
        try {
            String emailCodificado = URLEncoder.encode(emailUsuario, "UTF-8");

            String url = SupabaseConfig.SUPABASE_URL
                    + "/rest/v1/Usuario?select=ID_Gimnasio&Email_Usuario=eq."
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
                            Toast.makeText(AdminClasesActivity.this, "Error cargando gimnasio", Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                    String respuesta = response.body() != null ? response.body().string() : "";

                    try {
                        JSONArray array = new JSONArray(respuesta);

                        if (array.length() > 0) {
                            JSONObject usuario = array.getJSONObject(0);
                            idGimnasio = usuario.optInt("ID_Gimnasio", -1);

                            cargarClases();
                        } else {
                            runOnUiThread(() ->
                                    Toast.makeText(AdminClasesActivity.this, "No se encontró el usuario", Toast.LENGTH_LONG).show()
                            );
                        }

                    } catch (Exception e) {
                        Log.e("ADMIN_CLASES", "Error leyendo gimnasio", e);
                    }
                }
            });

        } catch (Exception e) {
            Log.e("ADMIN_CLASES", "Error codificando email", e);
        }
    }

    private void cargarClases() {
        if (idGimnasio == -1) {
            Toast.makeText(this, "No tienes gimnasio asignado", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(AdminClasesActivity.this, "Error cargando clases", Toast.LENGTH_LONG).show()
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

                        AdminClase clase = new AdminClase(
                                obj.optLong("ID_Clase", 0),
                                obj.optString("Nombre_Clase", "Sin nombre"),
                                obj.optString("Descripcion", ""),
                                obj.optString("Fecha_Clase", ""),
                                obj.optString("Hora_Clase", ""),
                                obj.optInt("Plazas_Maximas", 0),
                                obj.optInt("Duracion", 0),
                                obj.optString("Sala", ""),
                                obj.optString("Monitor", ""),
                                obj.optString("Imagen_Clase", "")
                        );

                        listaClases.add(clase);
                    }

                    runOnUiThread(() -> adapter.notifyDataSetChanged());

                } catch (Exception e) {
                    Log.e("ADMIN_CLASES", "Error leyendo clases", e);
                }
            }
        });
    }
}