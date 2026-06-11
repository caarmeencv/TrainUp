package com.carmen.trainup.models;

public class EjercicioRutina {

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

    public EjercicioRutina(long idEjercicio, String nombreEjercicio, String descripcion,
                           String grupoMuscular, String imagenEjercicio, int series,
                           int repeticiones, double peso, int descanso, int ordenEjercicio) {
        this.idEjercicio = idEjercicio;
        this.nombreEjercicio = nombreEjercicio;
        this.descripcion = descripcion;
        this.grupoMuscular = grupoMuscular;
        this.imagenEjercicio = imagenEjercicio;
        this.series = series;
        this.repeticiones = repeticiones;
        this.peso = peso;
        this.descanso = descanso;
        this.ordenEjercicio = ordenEjercicio;
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
}