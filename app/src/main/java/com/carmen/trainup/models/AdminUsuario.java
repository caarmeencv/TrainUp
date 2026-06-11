package com.carmen.trainup.models;

public class AdminUsuario {

    private int idUsuario;
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;
    private String rol;

    public AdminUsuario(int idUsuario, String nombre, String apellidos, String email, String telefono, String rol) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
        this.rol = rol;
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

    public String getTelefono() {
        return telefono;
    }

    public String getRol() {
        return rol;
    }
}