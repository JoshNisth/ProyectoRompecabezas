package com.joshnisth.proyectorompecabezas.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "jugador")
public class Jugador {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String nombre;
    private int tiempo; // en segundos

    public Jugador(String nombre, int tiempo) {
        this.nombre = nombre;
        this.tiempo = tiempo;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getTiempo() { return tiempo; }

    public void setId(int id) { this.id = id; }
}
