package com.devcore.formio.datos;

import static com.devcore.formio.modelos.TipoPregunta.CASILLAS;
import static com.devcore.formio.modelos.TipoPregunta.FECHA;
import static com.devcore.formio.modelos.TipoPregunta.OPCION_MULTIPLE;
import static com.devcore.formio.modelos.TipoPregunta.PARRAFO;
import static com.devcore.formio.modelos.TipoPregunta.TEXTO_CORTO;

import com.devcore.formio.modelos.Formulario;
import com.devcore.formio.modelos.Pregunta;
import com.devcore.formio.modelos.Respuesta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/*
 * Formularios y respuestas de ejemplo. Se suben a Cloud Firestore con el botón
 * "Cargar ejemplos" del Dashboard, para probar o presentar la app.
 */
final class DatosEjemplo {

    static final long HORA = 60L * 60 * 1000;
    static final long DIA = 24 * HORA;

    private static final Random R = new Random(2026);

    private static final String[] COMENTARIOS = {
            "Muy buena atención, todo rápido.", "Me gustaría más variedad.", "Excelente, sigan así.",
            "Los precios podrían ser más accesibles.", "Todo bien organizado.", "Mejorar el tiempo de espera.",
            "Me encantó la experiencia.", "Más opciones saludables, por favor.", "La explicación fue muy clara.",
            "Faltó tiempo para las preguntas.", "Muy interesante el tema.", "Ampliar el horario."
    };

    private DatosEjemplo() { }


