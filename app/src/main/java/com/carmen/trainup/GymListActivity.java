package com.carmen.trainup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class GymListActivity extends AppCompatActivity {

    private RecyclerView rvGimnasios;
    private ArrayList<Gym> listaGyms;
    private GymAdapter gymAdapter;

    private final OkHttpClient client = new OkHttpClient();

    private String accessToken;
    private String emailUsuario;

    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gym_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
        emailUsuario = prefs.getString("email", "");

        rvGimnasios = findViewById(R.id.rvGimnasios);
        rvGimnasios.setLayoutManager(new LinearLayoutManager(this));

        listaGyms = new ArrayList<>();

        gymAdapter = new GymAdapter(listaGyms, gym -> {
            irAInfoActivity(gym);
            guardarGimnasioEnUsuario(gym);
        });

        rvGimnasios.setAdapter(gymAdapter);

        cargarGimnasios();
    }

    private void cargarGimnasios() {

        Request request = new Request.Builder()
                .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/Gimnasio?select=*")
                .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {

            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(GymListActivity.this, "Error cargando gimnasios", Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {

                String respuesta = response.body() != null ? response.body().string() : "";

                Log.d("SUPABASE_GYMS", "Código: " + response.code());
                Log.d("SUPABASE_GYMS", "Respuesta: " + respuesta);

                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(respuesta);

                        listaGyms.clear();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);

                            int id = obj.getInt("ID_Gimnasio");
                            String nombre = obj.optString("Nombre_Gimnasio", "Sin nombre");
                            String ciudad = obj.optString("Ciudad", "Sin ciudad");
                            String descripcion = obj.optString("Descripcion", "Sin descripción");
                            String imagen = obj.optString("Imagen_Gimnasio", "");

                            String direccion = obj.optString("Direccion", "Sin dirección");
                            String email = obj.optString("Email", "Sin email");
                            String telefono = obj.optString("Telefono", "Sin teléfono");

                            listaGyms.add(new Gym(
                                    id,
                                    nombre,
                                    ciudad,
                                    descripcion,
                                    imagen,
                                    direccion,
                                    email,
                                    telefono
                            ));
                        }

                        runOnUiThread(() -> gymAdapter.notifyDataSetChanged());

                    } catch (Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(GymListActivity.this, "Error leyendo gimnasios", Toast.LENGTH_LONG).show()
                        );
                    }

                } else {
                    runOnUiThread(() ->
                            Toast.makeText(GymListActivity.this, "Error Supabase: " + respuesta, Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }

    private void irAInfoActivity(Gym gym) {
        Intent intent = new Intent(GymListActivity.this, InfoActivity.class);

        intent.putExtra("id_gimnasio", gym.getIdGym());
        intent.putExtra("nombre_gimnasio", gym.getNombre());
        intent.putExtra("ciudad_gimnasio", gym.getCiudad());
        intent.putExtra("descripcion_gimnasio", gym.getDescripcion());
        intent.putExtra("imagen_gimnasio", gym.getImagen());

        intent.putExtra("direccion_gimnasio", gym.getDireccion());
        intent.putExtra("email_gimnasio", gym.getEmail());
        intent.putExtra("telefono_gimnasio", gym.getTelefono());

        startActivity(intent);
    }

    private void guardarGimnasioEnUsuario(Gym gym) {

        if (emailUsuario == null || emailUsuario.isEmpty()) {
            Log.e("SUPABASE_SELECT_GYM", "Email usuario vacío");
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("ID_Gimnasio", gym.getIdGym());

            RequestBody body = RequestBody.create(json.toString(), JSON);

            String emailEncoded = URLEncoder.encode(emailUsuario, "UTF-8");

            String url = SupabaseConfig.SUPABASE_URL
                    + "/rest/v1/Usuario?Email_Usuario=eq."
                    + emailEncoded;

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=minimal")
                    .patch(body)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {

                @Override
                public void onFailure(okhttp3.Call call, java.io.IOException e) {
                    Log.e("SUPABASE_SELECT_GYM", "Error seleccionando gimnasio", e);
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {

                    String respuesta = response.body() != null ? response.body().string() : "";

                    Log.d("SUPABASE_SELECT_GYM", "Código: " + response.code());
                    Log.d("SUPABASE_SELECT_GYM", "Respuesta: " + respuesta);

                    if (!response.isSuccessful()) {
                        Log.e("SUPABASE_SELECT_GYM", "Error guardando gimnasio: " + respuesta);
                    }
                }
            });

        } catch (Exception e) {
            Log.e("SUPABASE_SELECT_GYM", "Error: " + e.getMessage());
        }
    }
}