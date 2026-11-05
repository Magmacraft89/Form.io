package com.devcore.formio.vistas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devcore.formio.R;
import com.devcore.formio.adaptadores.RespuestaAdapter;
import com.devcore.formio.datos.Estadisticas;
import com.devcore.formio.modelos.Formulario;

/*
 * 5.1 LISTA DE RESPUESTAS RECIBIDAS
 * Número, fecha, hora y tiempo de llenado. Se actualiza en tiempo real.
 */
public class RespuestasFragment extends VistaBase {

    private RespuestaAdapter adapter;

    public static RespuestasFragment nueva(String idFormulario) {
        RespuestasFragment f = new RespuestasFragment();
        f.setArguments(argsCon(idFormulario));
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_respuestas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Formulario f = formulario();
        if (f == null) {
            main().regresar();
            return;
        }
        RecyclerView rv = view.findViewById(R.id.rvRespuestas);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RespuestaAdapter(r -> main().abrirRespuesta(f.getId(), f.getRespuestas().indexOf(r)));
        rv.setAdapter(adapter);
        refrescar();
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.respuestas_titulo);
    }

    @Override
    protected void refrescar() {
        Formulario f = formulario();
        if (f == null) return;
        View v = requireView();
        int total = Estadisticas.total(f);
        ((TextView) v.findViewById(R.id.txtFormTitulo)).setText(f.getTitulo());
        ((TextView) v.findViewById(R.id.txtTotal)).setText(String.valueOf(total));
        ((TextView) v.findViewById(R.id.txtTotalTexto)).setText(
                getResources().getQuantityString(R.plurals.n_respuestas, total, total)
                        + " · " + getString(R.string.est_hoy, Estadisticas.deHoy(f)));
        adapter.setLista(f.getRespuestas());
        v.findViewById(R.id.layoutVacio).setVisibility(total == 0 ? View.VISIBLE : View.GONE);
    }
}
