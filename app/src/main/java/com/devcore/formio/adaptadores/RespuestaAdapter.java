package com.devcore.formio.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devcore.formio.R;
import com.devcore.formio.modelos.Respuesta;
import com.devcore.formio.util.Formato;

import java.util.ArrayList;
import java.util.List;

/*
 * Adapter de la lista de respuestas recibidas (5.1).
 * Muestra la más nueva primero; las que llegaron hace menos de 2 minutos dicen "EN VIVO".
 */
public class RespuestaAdapter extends RecyclerView.Adapter<RespuestaAdapter.ViewHolder> {

    public interface AlTocar {
        void abrir(Respuesta r);
    }

    private final List<Respuesta> lista = new ArrayList<>();
    private final AlTocar alTocar;

    public RespuestaAdapter(AlTocar alTocar) {
        this.alTocar = alTocar;
    }

    // Recibe las respuestas en orden de llegada y las guarda de la más nueva a la más vieja
    public void setLista(List<Respuesta> respuestas) {
        lista.clear();
        for (int i = respuestas.size() - 1; i >= 0; i--) lista.add(respuestas.get(i));
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView numero, titulo, nueva, fecha, duracion;

        ViewHolder(View v) {
            super(v);
            numero = v.findViewById(R.id.txtNumero);
            titulo = v.findViewById(R.id.txtTitulo);
            nueva = v.findViewById(R.id.txtNueva);
            fecha = v.findViewById(R.id.txtFecha);
            duracion = v.findViewById(R.id.txtDuracion);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_respuesta, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Respuesta r = lista.get(position);
        h.numero.setText("#" + r.getNumero());
        h.titulo.setText(h.itemView.getContext().getString(R.string.respuesta_n, r.getNumero()));
        h.fecha.setText(Formato.fechaHora(r.getFecha()));
        h.duracion.setText(Formato.duracion(r.getDuracionSegundos()));
        boolean reciente = System.currentTimeMillis() - r.getFecha() < 2 * 60 * 1000;
        h.nueva.setVisibility(reciente ? View.VISIBLE : View.GONE);
        h.itemView.setOnClickListener(v -> alTocar.abrir(r));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}