    static List<Formulario> crear() {
        List<Formulario> lista = new ArrayList<>();
        long ahora = System.currentTimeMillis();

        // ---------------------------------------------------------
        // 1. Satisfacción del cliente (activo, cierra pronto)
        // ---------------------------------------------------------
        Formulario f1 = new Formulario("Satisfacción del cliente – Cafetería",
                "Ayúdanos a mejorar el servicio de la cafetería del campus. Solo toma 2 minutos.", ahora - 9 * DIA);
        f1.getPreguntas().add(Pregunta.seccion("Tu visita", "Cuéntanos con qué frecuencia nos visitas."));
        f1.getPreguntas().add(Pregunta.de(OPCION_MULTIPLE, "¿Con qué frecuencia visitas la cafetería?", true,
                "Todos los días", "2 o 3 veces por semana", "Una vez por semana", "Casi nunca"));
        f1.getPreguntas().add(Pregunta.de(CASILLAS, "¿Qué productos compras normalmente?", false,
                "Desayunos", "Almuerzos", "Bebidas", "Snacks", "Postres"));
        f1.getPreguntas().add(Pregunta.escala("¿Qué tan satisfecho estás con la atención?", 5, true));
        f1.getPreguntas().add(Pregunta.seccion("Tu opinión", "Tus comentarios son anónimos."));
        f1.getPreguntas().add(Pregunta.escala("Calidad de la comida", 5, true));
        f1.getPreguntas().add(Pregunta.de(OPCION_MULTIPLE, "¿Recomendarías la cafetería?", true, "Sí", "Tal vez", "No"));
        f1.getPreguntas().add(Pregunta.de(PARRAFO, "¿Qué podríamos mejorar?", false));
        f1.setPublicado(true);
        f1.setFechaLimite(ahora + DIA + 5 * HORA);
        f1.setLimiteRespuestas(60);
        llenar(f1, new int[]{7, 9, 8, 6, 7, 5, 4}, 0.22);
        lista.add(f1);

        // ---------------------------------------------------------
        // 2. Asistencia a charla (activo, cerca del límite)
        // ---------------------------------------------------------
        Formulario f2 = new Formulario("Asistencia – Charla de ciberseguridad",
                "Registro de asistencia a la charla del Auditorio. Escanea el código al entrar.", ahora - 3 * DIA);
        f2.getPreguntas().add(Pregunta.de(OPCION_MULTIPLE, "Carrera que estudias", true,
                "Ingeniería en Sistemas", "Ciencias Jurídicas", "Administración", "Enfermería", "Otra"));
        f2.getPreguntas().add(Pregunta.de(OPCION_MULTIPLE, "Año que cursas", true, "1.er año", "2.º año", "3.er año", "4.º año", "5.º año"));
        f2.getPreguntas().add(Pregunta.escala("¿Qué tan útil te pareció la charla?", 10, true));
        f2.getPreguntas().add(Pregunta.de(CASILLAS, "Temas que te gustaría ver después", false,
                "Contraseñas seguras", "Redes sociales", "Phishing", "Wi-Fi público"));
        f2.getPreguntas().add(Pregunta.de(TEXTO_CORTO, "Comentario breve", false));
        f2.setPublicado(true);
        f2.setLimiteRespuestas(100);
        llenar(f2, new int[]{45, 31, 12}, 0.12);
        lista.add(f2);

        // ---------------------------------------------------------
        // 3. Evaluación de curso (activo)
        // ---------------------------------------------------------
        Formulario f3 = new Formulario("Evaluación del curso de Desarrollo Móvil",
                "Evaluación anónima de la materia Desarrollo de Aplicaciones Móviles I.", ahora - 6 * DIA);
        f3.getPreguntas().add(Pregunta.escala("Claridad de las explicaciones", 5, true));
        f3.getPreguntas().add(Pregunta.escala("Dificultad de las prácticas", 5, true));
        f3.getPreguntas().add(Pregunta.de(OPCION_MULTIPLE, "Ritmo de la clase", true, "Muy lento", "Adecuado", "Muy rápido"));
        f3.getPreguntas().add(Pregunta.de(CASILLAS, "Temas que más te gustaron", false,
                "Fragments", "RecyclerView", "Firebase", "Diseño en XML"));
        f3.getPreguntas().add(Pregunta.de(FECHA, "¿Cuándo te gustaría la próxima práctica?", false));
        f3.getPreguntas().add(Pregunta.de(PARRAFO, "Sugerencias para el curso", false));
        f3.setPublicado(true);
        f3.setFechaLimite(ahora + 12 * DIA);
        llenar(f3, new int[]{3, 5, 4, 6, 4, 5}, 0.18);
        lista.add(f3);

        // ---------------------------------------------------------
        // 4. Inscripción (cerrado manualmente)
        // ---------------------------------------------------------
        Formulario f4 = new Formulario("Inscripción – Torneo de fútbol interfacultades",
                "Inscripción de equipos para el torneo de noviembre.", ahora - 20 * DIA);
        f4.getPreguntas().add(Pregunta.de(TEXTO_CORTO, "Nombre del equipo", true));
        f4.getPreguntas().add(Pregunta.de(OPCION_MULTIPLE, "Facultad", true, "Ingeniería", "Ciencias Sociales", "Salud", "Económicas"));
        f4.getPreguntas().add(Pregunta.de(OPCION_MULTIPLE, "Categoría", true, "Masculino", "Femenino", "Mixto"));
        f4.getPreguntas().add(Pregunta.de(FECHA, "Fecha preferida para jugar", false));
        f4.setPublicado(true);
        f4.setAbierto(false);
        llenarDesde(f4, 12, new int[]{4, 6, 5, 8, 9}, 0.15);
        lista.add(f4);

        // ---------------------------------------------------------
        // 5. Borrador
        // ---------------------------------------------------------
        Formulario f5 = new Formulario("Encuesta de transporte estudiantil",
                "Queremos saber cómo llegas a la universidad.", ahora - DIA);
        f5.getPreguntas().add(Pregunta.de(OPCION_MULTIPLE, "¿Cómo llegas a la universidad?", true,
                "Bus", "Microbús", "Carro propio", "Moto", "A pie"));
        f5.getPreguntas().add(Pregunta.de(OPCION_MULTIPLE, "¿Cuánto tiempo tardas?", true,
                "Menos de 15 min", "15 a 30 min", "30 a 60 min", "Más de 1 hora"));
        f5.getPreguntas().add(Pregunta.escala("¿Qué tan seguro te sientes en el camino?", 5, false));
        lista.add(f5);

        // ---------------------------------------------------------
        // 6. Archivado (venció)
        // ---------------------------------------------------------
        Formulario f6 = new Formulario("Feria de emprendimiento 2025",
                "Votación del público para el mejor proyecto de la feria.", ahora - 45 * DIA);
        f6.getPreguntas().add(Pregunta.de(OPCION_MULTIPLE, "Mejor proyecto", true,
                "Café Cumbre", "EcoBolsa", "AppTutor", "Dulces Doña Tere"));
        f6.getPreguntas().add(Pregunta.escala("¿Qué te pareció la feria?", 5, true));
        f6.setPublicado(true);
        f6.setFechaLimite(ahora - 30 * DIA);
        f6.setArchivado(true);
        llenarDesde(f6, 31, new int[]{40, 52, 28}, 0.2);
        lista.add(f6);

        return lista;
    }


