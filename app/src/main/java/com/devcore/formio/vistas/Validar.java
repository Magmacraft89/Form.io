package com.devcore.formio.vistas;

import android.util.Patterns;

import com.devcore.formio.R;
import com.google.android.material.textfield.TextInputLayout;

/*
 * Validaciones de los campos de texto (muestran el error debajo del campo).
 */
final class Validar {

    private Validar() { }

    static String texto(TextInputLayout campo) {
        return campo.getEditText() == null ? "" : campo.getEditText().getText().toString().trim();
    }

    static boolean noVacio(TextInputLayout campo) {
        if (texto(campo).isEmpty()) {
            campo.setError(campo.getContext().getString(R.string.error_vacio));
            return false;
        }
        campo.setError(null);
        return true;
    }

    static boolean correo(TextInputLayout campo) {
        if (!noVacio(campo)) return false;
        if (!Patterns.EMAIL_ADDRESS.matcher(texto(campo)).matches()) {
            campo.setError(campo.getContext().getString(R.string.error_correo));
            return false;
        }
        campo.setError(null);
        return true;
    }

    static boolean contrasena(TextInputLayout campo) {
        if (!noVacio(campo)) return false;
        if (texto(campo).length() < 6) {
            campo.setError(campo.getContext().getString(R.string.error_contrasena_corta));
            return false;
        }
        campo.setError(null);
        return true;
    }
}
