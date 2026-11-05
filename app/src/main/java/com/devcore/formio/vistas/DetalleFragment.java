package com.devcore.formio.vistas;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.devcore.formio.R;
import com.devcore.formio.datos.Repositorio;
import com.devcore.formio.util.Alertas;

import cn.pedant.SweetAlert.SweetAlertDialog;
import com.devcore.formio.datos.Estadisticas;
import com.devcore.formio.modelos.EstadoFormulario;
import com.devcore.formio.modelos.Formulario;
import com.devcore.formio.util.Formato;

/*
 * DETALLE DEL FORMULARIO: centro de control de un formulario.
 * Estado, 2.6 Abrir/cerrar, 4.1 Publicar y acceso a Editar, QR, Respuestas,
 * Estadísticas, Configuración y Vista previa. También Duplicar, Archivar y Eliminar.
 */
public class DetalleFragment extends VistaBase {

    private CompoundButton swAbierto;
    private boolean pintando;   // evita que el switch dispare cambios mientras se pinta

    public static DetalleFragment nueva(String idFormulario) {
        DetalleFragment f = new DetalleFragment();
        f.setArguments(argsCon(idFormulario));
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Formulario f = formulario();
        if (f == null) {
            main().regresar();
            return;
        }
        String id = f.getId();

        swAbierto = view.findViewById(R.id.swAbierto);
        swAbierto.setOnCheckedChangeListener((b, activo) -> {
            if (!pintando) repo().setAbierto(f, activo);
        });

        view.findViewById(R.id.btnPublicar).setOnClickListener(v -> publicar(f));
        view.findViewById(R.id.btnGuardarPlantilla).setOnClickListener(v -> Acciones.guardarComoPlantilla(this, f));

        view.findViewById(R.id.tileEditar).setOnClickListener(v -> main().abrirEditor(id));
        view.findViewById(R.id.tileQr).setOnClickListener(v -> main().abrirCompartir(id, false));
        view.findViewById(R.id.tileRespuestas).setOnClickListener(v -> main().abrirRespuestas(id));
        view.findViewById(R.id.tileEstadisticas).setOnClickListener(v -> main().abrirEstadisticas(id));
        view.findViewById(R.id.tileConfig).setOnClickListener(v -> main().abrirConfiguracion(id));
        view.findViewById(R.id.tileVista).setOnClickListener(v -> main().abrirVistaPrevia(id));

        view.findViewById(R.id.btnDuplicar).setOnClickListener(v -> Acciones.duplicar(this, f));
        view.findViewById(R.id.btnArchivar).setOnClickListener(v -> Acciones.archivar(this, f, !f.isArchivado()));
        view.findViewById(R.id.btnEliminar).setOnClickListener(v -> Acciones.eliminar(this, f, () -> main().regresar()));

        refrescar();
    }

    // 4.1 Publicar: confirma, sube a Firestore y abre el QR
    private void publicar(Formulario f) {
        if (f.getPreguntas().isEmpty()) {
            Alertas.error(requireContext(), getString(R.string.publicar_sin_preguntas_titulo),
                    getString(R.string.publicar_sin_preguntas));
            return;
        }
        Alertas.confirmar(requireContext(), getString(R.string.publicar_titulo),
                getString(R.string.publicar_texto), getString(R.string.publicar), () -> {
                    SweetAlertDialog carga = Alertas.cargando(requireContext(), getString(R.string.publicando));
                    repo().publicar(f, new Repositorio.Resultado() {
                        @Override public void listo() {
                            if (!isAdded()) return;
                            Alertas.aExito(carga, getString(R.string.msg_publicado), getString(R.string.publicado_texto),
                                    () -> { if (isAdded()) main().abrirCompartir(f.getId(), true); });
                        }
                        @Override public void error(String mensaje) {
                            if (isAdded()) Alertas.aError(carga, getString(R.string.algo_salio_mal), mensaje);
                        }
                    });
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.detalle_titulo);
    }

    @Override
    protected void refrescar() {
        Formulario f = formulario();
        if (f == null) return;   // se eliminó
        View v = requireView();
        EstadoFormulario e = f.getEstado();

        // Cabecera
        TextView estado = v.findViewById(R.id.txtEstado);
        estado.setText(f.isArchivado() ? e.getNombre() + " · Archivado" : e.getNombre());
        estado.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                e == EstadoFormulario.CERRADO ? R.color.blanco : e.getColorFondo())));
        estado.setTextColor(ContextCompat.getColor(requireContext(),
                e == EstadoFormulario.CERRADO ? R.color.azul_noche : e.getColorTexto()));
        ((TextView) v.findViewById(R.id.txtTitulo)).setText(f.getTitulo());
        TextView desc = v.findViewById(R.id.txtDescripcion);
        desc.setText(f.getDescripcion());
        desc.setVisibility(f.getDescripcion().isEmpty() ? View.GONE : View.VISIBLE);
        ((TextView) v.findViewById(R.id.txtCreado)).setText(
                getString(R.string.creado_el, Formato.fecha(f.getFechaCreacion()), f.getCantidadPreguntas()));

        // 2.6 Abrir / cerrar (solo si ya está publicado)
        boolean publicado = f.isPublicado();
        v.findViewById(R.id.filaAbierto).setVisibility(publicado ? View.VISIBLE : View.GONE);
        v.findViewById(R.id.cardPublicar).setVisibility(publicado ? View.GONE : View.VISIBLE);
        pintando = true;
        swAbierto.setChecked(f.isAbierto());
        pintando = false;
        boolean recibe = f.recibeRespuestas();
        ((TextView) v.findViewById(R.id.txtRecibiendo)).setText(recibe ? R.string.recibiendo : R.string.no_recibiendo);
        v.findViewById(R.id.puntoEstado).setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), recibe ? R.color.verde_menta : R.color.azul_noche)));
        TextView motivo = v.findViewById(R.id.txtMotivo);
        if (publicado && !recibe) {
            int m = !f.isAbierto() ? R.string.cerrado_manual : f.vencio() ? R.string.cerrado_por_fecha : R.string.cerrado_por_limite;
            motivo.setText(m);
            motivo.setVisibility(View.VISIBLE);
        } else {
            motivo.setVisibility(View.GONE);
        }

        // Métricas rápidas
        ((TextView) v.findViewById(R.id.txtMetRespuestas)).setText(String.valueOf(Estadisticas.total(f)));
        ((TextView) v.findViewById(R.id.txtMetVisitas)).setText(String.valueOf(Math.max(f.getVisitas(), Estadisticas.total(f))));
        ((TextView) v.findViewById(R.id.txtMetTiempo)).setText(
                Estadisticas.total(f) == 0 ? "—" : Formato.duracion(Estadisticas.tiempoPromedio(f)));
        ((TextView) v.findViewById(R.id.txtTileRespuestas)).setText(
                getString(R.string.tile_respuestas_desc, Estadisticas.total(f)));

        ((TextView) v.findViewById(R.id.btnArchivar)).setText(f.isArchivado() ? R.string.accion_restaurar : R.string.accion_archivar);
    }
}
