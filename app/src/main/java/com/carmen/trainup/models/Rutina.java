package com.carmen.trainup.models;

public class Rutina {

    private long idRutina;
    private String nombreRutina;
    private String descripcion;
    private int diasSemana;
    private String objetivo;
    private String nivel;
    private String imagenRutina;

    public Rutina(long idRutina, String nombreRutina, String descripcion, int diasSemana,
                  String objetivo, String nivel, String imagenRutina) {
        this.idRutina = idRutina;
        this.nombreRutina = nombreRutina;
        this.descripcion = descripcion;
        this.diasSemana = diasSemana;
        this.objetivo = objetivo;
        this.nivel = nivel;
        this.imagenRutina = imagenRutina;
    }

    public long getIdRutina() {
        return idRutina;
    }

    public String getNombreRutina() {
        return nombreRutina;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getDiasSemana() {
        return diasSemana;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public String getNivel() {
        return nivel;
    }

    public String getImagenRutina() {
        return imagenRutina;
    }
}