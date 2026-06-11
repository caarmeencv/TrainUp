package com.carmen.trainup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.carmen.trainup.R;
import com.carmen.trainup.models.Rutina;

import java.util.ArrayList;

public class RutinaAdapter extends RecyclerView.Adapter<RutinaAdapter.RutinaViewHolder> {

    private ArrayList<Rutina> listaRutinas;
    private OnRutinaClickListener listener;

    public interface OnRutinaClickListener {
        void onRutinaClick(Rutina rutina);
    }

    public RutinaAdapter(ArrayList<Rutina> listaRutinas, OnRutinaClickListener listener) {
        this.listaRutinas = listaRutinas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RutinaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rutina, parent, false);
        return new RutinaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RutinaViewHolder holder, int position) {
        Rutina rutina = listaRutinas.get(position);

        holder.txtNombreRutina.setText(rutina.getNombreRutina());
        holder.txtObjetivoRutina.setText(rutina.getObjetivo());
        holder.txtDescripcionRutina.setText(rutina.getDescripcion());
        holder.txtDiasRutina.setText(rutina.getDiasSemana() + " días por semana");

        holder.btnVerRutina.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRutinaClick(rutina);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRutinaClick(rutina);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaRutinas.size();
    }

    public static class RutinaViewHolder extends RecyclerView.ViewHolder {

        TextView txtNombreRutina, txtObjetivoRutina, txtDescripcionRutina, txtDiasRutina;
        Button btnVerRutina;

        public RutinaViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNombreRutina = itemView.findViewById(R.id.txtNombreRutina);
            txtObjetivoRutina = itemView.findViewById(R.id.txtObjetivoRutina);
            txtDescripcionRutina = itemView.findViewById(R.id.txtDescripcionRutina);
            txtDiasRutina = itemView.findViewById(R.id.txtDiasRutina);
            btnVerRutina = itemView.findViewById(R.id.btnVerRutina);
        }
    }
}