    // =========================================================
    // GENERADOR DE RESPUESTAS
    // =========================================================

    // porDia[0] = respuestas de hoy, porDia[1] = ayer, ...
    private static void llenar(Formulario f, int[] porDia, double abandono) {
        llenarDesde(f, 0, porDia, abandono);
    }

    private static void llenarDesde(Formulario f, int diasAtras, int[] porDia, double abandono) {
        long ahora = System.currentTimeMillis();
        Calendar inicioHoy = Calendar.getInstance();
        inicioHoy.set(Calendar.HOUR_OF_DAY, 0);
        inicioHoy.set(Calendar.MINUTE, 0);
        inicioHoy.set(Calendar.SECOND, 0);

        List<Long> fechas = new ArrayList<>();
        for (int d = 0; d < porDia.length; d++) {
            long inicioDia = inicioHoy.getTimeInMillis() - (long) (d + diasAtras) * DIA;
            for (int i = 0; i < porDia[d]; i++) {
                long fecha = inicioDia + 7 * HORA + (long) (R.nextDouble() * 12 * HORA);
                if (fecha > ahora) {   // hoy: que no queden en el futuro
                    long desde = Math.min(inicioDia + 6 * HORA, ahora - HORA);
                    fecha = desde + (long) (R.nextDouble() * Math.max(1, ahora - desde));
                }
                fechas.add(fecha);
            }
        }
        Collections.sort(fechas);

        int numero = 1;
        for (long fecha : fechas) {
            Respuesta r = new Respuesta(numero++, fecha, 45 + R.nextInt(200));
            for (Pregunta p : f.getPreguntas()) {
                List<String> valor = valorAleatorio(p);
                if (valor != null) r.getValores().put(p.getId(), valor);
            }
            f.getRespuestas().add(r);
        }
        f.setVisitas((int) Math.round(fechas.size() / (1.0 - abandono)));
    }

    private static List<String> valorAleatorio(Pregunta p) {
        List<String> v = new ArrayList<>();
        switch (p.getTipo()) {
            case OPCION_MULTIPLE:
                v.add(p.getOpciones().get(indicePesado(p.getOpciones().size())));
                return v;
            case CASILLAS:
                for (int i = 0; i < p.getOpciones().size(); i++) {
                    if (R.nextDouble() < 0.65 - i * 0.1) v.add(p.getOpciones().get(i));
                }
                if (v.isEmpty()) v.add(p.getOpciones().get(0));
                return v;
            case ESCALA: {
                int max = p.getEscalaMax();
                int valor = max - indicePesado(Math.max(2, max / 2 + 1));
                v.add(String.valueOf(Math.max(1, valor)));
                return v;
            }
            case TEXTO_CORTO:
            case PARRAFO:
                if (!p.isObligatoria() && R.nextDouble() < 0.4) return null;   // opcional: algunos no responden
                v.add(COMENTARIOS[R.nextInt(COMENTARIOS.length)]);
                return v;
            case FECHA: {
                if (!p.isObligatoria() && R.nextDouble() < 0.3) return null;
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DAY_OF_YEAR, 3 + R.nextInt(25));
                v.add(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(c.getTime()));
                return v;
            }
            default:
                return null;   // secciones no tienen respuesta
        }
    }

    // Índice al azar donde las primeras opciones salen más (para gráficas más realistas)
    private static int indicePesado(int n) {
        double total = 0;
        for (int i = 0; i < n; i++) total += 1.0 / (i + 1);
        double x = R.nextDouble() * total;
        for (int i = 0; i < n; i++) {
            x -= 1.0 / (i + 1);
            if (x <= 0) return i;
        }
        return n - 1;
    }
}
