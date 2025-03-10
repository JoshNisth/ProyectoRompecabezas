package com.joshnisth.proyectorompecabezas.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import com.joshnisth.proyectorompecabezas.data.models.Jugador;

@Dao
public interface JugadorDao {
    @Insert
    void insertar(Jugador jugador);

    @Query("SELECT * FROM jugador ORDER BY tiempo ASC LIMIT 10")
    List<Jugador> obtenerMejoresTiempos();
}
