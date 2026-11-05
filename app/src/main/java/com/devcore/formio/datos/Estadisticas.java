package com.devcore.formio.datos;

import com.devcore.formio.modelos.Formulario;
import com.devcore.formio.modelos.Pregunta;
import com.devcore.formio.modelos.Respuesta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * Cálculos del módulo 6: Estadísticas por formulario.
 */
public final class Estadisticas {

    private Estadisticas() { }

    // 6.1 Total de respuestas
    public static int total(Formulario f) {
        return f.getRespuestas().size();
    }

    public static int deHoy(Formulario f) {
        long[] hoy = Repositorio.rangoDia(0);
        int n = 0;
        for (Respuesta r : f.getRespuestas()) if (r.getFecha() >= hoy[0]) n++;
        return n;
    }

    // 6.3 Respuestas por día (últimos "dias" días, el último es hoy)
    public static int[] porDia(Formulario f, int dias) {
        int[] conteo = new int[dias];
        for (int i = 0; i < dias; i++) {
            long[] rango = Repositorio.rangoDia(i);
            for (Respuesta r : f.getRespuestas()) {
                if (r.getFecha() >= rango[0] && r.getFecha() < rango[1]) conteo[dias - 1 - i]++;
            }
        }
        return conteo;
    }

    // 6.4 Tiempo promedio de llenado (en segundos)
    public static int tiempoPromedio(Formulario f) {
        if (f.getRespuestas().isEmpty()) return 0;
        long suma = 0;
        for (Respuesta r : f.getRespuestas()) suma += r.getDuracionSegundos();
        return (int) (suma / f.getRespuestas().size());
    }

    public static int tiempoMinimo(Formulario f) {
        int min = Integer.MAX_VALUE;
        for (Respuesta r : f.getRespuestas()) min = Math.min(min, r.getDuracionSegundos());
        return min == Integer.MAX_VALUE ? 0 : min;
    }

    public static int tiempoMaximo(Formulario f) {
        int max = 0;
        for (Respuesta r : f.getRespuestas()) max = Math.max(max, r.getDuracionSegundos());
        return max;
    }

    // 6.5 Tasa de abandono = (visitas - enviadas) / visitas
    public static int tasaAbandono(Formulario f) {
        int visitas = Math.max(f.getVisitas(), total(f));
        if (visitas == 0) return 0;
        return Math.round((visitas - total(f)) * 100f / visitas);
    }


    // =========================================================
    // 6.2 GRÁFICAS POR PREGUNTA
    // =========================================================

    // Opción múltiple y casillas: cuántas veces se eligió cada opción
    public static LinkedHashMap<String, Integer> conteoOpciones(Formulario f, Pregunta p) {
        LinkedHashMap<String, Integer> conteo = new LinkedHashMap<>();
        for (String o : p.getOpciones()) conteo.put(o, 0);
        for (Respuesta r : f.getRespuestas()) {
            List<String> v = r.valorDe(p.getId());
            if (v == null) continue;
            for (String elegido : v) {
                if (conteo.containsKey(elegido)) conteo.put(elegido, conteo.get(elegido) + 1);
            }
        }
        return conteo;
    }

    // Escala: cuántas personas eligieron cada número (1..max)
    public static LinkedHashMap<String, Integer> distribucionEscala(Formulario f, Pregunta p) {
        LinkedHashMap<String, Integer> conteo = new LinkedHashMap<>();
        for (int i = 1; i <= p.getEscalaMax(); i++) conteo.put(String.valueOf(i), 0);
        for (Respuesta r : f.getRespuestas()) {
            List<String> v = r.valorDe(p.getId());
            if (v == null || v.isEmpty()) continue;
            if (conteo.containsKey(v.get(0))) conteo.put(v.get(0), conteo.get(v.get(0)) + 1);
        }
        return conteo;
    }

    public static double promedioEscala(Formulario f, Pregunta p) {
        int suma = 0, n = 0;
        for (Map.Entry<String, Integer> e : distribucionEscala(f, p).entrySet()) {
            suma += Integer.parseInt(e.getKey()) * e.getValue();
            n += e.getValue();
        }
        return n == 0 ? 0 : (double) suma / n;
    }

    // Texto y fecha: las respuestas escritas (de la más nueva a la más vieja)
    public static List<String> textos(Formulario f, Pregunta p) {
        List<String> lista = new ArrayList<>();
        for (int i = f.getRespuestas().size() - 1; i >= 0; i--) {
            List<String> v = f.getRespuestas().get(i).valorDe(p.getId());
            if (v != null && !v.isEmpty() && !v.get(0).trim().isEmpty()) lista.add(v.get(0));
        }
        return lista;
    }

    // Cuántas personas respondieron esta pregunta
    public static int contestaron(Formulario f, Pregunta p) {
        int n = 0;
        for (Respuesta r : f.getRespuestas()) {
            List<String> v = r.valorDe(p.getId());
            if (v != null && !v.isEmpty()) n++;
        }
        return n;
    }
}
