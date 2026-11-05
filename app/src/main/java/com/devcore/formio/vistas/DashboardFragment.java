package com.devcore.formio.vistas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devcore.formio.R;
import com.devcore.formio.util.Alertas;

import cn.pedant.SweetAlert.SweetAlertDialog;
import com.devcore.formio.adaptadores.FormularioMiniAdapter;
import com.devcore.formio.adaptadores.PorVencerAdapter;
import com.devcore.formio.datos.Repositorio;
import com.devcore.formio.graficas.GraficaLinea;
import com.devcore.formio.modelos.EstadoFormulario;
import com.devcore.formio.modelos.Formulario;
import com.devcore.formio.util.Formato;

import java.util.List;

/*
 * 7. DASHBOARD
 * 7.1 Respuestas de hoy (con % respecto a ayer) · 7.2 Formularios activos · 7.3 Por vencer
 * Se actualiza solo cuando llega una respuesta (tiempo real).
 */
public class DashboardFragment extends VistaBase {

    private PorVencerAdapter porVencerAdapter;
    private FormularioMiniAdapter activosAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvPorVencer = view.findViewById(R.id.rvPorVencer);
        rvPorVencer.setLayoutManager(new LinearLayoutManager(requireContext()));
        porVencerAdapter = new PorVencerAdapter(f -> main().abrirDetalle(f.getId()));
        rvPorVencer.setAdapter(porVencerAdapter);

        RecyclerView rvActivos = view.findViewById(R.id.rvActivos);
        rvActivos.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        activosAdapter = new FormularioMiniAdapter(f -> main().abrirDetalle(f.getId()));
        rvActivos.setAdapter(activosAdapter);

        view.findViewById(R.id.fabCrear).setOnClickListener(v -> main().navegarA(R.id.nav_nuevo));
        view.findViewById(R.id.btnVerTodos).setOnClickListener(v -> main().navegarA(R.id.nav_formularios));
        view.findViewById(R.id.cardActivos).setOnClickListener(v -> main().navegarA(R.id.nav_formularios));
        view.findViewById(R.id.cardTotal).setOnClickListener(v -> main().navegarA(R.id.nav_formularios));
        view.findViewById(R.id.cardPorVencer).setOnClickListener(v -> main().navegarA(R.id.nav_formularios));
        view.findViewById(R.id.btnEmpezarCrear).setOnClickListener(v -> main().navegarA(R.id.nav_nuevo));
        view.findViewById(R.id.btnCargarEjemplos).setOnClickListener(v -> cargarEjemplos());

        refrescar();
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.menu_dashboard);
    }

    @Override
    protected void refrescar() {
        View v = requireView();
        Repositorio repo = repo();

        ((TextView) v.findViewById(R.id.txtSaludo)).setText(
                getString(R.string.dash_saludo, repo.getAdmin().getPrimerNombre()));

        // 7.1 Respuestas de hoy vs. ayer
        int hoy = repo.respuestasDelDia(0);
        int ayer = repo.respuestasDelDia(1);
        ((TextView) v.findViewById(R.id.txtHoy)).setText(String.valueOf(hoy));
        TextView vsAyer = v.findViewById(R.id.txtVsAyer);
        if (ayer == 0 || hoy == ayer) {
            vsAyer.setText(R.string.dash_vs_ayer_igual);
        } else if (hoy > ayer) {
            vsAyer.setText(getString(R.string.dash_vs_ayer_sube, Math.round((hoy - ayer) * 100f / ayer)));
        } else {
            vsAyer.setText(getString(R.string.dash_vs_ayer_baja, Math.round((ayer - hoy) * 100f / ayer)));
        }

        // 7.2 Activos y total
        List<Formulario> activos = repo.getPorEstado(EstadoFormulario.ACTIVO);
        ((TextView) v.findViewById(R.id.txtActivos)).setText(String.valueOf(activos.size()));
        ((TextView) v.findViewById(R.id.txtTotal)).setText(String.valueOf(repo.totalRespuestas()));
        activosAdapter.setLista(activos);

        // Gráfica de la semana
        int[] semana = repo.respuestasSemana();
        String[] dias = new String[7];
        for (int i = 0; i < 7; i++) {
            dias[i] = i == 6 ? "Hoy" : Formato.diaSemana(Repositorio.rangoDia(6 - i)[0]);
        }
        ((GraficaLinea) v.findViewById(R.id.graficaSemana)).setDatos(semana, dias);

        // 7.3 Por vencer
        List<Formulario> porVencer = repo.getPorVencer();
        porVencerAdapter.setLista(porVencer);
        ((TextView) v.findViewById(R.id.txtPorVencerNum)).setText(String.valueOf(porVencer.size()));
        v.findViewById(R.id.txtSinPorVencer).setVisibility(porVencer.isEmpty() ? View.VISIBLE : View.GONE);

        // Cuenta nueva: invitar a crear el primer formulario o cargar ejemplos
        boolean vacio = repo.isFormulariosCargados() && repo.getFormularios().isEmpty() && repo.getArchivados().isEmpty();
        v.findViewById(R.id.cardEmpezar).setVisibility(vacio ? View.VISIBLE : View.GONE);
    }

    // Sube a Firestore unos formularios con respuestas para probar la app
    private void cargarEjemplos() {
        Alertas.confirmar(requireContext(), getString(R.string.ejemplos_titulo), getString(R.string.ejemplos_texto),
                getString(R.string.cargar), () -> {
                    SweetAlertDialog carga = Alertas.cargando(requireContext(), getString(R.string.cargando_ejemplos));
                    repo().crearEjemplos(new Repositorio.Resultado() {
                        @Override public void listo() {
                            if (isAdded()) Alertas.aExito(carga, getString(R.string.ejemplos_listos), getString(R.string.ejemplos_listos_texto), null);
                        }
                        @Override public void error(String mensaje) {
                            if (isAdded()) Alertas.aError(carga, getString(R.string.algo_salio_mal), mensaje);
                        }
                    });
                });
    }
}
