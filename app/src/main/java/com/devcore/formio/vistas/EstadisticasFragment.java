package com.devcore.formio.vistas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.devcore.formio.R;
import com.devcore.formio.datos.Estadisticas;
import com.devcore.formio.datos.Repositorio;
import com.devcore.formio.graficas.GraficaBarras;
import com.devcore.formio.graficas.GraficaDona;
import com.devcore.formio.graficas.GraficaLinea;
import com.devcore.formio.modelos.Formulario;
import com.devcore.formio.modelos.Pregunta;
import com.devcore.formio.util.Formato;

import java.util.List;
import java.util.Locale;

/*
 * 6. ESTADÍSTICAS POR FORMULARIO (en tiempo real)
 * 6.1 Total · 6.2 Gráficas por pregunta (dona, barras, promedio y textos)
 * 6.3 Respuestas por día · 6.4 Tiempo promedio · 6.5 Tasa de abandono
 */
public class EstadisticasFragment extends VistaBase {

    private static final int DIAS = 7;
    private static final int MAX_TEXTOS = 5;

    public static EstadisticasFragment nueva(String idFormulario) {
        EstadisticasFragment f = new EstadisticasFragment();
        f.setArguments(argsCon(idFormulario));
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_estadisticas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (formulario() == null) {
            main().regresar();
            return;
        }
        refrescar();
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.estadisticas_titulo);
    }

    @Override
    protected void refrescar() {
        Formulario f = formulario();
        if (f == null) return;
        View v = requireView();
        int total = Estadisticas.total(f);

        ((TextView) v.findViewById(R.id.txtFormTitulo)).setText(f.getTitulo());

        // 6.1 Total
        ((TextView) v.findViewById(R.id.txtTotal)).setText(String.valueOf(total));
        ((TextView) v.findViewById(R.id.txtHoy)).setText(getString(R.string.est_hoy, Estadisticas.deHoy(f)));

        // 6.4 Tiempo promedio de llenado
        ((TextView) v.findViewById(R.id.txtTiempo)).setText(total == 0 ? "—" : Formato.duracion(Estadisticas.tiempoPromedio(f)));
        ((TextView) v.findViewById(R.id.txtTiempoRango)).setText(total == 0 ? "" : getString(R.string.est_tiempo_rango,
                Formato.duracion(Estadisticas.tiempoMinimo(f)), Formato.duracion(Estadisticas.tiempoMaximo(f))));

        // 6.5 Tasa de abandono (visitas vs. completadas)
        int visitas = Math.max(f.getVisitas(), total);
        int abandono = Estadisticas.tasaAbandono(f);
        ((TextView) v.findViewById(R.id.txtAbandono)).setText(abandono + "%");
        ((TextView) v.findViewById(R.id.txtVisitas)).setText(getString(R.string.est_visitas_vs, visitas, total));
        ((ProgressBar) v.findViewById(R.id.progresoCompletadas)).setProgress(visitas == 0 ? 0 : 100 - abandono);

        // 6.3 Respuestas por día (últimos 7 días)
        int[] porDia = Estadisticas.porDia(f, DIAS);
        String[] etiquetas = new String[DIAS];
        for (int i = 0; i < DIAS; i++) {
            etiquetas[i] = i == DIAS - 1 ? "Hoy" : Formato.diaSemana(Repositorio.rangoDia(DIAS - 1 - i)[0]);
        }
        ((GraficaLinea) v.findViewById(R.id.graficaDias)).setDatos(porDia, etiquetas);

        // 6.2 Gráficas por pregunta
        LinearLayout contenedor = v.findViewById(R.id.contenedorPreguntas);
        contenedor.removeAllViews();
        v.findViewById(R.id.txtSinDatos).setVisibility(total == 0 ? View.VISIBLE : View.GONE);
        if (total == 0) return;

        LayoutInflater inf = LayoutInflater.from(requireContext());
        int numero = 0;
        for (Pregunta p : f.getPreguntas()) {
            if (p.esSeccion()) continue;
            numero++;
            View tarjeta = inf.inflate(R.layout.item_estadistica_pregunta, contenedor, false);
            ((TextView) tarjeta.findViewById(R.id.txtNumero)).setText(String.valueOf(numero));
            ((TextView) tarjeta.findViewById(R.id.txtPregunta)).setText(p.getTexto());
            int contestaron = Estadisticas.contestaron(f, p);
            ((TextView) tarjeta.findViewById(R.id.txtDetalle)).setText(
                    p.getTipo().getNombre() + " · " + getString(R.string.est_contestaron, contestaron, total));
            FrameLayout zona = tarjeta.findViewById(R.id.contenedorGrafica);
            TextView destacado = tarjeta.findViewById(R.id.txtDestacado);

            switch (p.getTipo()) {
                case OPCION_MULTIPLE: {
                    GraficaDona dona = new GraficaDona(requireContext(), null);
                    dona.setDatos(Estadisticas.conteoOpciones(f, p));
                    zona.addView(dona);
                    break;
                }
                case CASILLAS: {
                    GraficaBarras barras = new GraficaBarras(requireContext(), null);
                    barras.setDatos(Estadisticas.conteoOpciones(f, p), contestaron);
                    zona.addView(barras);
                    break;
                }
                case ESCALA: {
                    destacado.setText(getString(R.string.est_promedio,
                            String.format(Locale.US, "%.1f", Estadisticas.promedioEscala(f, p)), p.getEscalaMax()));
                    destacado.setVisibility(View.VISIBLE);
                    GraficaBarras barras = new GraficaBarras(requireContext(), null);
                    barras.setDatos(Estadisticas.distribucionEscala(f, p), contestaron);
                    zona.addView(barras);
                    break;
                }
                default: {
                    // Texto y fecha: últimas respuestas escritas
                    zona.addView(listaTextos(inf, Estadisticas.textos(f, p)));
                    break;
                }
            }
            contenedor.addView(tarjeta);
        }
    }

    private View listaTextos(LayoutInflater inf, List<String> textos) {
        LinearLayout lista = new LinearLayout(requireContext());
        lista.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < Math.min(MAX_TEXTOS, textos.size()); i++) {
            TextView t = (TextView) inf.inflate(R.layout.item_texto_respuesta, lista, false);
            t.setText(textos.get(i));
            lista.addView(t);
        }
        if (textos.size() > MAX_TEXTOS) {
            TextView mas = new TextView(requireContext());
            mas.setText(getString(R.string.est_mas, textos.size() - MAX_TEXTOS));
            mas.setTextColor(requireContext().getColor(R.color.verde_azulado));
            lista.addView(mas);
        }
        return lista;
    }
}
