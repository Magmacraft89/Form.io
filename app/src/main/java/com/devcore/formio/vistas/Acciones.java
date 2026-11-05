package com.devcore.formio.vistas;

import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.devcore.formio.R;
import com.devcore.formio.datos.Repositorio;
import com.devcore.formio.modelos.Formulario;
import com.devcore.formio.util.Alertas;
import com.google.android.material.textfield.TextInputLayout;

import cn.pedant.SweetAlert.SweetAlertDialog;

/*
 * Acciones sobre un formulario que se usan desde la lista y desde el detalle:
 * 2.3 Duplicar · 2.4 Archivar/Restaurar · 2.5 Eliminar · Guardar como plantilla
 */
final class Acciones {

    private Acciones() { }

    // 2.3 Duplicar: pide el nombre de la copia
    static void duplicar(Fragment fr, Formulario f) {
        View v = LayoutInflater.from(fr.requireContext()).inflate(R.layout.dialogo_texto, null);
        TextInputLayout campo = v.findViewById(R.id.tilDialogo);
        campo.setHint(fr.getString(R.string.duplicar_nombre));
        if (campo.getEditText() != null) {
            campo.getEditText().setText(fr.getString(R.string.copia_de, f.getTitulo()));
            campo.getEditText().selectAll();
        }

        AlertDialog dialogo = new AlertDialog.Builder(fr.requireContext())
                .setTitle(R.string.duplicar_titulo)
                .setView(v)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.accion_duplicar, null)
                .create();
        dialogo.setOnShowListener(d -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(b -> {
            if (!Validar.noVacio(campo)) return;
            Formulario copia = Repositorio.get().duplicar(f, Validar.texto(campo));
            dialogo.dismiss();
            Alertas.exito(fr.requireContext(), fr.getString(R.string.duplicado_titulo),
                    fr.getString(R.string.msg_duplicado, copia.getTitulo()));
        }));
        dialogo.show();
    }

    // 2.4 Archivar o restaurar
    static void archivar(Fragment fr, Formulario f, boolean archivar) {
        Repositorio.get().archivar(f, archivar);
        Alertas.exito(fr.requireContext(), fr.getString(archivar ? R.string.msg_archivado : R.string.msg_restaurado), null);
    }

    // 2.5 Eliminar con confirmación (advertencia de pérdida irreversible)
    static void eliminar(Fragment fr, Formulario f, Runnable despues) {
        Alertas.confirmar(fr.requireContext(), fr.getString(R.string.eliminar_titulo),
                fr.getString(R.string.eliminar_texto, f.getTitulo(), f.getTotalRespuestas()),
                fr.getString(R.string.accion_eliminar), () -> {
                    SweetAlertDialog carga = Alertas.cargando(fr.requireContext(), fr.getString(R.string.eliminando));
                    Repositorio.get().eliminar(f, new Repositorio.Resultado() {
                        @Override public void listo() {
                            Alertas.aExito(carga, fr.getString(R.string.msg_eliminado), null, null);
                            if (despues != null) despues.run();
                        }
                        @Override public void error(String mensaje) {
                            Alertas.aError(carga, fr.getString(R.string.algo_salio_mal), mensaje);
                        }
                    });
                });
    }

    // Guarda la estructura del formulario como plantilla propia (usuarios/{uid}/plantillas)
    static void guardarComoPlantilla(Fragment fr, Formulario f) {
        if (f.getPreguntas().isEmpty()) {
            Alertas.error(fr.requireContext(), fr.getString(R.string.plantilla_sin_preguntas_titulo),
                    fr.getString(R.string.plantilla_sin_preguntas));
            return;
        }
        View v = LayoutInflater.from(fr.requireContext()).inflate(R.layout.dialogo_texto, null);
        TextInputLayout campo = v.findViewById(R.id.tilDialogo);
        campo.setHint(fr.getString(R.string.plantilla_nombre));
        if (campo.getEditText() != null) {
            campo.getEditText().setText(f.getTitulo());
            campo.getEditText().selectAll();
        }
        AlertDialog dialogo = new AlertDialog.Builder(fr.requireContext())
                .setTitle(R.string.guardar_como_plantilla)
                .setView(v)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.guardar, null)
                .create();
        dialogo.setOnShowListener(d -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(b -> {
            if (!Validar.noVacio(campo)) return;
            String nombre = Validar.texto(campo);
            dialogo.dismiss();
            SweetAlertDialog carga = Alertas.cargando(fr.requireContext(), fr.getString(R.string.guardando));
            Repositorio.get().guardarComoPlantilla(f, nombre, new Repositorio.Resultado() {
                @Override public void listo() {
                    Alertas.aExito(carga, fr.getString(R.string.plantilla_guardada),
                            fr.getString(R.string.plantilla_guardada_msg, nombre), null);
                }
                @Override public void error(String mensaje) {
                    Alertas.aError(carga, fr.getString(R.string.algo_salio_mal), mensaje);
                }
            });
        }));
        dialogo.show();
    }
}
