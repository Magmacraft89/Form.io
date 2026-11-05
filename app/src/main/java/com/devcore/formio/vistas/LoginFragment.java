package com.devcore.formio.vistas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

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
 * 1.2 LOGIN: autentica al administrador.
 * Usa Firebase Authentication (signInWithEmailAndPassword).
 */
public class LoginFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AuthActivity auth = (AuthActivity) requireActivity();

        TextInputLayout tilCorreo = view.findViewById(R.id.tilCorreo);
        TextInputLayout tilContrasena = view.findViewById(R.id.tilContrasena);
        CheckBox recordarme = view.findViewById(R.id.cbRecordarme);

        view.findViewById(R.id.btnIniciar).setOnClickListener(v -> {
            boolean ok = Validar.correo(tilCorreo) & Validar.noVacio(tilContrasena);
            if (!ok) return;
            SweetAlertDialog carga = Alertas.cargando(requireContext(), getString(R.string.iniciando_sesion));
            Repositorio.get().iniciarSesion(Validar.texto(tilCorreo), Validar.texto(tilContrasena),
                    new Repositorio.Resultado() {
                        @Override public void listo() {
                            Alertas.cerrar(carga);
                            auth.entrar(recordarme.isChecked());
                        }
                        @Override public void error(String mensaje) {
                            if (!isAdded()) return;
                            Alertas.aError(carga, getString(R.string.no_se_pudo_entrar), mensaje);
                        }
                    });
        });

        view.findViewById(R.id.btnOlvide).setOnClickListener(v -> auth.mostrarRecuperar());
        view.findViewById(R.id.btnIrRegistro).setOnClickListener(v -> auth.mostrarRegistro());
    }
}
