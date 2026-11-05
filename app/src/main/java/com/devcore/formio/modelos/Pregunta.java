package com.devcore.formio.modelos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 * Modelo: una pregunta del formulario (o un separador de sección).
 */
public class Pregunta {

    private final String id;
    private TipoPregunta tipo;
    private String texto;               // enunciado (o título de la sección)
    private String descripcion;         // solo para secciones
    private final List<String> opciones = new ArrayList<>();
    private boolean obligatoria;
    private int escalaMax = 5;          // escala 1-5 o 1-10


    public Pregunta(TipoPregunta tipo, String texto) {
        this(UUID.randomUUID().toString().substring(0, 8), tipo, texto);
    }

    // Para reconstruir una pregunta guardada en la base de datos (conserva su id)
    public Pregunta(String id, TipoPregunta tipo, String texto) {
        this.id = id;
        this.tipo = tipo;
        this.texto = texto;
        this.descripcion = "";
    }

    // Atajos para armar preguntas rápido (datos de ejemplo y plantillas)
    public static Pregunta de(TipoPregunta tipo, String texto, boolean obligatoria, String... opciones) {
        Pregunta p = new Pregunta(tipo, texto);
        p.obligatoria = obligatoria;
        for (String o : opciones) p.opciones.add(o);
        return p;
    }

    public static Pregunta escala(String texto, int max, boolean obligatoria) {
        Pregunta p = new Pregunta(TipoPregunta.ESCALA, texto);
        p.escalaMax = max;
        p.obligatoria = obligatoria;
        return p;
    }

    public static Pregunta seccion(String titulo, String descripcion) {
        Pregunta p = new Pregunta(TipoPregunta.SECCION, titulo);
        p.descripcion = descripcion;
        return p;
    }

    // Copia con un id nuevo (para duplicar formularios y usar plantillas)
    public Pregunta copia() {
        Pregunta p = new Pregunta(tipo, texto);
        p.descripcion = descripcion;
        p.opciones.addAll(opciones);
        p.obligatoria = obligatoria;
        p.escalaMax = escalaMax;
        return p;
    }

    public String getId() { return id; }
    public TipoPregunta getTipo() { return tipo; }
    public void setTipo(TipoPregunta tipo) { this.tipo = tipo; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public List<String> getOpciones() { return opciones; }
    public boolean isObligatoria() { return obligatoria; }
    public void setObligatoria(boolean obligatoria) { this.obligatoria = obligatoria; }
    public int getEscalaMax() { return escalaMax; }
    public void setEscalaMax(int escalaMax) { this.escalaMax = escalaMax; }
    public boolean esSeccion() { return tipo == TipoPregunta.SECCION; }
}
