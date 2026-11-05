package com.devcore.formio.util;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.devcore.formio.R;

import cn.pedant.SweetAlert.SweetAlertDialog;

/*
 * ALERTAS con SweetAlert: diálogos animados de éxito, error, confirmación y carga.
 * Todos usan la paleta de Form.io.
 */
public final class Alertas {

    public interface Accion {
        void ejecutar();
    }

    private Alertas() { }

    private static SweetAlertDialog crear(Context c, int tipo, String titulo, String mensaje) {
        SweetAlertDialog d = new SweetAlertDialog(c, tipo);
        d.setTitleText(titulo);
        if (mensaje != null) d.setContentText(mensaje);
        d.setConfirmButtonBackgroundColor(ContextCompat.getColor(c, R.color.verde_azulado));
        d.setCancelButtonBackgroundColor(ContextCompat.getColor(c, R.color.azul_noche));
        d.setCanceledOnTouchOutside(tipo != SweetAlertDialog.PROGRESS_TYPE);
        return d;
    }

    // Éxito (palomita verde animada)
    public static SweetAlertDialog exito(Context c, String titulo, String mensaje) {
        return exito(c, titulo, mensaje, null);
    }

    public static SweetAlertDialog exito(Context c, String titulo, String mensaje, Accion alAceptar) {
        SweetAlertDialog d = crear(c, SweetAlertDialog.SUCCESS_TYPE, titulo, mensaje);
        d.setConfirmText(c.getString(R.string.aceptar));
        d.setConfirmClickListener(s -> {
            s.dismissWithAnimation();
            if (alAceptar != null) alAceptar.ejecutar();
        });
        d.show();
        return d;
    }

    // Error (equis animada)
    public static SweetAlertDialog error(Context c, String titulo, String mensaje) {
        SweetAlertDialog d = crear(c, SweetAlertDialog.ERROR_TYPE, titulo, mensaje);
        d.setConfirmText(c.getString(R.string.aceptar));
        d.setConfirmClickListener(SweetAlertDialog::dismissWithAnimation);
        d.show();
        return d;
    }

    // Confirmación: advertencia con botones Confirmar / Cancelar
    public static SweetAlertDialog confirmar(Context c, String titulo, String mensaje,
                                             String textoConfirmar, Accion alConfirmar) {
        SweetAlertDialog d = crear(c, SweetAlertDialog.WARNING_TYPE, titulo, mensaje);
        d.setConfirmText(textoConfirmar);
        d.setCancelText(c.getString(R.string.cancelar));
        d.showCancelButton(true);
        d.setCancelClickListener(SweetAlertDialog::dismissWithAnimation);
        d.setConfirmClickListener(s -> {
            s.dismissWithAnimation();
            alConfirmar.ejecutar();
        });
        d.show();
        return d;
    }

    // Carga: rueda girando mientras Firebase responde
    public static SweetAlertDialog cargando(Context c, String titulo) {
        SweetAlertDialog d = crear(c, SweetAlertDialog.PROGRESS_TYPE, titulo, null);
        d.getProgressHelper().setBarColor(ContextCompat.getColor(c, R.color.verde_azulado));
        d.setCancelable(false);
        d.show();
        return d;
    }

    // Convierte el diálogo de carga en éxito sin cerrarlo (transición animada)
    public static void aExito(SweetAlertDialog d, String titulo, String mensaje, Accion alAceptar) {
        if (d == null || !d.isShowing()) return;
        d.setTitleText(titulo);
        d.setContentText(mensaje);
        d.setConfirmText(d.getContext().getString(R.string.aceptar));
        d.setCancelable(true);
        d.setConfirmClickListener(s -> {
            s.dismissWithAnimation();
            if (alAceptar != null) alAceptar.ejecutar();
        });
        d.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
    }

    public static void aError(SweetAlertDialog d, String titulo, String mensaje) {
        if (d == null || !d.isShowing()) return;
        d.setTitleText(titulo);
        d.setContentText(mensaje);
        d.setConfirmText(d.getContext().getString(R.string.aceptar));
        d.setCancelable(true);
        d.setConfirmClickListener(SweetAlertDialog::dismissWithAnimation);
        d.changeAlertType(SweetAlertDialog.ERROR_TYPE);
    }

    public static void cerrar(SweetAlertDialog d) {
        if (d != null && d.isShowing()) d.dismissWithAnimation();
    }
}
