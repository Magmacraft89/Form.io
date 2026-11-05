package com.devcore.formio.vistas;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.devcore.formio.R;
import com.devcore.formio.datos.Repositorio;
import com.devcore.formio.util.Alertas;

import cn.pedant.SweetAlert.SweetAlertDialog;
import com.devcore.formio.modelos.Formulario;
import com.devcore.formio.modelos.Pregunta;
import com.devcore.formio.modelos.TipoPregunta;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/*
 * 5.3 FORMULARIO WEB PARA PARTICIPANTES (vista previa en la app)
 *
 * Muestra el formulario tal como lo verá quien escanee el QR: por secciones (3.3),
 * con preguntas obligatorias (3.2) y de forma anónima. Al enviarlo:
 *   - se guarda la respuesta con su tiempo de llenado (6.4),
 *   - cuenta la visita para la tasa de abandono (6.5),
 *   - respeta "una respuesta por dispositivo" (2.10), la fecha límite y el límite,
 *   - y Respuestas, Estadísticas y Dashboard se actualizan al instante.
 */
public class VistaPreviaFragment extends VistaBase {

    private final List<List<Pregunta>> paginas = new ArrayList<>();
    private final Map<String, List<String>> valores = new HashMap<>();
    private final Map<String, View> tarjetas = new HashMap<>();
    private int pagina;
    private long inicio;
    private boolean guardaRespuestas;   // false en borradores (solo vista previa)

    public static VistaPreviaFragment nueva(String idFormulario) {
        VistaPreviaFragment f = new VistaPreviaFragment();
        f.setArguments(argsCon(idFormulario));
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vista_previa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Formulario f = formulario();
        if (f == null) {
            main().regresar();
            return;
        }

        ((TextView) view.findViewById(R.id.txtTitulo)).setText(f.getTitulo());
        TextView desc = view.findViewById(R.id.txtDescripcion);
        desc.setText(f.getDescripcion());
        desc.setVisibility(f.getDescripcion().isEmpty() ? View.GONE : View.VISIBLE);
        armarPaginas(f);

        view.findViewById(R.id.btnAnterior).setOnClickListener(v -> {
            pagina--;
            pintarPagina();
        });
        view.findViewById(R.id.btnSiguiente).setOnClickListener(v -> siguiente(f));
        view.findViewById(R.id.btnOtra).setOnClickListener(v -> empezar(f));
        view.findViewById(R.id.btnVolverDetalle).setOnClickListener(v -> main().regresar());

        guardaRespuestas = f.isPublicado();
        view.findViewById(R.id.txtAvisoBorrador).setVisibility(guardaRespuestas ? View.GONE : View.VISIBLE);

        // ¿Puede responder?
        if (guardaRespuestas && !f.recibeRespuestas()) {
            mostrarFinal(R.drawable.ic_candado, getString(R.string.vp_cerrado), f.getMensajeLimite(), false);
        } else if (guardaRespuestas && f.isUnaPorDispositivo() && f.isEsteDispositivoRespondio()) {
            mostrarFinal(R.drawable.ic_check, getString(R.string.vp_ya_respondiste),
                    getString(R.string.vp_ya_respondiste_texto), false);
        } else {
            empezar(f);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.vp_titulo);
    }

    // Divide el formulario en páginas: cada sección empieza una página nueva
    private void armarPaginas(Formulario f) {
        paginas.clear();
        List<Pregunta> actual = new ArrayList<>();
        for (Pregunta p : f.getPreguntas()) {
            if (p.esSeccion() && !actual.isEmpty()) {
                paginas.add(actual);
                actual = new ArrayList<>();
            }
            actual.add(p);
        }
        if (!actual.isEmpty() || paginas.isEmpty()) paginas.add(actual);
    }

    // Abre el formulario desde cero (cuenta como una visita)
    private void empezar(Formulario f) {
        valores.clear();
        pagina = 0;
        inicio = System.currentTimeMillis();
        if (guardaRespuestas) repo().registrarVisita(f);
        requireView().findViewById(R.id.layoutFinal).setVisibility(View.GONE);
        requireView().findViewById(R.id.layoutFormulario).setVisibility(View.VISIBLE);
        pintarPagina();
    }


