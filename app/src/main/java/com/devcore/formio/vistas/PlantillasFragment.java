package com.devcore.formio.vistas;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devcore.formio.R;
import com.devcore.formio.adaptadores.PlantillaAdapter;
import com.devcore.formio.adaptadores.PreguntaEditorAdapter;
import com.devcore.formio.datos.Plantillas;
import com.devcore.formio.datos.Repositorio;
import com.devcore.formio.util.Alertas;
import com.devcore.formio.modelos.Formulario;
import com.devcore.formio.modelos.Plantilla;
import com.devcore.formio.modelos.Pregunta;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/*
 * 3.4 PLANTILLAS: galería con vista previa y "Usar esta plantilla".
 *  - "Mis plantillas": las que el administrador guardó desde un formulario (Firestore).
 *  - Básicas: modelos preconfigurados que trae la app.
 */
public class PlantillasFragment extends VistaBase {

    private PlantillaAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plantillas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rv = view.findViewById(R.id.rvPlantillas);
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new PlantillaAdapter(lista(), new PlantillaAdapter.Acciones() {
            @Override
            public void usar(Plantilla p) {
                Formulario f = repo().crearDesdePlantilla(p);
                main().abrirFormularioNuevo(f.getId());
            }

            @Override
            public void verPrevia(Plantilla p) {
                mostrarPrevia(p);
            }

            @Override
            public void eliminar(Plantilla p) {
                eliminarPlantilla(p);
            }
        });
        rv.setAdapter(adapter);
    }

    // Propias primero (tiempo real desde Firestore) y luego las básicas
    private List<Plantilla> lista() {
        List<Plantilla> todas = new ArrayList<>(repo().getMisPlantillas());
        todas.addAll(Plantillas.todas());
        return todas;
    }

    @Override
    protected void refrescar() {
        if (adapter != null) adapter.actualizar(lista());
    }

    private void eliminarPlantilla(Plantilla p) {
        Alertas.confirmar(requireContext(), getString(R.string.eliminar_plantilla_titulo),
                getString(R.string.eliminar_plantilla_texto, p.getNombre()), getString(R.string.accion_eliminar), () -> {
                    SweetAlertDialog carga = Alertas.cargando(requireContext(), getString(R.string.eliminando));
                    repo().eliminarPlantilla(p, new Repositorio.Resultado() {
                        @Override public void listo() {
                            if (isAdded()) Alertas.aExito(carga, getString(R.string.plantilla_eliminada), null, null);
                        }
                        @Override public void error(String mensaje) {
                            if (isAdded()) Alertas.aError(carga, getString(R.string.algo_salio_mal), mensaje);
                        }
                    });
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.menu_plantillas);
    }

    // Vista previa: lista de preguntas que trae la plantilla
    private void mostrarPrevia(Plantilla p) {
        List<String> lineas = new ArrayList<>();
        int n = 0;
        for (Pregunta pr : p.getPreguntas()) {
            if (pr.esSeccion()) {
                lineas.add("▌ " + pr.getTexto().toUpperCase());
            } else {
                n++;
                lineas.add(n + ". " + pr.getTexto() + (pr.isObligatoria() ? " *" : "")
                        + "\n     " + pr.getTipo().getNombre() + " · " + PreguntaEditorAdapter.detalle(requireContext(), pr));
            }
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(p.getNombre())
                .setMessage(p.getDescripcion() + "\n\n" + TextUtils.join("\n\n", lineas))
                .setNegativeButton(R.string.cerrar, null)
                .setPositiveButton(R.string.usar_plantilla, (d, w) -> {
                    Formulario f = repo().crearDesdePlantilla(p);
                    main().abrirFormularioNuevo(f.getId());
                })
                .show();
    }
}
