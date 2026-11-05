package com.devcore.formio.datos;

import com.devcore.formio.R;
import com.devcore.formio.modelos.Administrador;
import com.devcore.formio.modelos.Formulario;
import com.devcore.formio.modelos.Plantilla;
import com.devcore.formio.modelos.Pregunta;
import com.devcore.formio.modelos.Respuesta;
import com.devcore.formio.modelos.TipoPregunta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Convierte los modelos de la app a mapas de Cloud Firestore y de regreso.
 *
 * Estructura de la base de datos:
 *   usuarios/{uid}                         -> datos del administrador
 *   usuarios/{uid}/plantillas/{id}         -> plantillas propias
 *   formularios/{codigo}                   -> un formulario (con sus preguntas)
 *   formularios/{codigo}/respuestas/{id}   -> respuestas anónimas
 */
public final class Mapeo {

    private Mapeo() { }

    // =========================================================
    // ADMINISTRADOR
    // =========================================================

    public static Map<String, Object> deAdmin(Administrador a) {
        Map<String, Object> m = new HashMap<>();
        m.put("nombre", a.getNombre());
        m.put("correo", a.getCorreo());
        m.put("telefono", a.getTelefono());
        m.put("organizacion", a.getOrganizacion());
        m.put("foto", a.getFoto());
        return m;
    }

    public static Administrador aAdmin(String uid, Map<String, Object> m, String correoPorDefecto) {
        Administrador a = new Administrador(uid, texto(m, "nombre", "Administrador"), texto(m, "correo", correoPorDefecto));
        a.setTelefono(texto(m, "telefono", ""));
        a.setOrganizacion(texto(m, "organizacion", ""));
        a.setFoto(texto(m, "foto", ""));
        return a;
    }


    // =========================================================
    // PREGUNTAS
    // =========================================================

