package com.carmen.trainup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.carmen.trainup.R;
import com.carmen.trainup.models.EjercicioRutina;

import java.util.ArrayList;

public class EjercicioRutinaAdapter extends RecyclerView.Adapter<EjercicioRutinaAdapter.EjercicioViewHolder> {

    private ArrayList<EjercicioRutina> listaEjercicios;

    public EjercicioRutinaAdapter(ArrayList<EjercicioRutina> listaEjercicios) {
        this.listaEjercicios = listaEjercicios;
    }

    @NonNull
    @Override
    public EjercicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ejercicio_rutina, parent, false);
        return new EjercicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EjercicioViewHolder holder, int position) {
        EjercicioRutina ejercicio = listaEjercicios.get(position);

        holder.txtNombreEjercicio.setText(ejercicio.getNombreEjercicio());
        holder.txtGrupoMuscular.setText(ejercicio.getGrupoMuscular());
        holder.txtDescripcionEjercicio.setText(ejercicio.getDescripcion());

        String detalle = ejercicio.getSeries() + " series x "
                + ejercicio.getRepeticiones() + " repeticiones";

        if (ejercicio.getPeso() > 0) {
            detalle += " · " + ejercicio.getPeso() + " kg";
        }

        if (ejercicio.getDescanso() > 0) {
            detalle += " · Descanso: " + ejercicio.getDescanso() + "s";
        }

        holder.txtDetalleEjercicio.setText(detalle);

        Glide.with(holder.itemView.getContext())
                .load(ejercicio.getImagenEjercicio())
                .placeholder(R.drawable.logo_trainup)
                .error(R.drawable.logo_trainup)
                .centerCrop()
                .into(holder.imgEjercicio);
    }

    @Override
    public int getItemCount() {
        return listaEjercicios.size();
    }

    public static class EjercicioViewHolder extends RecyclerView.ViewHolder {

        ImageView imgEjercicio;
        TextView txtNombreEjercicio, txtGrupoMuscular, txtDescripcionEjercicio, txtDetalleEjercicio;

        public EjercicioViewHolder(@NonNull View itemView) {
            super(itemView);

            imgEjercicio = itemView.findViewById(R.id.imgEjercicio);
            txtNombreEjercicio = itemView.findViewById(R.id.txtNombreEjercicio);
            txtGrupoMuscular = itemView.findViewById(R.id.txtGrupoMuscular);
            txtDescripcionEjercicio = itemView.findViewById(R.id.txtDescripcionEjercicio);
            txtDetalleEjercicio = itemView.findViewById(R.id.txtDetalleEjercicio);
        }
    }
}