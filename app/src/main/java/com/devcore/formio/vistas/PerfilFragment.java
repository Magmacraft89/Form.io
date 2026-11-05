package com.devcore.formio.vistas;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.devcore.formio.R;
import com.devcore.formio.datos.Repositorio;
import com.devcore.formio.modelos.Administrador;
import com.devcore.formio.modelos.EstadoFormulario;
import com.devcore.formio.util.Alertas;
import com.devcore.formio.util.FotoPerfil;
import com.google.android.material.textfield.TextInputLayout;

import cn.pedant.SweetAlert.SweetAlertDialog;

/*
 * 1.4 DATOS DEL ADMINISTRADOR
 *  - Foto de perfil: elegir de la galería o quitarla.
 *  - Información personal: modo "ver" y modo "editar" (botón Editar).
 *    Todo se guarda en Firestore (usuarios/{uid}). El correo es el de
 *    Firebase Authentication, por eso no se edita aquí.
 *  - Cambiar contraseña y cerrar sesión.
 */
public class PerfilFragment extends VistaBase {

    private TextInputLayout tilNombre, tilCorreo, tilTelefono, tilOrganizacion;
    private boolean editando;

    // Abre la galería para elegir una imagen
    private final ActivityResultLauncher<String> elegirFoto =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::alElegirFoto);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tilNombre = view.findViewById(R.id.tilNombre);
        tilCorreo = view.findViewById(R.id.tilCorreo);
        tilTelefono = view.findViewById(R.id.tilTelefono);
        tilOrganizacion = view.findViewById(R.id.tilOrganizacion);

        view.findViewById(R.id.btnCambiarFoto).setOnClickListener(v -> opcionesFoto());
        view.findViewById(R.id.imgAvatar).setOnClickListener(v -> opcionesFoto());
        view.findViewById(R.id.txtIniciales).setOnClickListener(v -> opcionesFoto());
        view.findViewById(R.id.btnEditar).setOnClickListener(v -> modoEditar(true));
        view.findViewById(R.id.btnCancelar).setOnClickListener(v -> modoEditar(false));
        view.findViewById(R.id.btnGuardar).setOnClickListener(v -> guardar());
        view.findViewById(R.id.opcionContrasena).setOnClickListener(v -> cambiarContrasena());
        view.findViewById(R.id.opcionSalir).setOnClickListener(v -> main().confirmarCerrarSesion());

        // El correo es la cuenta de Firebase: solo lectura
        if (tilCorreo.getEditText() != null) tilCorreo.getEditText().setEnabled(false);
        tilCorreo.setHelperText(getString(R.string.perfil_correo_fijo));

        refrescar();
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.perfil_titulo);
    }

    @Override
    protected void refrescar() {
        View v = requireView();
        Administrador a = repo().getAdmin();

        // Cabecera
        FotoPerfil.mostrar(a, v.findViewById(R.id.imgAvatar), v.findViewById(R.id.txtIniciales));
        ((TextView) v.findViewById(R.id.txtNombre)).setText(a.getNombre());
        ((TextView) v.findViewById(R.id.txtCorreoCabecera)).setText(a.getCorreo());
        ((TextView) v.findViewById(R.id.txtRol)).setText(getString(R.string.perfil_rol,
                a.getOrganizacion().isEmpty() ? getString(R.string.app_name) : a.getOrganizacion()));

        // Resumen
        ((TextView) v.findViewById(R.id.txtResFormularios)).setText(String.valueOf(repo().getFormularios().size()));
        ((TextView) v.findViewById(R.id.txtResRespuestas)).setText(String.valueOf(repo().totalRespuestas()));
        ((TextView) v.findViewById(R.id.txtResActivos)).setText(
                String.valueOf(repo().getPorEstado(EstadoFormulario.ACTIVO).size()));

        // Modo ver
        ponerDato(v.findViewById(R.id.txtVerNombre), a.getNombre());
        ponerDato(v.findViewById(R.id.txtVerCorreo), a.getCorreo());
        ponerDato(v.findViewById(R.id.txtVerTelefono), a.getTelefono());
        ponerDato(v.findViewById(R.id.txtVerOrganizacion), a.getOrganizacion());
    }

    private void ponerDato(TextView t, String valor) {
        boolean vacio = valor == null || valor.trim().isEmpty();
        t.setText(vacio ? getString(R.string.perfil_sin_dato) : valor);
        t.setAlpha(vacio ? 0.5f : 1f);
    }


    // =========================================================
    // VER / EDITAR
    // =========================================================

    private void modoEditar(boolean editar) {
        editando = editar;
        View v = requireView();
        v.findViewById(R.id.cardVer).setVisibility(editar ? View.GONE : View.VISIBLE);
        v.findViewById(R.id.cardEditar).setVisibility(editar ? View.VISIBLE : View.GONE);
        v.findViewById(R.id.btnEditar).setVisibility(editar ? View.INVISIBLE : View.VISIBLE);
        if (editar) {
            Administrador a = repo().getAdmin();
            poner(tilNombre, a.getNombre());
            poner(tilCorreo, a.getCorreo());
            poner(tilTelefono, a.getTelefono());
            poner(tilOrganizacion, a.getOrganizacion());
            tilNombre.setError(null);
        }
    }

    private void poner(TextInputLayout campo, String texto) {
        if (campo.getEditText() != null) campo.getEditText().setText(texto);
    }

    private void guardar() {
        if (!Validar.noVacio(tilNombre)) return;
        Administrador a = repo().getAdmin();
        a.setNombre(Validar.texto(tilNombre));
        a.setTelefono(Validar.texto(tilTelefono));
        a.setOrganizacion(Validar.texto(tilOrganizacion));
        modoEditar(false);
        guardarPerfil(getString(R.string.msg_perfil_guardado));
    }

    // Sube el perfil a Firestore mostrando la carga y luego el resultado
    private void guardarPerfil(String mensajeExito) {
        main().actualizarCabeceraMenu();
        SweetAlertDialog carga = Alertas.cargando(requireContext(), getString(R.string.guardando));
        repo().guardarPerfil(new Repositorio.Resultado() {
            @Override public void listo() {
                if (!isAdded()) return;
                Alertas.aExito(carga, mensajeExito, null, null);
            }
            @Override public void error(String mensaje) {
                if (!isAdded()) return;
                Alertas.aError(carga, getString(R.string.algo_salio_mal), mensaje);
            }
        });
    }


    // =========================================================
    // FOTO DE PERFIL
    // =========================================================

    private void opcionesFoto() {
        Administrador a = repo().getAdmin();
        if (!a.tieneFoto()) {
            elegirFoto.launch("image/*");
            return;
        }
        String[] opciones = {getString(R.string.perfil_elegir_galeria), getString(R.string.perfil_quitar_foto)};
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.perfil_foto)
                .setItems(opciones, (d, cual) -> {
                    if (cual == 0) {
                        elegirFoto.launch("image/*");
                    } else {
                        Alertas.confirmar(requireContext(), getString(R.string.perfil_quitar_foto),
                                getString(R.string.perfil_quitar_foto_msg), getString(R.string.quitar), () -> {
                                    a.setFoto("");
                                    guardarPerfil(getString(R.string.msg_foto_quitada));
                                });
                    }
                })
                .show();
    }

    private void alElegirFoto(@Nullable Uri uri) {
        if (uri == null || !isAdded()) return;
        Administrador a = repo().getAdmin();
        String foto = FotoPerfil.codificar(requireContext(), uri);
        if (foto == null) {
            Alertas.error(requireContext(), getString(R.string.algo_salio_mal), getString(R.string.msg_foto_error));
            return;
        }
        a.setFoto(foto);
        guardarPerfil(getString(R.string.msg_foto_actualizada));
    }


    // =========================================================
    // CAMBIAR CONTRASEÑA
    // =========================================================

    private void cambiarContrasena() {
        View d = LayoutInflater.from(requireContext()).inflate(R.layout.dialogo_contrasena, null);
        TextInputLayout tilActual = d.findViewById(R.id.tilActual);
        TextInputLayout tilNueva = d.findViewById(R.id.tilNueva);
        TextInputLayout tilConfirmar = d.findViewById(R.id.tilConfirmarNueva);

        AlertDialog dialogo = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.perfil_cambiar_contrasena)
                .setView(d)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.guardar, null)
                .create();
        dialogo.setOnShowListener(x -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(b -> {
            boolean ok = Validar.noVacio(tilActual) & Validar.contrasena(tilNueva) & Validar.noVacio(tilConfirmar);
            if (!ok) return;
            if (!Validar.texto(tilNueva).equals(Validar.texto(tilConfirmar))) {
                tilConfirmar.setError(getString(R.string.error_no_coinciden));
                return;
            }
            dialogo.dismiss();
            SweetAlertDialog carga = Alertas.cargando(requireContext(), getString(R.string.guardando));
            repo().cambiarContrasena(Validar.texto(tilActual), Validar.texto(tilNueva), new Repositorio.Resultado() {
                @Override public void listo() {
                    if (!isAdded()) return;
                    Alertas.aExito(carga, getString(R.string.msg_contrasena_cambiada), null, null);
                }
                @Override public void error(String mensaje) {
                    if (!isAdded()) return;
                    Alertas.aError(carga, getString(R.string.algo_salio_mal), mensaje);
                }
            });
        }));
        dialogo.show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("editando", editando);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.getBoolean("editando")) modoEditar(true);
    }
}
