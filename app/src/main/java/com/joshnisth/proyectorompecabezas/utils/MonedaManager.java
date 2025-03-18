package com.joshnisth.proyectorompecabezas.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class MonedaManager {
    private static final String PREFS_NAME = "monedas_prefs";
    private static final String KEY_MONEDAS = "monedas";

    public static int obtenerMonedas(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_MONEDAS, 0);
    }

    public static void agregarMonedas(Context context, int cantidad) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int actuales = prefs.getInt(KEY_MONEDAS, 0);
        prefs.edit().putInt(KEY_MONEDAS, actuales + cantidad).apply();
    }

    public static boolean gastarMonedas(Context context, int cantidad) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int actuales = prefs.getInt(KEY_MONEDAS, 0);
        if (actuales >= cantidad) {
            prefs.edit().putInt(KEY_MONEDAS, actuales - cantidad).apply();
            return true;
        }
        return false;
    }
}
