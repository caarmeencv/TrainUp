package com.carmen.trainup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.carmen.trainup.R;
import com.carmen.trainup.models.AdminReservaClase;

import java.util.ArrayList;

public class AdminReservaClaseAdapter extends RecyclerView.Adapter<AdminReservaClaseAdapter.ViewHolder> {

    public interface OnClaseClickListener {
        void onClaseClick(AdminReservaClase clase);
    }

    private ArrayList<AdminReservaClase> listaClases;
    private OnClaseClickListener listener;

    public AdminReservaClaseAdapter(ArrayList<AdminReservaClase> listaClases, OnClaseClickListener listener) {
        this.listaClases = listaClases;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminReservaClaseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_reserva_clase, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminReservaClaseAdapter.ViewHolder holder, int position) {
        AdminReservaClase clase = listaClases.get(position);

        holder.txtNombreClase.setText(clase.getNombreClase());
        holder.txtFechaHora.setText(clase.getFechaClase() + " · " + clase.getHoraClase());
        holder.txtInfoClase.setText("Monitor: " + clase.getMonitor() + " · Sala: " + clase.getSala());
        holder.txtReservasClase.setText("Reservas: " + clase.getTotalReservas() + "/" + clase.getPlazasMaximas());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClaseClick(clase);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaClases.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtNombreClase, txtFechaHora, txtInfoClase, txtReservasClase;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombreClase = itemView.findViewById(R.id.txtNombreClase);
            txtFechaHora = itemView.findViewById(R.id.txtFechaHora);
            txtInfoClase = itemView.findViewById(R.id.txtInfoClase);
            txtReservasClase = itemView.findViewById(R.id.txtReservasClase);
        }
    }
}