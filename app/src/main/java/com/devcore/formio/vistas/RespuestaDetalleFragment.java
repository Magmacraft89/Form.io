package com.devcore.formio.vistas;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

import com.devcore.formio.R;
import com.devcore.formio.adaptadores.PreguntaEditorAdapter;
import com.devcore.formio.modelos.Formulario;
import com.devcore.formio.modelos.Pregunta;
import com.devcore.formio.modelos.Respuesta;
import com.devcore.formio.util.Formato;

import java.util.List;

/*
 * 5.2 VER CADA RESPUESTA INDIVIDUAL
 * Ficha con las preguntas y lo que se contestó, identificada solo por su número.
 * Botones Anterior / Siguiente para moverse entre respuestas.
 */
public class RespuestaDetalleFragment extends VistaBase {

    private static final String ARG_INDICE = "indice";
    private int indice;

    public static RespuestaDetalleFragment nueva(String idFormulario, int indice) {
        RespuestaDetalleFragment f = new RespuestaDetalleFragment();
        Bundle b = argsCon(idFormulario);
        b.putInt(ARG_INDICE, indice);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_respuesta_detalle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (formulario() == null) {
            main().regresar();
            return;
        }
        indice = savedInstanceState != null ? savedInstanceState.getInt(ARG_INDICE) : requireArguments().getInt(ARG_INDICE);

        // "Anterior" = respuesta más vieja · "Siguiente" = más nueva
        view.findViewById(R.id.btnAnterior).setOnClickListener(v -> { indice--; refrescar(); });
        view.findViewById(R.id.btnSiguiente).setOnClickListener(v -> { indice++; refrescar(); });
        refrescar();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_INDICE, indice);
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.respuestas_titulo);
    }

    @Override
    protected void refrescar() {
        Formulario f = formulario();
        if (f == null || f.getRespuestas().isEmpty()) return;
        indice = Math.max(0, Math.min(indice, f.getRespuestas().size() - 1));
        Respuesta r = f.getRespuestas().get(indice);
        View v = requireView();

        ((TextView) v.findViewById(R.id.txtTitulo)).setText(getString(R.string.respuesta_n, r.getNumero()));
        ((TextView) v.findViewById(R.id.txtDeTotal)).setText(getString(R.string.de_total, indice + 1, f.getRespuestas().size()));
        ((TextView) v.findViewById(R.id.txtFecha)).setText(Formato.fechaHora(r.getFecha()));
        ((TextView) v.findViewById(R.id.txtDuracion)).setText(getString(R.string.tardo, Formato.duracion(r.getDuracionSegundos())));
        v.findViewById(R.id.btnAnterior).setEnabled(indice > 0);
        v.findViewById(R.id.btnSiguiente).setEnabled(indice < f.getRespuestas().size() - 1);

        // Ficha: cada pregunta con su respuesta
        LinearLayout contenedor = v.findViewById(R.id.contenedor);
        contenedor.removeAllViews();
        LayoutInflater inf = LayoutInflater.from(requireContext());
        int numero = 0;
        for (Pregunta p : f.getPreguntas()) {
            if (p.esSeccion()) {
                TextView titulo = (TextView) inf.inflate(R.layout.item_seccion_respuesta, contenedor, false);
                titulo.setText(p.getTexto());
                contenedor.addView(titulo);
                continue;
            }
            numero++;
            View tarjeta = inf.inflate(R.layout.item_respuesta_pregunta, contenedor, false);
            ImageView icono = tarjeta.findViewById(R.id.imgTipo);
            icono.setImageResource(p.getTipo().getIcono());
            int[] colores = PreguntaEditorAdapter.colorDeTipo(requireContext(), p.getTipo());
            icono.setBackgroundTintList(ColorStateList.valueOf(colores[0]));
            icono.setImageTintList(ColorStateList.valueOf(colores[1]));
            ((TextView) tarjeta.findViewById(R.id.txtPregunta)).setText(numero + ". " + p.getTexto());

            TextView respuesta = tarjeta.findViewById(R.id.txtRespuesta);
            List<String> valor = r.valorDe(p.getId());
            if (valor == null || valor.isEmpty()) {
                respuesta.setText(R.string.sin_contestar);
                respuesta.setAlpha(0.5f);
            } else if (p.getTipo() == com.devcore.formio.modelos.TipoPregunta.ESCALA) {
                respuesta.setText(valor.get(0) + " / " + p.getEscalaMax());
            } else {
                respuesta.setText(TextUtils.join("\n", valor));
            }
            contenedor.addView(tarjeta);
        }
        ((NestedScrollView) v.findViewById(R.id.scroll)).scrollTo(0, 0);
    }
}
