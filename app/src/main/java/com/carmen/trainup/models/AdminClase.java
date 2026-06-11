package com.carmen.trainup.models;

public class AdminClase {

    private long idClase;
    private String nombre, descripcion, fecha, hora, sala, monitor, imagen;
    private int plazas, duracion;

    public AdminClase(long idClase, String nombre, String descripcion, String fecha, String hora,
                      int plazas, int duracion, String sala, String monitor, String imagen) {
        this.idClase = idClase;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.hora = hora;
        this.plazas = plazas;
        this.duracion = duracion;
        this.sala = sala;
        this.monitor = monitor;
        this.imagen = imagen;
    }

    public long getIdClase() { return idClase; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public int getPlazas() { return plazas; }
    public int getDuracion() { return duracion; }
    public String getSala() { return sala; }
    public String getMonitor() { return monitor; }
    public String getImagen() { return imagen; }
}