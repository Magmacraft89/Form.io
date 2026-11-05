package com.devcore.formio.vistas;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.devcore.formio.R;
import com.devcore.formio.util.Alertas;
import com.devcore.formio.modelos.Formulario;
import com.devcore.formio.util.Formato;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

/*
 * CONFIGURACIÓN DEL FORMULARIO
 * 2.6 Abrir y cerrar · 2.7 Fecha límite (DatePicker + TimePicker)
 * 2.8 Límite de respuestas · 2.10 Una respuesta por dispositivo
 */
public class ConfiguracionFragment extends VistaBase {

    private final Calendar fechaLimite = Calendar.getInstance();

    public static ConfiguracionFragment nueva(String idFormulario) {
        ConfiguracionFragment f = new ConfiguracionFragment();
        f.setArguments(argsCon(idFormulario));
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configuracion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Formulario f = formulario();
        if (f == null) {
            main().regresar();
            return;
        }
        ((TextView) view.findViewById(R.id.txtFormTitulo)).setText(f.getTitulo());

        CompoundButton swAbierto = view.findViewById(R.id.swAbierto);
        CompoundButton swFecha = view.findViewById(R.id.swFecha);
        CompoundButton swLimite = view.findViewById(R.id.swLimite);
        CompoundButton swDispositivo = view.findViewById(R.id.swDispositivo);
        View layoutFecha = view.findViewById(R.id.layoutFecha);
        View layoutLimite = view.findViewById(R.id.layoutLimite);
        TextInputLayout tilCapacidad = view.findViewById(R.id.tilCapacidad);
        TextInputLayout tilMensaje = view.findViewById(R.id.tilMensaje);

        // Valores actuales
        swAbierto.setChecked(f.isAbierto());
        swFecha.setChecked(f.getFechaLimite() > 0);
        if (f.getFechaLimite() > 0) {
            fechaLimite.setTimeInMillis(f.getFechaLimite());
        } else {
            fechaLimite.add(Calendar.DAY_OF_YEAR, 7);   // por defecto: dentro de una semana
            fechaLimite.set(Calendar.HOUR_OF_DAY, 23);
            fechaLimite.set(Calendar.MINUTE, 59);
        }
        swLimite.setChecked(f.getLimiteRespuestas() > 0);
        if (f.getLimiteRespuestas() > 0) tilCapacidad.getEditText().setText(String.valueOf(f.getLimiteRespuestas()));
        tilMensaje.getEditText().setText(f.getMensajeLimite());
        swDispositivo.setChecked(f.isUnaPorDispositivo());

        layoutFecha.setVisibility(swFecha.isChecked() ? View.VISIBLE : View.GONE);
        layoutLimite.setVisibility(swLimite.isChecked() ? View.VISIBLE : View.GONE);
        swFecha.setOnCheckedChangeListener((b, on) -> layoutFecha.setVisibility(on ? View.VISIBLE : View.GONE));
        swLimite.setOnCheckedChangeListener((b, on) -> layoutLimite.setVisibility(on ? View.VISIBLE : View.GONE));
        pintarFecha(view);

        // 2.7 Selector de fecha y de hora
        view.findViewById(R.id.btnFecha).setOnClickListener(v -> new DatePickerDialog(requireContext(),
                (picker, anio, mes, dia) -> {
                    fechaLimite.set(anio, mes, dia);
                    pintarFecha(view);
                },
                fechaLimite.get(Calendar.YEAR), fechaLimite.get(Calendar.MONTH),
                fechaLimite.get(Calendar.DAY_OF_MONTH)).show());

        view.findViewById(R.id.btnHora).setOnClickListener(v -> new TimePickerDialog(requireContext(),
                (picker, hora, minuto) -> {
                    fechaLimite.set(Calendar.HOUR_OF_DAY, hora);
                    fechaLimite.set(Calendar.MINUTE, minuto);
                    pintarFecha(view);
                },
                fechaLimite.get(Calendar.HOUR_OF_DAY), fechaLimite.get(Calendar.MINUTE), false).show());

        // Guardar
        view.findViewById(R.id.btnGuardar).setOnClickListener(v -> {
            int capacidad = 0;
            if (swLimite.isChecked()) {
                try {
                    capacidad = Integer.parseInt(Validar.texto(tilCapacidad));
                } catch (NumberFormatException ignored) { }
                if (capacidad <= 0) {
                    tilCapacidad.setError(getString(R.string.error_capacidad));
                    return;
                }
                tilCapacidad.setError(null);
            }
            f.setAbierto(swAbierto.isChecked());
            f.setFechaLimite(swFecha.isChecked() ? fechaLimite.getTimeInMillis() : 0);
            f.setLimiteRespuestas(capacidad);
            String mensaje = Validar.texto(tilMensaje);
            if (!mensaje.isEmpty()) f.setMensajeLimite(mensaje);
            f.setUnaPorDispositivo(swDispositivo.isChecked());
            repo().guardar(f);
            Alertas.exito(requireContext(), getString(R.string.msg_config_guardada), null,
                    () -> { if (isAdded()) main().regresar(); });
        });
    }

    private void pintarFecha(View v) {
        ((MaterialButton) v.findViewById(R.id.btnFecha)).setText(Formato.fecha(fechaLimite.getTimeInMillis()));
        ((MaterialButton) v.findViewById(R.id.btnHora)).setText(Formato.hora(fechaLimite.getTimeInMillis()));
        long falta = fechaLimite.getTimeInMillis() - System.currentTimeMillis();
        ((TextView) v.findViewById(R.id.txtVence)).setText(falta > 0
                ? getString(R.string.cierra_en, Formato.tiempoRestante(falta))
                : getString(R.string.cerrado_por_fecha));
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.config_titulo);
    }
}
