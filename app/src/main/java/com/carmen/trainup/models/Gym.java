package com.carmen.trainup.models;

public class Gym {

    private int idGym;
    private String nombre;
    private String ciudad;
    private String descripcion;
    private String imagen;
    private String direccion;
    private String email;
    private String telefono;

    public Gym(int idGym,
               String nombre,
               String ciudad,
               String descripcion,
               String imagen,
               String direccion,
               String email,
               String telefono) {

        this.idGym = idGym;
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.descripcion = descripcion;
        this.imagen = imagen;
        this.direccion = direccion;
        this.email = email;
        this.telefono = telefono;
    }

    public int getIdGym() {
        return idGym;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getImagen() {
        return imagen;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }
}