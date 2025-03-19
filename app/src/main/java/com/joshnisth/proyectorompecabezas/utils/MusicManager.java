package com.joshnisth.proyectorompecabezas.utils;

import android.content.Context;
import android.media.MediaPlayer;
import com.joshnisth.proyectorompecabezas.R;
import java.util.Random;

public class MusicManager {
    private static MediaPlayer mediaPlayer = null;
    private static boolean isMusicOn = true; // Indica si la música está activa
    private static int[] canciones = {
            R.raw.apt,
            R.raw.asitwas,
            R.raw.giorno
    };
    private static int currentIndex = 0;

    public static void setMusicOn(boolean on) {
        isMusicOn = on;
    }

    public static boolean isMusicOn() {
        return isMusicOn;
    }

    public static void startMusic(Context context) {
        // Verificar que no esté ya reproduciendo
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // Sólo reproducir si isMusicOn = true
        if (!isMusicOn) return;

        // Crear nuevo MediaPlayer con la canción actual
        mediaPlayer = MediaPlayer.create(context, canciones[currentIndex]);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    public static void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // Cambia a la siguiente canción aleatoria
    public static void changeSongRandom(Context context) {
        if (!isMusicOn) return; // No cambiar si la música está apagada

        // Detener la anterior
        stopMusic();

        // Elegir un nuevo índice random
        Random random = new Random();
        currentIndex = random.nextInt(canciones.length);

        // Reproducir la nueva
        startMusic(context);
    }
}