    // =========================================================
    // PINTAR UNA PÁGINA (SECCIÓN)
    // =========================================================

    private void pintarPagina() {
        View v = requireView();
        List<Pregunta> lista = paginas.get(pagina);
        LinearLayout contenedor = v.findViewById(R.id.contenedorPreguntas);
        contenedor.removeAllViews();
        tarjetas.clear();

        // Progreso por secciones
        boolean variasPaginas = paginas.size() > 1;
        v.findViewById(R.id.layoutProgreso).setVisibility(variasPaginas ? View.VISIBLE : View.GONE);
        ((TextView) v.findViewById(R.id.txtSeccionDe)).setText(getString(R.string.vp_seccion_de, pagina + 1, paginas.size()));
        ((ProgressBar) v.findViewById(R.id.progreso)).setProgress((pagina + 1) * 100 / paginas.size());

        // Título de la sección
        View layoutSeccion = v.findViewById(R.id.layoutSeccion);
        layoutSeccion.setVisibility(View.GONE);
        for (Pregunta p : lista) {
            if (p.esSeccion()) {
                layoutSeccion.setVisibility(View.VISIBLE);
                ((TextView) v.findViewById(R.id.txtSeccion)).setText(p.getTexto());
                TextView d = v.findViewById(R.id.txtSeccionDesc);
                d.setText(p.getDescripcion());
                d.setVisibility(p.getDescripcion().isEmpty() ? View.GONE : View.VISIBLE);
                continue;
            }
            contenedor.addView(crearPregunta(contenedor, p));
        }

        // Botones
        MaterialButton anterior = v.findViewById(R.id.btnAnterior);
        MaterialButton siguiente = v.findViewById(R.id.btnSiguiente);
        anterior.setVisibility(pagina > 0 ? View.VISIBLE : View.GONE);
        boolean ultima = pagina == paginas.size() - 1;
        siguiente.setText(ultima ? R.string.vp_enviar : R.string.siguiente);
        siguiente.setIconResource(ultima ? R.drawable.ic_enviar : R.drawable.ic_flecha_der);
        ((NestedScrollView) v.findViewById(R.id.scroll)).scrollTo(0, 0);
    }

