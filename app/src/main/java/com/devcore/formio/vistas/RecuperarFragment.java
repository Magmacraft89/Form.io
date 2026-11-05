package com.devcore.formio.vistas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
 * 1.3 RECUPERAR CONTRASEÑA: envía un enlace de recuperación al correo.
 * Usa Firebase Authentication (sendPasswordResetEmail).
 */
public class RecuperarFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recuperar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AuthActivity auth = (AuthActivity) requireActivity();
        TextInputLayout tilCorreo = view.findViewById(R.id.tilCorreo);
        View pedir = view.findViewById(R.id.layoutPedir);
        View enviado = view.findViewById(R.id.layoutEnviado);

        view.findViewById(R.id.btnEnviar).setOnClickListener(v -> {
            if (!Validar.correo(tilCorreo)) return;
            String correo = Validar.texto(tilCorreo);
            SweetAlertDialog carga = Alertas.cargando(requireContext(), getString(R.string.enviando));
            Repositorio.get().recuperarContrasena(correo, new Repositorio.Resultado() {
                @Override public void listo() {
                    if (!isAdded()) return;
                    Alertas.cerrar(carga);
                    ((TextView) view.findViewById(R.id.txtEnviado)).setText(getString(R.string.recuperar_enviado, correo));
                    pedir.setVisibility(View.GONE);
                    enviado.setVisibility(View.VISIBLE);
                }
                @Override public void error(String mensaje) {
                    if (!isAdded()) return;
                    Alertas.aError(carga, getString(R.string.no_se_pudo_enviar), mensaje);
                }
            });
        });

        view.findViewById(R.id.btnVolver).setOnClickListener(v -> auth.volverALogin());
    }
}
