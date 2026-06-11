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
import com.carmen.trainup.adapters.AdminReservaClaseAdapter;
import com.carmen.trainup.adapters.AdminUsuarioReservaAdapter;
import com.carmen.trainup.models.AdminReservaClase;
import com.carmen.trainup.models.AdminUsuarioReserva;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AdminReservasActivity extends AppCompatActivity {

    private TextView txtClaseSeleccionada, txtSinUsuarios;
    private RecyclerView rvReservas, rvUsuariosReservas;

    private final OkHttpClient client = new OkHttpClient();

    private ArrayList<AdminReservaClase> listaClases;
    private ArrayList<AdminUsuarioReserva> listaUsuarios;

    private AdminReservaClaseAdapter claseAdapter;
    private AdminUsuarioReservaAdapter usuarioAdapter;

    private String emailUsuario;
    private int idGimnasio = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_reservas);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtClaseSeleccionada = findViewById(R.id.txtClaseSeleccionada);
        txtSinUsuarios = findViewById(R.id.txtSinUsuarios);
        rvReservas = findViewById(R.id.rvReservas);
        rvUsuariosReservas = findViewById(R.id.rvUsuariosReservas);

        listaClases = new ArrayList<>();
        listaUsuarios = new ArrayList<>();

        claseAdapter = new AdminReservaClaseAdapter(listaClases, clase -> {
            txtClaseSeleccionada.setText("Usuarios apuntados a " + clase.getNombreClase());
            cargarUsuariosReserva(clase.getIdClase());
        });

        usuarioAdapter = new AdminUsuarioReservaAdapter(listaUsuarios);

        rvReservas.setLayoutManager(new LinearLayoutManager(this));
        rvReservas.setAdapter(claseAdapter);

        rvUsuariosReservas.setLayoutManager(new LinearLayoutManager(this));
        rvUsuariosReservas.setAdapter(usuarioAdapter);

        SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);

        emailUsuario = prefs.getString("email", "");
        idGimnasio = prefs.getInt("id_gimnasio", -1);

        Log.d("AdminReservas", "Email guardado: " + emailUsuario);
        Log.d("AdminReservas", "ID gimnasio guardado: " + idGimnasio);

        if (emailUsuario.isEmpty()) {
            Toast.makeText(this, "No se encontró el email del usuario", Toast.LENGTH_SHORT).show();
            txtSinUsuarios.setText("No se encontró el email del usuario");
            return;
        }

        if (idGimnasio == -1) {
            Toast.makeText(this, "No se encontró el gimnasio del administrador", Toast.LENGTH_SHORT).show();
            txtSinUsuarios.setText("No se encontró el gimnasio del administrador");
            return;
        }

        txtClaseSeleccionada.setText("Selecciona una clase");
        txtSinUsuarios.setVisibility(View.VISIBLE);
        txtSinUsuarios.setText("Cargando clases...");

        cargarClasesConReservas();
    }

    private Request.Builder getRequestBuilder(String url) {
        return new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json");
    }

    private void cargarClasesConReservas() {
        new Thread(() -> {
            try {
                String url = SupabaseConfig.SUPABASE_URL
                        + "/rest/v1/Clase?select=ID_Clase,Nombre_Clase,Fecha_Clase,Hora_Clase,Monitor,Sala,Plazas_Maximas,ID_Gimnasio"
                        + "&ID_Gimnasio=eq." + idGimnasio
                        + "&order=Fecha_Clase.asc";

                Request request = getRequestBuilder(url).get().build();
                Response response = client.newCall(request).execute();

                String body = response.body() != null ? response.body().string() : "";

                Log.d("AdminReservas", "ID gimnasio usado: " + idGimnasio);
                Log.d("AdminReservas", "Clases response: " + response.code() + " - " + body);

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        txtSinUsuarios.setVisibility(View.VISIBLE);
                        txtSinUsuarios.setText("Error cargando clases. Código: " + response.code());
                        Toast.makeText(this, "Error cargando clases", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                JSONArray clasesArray = new JSONArray(body);
                ArrayList<AdminReservaClase> nuevasClases = new ArrayList<>();

                int totalReservas = 0;

                for (int i = 0; i < clasesArray.length(); i++) {
                    JSONObject obj = clasesArray.getJSONObject(i);

                    int idClase = obj.optInt("ID_Clase", -1);
                    String nombre = obj.optString("Nombre_Clase", "");
                    String fecha = obj.optString("Fecha_Clase", "");
                    String hora = obj.optString("Hora_Clase", "");
                    String monitor = obj.optString("Monitor", "");
                    String sala = obj.optString("Sala", "");
                    int plazasMaximas = obj.optInt("Plazas_Maximas", 0);

                    int reservas = contarReservasClase(idClase);
                    totalReservas += reservas;

                    nuevasClases.add(new AdminReservaClase(
                            idClase,
                            nombre,
                            fecha,
                            hora,
                            monitor,
                            sala,
                            plazasMaximas,
                            reservas
                    ));
                }

                int totalFinal = totalReservas;

                runOnUiThread(() -> {
                    listaClases.clear();
                    listaClases.addAll(nuevasClases);
                    claseAdapter.notifyDataSetChanged();

                    listaUsuarios.clear();
                    usuarioAdapter.notifyDataSetChanged();

                    txtClaseSeleccionada.setText("Selecciona una clase");

                    if (listaClases.isEmpty()) {
                        txtSinUsuarios.setVisibility(View.VISIBLE);
                        txtSinUsuarios.setText("No hay clases para el gimnasio con ID: " + idGimnasio);
                    } else {
                        txtSinUsuarios.setVisibility(View.VISIBLE);
                        txtSinUsuarios.setText("Pulsa una clase para ver los usuarios reservados");
                    }
                });

            } catch (Exception e) {
                Log.e("AdminReservas", "Error cargando clases", e);
                runOnUiThread(() -> {
                    txtSinUsuarios.setVisibility(View.VISIBLE);
                    txtSinUsuarios.setText("Error cargando clases");
                    Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private int contarReservasClase(int idClase) {
        try {
            String url = SupabaseConfig.SUPABASE_URL
                    + "/rest/v1/Reserva?select=ID_Reserva"
                    + "&ID_Clase=eq." + idClase
                    + "&Estado=eq.Activa";

            Request request = getRequestBuilder(url).get().build();
            Response response = client.newCall(request).execute();

            String body = response.body() != null ? response.body().string() : "";

            Log.d("AdminReservas", "Reservas clase " + idClase + ": " + response.code() + " - " + body);

            if (!response.isSuccessful()) {
                return 0;
            }

            JSONArray array = new JSONArray(body);
            return array.length();

        } catch (Exception e) {
            Log.e("AdminReservas", "Error contando reservas", e);
            return 0;
        }
    }

    private void cargarUsuariosReserva(int idClase) {
        new Thread(() -> {
            try {
                String url = SupabaseConfig.SUPABASE_URL
                        + "/rest/v1/Reserva?select=ID_Reserva,created_at,Estado,Usuario(ID_Usuario,Nombre_Usuario,Apellidos_Usuario,Email_Usuario)"
                        + "&ID_Clase=eq." + idClase
                        + "&Estado=eq.Activa"
                        + "&order=created_at.desc";

                Request request = getRequestBuilder(url).get().build();
                Response response = client.newCall(request).execute();

                String body = response.body() != null ? response.body().string() : "";

                Log.d("AdminReservas", "Usuarios reservas response: " + response.code() + " - " + body);

                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error cargando usuarios", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                JSONArray array = new JSONArray(body);
                ArrayList<AdminUsuarioReserva> nuevosUsuarios = new ArrayList<>();

                for (int i = 0; i < array.length(); i++) {
                    JSONObject reserva = array.getJSONObject(i);

                    int idReserva = reserva.optInt("ID_Reserva", -1);
                    String fechaReserva = reserva.optString("created_at", "");
                    String estado = reserva.optString("Estado", "");

                    JSONObject usuario = reserva.optJSONObject("Usuario");

                    if (usuario != null) {
                        int idUsuario = usuario.optInt("ID_Usuario", -1);
                        String nombre = usuario.optString("Nombre_Usuario", "");
                        String apellidos = usuario.optString("Apellidos_Usuario", "");
                        String email = usuario.optString("Email_Usuario", "");

                        nuevosUsuarios.add(new AdminUsuarioReserva(
                                idReserva,
                                idUsuario,
                                nombre,
                                apellidos,
                                email,
                                estado,
                                fechaReserva
                        ));
                    }
                }

                runOnUiThread(() -> {
                    listaUsuarios.clear();
                    listaUsuarios.addAll(nuevosUsuarios);
                    usuarioAdapter.notifyDataSetChanged();

                    if (listaUsuarios.isEmpty()) {
                        txtSinUsuarios.setVisibility(View.VISIBLE);
                        txtSinUsuarios.setText("No hay usuarios reservados en esta clase");
                    } else {
                        txtSinUsuarios.setVisibility(View.GONE);
                    }
                });

            } catch (Exception e) {
                Log.e("AdminReservas", "Error cargando usuarios", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}