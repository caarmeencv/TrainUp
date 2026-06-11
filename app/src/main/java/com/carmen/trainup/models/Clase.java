package com.carmen.trainup.models;

public class Clase {

    private long idClase;
    private String nombre;
    private String descripcion;
    private String fecha;
    private String hora;
    private int plazasMaximas;
    private int duracion;
    private String sala;
    private String monitor;
    private int idGimnasio;
    private String imagen;

    public Clase(long idClase, String nombre, String descripcion, String fecha, String hora,
                 int plazasMaximas, int duracion, String sala, String monitor,
                 int idGimnasio, String imagen) {
        this.idClase = idClase;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.hora = hora;
        this.plazasMaximas = plazasMaximas;
        this.duracion = duracion;
        this.sala = sala;
        this.monitor = monitor;
        this.idGimnasio = idGimnasio;
        this.imagen = imagen;
    }

    public long getIdClase() { return idClase; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public int getPlazasMaximas() { return plazasMaximas; }
    public int getDuracion() { return duracion; }
    public String getSala() { return sala; }
    public String getMonitor() { return monitor; }
    public int getIdGimnasio() { return idGimnasio; }
    public String getImagen() { return imagen; }
}