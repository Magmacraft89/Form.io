package com.devcore.formio.modelos;

import com.devcore.formio.R;

/*
 * Tipos de pregunta que se pueden agregar a un formulario (módulo 3.1).
 * SECCION no es una pregunta: sirve para dividir el formulario en páginas (módulo 3.3).
 */
public enum TipoPregunta {
    TEXTO_CORTO("Texto corto", R.drawable.ic_texto_corto),
    PARRAFO("Párrafo", R.drawable.ic_parrafo),
    OPCION_MULTIPLE("Opción múltiple", R.drawable.ic_opcion_multiple),
    CASILLAS("Casillas", R.drawable.ic_casillas),
    ESCALA("Escala", R.drawable.ic_escala),
    FECHA("Fecha", R.drawable.ic_fecha),
    SECCION("Sección", R.drawable.ic_seccion);

    private final String nombre;
    private final int icono;

    TipoPregunta(String nombre, int icono) {
        this.nombre = nombre;
        this.icono = icono;
    }

    public String getNombre() { return nombre; }
    public int getIcono() { return icono; }

    // ¿Este tipo usa una lista de opciones?
    public boolean usaOpciones() {
        return this == OPCION_MULTIPLE || this == CASILLAS;
    }
}
