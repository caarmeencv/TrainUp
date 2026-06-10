package com.carmen.trainup;

public class Plan {

    private int idPlan;
    private String nombrePlan;
    private String descripcion;
    private String precio;

    public Plan(int idPlan, String nombrePlan, String descripcion, String precio) {
        this.idPlan = idPlan;
        this.nombrePlan = nombrePlan;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    public int getIdPlan() {
        return idPlan;
    }

    public String getNombrePlan() {
        return nombrePlan;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getPrecio() {
        return precio;
    }
}