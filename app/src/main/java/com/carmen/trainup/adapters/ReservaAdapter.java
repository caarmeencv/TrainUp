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
import com.carmen.trainup.models.Reserva;

import java.util.ArrayList;

public class ReservaAdapter extends RecyclerView.Adapter<ReservaAdapter.ReservaViewHolder> {

    public interface OnCancelarReservaListener {
        void onCancelarReserva(Reserva reserva);
    }

    private ArrayList<Reserva> listaReservas;
    private OnCancelarReservaListener listener;

    public ReservaAdapter(ArrayList<Reserva> listaReservas, OnCancelarReservaListener listener) {
        this.listaReservas = listaReservas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reserva, parent, false);
        return new ReservaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Reserva reserva = listaReservas.get(position);

        holder.txtNombreClase.setText(reserva.getNombreClase());
        holder.txtMonitorClase.setText("Monitor: " + reserva.getMonitor());
        holder.txtFechaHoraClase.setText(reserva.getFechaClase() + " · " + reserva.getHoraClase());
        holder.txtSalaClase.setText("Sala: " + reserva.getSala());

        holder.btnCancelarReserva.setText("Cancelar reserva");
        holder.btnCancelarReserva.setEnabled(true);

        holder.btnCancelarReserva.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelarReserva(reserva);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaReservas.size();
    }

    public static class ReservaViewHolder extends RecyclerView.ViewHolder {

        ImageView imgClase;
        TextView txtNombreClase, txtMonitorClase, txtFechaHoraClase, txtSalaClase;
        Button btnCancelarReserva;

        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);

            imgClase = itemView.findViewById(R.id.imgClase);
            txtNombreClase = itemView.findViewById(R.id.txtNombreClase);
            txtMonitorClase = itemView.findViewById(R.id.txtMonitorClase);
            txtFechaHoraClase = itemView.findViewById(R.id.txtFechaHoraClase);
            txtSalaClase = itemView.findViewById(R.id.txtSalaClase);
            btnCancelarReserva = itemView.findViewById(R.id.btnCancelarReserva);
        }
    }
}