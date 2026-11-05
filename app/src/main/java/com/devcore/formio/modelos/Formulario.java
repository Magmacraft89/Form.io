package com.devcore.formio.modelos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
 * Modelo: un formulario creado por el administrador.
 */
public class Formulario {

    // Dominio de Firebase Hosting donde vive la página web del formulario (módulo 4.4).
    // Se arma con el id del proyecto de Firebase: <proyecto>.web.app/f/
    private static String dominio = "formio-devcore.web.app/f/";

    public static void setDominio(String d) { dominio = d; }
    public static String getDominio() { return dominio; }

    private final String id;                     // también es el código corto del enlace: x1y2z9
    private String creadorId = "";               // uid del administrador que lo creó
    private String titulo;
    private String descripcion;
    private final long fechaCreacion;

    private boolean publicado;                   // false = Borrador
    private boolean abierto = true;              // interruptor Abierto / Cerrado (2.6)
    private boolean archivado;                   // 2.4
    private long fechaLimite;                    // 2.7 (0 = sin fecha límite)
    private int limiteRespuestas;                // 2.8 (0 = sin límite)
    private String mensajeLimite = "Este formulario ya no recibe respuestas. ¡Gracias por tu interés!";
    private boolean unaPorDispositivo;           // 2.10

    private final List<Pregunta> preguntas = new ArrayList<>();
    private final List<Respuesta> respuestas = new ArrayList<>();
    private int visitas;                         // veces que se abrió (para la tasa de abandono)
    private int totalRespuestas;                 // contador en la base de datos (numera las respuestas)
    private boolean esteDispositivoRespondio;    // simula la marca del navegador (2.10)


    public Formulario(String titulo, String descripcion, long fechaCreacion) {
        this(generarCodigo(), titulo, descripcion, fechaCreacion);
    }

    // Para reconstruir un formulario guardado en la base de datos
    public Formulario(String id, String titulo, String descripcion, long fechaCreacion) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
    }

    // Código corto y fácil de escribir (sin letras que se confunden como l, o, 0, 1)
    public static String generarCodigo() {
        String letras = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) sb.append(letras.charAt(r.nextInt(letras.length())));
        return sb.toString();
    }


    // =========================================================
    // ESTADO: Borrador, Activo o Cerrado
    // =========================================================

    public EstadoFormulario getEstado() {
        if (!publicado) return EstadoFormulario.BORRADOR;
        if (!abierto || vencio() || llegoAlLimite()) return EstadoFormulario.CERRADO;
        return EstadoFormulario.ACTIVO;
    }

    public boolean vencio() {
        return fechaLimite > 0 && System.currentTimeMillis() > fechaLimite;
    }

    public boolean llegoAlLimite() {
        return limiteRespuestas > 0 && Math.max(totalRespuestas, respuestas.size()) >= limiteRespuestas;
    }

    public boolean recibeRespuestas() {
        return getEstado() == EstadoFormulario.ACTIVO;
    }

    public String getEnlace() { return "https://" + dominio + id; }
    public String getEnlaceCorto() { return dominio + id; }

    public int getCantidadPreguntas() {
        int n = 0;
        for (Pregunta p : preguntas) if (!p.esSeccion()) n++;
        return n;
    }

    public int getSiguienteNumero() {
        return respuestas.isEmpty() ? 1 : respuestas.get(respuestas.size() - 1).getNumero() + 1;
    }


    // =========================================================
    // GETTERS Y SETTERS
    // =========================================================

    public String getId() { return id; }
    public String getCodigo() { return id; }
    public String getCreadorId() { return creadorId; }
    public void setCreadorId(String creadorId) { this.creadorId = creadorId; }
    public int getTotalRespuestas() { return Math.max(totalRespuestas, respuestas.size()); }
    public void setTotalRespuestas(int totalRespuestas) { this.totalRespuestas = totalRespuestas; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public long getFechaCreacion() { return fechaCreacion; }
    public boolean isPublicado() { return publicado; }
    public void setPublicado(boolean publicado) { this.publicado = publicado; }
    public boolean isAbierto() { return abierto; }
    public void setAbierto(boolean abierto) { this.abierto = abierto; }
    public boolean isArchivado() { return archivado; }
    public void setArchivado(boolean archivado) { this.archivado = archivado; }
    public long getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(long fechaLimite) { this.fechaLimite = fechaLimite; }
    public int getLimiteRespuestas() { return limiteRespuestas; }
    public void setLimiteRespuestas(int limiteRespuestas) { this.limiteRespuestas = limiteRespuestas; }
    public String getMensajeLimite() { return mensajeLimite; }
    public void setMensajeLimite(String mensajeLimite) { this.mensajeLimite = mensajeLimite; }
    public boolean isUnaPorDispositivo() { return unaPorDispositivo; }
    public void setUnaPorDispositivo(boolean unaPorDispositivo) { this.unaPorDispositivo = unaPorDispositivo; }
    public List<Pregunta> getPreguntas() { return preguntas; }
    public List<Respuesta> getRespuestas() { return respuestas; }
    public int getVisitas() { return visitas; }
    public void setVisitas(int visitas) { this.visitas = visitas; }
    public boolean isEsteDispositivoRespondio() { return esteDispositivoRespondio; }
    public void setEsteDispositivoRespondio(boolean v) { this.esteDispositivoRespondio = v; }
}
