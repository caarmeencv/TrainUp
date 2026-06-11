package com.carmen.trainup.cliente;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.carmen.trainup.R;
import com.carmen.trainup.utils.SupabaseConfig;
import com.carmen.trainup.adapters.ReservaAdapter;
import com.carmen.trainup.models.Reserva;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ReservasActivity extends AppCompatActivity {

    private RecyclerView rvReservas;
    private TextView txtSinReservas;

    private ArrayList<Reserva> listaReservas;
    private ReservaAdapter reservaAdapter;

    private final OkHttpClient client = new OkHttpClient();

    private String accessToken;
    private String emailUsuario;
    private long idUsuario = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservas);

        SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
        emailUsuario = prefs.getString("email", "");

        rvReservas = findViewById(R.id.rvReservas);
        txtSinReservas = findViewById(R.id.txtSinReservas);

        listaReservas = new ArrayList<>();

        reservaAdapter = new ReservaAdapter(listaReservas, reserva -> {
            cancelarReserva(reserva);
        });

        rvReservas.setLayoutManager(new LinearLayoutManager(this));
        rvReservas.setAdapter(reservaAdapter);

        cargarUsuario();
    }

    private void cargarUsuario() {
        try {
            String emailCodificado = URLEncoder.encode(emailUsuario, "UTF-8");

            String url = SupabaseConfig.SUPABASE_URL
                    + "/rest/v1/Usuario?select=ID_Usuario&Email_Usuario=eq."
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
                            Toast.makeText(ReservasActivity.this, "Error cargando usuario", Toast.LENGTH_LONG).show()
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

                            cargarReservas();

                        } else {
                            runOnUiThread(() -> {
                                txtSinReservas.setVisibility(View.VISIBLE);
                                Toast.makeText(ReservasActivity.this, "No se encontró el usuario", Toast.LENGTH_LONG).show();
                            });
                        }

                    } catch (Exception e) {
                        Log.e("RESERVAS", "Error leyendo usuario", e);
                    }
                }
            });

        } catch (Exception e) {
            Log.e("RESERVAS", "Error codificando email", e);
        }
    }

    private void cargarReservas() {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/Reserva?select=ID_Reserva,Estado,ID_Clase,Clase(*)&ID_Usuario=eq."
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
                runOnUiThread(() ->
                        Toast.makeText(ReservasActivity.this, "Error cargando reservas", Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                String respuesta = response.body() != null ? response.body().string() : "";

                Log.d("RESERVAS", "Respuesta: " + respuesta);

                try {
                    JSONArray array = new JSONArray(respuesta);

                    listaReservas.clear();

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);

                        JSONObject claseObj = obj.optJSONObject("Clase");

                        if (claseObj != null) {
                            Reserva reserva = new Reserva(
                                    obj.optLong("ID_Reserva", 0),
                                    obj.optString("Estado", "Activa"),
                                    obj.optLong("ID_Clase", 0),
                                    claseObj.optString("Nombre_Clase", "Sin nombre"),
                                    claseObj.optString("Fecha_Clase", ""),
                                    claseObj.optString("Hora_Clase", ""),
                                    claseObj.optString("Sala", ""),
                                    claseObj.optString("Monitor", ""),
                                    claseObj.optString("Imagen_Clase", "")
                            );

                            listaReservas.add(reserva);
                        }
                    }

                    runOnUiThread(() -> {
                        reservaAdapter.notifyDataSetChanged();
                        txtSinReservas.setVisibility(listaReservas.isEmpty() ? View.VISIBLE : View.GONE);
                    });

                } catch (Exception e) {
                    Log.e("RESERVAS", "Error leyendo reservas", e);
                }
            }
        });
    }

    private void cancelarReserva(Reserva reserva) {

        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/Reserva?ID_Reserva=eq."
                + reserva.getIdReserva();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .delete()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {

            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(ReservasActivity.this, "Error cancelando reserva", Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {

                String respuesta = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        listaReservas.remove(reserva);
                        reservaAdapter.notifyDataSetChanged();

                        txtSinReservas.setVisibility(listaReservas.isEmpty() ? View.VISIBLE : View.GONE);

                        Toast.makeText(ReservasActivity.this, "Reserva cancelada", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Log.e("RESERVAS", "Error cancelar: " + respuesta);

                    runOnUiThread(() ->
                            Toast.makeText(ReservasActivity.this, "No se pudo cancelar", Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }
}