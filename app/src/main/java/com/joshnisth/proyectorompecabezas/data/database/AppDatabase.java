package com.joshnisth.proyectorompecabezas.data.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.joshnisth.proyectorompecabezas.data.models.Jugador;
import com.joshnisth.proyectorompecabezas.data.models.Rompecabezas;

@Database(entities = {Jugador.class, Rompecabezas.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract JugadorDao jugadorDao();
    public abstract RompecabezasDao rompecabezasDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "rompecabezas_db"
                    ).fallbackToDestructiveMigration() // 🔴 Esto BORRA Y RECREA la base si hay cambios en el esquema
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
