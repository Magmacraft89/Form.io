package com.devcore.formio.modelos;

import java.util.ArrayList;
import java.util.List;

/*
 * Modelo: una plantilla básica (módulo 3.4) con preguntas ya armadas.
 */
public class Plantilla {

    private String id = "";             // solo las plantillas propias (guardadas por el administrador)
    private boolean propia;
    private final String nombre;
    private final String descripcion;
    private final int icono;
    private final int color;
    private final List<Pregunta> preguntas = new ArrayList<>();

    public Plantilla(String nombre, String descripcion, int icono, int color, Pregunta... preguntas) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.icono = icono;
        this.color = color;
        for (Pregunta p : preguntas) this.preguntas.add(p);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public boolean isPropia() { return propia; }
    public void setPropia(boolean propia) { this.propia = propia; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getIcono() { return icono; }
    public int getColor() { return color; }
    public List<Pregunta> getPreguntas() { return preguntas; }

    public int getCantidadPreguntas() {
        int n = 0;
        for (Pregunta p : preguntas) if (!p.esSeccion()) n++;
        return n;
    }
}
