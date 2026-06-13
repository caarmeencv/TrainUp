package com.carmen.trainup.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.carmen.trainup.auth.LoginActivity;
import com.carmen.trainup.cliente.ClassListActivity;
import com.carmen.trainup.cliente.InfoActivity2;
import com.carmen.trainup.cliente.ReservasActivity;
import com.carmen.trainup.cliente.RutinaActivity;
import com.carmen.trainup.cliente.SettingsActivity;

public class MenuHelper {

    public static void configurarMenu(Activity activity, TextView btnMenu) {
        btnMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(activity, btnMenu);

            popupMenu.getMenu().add("Rutinas");
            popupMenu.getMenu().add("Clases");
            popupMenu.getMenu().add("Mis reservas");
            popupMenu.getMenu().add("Información de gimnasio");
            popupMenu.getMenu().add("Ajustes");
            popupMenu.getMenu().add("Cerrar sesión");

            popupMenu.setOnMenuItemClickListener(item -> {
                String opcion = item.getTitle().toString();

                if (opcion.equals("Rutinas")) {
                    activity.startActivity(new Intent(activity, RutinaActivity.class));

                } else if (opcion.equals("Clases")) {
                    activity.startActivity(new Intent(activity, ClassListActivity.class));

                } else if (opcion.equals("Mis reservas")) {
                    activity.startActivity(new Intent(activity, ReservasActivity.class));

                } else if (opcion.equals("Información de gimnasio")) {
                    SharedPreferences prefs = activity.getSharedPreferences("TrainUpPrefs", Activity.MODE_PRIVATE);
                    int idGimnasio = prefs.getInt("id_gimnasio", -1);

                    Intent intent = new Intent(activity, InfoActivity2.class);
                    intent.putExtra("id_gimnasio", idGimnasio);
                    activity.startActivity(intent);

                } else if (opcion.equals("Ajustes")) {
                    activity.startActivity(new Intent(activity, SettingsActivity.class));

                } else if (opcion.equals("Cerrar sesión")) {
                    mostrarDialogoCerrarSesion(activity);
                }

                return true;
            });

            popupMenu.show();
        });
    }

    private static void mostrarDialogoCerrarSesion(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que quieres cerrar sesión?")
                .setPositiveButton("Sí, cerrar sesión", (dialog, which) -> {
                    SharedPreferences prefs = activity.getSharedPreferences("TrainUpPrefs", Activity.MODE_PRIVATE);
                    prefs.edit().clear().apply();

                    Intent intent = new Intent(activity, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    activity.startActivity(intent);
                    activity.finish();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }
}