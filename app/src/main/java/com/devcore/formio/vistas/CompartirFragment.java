package com.devcore.formio.vistas;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.devcore.formio.QrPantallaCompletaActivity;
import com.devcore.formio.R;
import com.devcore.formio.modelos.EstadoFormulario;
import com.devcore.formio.modelos.Formulario;
import com.devcore.formio.util.Compartir;
import com.devcore.formio.util.GeneradorQr;

/*
 * 4. QR Y COMPARTIR
 * 4.1 QR generado al publicar · 4.2 Pantalla completa · 4.3 Descargar (PNG/JPG)
 * 4.4 Enlace corto + copiar · 4.5 Enviar por WhatsApp o correo
 */
public class CompartirFragment extends VistaBase {

    private static final String ARG_PUBLICADO = "recien_publicado";
    private Bitmap qr;

    public static CompartirFragment nueva(String idFormulario, boolean recienPublicado) {
        CompartirFragment f = new CompartirFragment();
        Bundle b = argsCon(idFormulario);
        b.putBoolean(ARG_PUBLICADO, recienPublicado);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compartir, container, false);
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
        view.findViewById(R.id.txtPublicado).setVisibility(
                requireArguments().getBoolean(ARG_PUBLICADO) ? View.VISIBLE : View.GONE);

        // 4.1 Generar el QR con el enlace del formulario
        qr = GeneradorQr.generar(requireContext(), f.getEnlace(), 720);
        ((ImageView) view.findViewById(R.id.imgQr)).setImageBitmap(qr);
        ((TextView) view.findViewById(R.id.txtEnlace)).setText(f.getEnlaceCorto());

        // 4.2 Pantalla completa
        view.findViewById(R.id.btnPantallaCompleta).setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), QrPantallaCompletaActivity.class);
            i.putExtra(QrPantallaCompletaActivity.EXTRA_FORMULARIO, f.getId());
            startActivity(i);
        });

        // 4.3 Descargar en PNG o JPG
        CompoundButton chipPng = view.findViewById(R.id.chipPng);
        view.findViewById(R.id.btnDescargar).setOnClickListener(v ->
                Compartir.descargarQr(requireContext(), f, qr, chipPng.isChecked()));

        // 4.4 Copiar enlace · 4.5 Enviar
        view.findViewById(R.id.btnCopiar).setOnClickListener(v -> Compartir.copiarEnlace(requireContext(), f));
        view.findViewById(R.id.btnWhatsApp).setOnClickListener(v -> Compartir.porWhatsApp(requireContext(), f));
        view.findViewById(R.id.btnCorreo).setOnClickListener(v -> Compartir.porCorreo(requireContext(), f));
        view.findViewById(R.id.btnOtros).setOnClickListener(v -> Compartir.otros(requireContext(), f));

        refrescar();
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.compartir_titulo_vista);
    }

    @Override
    protected void refrescar() {
        Formulario f = formulario();
        if (f == null) return;
        View v = requireView();
        boolean borrador = f.getEstado() == EstadoFormulario.BORRADOR;
        TextView aviso = v.findViewById(R.id.txtAviso);

        // Un borrador todavía no tiene QR: hay que publicarlo
        v.findViewById(R.id.marcoQr).setVisibility(borrador ? View.GONE : View.VISIBLE);
        v.findViewById(R.id.txtInstruccion).setVisibility(borrador ? View.GONE : View.VISIBLE);
        v.findViewById(R.id.layoutAccionesQr).setVisibility(borrador ? View.GONE : View.VISIBLE);
        v.findViewById(R.id.layoutCompartir).setVisibility(borrador ? View.GONE : View.VISIBLE);
        if (borrador) {
            aviso.setText(R.string.qr_borrador);
            aviso.setVisibility(View.VISIBLE);
        } else if (f.getEstado() == EstadoFormulario.CERRADO) {
            aviso.setText(R.string.qr_cerrado_aviso);
            aviso.setVisibility(View.VISIBLE);
        } else {
            aviso.setVisibility(View.GONE);
        }
    }
}
