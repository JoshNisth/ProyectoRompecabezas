package com.joshnisth.proyectorompecabezas.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rompecabezas")
public class Rompecabezas {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String nombre;
    private String rutaImagen; // Guardamos solo la ruta de la imagen

    public Rompecabezas(String nombre, String rutaImagen) {
        this.nombre = nombre;
        this.rutaImagen = rutaImagen;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getRutaImagen() { return rutaImagen; }

    public void setId(int id) { this.id = id; }
}
