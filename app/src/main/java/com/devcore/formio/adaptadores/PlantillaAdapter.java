package com.devcore.formio.adaptadores;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.devcore.formio.R;
import com.devcore.formio.modelos.Plantilla;

import java.util.List;

/*
 * Adapter de la galería de plantillas (3.4), en cuadrícula de 2 columnas.
 * Muestra primero las plantillas propias del administrador y luego las básicas.
 */
public class PlantillaAdapter extends RecyclerView.Adapter<PlantillaAdapter.ViewHolder> {

    public interface Acciones {
        void usar(Plantilla p);
        void verPrevia(Plantilla p);
        void eliminar(Plantilla p);
    }

    private List<Plantilla> lista;
    private final Acciones acciones;

    public PlantillaAdapter(List<Plantilla> lista, Acciones acciones) {
        this.lista = lista;
        this.acciones = acciones;
    }

    public void actualizar(List<Plantilla> nueva) {
        this.lista = nueva;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View usar, ver, propia, eliminar;
        final ImageView icono;
        final TextView nombre, descripcion, preguntas;

        ViewHolder(View v) {
            super(v);
            usar = v.findViewById(R.id.btnUsar);
            ver = v.findViewById(R.id.btnVer);
            propia = v.findViewById(R.id.txtPropia);
            eliminar = v.findViewById(R.id.btnEliminarPlantilla);
            icono = v.findViewById(R.id.imgPlantilla);
            nombre = v.findViewById(R.id.txtNombre);
            descripcion = v.findViewById(R.id.txtDescripcion);
            preguntas = v.findViewById(R.id.txtPreguntas);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plantilla, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Plantilla p = lista.get(position);
        int color = ContextCompat.getColor(h.itemView.getContext(), p.getColor());
        // Círculo del color de la plantilla con el ícono en blanco (o azul noche sobre menta)
        h.icono.setBackgroundTintList(ColorStateList.valueOf(color));
        h.icono.setImageResource(p.getIcono());
        int colorIcono = p.getColor() == R.color.verde_menta ? R.color.azul_noche : R.color.blanco;
        h.icono.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(h.itemView.getContext(), colorIcono)));
        h.nombre.setText(p.getNombre());
        h.descripcion.setText(p.getDescripcion());
        int n = p.getCantidadPreguntas();
        h.preguntas.setText(h.itemView.getResources().getQuantityString(R.plurals.n_preguntas, n, n));
        h.usar.setOnClickListener(v -> acciones.usar(p));
        h.ver.setOnClickListener(v -> acciones.verPrevia(p));
        h.propia.setVisibility(p.isPropia() ? View.VISIBLE : View.GONE);
        h.eliminar.setVisibility(p.isPropia() ? View.VISIBLE : View.GONE);
        h.eliminar.setOnClickListener(v -> acciones.eliminar(p));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}
