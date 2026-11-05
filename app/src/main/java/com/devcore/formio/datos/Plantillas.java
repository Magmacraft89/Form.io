package com.devcore.formio.datos;

import static com.devcore.formio.modelos.TipoPregunta.CASILLAS;
import static com.devcore.formio.modelos.TipoPregunta.FECHA;
import static com.devcore.formio.modelos.TipoPregunta.OPCION_MULTIPLE;
import static com.devcore.formio.modelos.TipoPregunta.PARRAFO;
import static com.devcore.formio.modelos.TipoPregunta.TEXTO_CORTO;

import com.devcore.formio.R;
import com.devcore.formio.modelos.Plantilla;
import com.devcore.formio.modelos.Pregunta;

import java.util.ArrayList;
import java.util.List;

/*
 * Plantillas básicas (módulo 3.4): estructuras listas para necesidades comunes.
 */
public final class Plantillas {

    private Plantillas() { }

    public static List<Plantilla> todas() {
        List<Plantilla> lista = new ArrayList<>();

        lista.add(new Plantilla("Satisfacción del cliente",
                "Mide qué tan satisfechos están tus clientes con el servicio.",
                R.drawable.ic_escala, R.color.verde_azulado,
                Pregunta.escala("¿Qué tan satisfecho estás con nuestro servicio?", 5, true),
                Pregunta.escala("¿Qué tan probable es que nos recomiendes?", 10, true),
                Pregunta.de(OPCION_MULTIPLE, "¿Volverías a visitarnos?", true, "Sí", "Tal vez", "No"),
                Pregunta.de(PARRAFO, "¿Qué podríamos mejorar?", false)));

        lista.add(new Plantilla("Asistencia a eventos",
                "Registra cuántas personas llegan y qué les pareció el evento.",
                R.drawable.ic_usuarios, R.color.verde_menta,
                Pregunta.de(OPCION_MULTIPLE, "¿Cómo te enteraste del evento?", true,
                        "Redes sociales", "Un amigo", "Afiche", "Correo"),
                Pregunta.escala("¿Qué tan útil te pareció el evento?", 5, true),
                Pregunta.de(CASILLAS, "¿Qué temas te gustaría para el próximo evento?", false,
                        "Tecnología", "Emprendimiento", "Salud", "Medio ambiente"),
                Pregunta.de(TEXTO_CORTO, "Comentario breve", false)));

        lista.add(new Plantilla("Registro de datos",
                "Recoge información general de un grupo sin pedir datos personales.",
                R.drawable.ic_formularios, R.color.azul_noche,
                Pregunta.de(OPCION_MULTIPLE, "Rango de edad", true, "Menos de 18", "18 a 25", "26 a 35", "Más de 35"),
                Pregunta.de(OPCION_MULTIPLE, "Municipio", true, "Ilobasco", "Sensuntepeque", "San Salvador", "Otro"),
                Pregunta.de(TEXTO_CORTO, "Institución u organización", false),
                Pregunta.de(CASILLAS, "Áreas de interés", false, "Tecnología", "Arte", "Deportes", "Ciencia")));

        lista.add(new Plantilla("Evaluación de curso",
                "Conoce la opinión de tus estudiantes sobre la materia.",
                R.drawable.ic_estadisticas, R.color.verde_azulado,
                Pregunta.seccion("La clase", "Cuéntanos cómo te pareció el curso."),
                Pregunta.escala("Claridad de las explicaciones", 5, true),
                Pregunta.de(OPCION_MULTIPLE, "Ritmo de la clase", true, "Muy lento", "Adecuado", "Muy rápido"),
                Pregunta.seccion("Sugerencias", "Tus ideas nos ayudan a mejorar."),
                Pregunta.de(PARRAFO, "¿Qué cambiarías del curso?", false)));

        lista.add(new Plantilla("Inscripción a taller",
                "Organiza los cupos y horarios de un taller o capacitación.",
                R.drawable.ic_calendario, R.color.verde_menta,
                Pregunta.de(OPCION_MULTIPLE, "Horario preferido", true, "Mañana", "Tarde", "Sábado"),
                Pregunta.de(OPCION_MULTIPLE, "Nivel de experiencia", true, "Principiante", "Intermedio", "Avanzado"),
                Pregunta.de(FECHA, "¿Desde qué fecha puedes asistir?", true),
                Pregunta.de(PARRAFO, "¿Qué esperas aprender?", false)));

        return lista;
    }
}
