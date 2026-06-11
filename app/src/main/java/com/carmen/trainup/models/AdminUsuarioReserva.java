package com.carmen.trainup.models;

public class AdminUsuarioReserva {

    private int idReserva;
    private int idUsuario;
    private String nombre;
    private String apellidos;
    private String email;
    private String estado;
    private String fechaReserva;

    public AdminUsuarioReserva(int idReserva, int idUsuario, String nombre, String apellidos,
                               String email, String estado, String fechaReserva) {
        this.idReserva = idReserva;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.estado = estado;
        this.fechaReserva = fechaReserva;
    }

    public int getIdReserva() {
        return idReserva;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    public String getEmail() {
        return email;
    }

    public String getEstado() {
        return estado;
    }

    public String getFechaReserva() {
        return fechaReserva;
    }
}