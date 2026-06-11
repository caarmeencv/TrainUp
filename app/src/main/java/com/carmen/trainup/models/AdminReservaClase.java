package com.carmen.trainup.models;

public class AdminReservaClase {

    private int idClase;
    private String nombreClase;
    private String fechaClase;
    private String horaClase;
    private String monitor;
    private String sala;
    private int plazasMaximas;
    private int totalReservas;

    public AdminReservaClase(int idClase, String nombreClase, String fechaClase, String horaClase,
                             String monitor, String sala, int plazasMaximas, int totalReservas) {
        this.idClase = idClase;
        this.nombreClase = nombreClase;
        this.fechaClase = fechaClase;
        this.horaClase = horaClase;
        this.monitor = monitor;
        this.sala = sala;
        this.plazasMaximas = plazasMaximas;
        this.totalReservas = totalReservas;
    }

    public int getIdClase() {
        return idClase;
    }

    public String getNombreClase() {
        return nombreClase;
    }

    public String getFechaClase() {
        return fechaClase;
    }

    public String getHoraClase() {
        return horaClase;
    }

    public String getMonitor() {
        return monitor;
    }

    public String getSala() {
        return sala;
    }

    public int getPlazasMaximas() {
        return plazasMaximas;
    }

    public int getTotalReservas() {
        return totalReservas;
    }
}