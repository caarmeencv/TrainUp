package com.carmen.trainup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {

    private ArrayList<Plan> listaPlanes;
    private OnPlanClickListener listener;

    public interface OnPlanClickListener {
        void onSeleccionarPlan(Plan plan);
    }

    public PlanAdapter(ArrayList<Plan> listaPlanes, OnPlanClickListener listener) {
        this.listaPlanes = listaPlanes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plan, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        Plan plan = listaPlanes.get(position);

        holder.txtNombrePlan.setText(plan.getNombrePlan());
        holder.txtDescripcionPlan.setText(plan.getDescripcion());
        holder.txtPrecioPlan.setText(plan.getPrecio() + " €");

        holder.btnSeleccionarPlan.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSeleccionarPlan(plan);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaPlanes.size();
    }

    public static class PlanViewHolder extends RecyclerView.ViewHolder {

        TextView txtNombrePlan;
        TextView txtDescripcionPlan;
        TextView txtPrecioPlan;
        Button btnSeleccionarPlan;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNombrePlan = itemView.findViewById(R.id.txtNombrePlan);
            txtDescripcionPlan = itemView.findViewById(R.id.txtDescripcionPlan);
            txtPrecioPlan = itemView.findViewById(R.id.txtPrecioPlan);
            btnSeleccionarPlan = itemView.findViewById(R.id.btnSeleccionarPlan);
        }
    }
}