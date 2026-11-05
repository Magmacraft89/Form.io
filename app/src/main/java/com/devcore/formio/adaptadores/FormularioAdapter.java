package com.devcore.formio.adaptadores;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.devcore.formio.R;
import com.devcore.formio.datos.Estadisticas;
import com.devcore.formio.modelos.EstadoFormulario;
import com.devcore.formio.modelos.Formulario;
import com.devcore.formio.util.Formato;

import java.util.ArrayList;
import java.util.List;

/*
 * Adapter del RecyclerView de "Mis formularios" (2.9) y "Archivados" (2.4).
 */
public class FormularioAdapter extends RecyclerView.Adapter<FormularioAdapter.ViewHolder> {

    public interface Acciones {
        void abrir(Formulario f);
        void menu(Formulario f, View ancla);
    }

    private final List<Formulario> lista = new ArrayList<>();
    private final Acciones acciones;

    public FormularioAdapter(Acciones acciones) {
        this.acciones = acciones;
    }

    public void setLista(List<Formulario> nueva) {
        lista.clear();
        lista.addAll(nueva);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View franja, btnMas;
        final TextView estado, hoy, titulo, descripcion, respuestas, fecha;

        ViewHolder(View v) {
            super(v);
            franja = v.findViewById(R.id.franjaEstado);
            btnMas = v.findViewById(R.id.btnMas);
            estado = v.findViewById(R.id.txtEstado);
            hoy = v.findViewById(R.id.txtHoy);
            titulo = v.findViewById(R.id.txtTitulo);
            descripcion = v.findViewById(R.id.txtDescripcion);
            respuestas = v.findViewById(R.id.txtRespuestas);
            fecha = v.findViewById(R.id.txtFecha);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_formulario, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Formulario f = lista.get(position);
        Context c = h.itemView.getContext();
        EstadoFormulario e = f.getEstado();

        // Etiqueta y franja del color del estado
        h.estado.setText(e.getNombre());
        h.estado.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(c, e.getColorFondo())));
        h.estado.setTextColor(ContextCompat.getColor(c, e.getColorTexto()));
        h.franja.setBackgroundColor(ContextCompat.getColor(c, e.getColorFondo()));

        h.titulo.setText(f.getTitulo());
        h.descripcion.setText(f.getDescripcion());
        h.descripcion.setVisibility(f.getDescripcion().isEmpty() ? View.GONE : View.VISIBLE);
        int total = f.getRespuestas().size();
        h.respuestas.setText(c.getResources().getQuantityString(R.plurals.n_respuestas, total, total));

        int hoy = Estadisticas.deHoy(f);
        h.hoy.setText("+" + hoy + " hoy");
        h.hoy.setVisibility(hoy > 0 && e == EstadoFormulario.ACTIVO ? View.VISIBLE : View.GONE);

        if (f.getFechaLimite() > 0) {
            String texto = f.vencio() ? "Venció el " : "Cierra el ";
            h.fecha.setText(texto + Formato.fechaCorta(f.getFechaLimite()));
        } else {
            h.fecha.setText("Creado el " + Formato.fechaCorta(f.getFechaCreacion()));
        }

        h.itemView.setOnClickListener(v -> acciones.abrir(f));
        h.btnMas.setOnClickListener(v -> acciones.menu(f, v));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}
