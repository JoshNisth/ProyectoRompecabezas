package com.joshnisth.proyectorompecabezas.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.joshnisth.proyectorompecabezas.data.models.Jugador;
import java.util.List;

@Dao
public interface JugadorDao {
    @Insert
    void insertar(Jugador jugador);

    // Obtener mejores tiempos por tamaño
    @Query("SELECT * FROM jugador WHERE tamano = :tamano ORDER BY tiempo ASC LIMIT 10")
    List<Jugador> obtenerMejoresTiemposPorTamano(int tamano);
    @Query("DELETE FROM jugador")
    void eliminarTodos();

}
