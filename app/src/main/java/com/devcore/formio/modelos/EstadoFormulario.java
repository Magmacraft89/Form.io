package com.devcore.formio.modelos;

import com.devcore.formio.R;

/*
 * Estado de un formulario (módulo 2.9), con el color de su etiqueta.
 *   Borrador = verde azulado · Activo = verde menta · Cerrado = azul noche
 */
public enum EstadoFormulario {
    BORRADOR("Borrador", R.color.verde_azulado, R.color.blanco),
    ACTIVO("Activo", R.color.verde_menta, R.color.azul_noche),
    CERRADO("Cerrado", R.color.azul_noche, R.color.blanco);

    private final String nombre;
    private final int colorFondo;
    private final int colorTexto;

    EstadoFormulario(String nombre, int colorFondo, int colorTexto) {
        this.nombre = nombre;
        this.colorFondo = colorFondo;
        this.colorTexto = colorTexto;
    }

    public String getNombre() { return nombre; }
    public int getColorFondo() { return colorFondo; }
    public int getColorTexto() { return colorTexto; }
}
