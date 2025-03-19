package com.joshnisth.proyectorompecabezas.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import com.joshnisth.proyectorompecabezas.data.models.Rompecabezas;

@Dao
public interface RompecabezasDao {
    @Insert
    void insertar(Rompecabezas rompecabezas);

    @Query("SELECT * FROM rompecabezas")
    List<Rompecabezas> obtenerTodos();
    @Query("DELETE FROM rompecabezas WHERE id = :id")
    void eliminarPorId(int id);

}
