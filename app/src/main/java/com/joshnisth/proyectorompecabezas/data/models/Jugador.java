package com.joshnisth.proyectorompecabezas.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "jugador")
public class Jugador {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String nombre;
    private int tiempo;  // en segundos
    private int tamano;  // 3 para 3x3, 4 para 4x4

    public Jugador(String nombre, int tiempo, int tamano) {
        this.nombre = nombre;
        this.tiempo = tiempo;
        this.tamano = tamano;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getTiempo() { return tiempo; }
    public int getTamano() { return tamano; }

    public void setId(int id) { this.id = id; }
}
