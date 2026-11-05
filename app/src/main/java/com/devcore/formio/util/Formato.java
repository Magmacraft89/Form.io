package com.devcore.formio.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/*
 * Formatos de fecha, hora y duración usados en toda la app.
 */
public final class Formato {

    private static final Locale ES = Locale.forLanguageTag("es-SV");

    private Formato() { }

    // 165 -> "2m 45s"
    public static String duracion(int segundos) {
        if (segundos < 60) return segundos + "s";
        return (segundos / 60) + "m " + (segundos % 60) + "s";
    }

    // "29 oct 2026 · 3:15 p. m."
    public static String fechaHora(long millis) {
        return new SimpleDateFormat("d MMM yyyy · h:mm a", ES).format(new Date(millis));
    }

    // "29 oct"
    public static String fechaCorta(long millis) {
        return new SimpleDateFormat("d MMM", ES).format(new Date(millis));
    }

    // "29 oct 2026"
    public static String fecha(long millis) {
        return new SimpleDateFormat("d MMM yyyy", ES).format(new Date(millis));
    }

    // "3:15 p. m."
    public static String hora(long millis) {
        return new SimpleDateFormat("h:mm a", ES).format(new Date(millis));
    }

    // Letra del día de la semana: "L", "M", "X"...
    public static String diaSemana(long millis) {
        String d = new SimpleDateFormat("EEE", ES).format(new Date(millis));
        return d.isEmpty() ? "" : d.substring(0, 1).toUpperCase(ES) + d.substring(1, Math.min(3, d.length())).replace(".", "");
    }

    // Tiempo que falta: "1 d 4 h", "5 h 20 min", "12 min"
    public static String tiempoRestante(long millis) {
        if (millis <= 0) return "Vencido";
        long min = millis / 60000;
        long horas = min / 60;
        long dias = horas / 24;
        if (dias > 0) return dias + " d " + (horas % 24) + " h";
        if (horas > 0) return horas + " h " + (min % 60) + " min";
        return Math.max(1, min) + " min";
    }
}
