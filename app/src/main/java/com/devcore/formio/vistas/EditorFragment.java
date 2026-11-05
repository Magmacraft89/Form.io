package com.devcore.formio.vistas;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devcore.formio.R;
import com.devcore.formio.util.Alertas;
import com.devcore.formio.adaptadores.PreguntaEditorAdapter;
import com.devcore.formio.modelos.EstadoFormulario;
import com.devcore.formio.modelos.Formulario;
import com.devcore.formio.modelos.Pregunta;
import com.devcore.formio.modelos.TipoPregunta;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

/*
 * 2.2 EDITAR + 3. CREADOR DE FORMULARIOS
 * 3.1 Tipos de pregunta · 3.2 Preguntas obligatorias · 3.3 Secciones
 * Los cambios se guardan automáticamente. Las preguntas se reordenan arrastrando.
 */
public class EditorFragment extends VistaBase {

    private PreguntaEditorAdapter adapter;

    public static EditorFragment nueva(String idFormulario) {
        EditorFragment f = new EditorFragment();
        f.setArguments(argsCon(idFormulario));
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_editor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Formulario f = formulario();
        if (f == null) {
            main().regresar();
            return;
        }

        // Título y descripción con guardado automático
        EditText etTitulo = view.findViewById(R.id.etTitulo);
        EditText etDescripcion = view.findViewById(R.id.etDescripcion);
        etTitulo.setText(f.getTitulo());
        etDescripcion.setText(f.getDescripcion());
        etTitulo.addTextChangedListener(new Cambio(texto -> {
            if (!texto.trim().isEmpty()) f.setTitulo(texto.trim());
        }));
        etDescripcion.addTextChangedListener(new Cambio(texto -> f.setDescripcion(texto.trim())));

        // Lista de preguntas
        RecyclerView rv = view.findViewById(R.id.rvPreguntas);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PreguntaEditorAdapter(f.getPreguntas(), p -> mostrarDialogo(f, p, p.getTipo()));
        rv.setAdapter(adapter);

        // Mantener presionado y arrastrar para reordenar
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder desde,
                                  @NonNull RecyclerView.ViewHolder hasta) {
                adapter.mover(desde.getBindingAdapterPosition(), hasta.getBindingAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direccion) { }

            @Override
            public void clearView(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder vh) {
                super.clearView(r, vh);
                adapter.notifyDataSetChanged();   // vuelve a numerar
                guardar(f);
            }
        }).attachToRecyclerView(rv);

        view.findViewById(R.id.btnAgregarPregunta).setOnClickListener(v -> elegirTipo(f));
        view.findViewById(R.id.btnAgregarSeccion).setOnClickListener(v -> mostrarDialogo(f, null, TipoPregunta.SECCION));
        view.findViewById(R.id.btnVistaPrevia).setOnClickListener(v -> main().abrirVistaPrevia(f.getId()));

        refrescar();
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.editor_titulo);
    }

    private void guardar(Formulario f) {
        repo().guardar(f);
    }

    @Override
    protected void refrescar() {
        Formulario f = formulario();
        if (f == null) return;
        View v = requireView();
        int n = f.getCantidadPreguntas();
        ((TextView) v.findViewById(R.id.txtCantidad)).setText(
                getResources().getQuantityString(R.plurals.n_preguntas, n, n));
        v.findViewById(R.id.txtVacio).setVisibility(f.getPreguntas().isEmpty() ? View.VISIBLE : View.GONE);
        v.findViewById(R.id.txtAyudaMover).setVisibility(f.getPreguntas().size() > 1 ? View.VISIBLE : View.GONE);

        EstadoFormulario e = f.getEstado();
        TextView estado = v.findViewById(R.id.txtEstadoEditor);
        estado.setText(e.getNombre());
        estado.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), e.getColorFondo())));
        estado.setTextColor(ContextCompat.getColor(requireContext(), e.getColorTexto()));
    }


    // =========================================================
    // 3.1 ELEGIR EL TIPO DE PREGUNTA (hoja inferior)
    // =========================================================

    private void elegirTipo(Formulario f) {
        BottomSheetDialog hoja = new BottomSheetDialog(requireContext());
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialogo_tipos, null);
        int[] ids = {R.id.tipoTextoCorto, R.id.tipoParrafo, R.id.tipoOpcion, R.id.tipoCasillas, R.id.tipoEscala, R.id.tipoFecha};
        TipoPregunta[] tipos = {TipoPregunta.TEXTO_CORTO, TipoPregunta.PARRAFO, TipoPregunta.OPCION_MULTIPLE,
                TipoPregunta.CASILLAS, TipoPregunta.ESCALA, TipoPregunta.FECHA};
        for (int i = 0; i < ids.length; i++) {
            TipoPregunta tipo = tipos[i];
            v.findViewById(ids[i]).setOnClickListener(x -> {
                hoja.dismiss();
                mostrarDialogo(f, null, tipo);
            });
        }
        hoja.setContentView(v);
        hoja.show();
    }


    // =========================================================
    // DIÁLOGO PARA CREAR O EDITAR UNA PREGUNTA / SECCIÓN
    // =========================================================

    private void mostrarDialogo(Formulario f, @Nullable Pregunta existente, TipoPregunta tipo) {
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialogo_pregunta, null);
        boolean esSeccion = tipo == TipoPregunta.SECCION;

        ImageView imgTipo = v.findViewById(R.id.imgTipo);
        imgTipo.setImageResource(tipo.getIcono());
        int[] colores = PreguntaEditorAdapter.colorDeTipo(requireContext(), tipo);
        imgTipo.setBackgroundTintList(ColorStateList.valueOf(colores[0]));
        imgTipo.setImageTintList(ColorStateList.valueOf(colores[1]));
        ((TextView) v.findViewById(R.id.txtTipo)).setText(tipo.getNombre());

        TextInputLayout tilTexto = v.findViewById(R.id.tilTexto);
        TextInputLayout tilDescripcion = v.findViewById(R.id.tilDescripcion);
        View layoutOpciones = v.findViewById(R.id.layoutOpciones);
        LinearLayout contenedorOpciones = v.findViewById(R.id.contenedorOpciones);
        View errorOpciones = v.findViewById(R.id.txtErrorOpciones);
        View layoutEscala = v.findViewById(R.id.layoutEscala);
        RadioButton rb10 = v.findViewById(R.id.rbEscala10);
        CompoundButton swObligatoria = v.findViewById(R.id.swObligatoria);

        if (esSeccion) {
            tilTexto.setHint(getString(R.string.campo_titulo_seccion));
            tilDescripcion.setVisibility(View.VISIBLE);
            v.findViewById(R.id.filaObligatoria).setVisibility(View.GONE);
        }
        layoutOpciones.setVisibility(tipo.usaOpciones() ? View.VISIBLE : View.GONE);
        layoutEscala.setVisibility(tipo == TipoPregunta.ESCALA ? View.VISIBLE : View.GONE);

        // Valores actuales (o por defecto)
        if (existente != null) {
            tilTexto.getEditText().setText(existente.getTexto());
            tilDescripcion.getEditText().setText(existente.getDescripcion());
            swObligatoria.setChecked(existente.isObligatoria());
            rb10.setChecked(existente.getEscalaMax() == 10);
            for (String o : existente.getOpciones()) agregarFilaOpcion(contenedorOpciones, tipo, o);
        } else if (tipo.usaOpciones()) {
            agregarFilaOpcion(contenedorOpciones, tipo, "");
            agregarFilaOpcion(contenedorOpciones, tipo, "");
        }
        v.findViewById(R.id.btnAgregarOpcion).setOnClickListener(
                x -> agregarFilaOpcion(contenedorOpciones, tipo, ""));

        int titulo = esSeccion
                ? (existente == null ? R.string.dlg_nueva_seccion : R.string.dlg_editar_seccion)
                : (existente == null ? R.string.dlg_nueva_pregunta : R.string.dlg_editar_pregunta);
        AlertDialog.Builder b = new AlertDialog.Builder(requireContext())
                .setTitle(titulo)
                .setView(v)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.guardar, null);
        if (existente != null) {
            b.setNeutralButton(R.string.accion_eliminar, (d, w) -> Alertas.confirmar(requireContext(),
                    getString(esSeccion ? R.string.eliminar_seccion_titulo : R.string.eliminar_pregunta_titulo),
                    existente.getTexto(), getString(R.string.accion_eliminar), () -> {
                        f.getPreguntas().remove(existente);
                        adapter.notifyDataSetChanged();
                        guardar(f);
                    }));
        }
        AlertDialog dialogo = b.create();
        dialogo.setOnShowListener(d -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(x -> {
            // Validar
            String texto = Validar.texto(tilTexto);
            if (texto.isEmpty()) {
                tilTexto.setError(getString(R.string.error_pregunta_vacia));
                return;
            }
            List<String> opciones = leerOpciones(contenedorOpciones);
            if (tipo.usaOpciones() && opciones.size() < 2) {
                errorOpciones.setVisibility(View.VISIBLE);
                return;
            }

            // Guardar
            Pregunta p = existente != null ? existente : new Pregunta(tipo, texto);
            p.setTexto(texto);
            p.setDescripcion(Validar.texto(tilDescripcion));
            p.setObligatoria(!esSeccion && swObligatoria.isChecked());
            p.setEscalaMax(rb10.isChecked() ? 10 : 5);
            p.getOpciones().clear();
            if (tipo.usaOpciones()) p.getOpciones().addAll(opciones);

            if (existente == null) {
                f.getPreguntas().add(p);
                adapter.notifyItemInserted(f.getPreguntas().size() - 1);
                ((RecyclerView) requireView().findViewById(R.id.rvPreguntas))
                        .scrollToPosition(f.getPreguntas().size() - 1);
            } else {
                adapter.notifyDataSetChanged();
            }
            guardar(f);
            dialogo.dismiss();
        }));
        dialogo.show();
    }

    // Agrega una fila editable de opción (con botón para quitarla)
    private void agregarFilaOpcion(LinearLayout contenedor, TipoPregunta tipo, String texto) {
        View fila = LayoutInflater.from(requireContext()).inflate(R.layout.fila_opcion, contenedor, false);
        ((ImageView) fila.findViewById(R.id.imgMarca)).setImageResource(
                tipo == TipoPregunta.CASILLAS ? R.drawable.ic_casillas : R.drawable.ic_opcion_multiple);
        EditText et = fila.findViewById(R.id.etOpcion);
        et.setText(texto);
        et.setHint(getString(R.string.opcion_n, contenedor.getChildCount() + 1));
        fila.findViewById(R.id.btnQuitar).setOnClickListener(v -> contenedor.removeView(fila));
        contenedor.addView(fila);
        if (texto.isEmpty() && contenedor.getChildCount() > 2) et.requestFocus();
    }

    private List<String> leerOpciones(LinearLayout contenedor) {
        List<String> lista = new ArrayList<>();
        for (int i = 0; i < contenedor.getChildCount(); i++) {
            String t = ((EditText) contenedor.getChildAt(i).findViewById(R.id.etOpcion)).getText().toString().trim();
            if (!t.isEmpty() && !lista.contains(t)) lista.add(t);
        }
        return lista;
    }


    // TextWatcher corto: avisa el texto nuevo cada vez que cambia
    private static class Cambio implements TextWatcher {
        interface AlCambiar { void con(String texto); }

        private final AlCambiar alCambiar;

        Cambio(AlCambiar alCambiar) { this.alCambiar = alCambiar; }

        @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
        @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
        @Override public void afterTextChanged(Editable s) { alCambiar.con(s.toString()); }
    }
}
