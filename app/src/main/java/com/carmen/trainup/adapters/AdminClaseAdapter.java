package com.carmen.trainup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.carmen.trainup.R;
import com.carmen.trainup.models.AdminClase;

import java.util.ArrayList;

public class AdminClaseAdapter extends RecyclerView.Adapter<AdminClaseAdapter.AdminClaseViewHolder> {

    public interface OnClaseClickListener {
        void onEditar(AdminClase clase);
        void onEliminar(AdminClase clase);
    }

    private ArrayList<AdminClase> listaClases;
    private OnClaseClickListener listener;

    public AdminClaseAdapter(ArrayList<AdminClase> listaClases, OnClaseClickListener listener) {
        this.listaClases = listaClases;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminClaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_clase, parent, false);
        return new AdminClaseViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminClaseViewHolder holder, int position) {
        AdminClase clase = listaClases.get(position);

        holder.txtNombreClase.setText(clase.getNombre());
        holder.txtFechaHora.setText(clase.getFecha() + " · " + clase.getHora());
        holder.txtDatosClase.setText(clase.getSala() + " · " + clase.getMonitor() + " · " + clase.getPlazas() + " plazas");

        holder.btnEditarClase.setOnClickListener(v -> listener.onEditar(clase));
        holder.btnEliminarClase.setOnClickListener(v -> listener.onEliminar(clase));
    }

    @Override
    public int getItemCount() {
        return listaClases.size();
    }

    public static class AdminClaseViewHolder extends RecyclerView.ViewHolder {

        TextView txtNombreClase, txtFechaHora, txtDatosClase, btnEditarClase, btnEliminarClase;

        public AdminClaseViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNombreClase = itemView.findViewById(R.id.txtNombreClase);
            txtFechaHora = itemView.findViewById(R.id.txtFechaHora);
            txtDatosClase = itemView.findViewById(R.id.txtDatosClase);
            btnEditarClase = itemView.findViewById(R.id.btnEditarClase);
            btnEliminarClase = itemView.findViewById(R.id.btnEliminarClase);
        }
    }
}