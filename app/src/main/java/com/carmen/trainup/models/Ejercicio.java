package com.carmen.trainup.models;

public class Ejercicio {

    private long idEjercicio;
    private String nombreEjercicio;
    private String descripcion;
    private String grupoMuscular;
    private String imagenEjercicio;

    private int series;
    private int repeticiones;
    private double peso;
    private int descanso;
    private int ordenEjercicio;

    public Ejercicio(long idEjercicio, String nombreEjercicio) {
        this.idEjercicio = idEjercicio;
        this.nombreEjercicio = nombreEjercicio;
    }

    public Ejercicio(long idEjercicio, String nombreEjercicio, String descripcion,
                     String grupoMuscular, String imagenEjercicio) {
        this.idEjercicio = idEjercicio;
        this.nombreEjercicio = nombreEjercicio;
        this.descripcion = descripcion;
        this.grupoMuscular = grupoMuscular;
        this.imagenEjercicio = imagenEjercicio;
    }

    public long getIdEjercicio() {
        return idEjercicio;
    }

    public String getNombreEjercicio() {
        return nombreEjercicio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getGrupoMuscular() {
        return grupoMuscular;
    }

    public String getImagenEjercicio() {
        return imagenEjercicio;
    }

    public int getSeries() {
        return series;
    }

    public int getRepeticiones() {
        return repeticiones;
    }

    public double getPeso() {
        return peso;
    }

    public int getDescanso() {
        return descanso;
    }

    public int getOrdenEjercicio() {
        return ordenEjercicio;
    }

    public void setSeries(int series) {
        this.series = series;
    }

    public void setRepeticiones(int repeticiones) {
        this.repeticiones = repeticiones;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public void setDescanso(int descanso) {
        this.descanso = descanso;
    }

    public void setOrdenEjercicio(int ordenEjercicio) {
        this.ordenEjercicio = ordenEjercicio;
    }

    @Override
    public String toString() {
        return nombreEjercicio;
    }
}