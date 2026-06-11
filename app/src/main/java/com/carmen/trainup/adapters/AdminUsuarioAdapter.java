package com.carmen.trainup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.carmen.trainup.R;
import com.carmen.trainup.models.AdminUsuario;

import java.util.ArrayList;

public class AdminUsuarioAdapter extends RecyclerView.Adapter<AdminUsuarioAdapter.UsuarioViewHolder> {

    private ArrayList<AdminUsuario> listaUsuarios;

    public AdminUsuarioAdapter(ArrayList<AdminUsuario> listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        AdminUsuario usuario = listaUsuarios.get(position);

        holder.txtNombreUsuario.setText(usuario.getNombreCompleto());
        holder.txtEmailUsuario.setText(usuario.getEmail());

        if (usuario.getTelefono() == null || usuario.getTelefono().isEmpty() || usuario.getTelefono().equals("null")) {
            holder.txtTelefonoUsuario.setText("Sin teléfono");
        } else {
            holder.txtTelefonoUsuario.setText(usuario.getTelefono());
        }

    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {

        TextView txtNombreUsuario, txtEmailUsuario, txtTelefonoUsuario;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNombreUsuario = itemView.findViewById(R.id.txtNombreUsuario);
            txtEmailUsuario = itemView.findViewById(R.id.txtEmailUsuario);
            txtTelefonoUsuario = itemView.findViewById(R.id.txtTelefonoUsuario);
        }
    }
}