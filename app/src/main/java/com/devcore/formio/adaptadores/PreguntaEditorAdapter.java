package com.devcore.formio.adaptadores;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.devcore.formio.R;
import com.devcore.formio.modelos.Pregunta;
import com.devcore.formio.modelos.TipoPregunta;

import java.util.Collections;
import java.util.List;

/*
 * Adapter del editor (2.2 / 3.x): muestra preguntas y secciones,
 * y permite cambiarlas de lugar arrastrando (ItemTouchHelper).
 */
public class PreguntaEditorAdapter extends RecyclerView.Adapter<PreguntaEditorAdapter.ViewHolder> {

    private static final int TIPO_PREGUNTA = 0;
    private static final int TIPO_SECCION = 1;

    public interface AlTocar {
        void editar(Pregunta p);
    }

    private final List<Pregunta> preguntas;
    private final AlTocar alTocar;

    public PreguntaEditorAdapter(List<Pregunta> preguntas, AlTocar alTocar) {
        this.preguntas = preguntas;
        this.alTocar = alTocar;
    }

    // Llamado al arrastrar una tarjeta
    public void mover(int desde, int hasta) {
        if (desde < hasta) {
            for (int i = desde; i < hasta; i++) Collections.swap(preguntas, i, i + 1);
        } else {
            for (int i = desde; i > hasta; i--) Collections.swap(preguntas, i, i - 1);
        }
        notifyItemMoved(desde, hasta);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView tipo;
        final TextView numero, texto, detalle, obligatoria;

        ViewHolder(View v) {
            super(v);
            tipo = v.findViewById(R.id.imgTipo);
            numero = v.findViewById(R.id.txtNumero);
            texto = v.findViewById(R.id.txtTexto);
            detalle = v.findViewById(R.id.txtDetalle);
            obligatoria = v.findViewById(R.id.txtObligatoria);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return preguntas.get(position).esSeccion() ? TIPO_SECCION : TIPO_PREGUNTA;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == TIPO_SECCION ? R.layout.item_seccion_editor : R.layout.item_pregunta_editor;
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Pregunta p = preguntas.get(position);
        Context c = h.itemView.getContext();
        h.texto.setText(p.getTexto());

        if (p.esSeccion()) {
            h.numero.setText(c.getString(R.string.seccion_n).toUpperCase());
            h.detalle.setText(p.getDescripcion());
            h.detalle.setVisibility(p.getDescripcion().isEmpty() ? View.GONE : View.VISIBLE);
        } else {
            h.numero.setText((numeroDe(position)) + ". " + p.getTipo().getNombre().toUpperCase());
            h.tipo.setImageResource(p.getTipo().getIcono());
            int[] colores = colorDeTipo(c, p.getTipo());
            h.tipo.setBackgroundTintList(ColorStateList.valueOf(colores[0]));
            h.tipo.setImageTintList(ColorStateList.valueOf(colores[1]));
            h.detalle.setText(detalle(c, p));
            h.obligatoria.setVisibility(p.isObligatoria() ? View.VISIBLE : View.GONE);
        }
        h.itemView.setOnClickListener(v -> alTocar.editar(p));
    }

    // Número de pregunta sin contar las secciones
    private int numeroDe(int posicion) {
        int n = 0;
        for (int i = 0; i <= posicion; i++) if (!preguntas.get(i).esSeccion()) n++;
        return n;
    }

    public static String detalle(Context c, Pregunta p) {
        switch (p.getTipo()) {
            case OPCION_MULTIPLE:
            case CASILLAS:
                return TextUtils.join(" · ", p.getOpciones());
            case ESCALA:
                return c.getString(R.string.escala_de, p.getEscalaMax());
            case PARRAFO:
                return c.getString(R.string.respuesta_parrafo);
            case FECHA:
                return c.getString(R.string.respuesta_fecha);
            default:
                return c.getString(R.string.respuesta_texto_corto);
        }
    }

    // [fondo, ícono] del círculo de cada tipo de pregunta
    public static int[] colorDeTipo(Context c, TipoPregunta tipo) {
        int azul = ContextCompat.getColor(c, R.color.azul_noche);
        int verde = ContextCompat.getColor(c, R.color.verde_azulado);
        int menta = ContextCompat.getColor(c, R.color.verde_menta);
        int blanco = ContextCompat.getColor(c, R.color.blanco);
        switch (tipo) {
            case OPCION_MULTIPLE:
            case ESCALA:
                return new int[]{menta, azul};
            case CASILLAS:
            case FECHA:
                return new int[]{azul, menta};
            default:
                return new int[]{verde, blanco};
        }
    }

    @Override
    public int getItemCount() {
        return preguntas.size();
    }
}
