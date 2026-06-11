package com.carmen.trainup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.carmen.trainup.R;
import com.carmen.trainup.models.Gym;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GymAdapter extends RecyclerView.Adapter<GymAdapter.GymViewHolder> {

    private List<Gym> listaGyms;
    private OnGymClickListener listener;

    public interface OnGymClickListener {
        void onSeleccionarGym(Gym gym);
    }

    public GymAdapter(List<Gym> listaGyms, OnGymClickListener listener) {
        this.listaGyms = listaGyms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GymViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gym, parent, false);

        return new GymViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GymViewHolder holder, int position) {
        Gym gym = listaGyms.get(position);

        holder.txtNombreGimnasio.setText(gym.getNombre());
        holder.txtCiudadGimnasio.setText(gym.getCiudad());
        holder.txtDescripcionGimnasio.setText(gym.getDescripcion());

        Glide.with(holder.itemView.getContext())
                .load(gym.getImagen())
                .placeholder(R.drawable.logo_trainup)
                .error(R.drawable.logo_trainup)
                .into(holder.imgGimnasio);

        holder.btnSeleccionar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSeleccionarGym(gym);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaGyms.size();
    }

    public static class GymViewHolder extends RecyclerView.ViewHolder {

        ImageView imgGimnasio;
        TextView txtNombreGimnasio;
        TextView txtCiudadGimnasio;
        TextView txtDescripcionGimnasio;
        Button btnSeleccionar;

        public GymViewHolder(@NonNull View itemView) {
            super(itemView);

            imgGimnasio = itemView.findViewById(R.id.imgGimnasio);
            txtNombreGimnasio = itemView.findViewById(R.id.txtNombreGimnasio);
            txtCiudadGimnasio = itemView.findViewById(R.id.txtCiudadGimnasio);
            txtDescripcionGimnasio = itemView.findViewById(R.id.txtDescripcionGimnasio);
            btnSeleccionar = itemView.findViewById(R.id.btnSeleccionar);
        }
    }
}