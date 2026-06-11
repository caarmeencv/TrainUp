package com.carmen.trainup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.carmen.trainup.R;
import com.carmen.trainup.models.Clase;

import java.util.ArrayList;
import java.util.HashSet;

public class ClaseAdapter extends RecyclerView.Adapter<ClaseAdapter.ClaseViewHolder> {

    public interface OnReservarClickListener {
        void onReservarClick(Clase clase, Button boton);
    }

    private ArrayList<Clase> listaClases;
    private HashSet<Long> clasesReservadas;
    private OnReservarClickListener listener;

    public ClaseAdapter(ArrayList<Clase> listaClases,
                        HashSet<Long> clasesReservadas,
                        OnReservarClickListener listener) {
        this.listaClases = listaClases;
        this.clasesReservadas = clasesReservadas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_clase, parent, false);
        return new ClaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClaseViewHolder holder, int position) {
        Clase clase = listaClases.get(position);

        holder.txtNombreClase.setText(clase.getNombre());
        holder.txtMonitorClase.setText("Monitor: " + clase.getMonitor());
        holder.txtFechaHoraClase.setText(clase.getFecha() + " · " + clase.getHora());
        holder.txtSalaClase.setText("Sala: " + clase.getSala());

        if (clasesReservadas.contains(clase.getIdClase())) {
            holder.btnReservarClase.setText("Reservado");
            holder.btnReservarClase.setEnabled(false);
        } else {
            holder.btnReservarClase.setText("Reservar");
            holder.btnReservarClase.setEnabled(true);

            holder.btnReservarClase.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReservarClick(clase, holder.btnReservarClase);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listaClases.size();
    }

    public static class ClaseViewHolder extends RecyclerView.ViewHolder {

        ImageView imgClase;
        TextView txtNombreClase, txtMonitorClase, txtFechaHoraClase, txtSalaClase;
        Button btnReservarClase;

        public ClaseViewHolder(@NonNull View itemView) {
            super(itemView);

            imgClase = itemView.findViewById(R.id.imgClase);
            txtNombreClase = itemView.findViewById(R.id.txtNombreClase);
            txtMonitorClase = itemView.findViewById(R.id.txtMonitorClase);
            txtFechaHoraClase = itemView.findViewById(R.id.txtFechaHoraClase);
            txtSalaClase = itemView.findViewById(R.id.txtSalaClase);
            btnReservarClase = itemView.findViewById(R.id.btnReservarClase);
        }
    }
}