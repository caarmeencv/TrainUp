package com.carmen.trainup.models;

public class Reserva {

    private long idReserva;
    private String estado;

    private long idClase;
    private String nombreClase;
    private String fechaClase;
    private String horaClase;
    private String sala;
    private String monitor;
    private String imagenClase;

    public Reserva(long idReserva, String estado, long idClase, String nombreClase,
                   String fechaClase, String horaClase, String sala,
                   String monitor, String imagenClase) {
        this.idReserva = idReserva;
        this.estado = estado;
        this.idClase = idClase;
        this.nombreClase = nombreClase;
        this.fechaClase = fechaClase;
        this.horaClase = horaClase;
        this.sala = sala;
        this.monitor = monitor;
        this.imagenClase = imagenClase;
    }

    public long getIdReserva() { return idReserva; }
    public String getEstado() { return estado; }
    public long getIdClase() { return idClase; }
    public String getNombreClase() { return nombreClase; }
    public String getFechaClase() { return fechaClase; }
    public String getHoraClase() { return horaClase; }
    public String getSala() { return sala; }
    public String getMonitor() { return monitor; }
    public String getImagenClase() { return imagenClase; }
}