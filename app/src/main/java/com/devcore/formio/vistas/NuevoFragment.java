package com.devcore.formio.vistas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.devcore.formio.R;
import com.devcore.formio.modelos.Formulario;
import com.google.android.material.textfield.TextInputLayout;

/*
 * 2.1 CREAR: título y descripción del formulario nuevo.
 * Al tocar "Comenzar a crear" se guarda como Borrador y se abre el editor.
 */
public class NuevoFragment extends VistaBase {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nuevo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextInputLayout tilTitulo = view.findViewById(R.id.tilTitulo);
        TextInputLayout tilDescripcion = view.findViewById(R.id.tilDescripcion);

        view.findViewById(R.id.btnComenzar).setOnClickListener(v -> {
            if (!Validar.noVacio(tilTitulo)) return;
            Formulario f = repo().crear(Validar.texto(tilTitulo), Validar.texto(tilDescripcion));
            main().abrirFormularioNuevo(f.getId());
        });

        view.findViewById(R.id.cardPlantillas).setOnClickListener(v -> main().navegarA(R.id.nav_plantillas));
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.menu_nuevo);
    }
}
