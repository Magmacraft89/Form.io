package com.devcore.formio.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devcore.formio.R;
import com.devcore.formio.datos.Estadisticas;
import com.devcore.formio.modelos.Formulario;

import java.util.ArrayList;
import java.util.List;

/*
 * Adapter de la lista horizontal de formularios activos (Dashboard 7.2).
 */
public class FormularioMiniAdapter extends RecyclerView.Adapter<FormularioMiniAdapter.ViewHolder> {

    public interface AlTocar {
        void abrir(Formulario f);
    }

    private final List<Formulario> lista = new ArrayList<>();
    private final AlTocar alTocar;

    public FormularioMiniAdapter(AlTocar alTocar) {
        this.alTocar = alTocar;
    }

    public void setLista(List<Formulario> nueva) {
        lista.clear();
        lista.addAll(nueva);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView titulo, respuestas, hoy;

        ViewHolder(View v) {
            super(v);
            titulo = v.findViewById(R.id.txtTitulo);
            respuestas = v.findViewById(R.id.txtRespuestas);
            hoy = v.findViewById(R.id.txtHoy);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_formulario_mini, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Formulario f = lista.get(position);
        h.titulo.setText(f.getTitulo());
        int total = f.getRespuestas().size();
        h.respuestas.setText(h.itemView.getResources().getQuantityString(R.plurals.n_respuestas, total, total));
        h.hoy.setText("+" + Estadisticas.deHoy(f) + " hoy");
        h.itemView.setOnClickListener(v -> alTocar.abrir(f));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}
