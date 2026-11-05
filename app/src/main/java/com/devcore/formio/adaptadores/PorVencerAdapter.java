package com.devcore.formio.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devcore.formio.R;
import com.devcore.formio.modelos.Formulario;
import com.devcore.formio.util.Formato;

import java.util.ArrayList;
import java.util.List;

/*
 * Adapter de los formularios por vencer (Dashboard 7.3):
 * muestra cuánto tiempo falta o cuántas respuestas quedan, con una barra de progreso.
 */
public class PorVencerAdapter extends RecyclerView.Adapter<PorVencerAdapter.ViewHolder> {

    private static final long TRES_DIAS = 3L * 24 * 60 * 60 * 1000;

    private final List<Formulario> lista = new ArrayList<>();
    private final FormularioMiniAdapter.AlTocar alTocar;

    public PorVencerAdapter(FormularioMiniAdapter.AlTocar alTocar) {
        this.alTocar = alTocar;
    }

    public void setLista(List<Formulario> nueva) {
        lista.clear();
        lista.addAll(nueva);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView icono;
        final TextView titulo, detalle;
        final ProgressBar progreso;

        ViewHolder(View v) {
            super(v);
            icono = v.findViewById(R.id.imgIcono);
            titulo = v.findViewById(R.id.txtTitulo);
            detalle = v.findViewById(R.id.txtDetalle);
            progreso = v.findViewById(R.id.progreso);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_por_vencer, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Formulario f = lista.get(position);
        h.titulo.setText(f.getTitulo());

        long falta = f.getFechaLimite() - System.currentTimeMillis();
        if (f.getFechaLimite() > 0 && falta < TRES_DIAS) {
            // Por fecha: contador regresivo
            h.icono.setImageResource(R.drawable.ic_reloj);
            h.detalle.setText(h.itemView.getContext().getString(R.string.cierra_en, Formato.tiempoRestante(falta)));
            h.progreso.setProgress((int) (100 - falta * 100 / TRES_DIAS));
        } else {
            // Por límite de respuestas
            int total = f.getRespuestas().size();
            h.icono.setImageResource(R.drawable.ic_usuarios);
            h.detalle.setText(h.itemView.getContext().getString(R.string.de_limite, total, f.getLimiteRespuestas()));
            h.progreso.setProgress(total * 100 / Math.max(1, f.getLimiteRespuestas()));
        }
        h.itemView.setOnClickListener(v -> alTocar.abrir(f));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}
