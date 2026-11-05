package com.devcore.formio.vistas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.devcore.formio.AuthActivity;
import com.devcore.formio.R;
import com.devcore.formio.datos.Repositorio;
import com.devcore.formio.util.Alertas;

import cn.pedant.SweetAlert.SweetAlertDialog;
import com.google.android.material.textfield.TextInputLayout;

/*
 * 1.1 REGISTRO: crea la cuenta de un nuevo administrador.
 * Usa Firebase Authentication (createUserWithEmailAndPassword) y guarda
 * el perfil en Firestore (usuarios/{uid}).
 */
public class RegistroFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registro, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AuthActivity auth = (AuthActivity) requireActivity();

        TextInputLayout tilNombre = view.findViewById(R.id.tilNombre);
        TextInputLayout tilCorreo = view.findViewById(R.id.tilCorreo);
        TextInputLayout tilContrasena = view.findViewById(R.id.tilContrasena);
        TextInputLayout tilConfirmar = view.findViewById(R.id.tilConfirmar);

        view.findViewById(R.id.btnRegistrar).setOnClickListener(v -> {
            boolean ok = Validar.noVacio(tilNombre) & Validar.correo(tilCorreo)
                    & Validar.contrasena(tilContrasena) & Validar.noVacio(tilConfirmar);
            if (!ok) return;
            if (!Validar.texto(tilContrasena).equals(Validar.texto(tilConfirmar))) {
                tilConfirmar.setError(getString(R.string.error_no_coinciden));
                return;
            }
            SweetAlertDialog carga = Alertas.cargando(requireContext(), getString(R.string.creando_cuenta));
            Repositorio.get().registrar(Validar.texto(tilNombre), Validar.texto(tilCorreo),
                    Validar.texto(tilContrasena), new Repositorio.Resultado() {
                        @Override public void listo() {
                            if (!isAdded()) return;
                            Alertas.aExito(carga, getString(R.string.cuenta_creada),
                                    getString(R.string.bienvenida, Repositorio.get().getAdmin().getPrimerNombre()),
                                    () -> auth.entrar(true));
                        }
                        @Override public void error(String mensaje) {
                            if (!isAdded()) return;
                            Alertas.aError(carga, getString(R.string.no_se_pudo_registrar), mensaje);
                        }
                    });
        });

        view.findViewById(R.id.btnIrLogin).setOnClickListener(v -> auth.volverALogin());
    }
}
