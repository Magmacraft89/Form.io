package com.devcore.formio.vistas;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.devcore.formio.MainActivity;
import com.devcore.formio.datos.Repositorio;
import com.devcore.formio.modelos.Formulario;

/*
 * Base de las vistas del panel (Fragments).
 * Se registra como "oyente" del repositorio: cuando algo cambia (por ejemplo,
 * llega una respuesta), la vista se actualiza sola -> tiempo real.
 */
public abstract class VistaBase extends Fragment implements Repositorio.Oyente {

    protected static final String ARG_ID = "id_formulario";

    protected MainActivity main() {
        return (MainActivity) requireActivity();
    }

    protected Repositorio repo() {
        return Repositorio.get();
    }

    // Crea los argumentos con el id del formulario
    protected static Bundle argsCon(String idFormulario) {
        Bundle b = new Bundle();
        b.putString(ARG_ID, idFormulario);
        return b;
    }

    // Formulario que recibió esta vista (o null si ya no existe)
    protected Formulario formulario() {
        Bundle b = getArguments();
        return b == null ? null : repo().buscar(b.getString(ARG_ID));
    }

    @Override
    public void onStart() {
        super.onStart();
        repo().agregarOyente(this);
    }

    @Override
    public void onStop() {
        repo().quitarOyente(this);
        super.onStop();
    }

    @Override
    public void alCambiarDatos() {
        if (isAdded() && getView() != null) refrescar();
    }

    // Cada vista vuelve a pintar sus datos aquí
    protected void refrescar() { }
}
