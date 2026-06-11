package com.carmen.trainup.admin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.carmen.trainup.adapters.AdminUsuarioAdapter;
import com.carmen.trainup.models.AdminUsuario;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class AdminUsuariosActivity extends AppCompatActivity {

    private RecyclerView rvUsuarios;
    private TextView txtSinUsuarios;

    private ArrayList<AdminUsuario> listaUsuarios;
    private AdminUsuarioAdapter adapter;

    private final OkHttpClient client = new OkHttpClient();

    private String accessToken;
    private String emailUsuario;
    private int idGimnasio = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_usuarios);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvUsuarios = findViewById(R.id.rvUsuarios);
        txtSinUsuarios = findViewById(R.id.txtSinUsuarios);

        SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
        emailUsuario = prefs.getString("email", "");

        if (accessToken.isEmpty() || emailUsuario.isEmpty()) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        listaUsuarios = new ArrayList<>();
        adapter = new AdminUsuarioAdapter(listaUsuarios);

        rvUsuarios.setLayoutManager(new LinearLayoutManager(this));
        rvUsuarios.setAdapter(adapter);

        cargarIdGimnasioDelAdmin();
    }

    private void cargarIdGimnasioDelAdmin() {
        try {
            String emailEncoded = URLEncoder.encode(emailUsuario, "UTF-8");

            String url = SupabaseConfig.SUPABASE_URL
                    + "/rest/v1/Usuario?Email_Usuario=eq."
                    + emailEncoded
                    + "&select=ID_Gimnasio";

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
                            Toast.makeText(AdminUsuariosActivity.this, "Error cargando gimnasio", Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                    String respuesta = response.body() != null ? response.body().string() : "";

                    Log.d("ADMIN_USUARIOS_GYM", "Código: " + response.code());
                    Log.d("ADMIN_USUARIOS_GYM", "Respuesta: " + respuesta);

                    if (response.isSuccessful()) {
                        try {
                            JSONArray array = new JSONArray(respuesta);

                            if (array.length() > 0 && !array.getJSONObject(0).isNull("ID_Gimnasio")) {
                                idGimnasio = array.getJSONObject(0).getInt("ID_Gimnasio");
                                cargarUsuariosDelGimnasio();
                            } else {
                                runOnUiThread(() -> {
                                    txtSinUsuarios.setVisibility(View.VISIBLE);
                                    Toast.makeText(AdminUsuariosActivity.this, "No tienes gimnasio asignado", Toast.LENGTH_LONG).show();
                                });
                            }

                        } catch (Exception e) {
                            runOnUiThread(() ->
                                    Toast.makeText(AdminUsuariosActivity.this, "Error leyendo gimnasio", Toast.LENGTH_LONG).show()
                            );
                        }
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(AdminUsuariosActivity.this, "Error Supabase: " + respuesta, Toast.LENGTH_LONG).show()
                        );
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void cargarUsuariosDelGimnasio() {

        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/Usuario?ID_Gimnasio=eq."
                + idGimnasio
                + "&select=ID_Usuario,Nombre_Usuario,Apellidos_Usuario,Email_Usuario,Telefono,Rol"
                + "&order=Nombre_Usuario.asc";

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
                        Toast.makeText(AdminUsuariosActivity.this,
                                "Error cargando usuarios",
                                Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(okhttp3.Call call,
                                   okhttp3.Response response) throws java.io.IOException {

                String respuesta = response.body() != null
                        ? response.body().string()
                        : "";

                Log.d("ADMIN_USUARIOS", "Código: " + response.code());
                Log.d("ADMIN_USUARIOS", "Respuesta: " + respuesta);

                if (response.isSuccessful()) {

                    try {

                        JSONArray array = new JSONArray(respuesta);

                        listaUsuarios.clear();

                        for (int i = 0; i < array.length(); i++) {

                            JSONObject obj = array.getJSONObject(i);

                            String rol = obj.optString("Rol", "cliente");

                            // NO mostrar administradores
                            if (rol.equalsIgnoreCase("administrador")
                                    || rol.equalsIgnoreCase("admin")) {
                                continue;
                            }

                            int idUsuario = obj.getInt("ID_Usuario");

                            String nombre =
                                    obj.optString("Nombre_Usuario", "");

                            String apellidos =
                                    obj.optString("Apellidos_Usuario", "");

                            String email =
                                    obj.optString("Email_Usuario", "");

                            String telefono =
                                    obj.optString("Telefono", "");

                            listaUsuarios.add(
                                    new AdminUsuario(
                                            idUsuario,
                                            nombre,
                                            apellidos,
                                            email,
                                            telefono,
                                            rol
                                    )
                            );
                        }

                        runOnUiThread(() -> {

                            adapter.notifyDataSetChanged();

                            if (listaUsuarios.isEmpty()) {
                                txtSinUsuarios.setVisibility(View.VISIBLE);
                            } else {
                                txtSinUsuarios.setVisibility(View.GONE);
                            }
                        });

                    } catch (Exception e) {

                        Log.e("ADMIN_USUARIOS",
                                "Error parseando usuarios", e);

                        runOnUiThread(() ->
                                Toast.makeText(
                                        AdminUsuariosActivity.this,
                                        "Error leyendo usuarios",
                                        Toast.LENGTH_LONG
                                ).show()
                        );
                    }

                } else {

                    runOnUiThread(() ->
                            Toast.makeText(
                                    AdminUsuariosActivity.this,
                                    "Error Supabase: " + respuesta,
                                    Toast.LENGTH_LONG
                            ).show()
                    );
                }
            }
        });
    }
}