    public static List<Map<String, Object>> dePreguntas(List<Pregunta> preguntas) {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Pregunta p : preguntas) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("tipo", p.getTipo().name());
            m.put("texto", p.getTexto());
            m.put("descripcion", p.getDescripcion());
            m.put("opciones", new ArrayList<>(p.getOpciones()));
            m.put("obligatoria", p.isObligatoria());
            m.put("escalaMax", p.getEscalaMax());
            lista.add(m);
        }
        return lista;
    }

    @SuppressWarnings("unchecked")
    public static List<Pregunta> aPreguntas(Object valor) {
        List<Pregunta> lista = new ArrayList<>();
        if (!(valor instanceof List)) return lista;
        for (Object o : (List<Object>) valor) {
            if (!(o instanceof Map)) continue;
            Map<String, Object> m = (Map<String, Object>) o;
            TipoPregunta tipo;
            try {
                tipo = TipoPregunta.valueOf(texto(m, "tipo", "TEXTO_CORTO"));
            } catch (IllegalArgumentException e) {
                tipo = TipoPregunta.TEXTO_CORTO;
            }
            Pregunta p = new Pregunta(texto(m, "id", String.valueOf(lista.size())), tipo, texto(m, "texto", ""));
            p.setDescripcion(texto(m, "descripcion", ""));
            p.setObligatoria(Boolean.TRUE.equals(m.get("obligatoria")));
            p.setEscalaMax((int) numero(m, "escalaMax", 5));
            Object op = m.get("opciones");
            if (op instanceof List) for (Object x : (List<Object>) op) p.getOpciones().add(String.valueOf(x));
            lista.add(p);
        }
        return lista;
    }


    // =========================================================
    // FORMULARIO
    // =========================================================

    // Todos los datos del formulario MENOS los contadores (visitas y totalRespuestas),
    // que los suma la página web y no se deben sobrescribir al editar.
    public static Map<String, Object> deFormulario(Formulario f) {
        Map<String, Object> m = new HashMap<>();
        m.put("creadorId", f.getCreadorId());
        m.put("titulo", f.getTitulo());
        m.put("descripcion", f.getDescripcion());
        m.put("fechaCreacion", f.getFechaCreacion());
        m.put("publicado", f.isPublicado());
        m.put("abierto", f.isAbierto());
        m.put("archivado", f.isArchivado());
        m.put("fechaLimite", f.getFechaLimite());
        m.put("limiteRespuestas", f.getLimiteRespuestas());
        m.put("mensajeLimite", f.getMensajeLimite());
        m.put("unaPorDispositivo", f.isUnaPorDispositivo());
        m.put("preguntas", dePreguntas(f.getPreguntas()));
        return m;
    }

    // Para crearlo por primera vez (con los contadores en 0)
    public static Map<String, Object> deFormularioNuevo(Formulario f) {
        Map<String, Object> m = deFormulario(f);
        m.put("visitas", (long) f.getVisitas());
        m.put("totalRespuestas", (long) f.getTotalRespuestas());
        return m;
    }

    public static Formulario aFormulario(String id, Map<String, Object> m) {
        Formulario f = new Formulario(id, texto(m, "titulo", "Sin título"), texto(m, "descripcion", ""),
                numero(m, "fechaCreacion", System.currentTimeMillis()));
        actualizar(f, m);
        return f;
    }

    // Copia los datos del documento al objeto que ya existe (las vistas guardan referencias a él)
    public static void actualizar(Formulario f, Map<String, Object> m) {
        f.setCreadorId(texto(m, "creadorId", ""));
        f.setTitulo(texto(m, "titulo", f.getTitulo()));
        f.setDescripcion(texto(m, "descripcion", ""));
        f.setPublicado(Boolean.TRUE.equals(m.get("publicado")));
        f.setAbierto(!Boolean.FALSE.equals(m.get("abierto")));
        f.setArchivado(Boolean.TRUE.equals(m.get("archivado")));
        f.setFechaLimite(numero(m, "fechaLimite", 0));
        f.setLimiteRespuestas((int) numero(m, "limiteRespuestas", 0));
        f.setMensajeLimite(texto(m, "mensajeLimite", f.getMensajeLimite()));
        f.setUnaPorDispositivo(Boolean.TRUE.equals(m.get("unaPorDispositivo")));
        f.setVisitas((int) numero(m, "visitas", 0));
        f.setTotalRespuestas((int) numero(m, "totalRespuestas", 0));

        // Las preguntas solo se reemplazan si cambiaron (así no se pierden las referencias al editar)
        List<Pregunta> nuevas = aPreguntas(m.get("preguntas"));
        if (!dePreguntas(nuevas).equals(dePreguntas(f.getPreguntas()))) {
            f.getPreguntas().clear();
            f.getPreguntas().addAll(nuevas);
        }
    }


    // =========================================================
    // RESPUESTA
    // =========================================================

    public static Map<String, Object> deRespuesta(Respuesta r) {
        Map<String, Object> m = new HashMap<>();
        m.put("numero", (long) r.getNumero());
        m.put("fecha", r.getFecha());
        m.put("duracion", (long) r.getDuracionSegundos());
        Map<String, Object> valores = new HashMap<>();
        for (Map.Entry<String, List<String>> e : r.getValores().entrySet()) valores.put(e.getKey(), new ArrayList<>(e.getValue()));
        m.put("valores", valores);
        return m;
    }

    @SuppressWarnings("unchecked")
    public static Respuesta aRespuesta(Map<String, Object> m) {
        Respuesta r = new Respuesta((int) numero(m, "numero", 0), numero(m, "fecha", 0),
                (int) numero(m, "duracion", 0));
        Object v = m.get("valores");
        if (v instanceof Map) {
            for (Map.Entry<String, Object> e : ((Map<String, Object>) v).entrySet()) {
                List<String> lista = new ArrayList<>();
                if (e.getValue() instanceof List) {
                    for (Object x : (List<Object>) e.getValue()) lista.add(String.valueOf(x));
                } else if (e.getValue() != null) {
                    lista.add(String.valueOf(e.getValue()));
                }
                if (!lista.isEmpty()) r.getValores().put(e.getKey(), lista);
            }
        }
        return r;
    }


    // =========================================================
    // PLANTILLA PROPIA
    // =========================================================

    public static Map<String, Object> dePlantilla(String nombre, String descripcion, List<Pregunta> preguntas) {
        Map<String, Object> m = new HashMap<>();
        m.put("nombre", nombre);
        m.put("descripcion", descripcion);
        m.put("preguntas", dePreguntas(preguntas));
        m.put("fecha", System.currentTimeMillis());
        return m;
    }

    public static Plantilla aPlantilla(String id, Map<String, Object> m) {
        List<Pregunta> preguntas = aPreguntas(m.get("preguntas"));
        Plantilla p = new Plantilla(texto(m, "nombre", "Mi plantilla"), texto(m, "descripcion", ""),
                R.drawable.ic_formularios, R.color.azul_noche, preguntas.toArray(new Pregunta[0]));
        p.setId(id);
        p.setPropia(true);
        return p;
    }


    // =========================================================
    // AYUDAS
    // =========================================================

    static String texto(Map<String, Object> m, String clave, String porDefecto) {
        Object v = m == null ? null : m.get(clave);
        return v == null ? porDefecto : String.valueOf(v);
    }

    static long numero(Map<String, Object> m, String clave, long porDefecto) {
        Object v = m == null ? null : m.get(clave);
        return v instanceof Number ? ((Number) v).longValue() : porDefecto;
    }
}