    private View crearPregunta(ViewGroup padre, Pregunta p) {
        Context c = requireContext();
        View tarjeta = LayoutInflater.from(c).inflate(R.layout.vp_pregunta, padre, false);
        tarjetas.put(p.getId(), tarjeta);

        // Enunciado con asterisco si es obligatoria
        SpannableStringBuilder texto = new SpannableStringBuilder(p.getTexto());
        if (p.isObligatoria()) {
            texto.append(" *");
            texto.setSpan(new ForegroundColorSpan(ContextCompat.getColor(c, R.color.verde_azulado)),
                    texto.length() - 1, texto.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        ((TextView) tarjeta.findViewById(R.id.txtPregunta)).setText(texto);

        LinearLayout entrada = tarjeta.findViewById(R.id.contenedorEntrada);
        List<String> actual = valores.get(p.getId());
        switch (p.getTipo()) {
            case TEXTO_CORTO:
            case PARRAFO:
                entrada.addView(campoTexto(p, actual));
                break;
            case OPCION_MULTIPLE:
                entrada.addView(opcionMultiple(p, actual));
                break;
            case CASILLAS:
                for (View casilla : casillas(p, actual)) entrada.addView(casilla);
                break;
            case ESCALA:
                entrada.addView(escala(p, actual));
                break;
            case FECHA:
                entrada.addView(fecha(p, actual));
                break;
            default:
                break;
        }
        return tarjeta;
    }


    // =========================================================
    // ENTRADAS SEGÚN EL TIPO DE PREGUNTA
    // =========================================================

    private EditText campoTexto(Pregunta p, List<String> actual) {
        Context c = requireContext();
        EditText et = new EditText(c);
        et.setBackgroundResource(R.drawable.fondo_borde);
        int pad = dp(12);
        et.setPadding(pad, pad, pad, pad);
        et.setHint(R.string.vp_tu_respuesta);
        et.setTextColor(ContextCompat.getColor(c, R.color.azul_noche));
        et.setHintTextColor(ContextCompat.getColor(c, R.color.verde_azulado));
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        if (p.getTipo() == TipoPregunta.PARRAFO) {
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            et.setMinLines(3);
            et.setGravity(Gravity.TOP | Gravity.START);
        } else {
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            et.setSingleLine(true);
        }
        if (actual != null && !actual.isEmpty()) et.setText(actual.get(0));
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int x) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int x) { }
            @Override
            public void afterTextChanged(Editable s) {
                guardarValor(p, s.toString().trim().isEmpty() ? null : lista(s.toString().trim()));
            }
        });
        return et;
    }

    private RadioGroup opcionMultiple(Pregunta p, List<String> actual) {
        RadioGroup grupo = new RadioGroup(requireContext());
        for (String opcion : p.getOpciones()) {
            RadioButton rb = new RadioButton(requireContext());
            rb.setId(View.generateViewId());
            estiloOpcion(rb, opcion);
            grupo.addView(rb);
            if (actual != null && actual.contains(opcion)) rb.setChecked(true);
            rb.setOnCheckedChangeListener((b, marcado) -> {
                if (marcado) guardarValor(p, lista(opcion));
            });
        }
        return grupo;
    }

    private List<View> casillas(Pregunta p, List<String> actual) {
        List<View> vistas = new ArrayList<>();
        for (String opcion : p.getOpciones()) {
            CheckBox cb = new CheckBox(requireContext());
            estiloOpcion(cb, opcion);
            if (actual != null && actual.contains(opcion)) cb.setChecked(true);
            cb.setOnCheckedChangeListener((b, marcado) -> {
                List<String> elegidas = valores.containsKey(p.getId()) ? new ArrayList<>(valores.get(p.getId())) : new ArrayList<>();
                if (marcado && !elegidas.contains(opcion)) elegidas.add(opcion);
                if (!marcado) elegidas.remove(opcion);
                // conserva el orden de las opciones
                List<String> ordenadas = new ArrayList<>();
                for (String o : p.getOpciones()) if (elegidas.contains(o)) ordenadas.add(o);
                guardarValor(p, ordenadas.isEmpty() ? null : ordenadas);
            });
            vistas.add(cb);
        }
        return vistas;
    }

    private void estiloOpcion(android.widget.CompoundButton b, String texto) {
        Context c = requireContext();
        b.setText(texto);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        b.setTextColor(ContextCompat.getColor(c, R.color.azul_noche));
        b.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(c, R.color.verde_azulado)));
        b.setPadding(dp(4), dp(8), 0, dp(8));
    }

    // Escala 1-5 o 1-10: círculos numerados
    private LinearLayout escala(Pregunta p, List<String> actual) {
        Context c = requireContext();
        LinearLayout fila = new LinearLayout(c);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        List<TextView> botones = new ArrayList<>();
        int tam = p.getEscalaMax() > 5 ? dp(28) : dp(46);
        for (int i = 1; i <= p.getEscalaMax(); i++) {
            String valor = String.valueOf(i);
            TextView b = new TextView(c);
            b.setText(valor);
            b.setGravity(Gravity.CENTER);
            b.setTypeface(Typeface.DEFAULT_BOLD);
            b.setTextSize(TypedValue.COMPLEX_UNIT_SP, p.getEscalaMax() > 5 ? 13 : 16);
            b.setTextColor(ContextCompat.getColor(c, R.color.azul_noche));
            b.setBackgroundResource(R.drawable.fondo_escala);
            b.setSelected(actual != null && actual.contains(valor));
            // cada número va en una celda del mismo ancho; el círculo queda centrado
            android.widget.FrameLayout celda = new android.widget.FrameLayout(c);
            celda.addView(b, new android.widget.FrameLayout.LayoutParams(tam, tam, Gravity.CENTER));
            fila.addView(celda, new LinearLayout.LayoutParams(0, tam, 1f));
            botones.add(b);
            b.setOnClickListener(x -> {
                for (TextView otro : botones) otro.setSelected(otro == b);
                guardarValor(p, lista(valor));
            });
        }
        return fila;
    }

    private MaterialButton fecha(Pregunta p, List<String> actual) {
        MaterialButton b = (MaterialButton) LayoutInflater.from(requireContext())
                .inflate(R.layout.vp_boton_fecha, null, false);
        if (actual != null && !actual.isEmpty()) b.setText(actual.get(0));
        b.setOnClickListener(x -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (picker, anio, mes, dia) -> {
                cal.set(anio, mes, dia);
                String texto = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.getTime());
                b.setText(texto);
                guardarValor(p, lista(texto));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
        return b;
    }


    // =========================================================
    // VALIDAR Y ENVIAR
    // =========================================================

    private void guardarValor(Pregunta p, @Nullable List<String> valor) {
        if (valor == null) valores.remove(p.getId());
        else valores.put(p.getId(), valor);
        View t = tarjetas.get(p.getId());
        if (t != null && valor != null) t.findViewById(R.id.txtError).setVisibility(View.GONE);
    }

    // 3.2 Las obligatorias de la página deben tener respuesta
    private boolean paginaCompleta() {
        View primera = null;
        for (Pregunta p : paginas.get(pagina)) {
            if (!p.isObligatoria() || p.esSeccion()) continue;
            View t = tarjetas.get(p.getId());
            boolean falta = !valores.containsKey(p.getId());
            if (t != null) t.findViewById(R.id.txtError).setVisibility(falta ? View.VISIBLE : View.GONE);
            if (falta && primera == null) primera = t;
        }
        if (primera != null) {
            View destino = primera;
            NestedScrollView scroll = requireView().findViewById(R.id.scroll);
            scroll.post(() -> scroll.smoothScrollTo(0, destino.getTop()));
            return false;
        }
        return true;
    }

    private void siguiente(Formulario f) {
        if (!paginaCompleta()) return;
        if (pagina < paginas.size() - 1) {
            pagina++;
            pintarPagina();
            return;
        }
        // Enviar
        if (guardaRespuestas) {
            if (!f.recibeRespuestas()) {   // se cerró mientras respondía
                mostrarFinal(R.drawable.ic_candado, getString(R.string.vp_cerrado), f.getMensajeLimite(), false);
                return;
            }
            int segundos = (int) Math.max(3, (System.currentTimeMillis() - inicio) / 1000);
            SweetAlertDialog carga = Alertas.cargando(requireContext(), getString(R.string.enviando));
            repo().agregarRespuesta(f, new HashMap<>(valores), segundos, new Repositorio.Resultado() {
                @Override public void listo() {
                    if (!isAdded()) return;
                    Alertas.aExito(carga, getString(R.string.respuesta_enviada), null, null);
                    mostrarFinal(R.drawable.ic_check, getString(R.string.vp_gracias),
                            getString(R.string.vp_gracias_texto), !f.isUnaPorDispositivo() && f.recibeRespuestas());
                }
                @Override public void error(String mensaje) {
                    if (isAdded()) Alertas.aError(carga, getString(R.string.no_se_pudo_enviar), mensaje);
                }
            });
        } else {
            mostrarFinal(R.drawable.ic_check, getString(R.string.vp_gracias),
                    getString(R.string.vp_aviso_borrador), true);
        }
    }

    private void mostrarFinal(int icono, String titulo, String texto, boolean permitirOtra) {
        View v = requireView();
        v.findViewById(R.id.layoutFormulario).setVisibility(View.GONE);
        v.findViewById(R.id.layoutFinal).setVisibility(View.VISIBLE);
        ((ImageView) v.findViewById(R.id.imgFinal)).setImageResource(icono);
        ((TextView) v.findViewById(R.id.txtFinalTitulo)).setText(titulo);
        ((TextView) v.findViewById(R.id.txtFinalTexto)).setText(texto);
        v.findViewById(R.id.btnOtra).setVisibility(permitirOtra ? View.VISIBLE : View.GONE);
    }


    private static List<String> lista(String valor) {
        List<String> l = new ArrayList<>();
        l.add(valor);
        return l;
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}
