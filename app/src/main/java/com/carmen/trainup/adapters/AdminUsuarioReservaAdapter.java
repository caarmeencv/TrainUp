package com.carmen.trainup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.carmen.trainup.R;
import com.carmen.trainup.models.AdminUsuarioReserva;

import java.util.ArrayList;

public class AdminUsuarioReservaAdapter extends RecyclerView.Adapter<AdminUsuarioReservaAdapter.ViewHolder> {

    private ArrayList<AdminUsuarioReserva> listaUsuarios;

    public AdminUsuarioReservaAdapter(ArrayList<AdminUsuarioReserva> listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
    }

    @NonNull
    @Override
    public AdminUsuarioReservaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_usuario_reserva, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminUsuarioReservaAdapter.ViewHolder holder, int position) {
        AdminUsuarioReserva usuario = listaUsuarios.get(position);

        holder.txtNombreUsuario.setText(usuario.getNombreCompleto());
        holder.txtEmailUsuario.setText(usuario.getEmail());
        holder.txtEstadoReserva.setText("Estado: " + usuario.getEstado());
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtNombreUsuario, txtEmailUsuario, txtEstadoReserva;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombreUsuario = itemView.findViewById(R.id.txtNombreUsuario);
            txtEmailUsuario = itemView.findViewById(R.id.txtEmailUsuario);
            txtEstadoReserva = itemView.findViewById(R.id.txtEstadoReserva);
        }
    }
}