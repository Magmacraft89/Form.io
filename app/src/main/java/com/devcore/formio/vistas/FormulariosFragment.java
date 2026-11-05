package com.devcore.formio.vistas;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devcore.formio.R;
import com.devcore.formio.adaptadores.FormularioAdapter;
import com.devcore.formio.modelos.EstadoFormulario;
import com.devcore.formio.modelos.Formulario;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/*
 * 2.9 LISTA CON ESTADO (Borrador, Activo, Cerrado) con búsqueda y filtros.
 * Con modo "archivados" muestra los formularios archivados (2.4) para restaurarlos.
 */
public class FormulariosFragment extends VistaBase {

    private static final String ARG_ARCHIVADOS = "archivados";

    private boolean archivados;
    private EstadoFormulario filtro;     // null = Todos
    private String busqueda = "";
    private FormularioAdapter adapter;

    public static FormulariosFragment nueva(boolean archivados) {
        FormulariosFragment f = new FormulariosFragment();
        Bundle b = new Bundle();
        b.putBoolean(ARG_ARCHIVADOS, archivados);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_formularios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        archivados = requireArguments().getBoolean(ARG_ARCHIVADOS);

        if (archivados) {
            ((TextView) view.findViewById(R.id.txtTituloLista)).setText(R.string.form_titulo_archivados);
            ((TextView) view.findViewById(R.id.txtSubLista)).setText(R.string.form_sub_archivados);
            ((TextView) view.findViewById(R.id.txtVacio)).setText(R.string.sin_archivados_texto);
            view.findViewById(R.id.scrollFiltros).setVisibility(View.GONE);
            view.findViewById(R.id.fabNuevo).setVisibility(View.GONE);
        }

        RecyclerView rv = view.findViewById(R.id.rvFormularios);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new FormularioAdapter(new FormularioAdapter.Acciones() {
            @Override
            public void abrir(Formulario f) {
                main().abrirDetalle(f.getId());
            }

            @Override
            public void menu(Formulario f, View ancla) {
                mostrarMenu(f, ancla);
            }
        });
        rv.setAdapter(adapter);

        // Búsqueda
        ((EditText) view.findViewById(R.id.etBuscar)).addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override
            public void afterTextChanged(Editable s) {
                busqueda = s.toString().trim().toLowerCase(Locale.ROOT);
                refrescar();
            }
        });

        // Filtros por estado
        view.findViewById(R.id.chipTodos).setOnClickListener(v -> { filtro = null; refrescar(); });
        view.findViewById(R.id.chipBorrador).setOnClickListener(v -> { filtro = EstadoFormulario.BORRADOR; refrescar(); });
        view.findViewById(R.id.chipActivo).setOnClickListener(v -> { filtro = EstadoFormulario.ACTIVO; refrescar(); });
        view.findViewById(R.id.chipCerrado).setOnClickListener(v -> { filtro = EstadoFormulario.CERRADO; refrescar(); });

        view.findViewById(R.id.fabNuevo).setOnClickListener(v -> main().navegarA(R.id.nav_nuevo));
        refrescar();
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(archivados ? R.string.menu_archivados : R.string.menu_formularios);
    }

    @Override
    protected void refrescar() {
        View v = requireView();
        List<Formulario> base = archivados ? repo().getArchivados() : repo().getFormularios();
        List<Formulario> lista = new ArrayList<>();
        for (Formulario f : base) {
            if (filtro != null && f.getEstado() != filtro) continue;
            if (!busqueda.isEmpty() && !f.getTitulo().toLowerCase(Locale.ROOT).contains(busqueda)) continue;
            lista.add(f);
        }
        adapter.setLista(lista);
        ((TextView) v.findViewById(R.id.txtConteo)).setText(
                getResources().getQuantityString(R.plurals.n_formularios, lista.size(), lista.size()));
        v.findViewById(R.id.layoutVacio).setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // Menú de cada tarjeta: Editar, Duplicar, Archivar/Restaurar, Eliminar
    private void mostrarMenu(Formulario f, View ancla) {
        PopupMenu menu = new PopupMenu(requireContext(), ancla);
        if (!archivados) {
            menu.getMenu().add(0, 1, 0, R.string.accion_editar);
            menu.getMenu().add(0, 2, 1, R.string.accion_duplicar);
            menu.getMenu().add(0, 3, 2, R.string.accion_archivar);
        } else {
            menu.getMenu().add(0, 4, 0, R.string.accion_restaurar);
        }
        menu.getMenu().add(0, 5, 3, R.string.accion_eliminar);
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: main().abrirEditor(f.getId()); break;
                case 2: Acciones.duplicar(this, f); break;
                case 3: Acciones.archivar(this, f, true); break;
                case 4: Acciones.archivar(this, f, false); break;
                case 5: Acciones.eliminar(this, f, null); break;
                default: return false;
            }
            return true;
        });
        menu.show();
    }
}
