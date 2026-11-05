package com.devcore.formio.modelos;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * Modelo: una respuesta ANÓNIMA a un formulario.
 * No guarda datos personales: solo número, fecha, tiempo de llenado y lo que se contestó.
 */
public class Respuesta {

    private final int numero;                  // #1, #2, #3...
    private final long fecha;                  // momento en que se envió
    private final int duracionSegundos;        // tiempo de llenado (abrir -> enviar)
    private final Map<String, List<String>> valores = new LinkedHashMap<>();   // idPregunta -> respuesta(s)


    public Respuesta(int numero, long fecha, int duracionSegundos) {
        this.numero = numero;
        this.fecha = fecha;
        this.duracionSegundos = duracionSegundos;
    }

    public int getNumero() { return numero; }
    public long getFecha() { return fecha; }
    public int getDuracionSegundos() { return duracionSegundos; }
    public Map<String, List<String>> getValores() { return valores; }

    public List<String> valorDe(String idPregunta) {
        return valores.get(idPregunta);
    }
}
