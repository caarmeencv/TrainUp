package com.carmen.trainup.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.carmen.trainup.R;
import com.carmen.trainup.models.AdminUsuario;

import java.util.ArrayList;

public class AdminUsuarioAdapter extends RecyclerView.Adapter<AdminUsuarioAdapter.UsuarioViewHolder> {

    public interface OnDesapuntarClickListener {
        void onDesapuntar(AdminUsuario usuario);
    }

    private ArrayList<AdminUsuario> listaUsuarios;
    private OnDesapuntarClickListener listener;

    public AdminUsuarioAdapter(
            ArrayList<AdminUsuario> listaUsuarios,
            OnDesapuntarClickListener listener
    ) {
        this.listaUsuarios = listaUsuarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_usuario, parent, false);

        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull UsuarioViewHolder holder,
            int position
    ) {

        AdminUsuario usuario = listaUsuarios.get(position);

        holder.txtNombreUsuario.setText(
                usuario.getNombreCompleto()
        );

        holder.txtEmailUsuario.setText(
                usuario.getEmail()
        );

        if (usuario.getTelefono() == null
                || usuario.getTelefono().isEmpty()
                || usuario.getTelefono().equals("null")) {

            holder.txtTelefonoUsuario.setText(
                    "Sin teléfono"
            );

        } else {

            holder.txtTelefonoUsuario.setText(
                    usuario.getTelefono()
            );
        }

        holder.txtPlanUsuario.setText(
                "Plan: " + usuario.getNombrePlan()
        );

        Glide.with(holder.itemView.getContext())
                .load(usuario.getImagenUsuario())
                .placeholder(R.drawable.logo_trainup)
                .error(R.drawable.logo_trainup)
                .centerCrop()
                .into(holder.imgUsuario);

        holder.btnDesapuntar.setOnClickListener(v -> {

            if (listener != null) {
                listener.onDesapuntar(usuario);
            }

        });
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public static class UsuarioViewHolder
            extends RecyclerView.ViewHolder {

        ImageView imgUsuario;

        TextView txtNombreUsuario;
        TextView txtEmailUsuario;
        TextView txtTelefonoUsuario;
        TextView txtPlanUsuario;

        Button btnDesapuntar;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);

            imgUsuario =
                    itemView.findViewById(R.id.imgUsuario);

            txtNombreUsuario =
                    itemView.findViewById(R.id.txtNombreUsuario);

            txtEmailUsuario =
                    itemView.findViewById(R.id.txtEmailUsuario);

            txtTelefonoUsuario =
                    itemView.findViewById(R.id.txtTelefonoUsuario);

            txtPlanUsuario =
                    itemView.findViewById(R.id.txtPlanUsuario);

            btnDesapuntar =
                    itemView.findViewById(R.id.btnDesapuntar);
        }
    }
}