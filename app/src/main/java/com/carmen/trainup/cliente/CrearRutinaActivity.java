package com.carmen.trainup.cliente;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.carmen.trainup.models.Ejercicio;
import com.carmen.trainup.models.EjercicioRutina;
import com.carmen.trainup.adapters.EjercicioRutinaAdapter;
import com.carmen.trainup.R;
import com.carmen.trainup.utils.SupabaseConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class CrearRutinaActivity extends AppCompatActivity {

    private EditText etNombreRutina, etDescripcionRutina, etDiasSemana, etObjetivoRutina;
    private EditText etSeries, etRepeticiones, etPeso, etDescanso;
    private Spinner spinnerEjercicios;
    private Button btnAnadirEjercicio, btnGuardarRutina;
    private RecyclerView rvEjerciciosSeleccionados;

    private ArrayList<Ejercicio> listaEjercicios;
    private ArrayList<EjercicioRutina> ejerciciosSeleccionados;

    private EjercicioRutinaAdapter ejercicioAdapter;

    private final OkHttpClient client = new OkHttpClient();

    private String accessToken;
    private String emailUsuario;
    private int idUsuario = -1;

    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crear_rutina);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("TrainUpPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
        emailUsuario = prefs.getString("email", "");

        etNombreRutina = findViewById(R.id.etNombreRutina);
        etDescripcionRutina = findViewById(R.id.etDescripcionRutina);
        etDiasSemana = findViewById(R.id.etDiasSemana);
        etObjetivoRutina = findViewById(R.id.etObjetivoRutina);

        spinnerEjercicios = findViewById(R.id.spinnerEjercicios);
        etSeries = findViewById(R.id.etSeries);
        etRepeticiones = findViewById(R.id.etRepeticiones);
        etPeso = findViewById(R.id.etPeso);
        etDescanso = findViewById(R.id.etDescanso);

        btnAnadirEjercicio = findViewById(R.id.btnAnadirEjercicio);
        btnGuardarRutina = findViewById(R.id.btnGuardarRutina);
        rvEjerciciosSeleccionados = findViewById(R.id.rvEjerciciosSeleccionados);

        listaEjercicios = new ArrayList<>();
        ejerciciosSeleccionados = new ArrayList<>();

        ejercicioAdapter = new EjercicioRutinaAdapter(ejerciciosSeleccionados);
        rvEjerciciosSeleccionados.setLayoutManager(new LinearLayoutManager(this));
        rvEjerciciosSeleccionados.setAdapter(ejercicioAdapter);

        btnAnadirEjercicio.setOnClickListener(v -> anadirEjercicio());
        btnGuardarRutina.setOnClickListener(v -> guardarRutina());

        obtenerIdUsuario();
        cargarEjercicios();
    }

    private void obtenerIdUsuario() {
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
                    runOnUiThread(() ->
                            Toast.makeText(CrearRutinaActivity.this, "Error obteniendo usuario", Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                    String respuesta = response.body() != null ? response.body().string() : "";

                    if (response.isSuccessful()) {
                        try {
                            JSONArray array = new JSONArray(respuesta);

                            if (array.length() > 0) {
                                idUsuario = array.getJSONObject(0).getInt("ID_Usuario");
                            }

                        } catch (Exception e) {
                            Log.e("CREAR_RUTINA", "Error leyendo usuario", e);
                        }
                    }
                }
            });

        } catch (Exception e) {
            Log.e("CREAR_RUTINA", "Error usuario: " + e.getMessage());
        }
    }

    private void cargarEjercicios() {
        String url = SupabaseConfig.SUPABASE_URL
                + "/rest/v1/Ejercicio?select=ID_Ejercicio,Nombre_Ejercicio&order=Nombre_Ejercicio.asc";

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
                        Toast.makeText(CrearRutinaActivity.this, "Error cargando ejercicios", Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                String respuesta = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(respuesta);
                        listaEjercicios.clear();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);

                            long id = obj.getLong("ID_Ejercicio");
                            String nombre = obj.optString("Nombre_Ejercicio", "Sin nombre");

                            listaEjercicios.add(new Ejercicio(id, nombre));
                        }

                        runOnUiThread(() -> {
                            ArrayAdapter<Ejercicio> adapter = new ArrayAdapter<>(
                                    CrearRutinaActivity.this,
                                    android.R.layout.simple_spinner_item,
                                    listaEjercicios
                            );

                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerEjercicios.setAdapter(adapter);
                        });

                    } catch (Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(CrearRutinaActivity.this, "Error leyendo ejercicios", Toast.LENGTH_LONG).show()
                        );
                    }

                } else {
                    runOnUiThread(() ->
                            Toast.makeText(CrearRutinaActivity.this, "Error Supabase ejercicios", Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }

    private void anadirEjercicio() {
        if (spinnerEjercicios.getSelectedItem() == null) {
            Toast.makeText(this, "Selecciona un ejercicio", Toast.LENGTH_SHORT).show();
            return;
        }

        String seriesTxt = etSeries.getText().toString().trim();
        String repsTxt = etRepeticiones.getText().toString().trim();

        if (seriesTxt.isEmpty() || repsTxt.isEmpty()) {
            Toast.makeText(this, "Introduce series y repeticiones", Toast.LENGTH_SHORT).show();
            return;
        }

        Ejercicio ejercicio = (Ejercicio) spinnerEjercicios.getSelectedItem();

        int series = Integer.parseInt(seriesTxt);
        int repeticiones = Integer.parseInt(repsTxt);

        double peso = 0;
        int descanso = 0;

        if (!etPeso.getText().toString().trim().isEmpty()) {
            peso = Double.parseDouble(etPeso.getText().toString().trim());
        }

        if (!etDescanso.getText().toString().trim().isEmpty()) {
            descanso = Integer.parseInt(etDescanso.getText().toString().trim());
        }

        int orden = ejerciciosSeleccionados.size() + 1;

        EjercicioRutina ejercicioRutina = new EjercicioRutina(
                ejercicio.getIdEjercicio(),
                ejercicio.getNombreEjercicio(),
                "",
                "",
                "",
                series,
                repeticiones,
                peso,
                descanso,
                orden
        );

        ejerciciosSeleccionados.add(ejercicioRutina);
        ejercicioAdapter.notifyDataSetChanged();

        etSeries.setText("");
        etRepeticiones.setText("");
        etPeso.setText("");
        etDescanso.setText("");

        Toast.makeText(this, "Ejercicio añadido", Toast.LENGTH_SHORT).show();
    }

    private void guardarRutina() {
        String nombre = etNombreRutina.getText().toString().trim();
        String descripcion = etDescripcionRutina.getText().toString().trim();
        String diasTxt = etDiasSemana.getText().toString().trim();
        String objetivo = etObjetivoRutina.getText().toString().trim();

        if (nombre.isEmpty() || diasTxt.isEmpty() || objetivo.isEmpty()) {
            Toast.makeText(this, "Rellena nombre, días y objetivo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ejerciciosSeleccionados.isEmpty()) {
            Toast.makeText(this, "Añade al menos un ejercicio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (idUsuario == -1) {
            Toast.makeText(this, "No se encontró el usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int dias = Integer.parseInt(diasTxt);

            JSONObject json = new JSONObject();
            json.put("Nombre_Rutina", nombre);
            json.put("Descripcion", descripcion);
            json.put("Dias_Semana", dias);
            json.put("Objetivo", objetivo);
            json.put("ID_Usuario", idUsuario);

            RequestBody body = RequestBody.create(json.toString(), JSON);

            String url = SupabaseConfig.SUPABASE_URL + "/rest/v1/Rutina";

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
                    runOnUiThread(() ->
                            Toast.makeText(CrearRutinaActivity.this, "Error guardando rutina", Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                    String respuesta = response.body() != null ? response.body().string() : "";

                    if (response.isSuccessful()) {
                        try {
                            JSONArray array = new JSONArray(respuesta);
                            long idRutinaNueva = array.getJSONObject(0).getLong("ID_Rutina");

                            guardarEjerciciosRutina(idRutinaNueva);

                        } catch (Exception e) {
                            runOnUiThread(() ->
                                    Toast.makeText(CrearRutinaActivity.this, "Error leyendo rutina creada", Toast.LENGTH_LONG).show()
                            );
                        }

                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(CrearRutinaActivity.this, "Error Supabase: " + respuesta, Toast.LENGTH_LONG).show()
                        );
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void guardarEjerciciosRutina(long idRutinaNueva) {
        try {
            JSONArray array = new JSONArray();

            for (EjercicioRutina ejercicio : ejerciciosSeleccionados) {
                JSONObject obj = new JSONObject();

                obj.put("ID_Rutina", idRutinaNueva);
                obj.put("ID_Ejercicio", ejercicio.getIdEjercicio());
                obj.put("Series", ejercicio.getSeries());
                obj.put("Repeticiones", ejercicio.getRepeticiones());

                if (ejercicio.getPeso() > 0) {
                    obj.put("Peso", ejercicio.getPeso());
                }

                if (ejercicio.getDescanso() > 0) {
                    obj.put("Descanso", ejercicio.getDescanso());
                }

                obj.put("Orden_Ejercicio", ejercicio.getOrdenEjercicio());

                array.put(obj);
            }

            RequestBody body = RequestBody.create(array.toString(), JSON);

            String url = SupabaseConfig.SUPABASE_URL + "/rest/v1/Rutina_Ejercicio";

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_API_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=minimal")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, java.io.IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(CrearRutinaActivity.this, "Rutina creada, pero error guardando ejercicios", Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                    String respuesta = response.body() != null ? response.body().string() : "";

                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            Toast.makeText(CrearRutinaActivity.this, "Rutina creada correctamente", Toast.LENGTH_LONG).show();
                            finish();
                        });
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(CrearRutinaActivity.this, "Error ejercicios: " + respuesta, Toast.LENGTH_LONG).show()
                        );
                    }
                }
            });

        } catch (Exception e) {
            runOnUiThread(() ->
                    Toast.makeText(CrearRutinaActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
        }
    }
}