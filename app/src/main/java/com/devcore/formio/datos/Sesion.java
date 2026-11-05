package com.devcore.formio.datos;

import android.content.Context;
import android.content.SharedPreferences;

/*
 * Opción "Recordarme" del login.
 * Firebase guarda la sesión en el teléfono; si el usuario NO quiso que lo recuerden,
 * al abrir la app otra vez se cierra la sesión y se pide el login.
 */
public final class Sesion {

    private static final String PREFS = "formio_sesion";
    private static final String RECORDAR = "recordar";

    private Sesion() { }

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static void setRecordar(Context c, boolean recordar) {
        prefs(c).edit().putBoolean(RECORDAR, recordar).apply();
    }

    public static boolean recordar(Context c) {
        return prefs(c).getBoolean(RECORDAR, true);
    }
}
