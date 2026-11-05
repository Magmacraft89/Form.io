package com.devcore.formio;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.devcore.formio.datos.Repositorio;
import com.devcore.formio.modelos.Formulario;
import com.devcore.formio.util.GeneradorQr;

/*
 * 4.2 PANTALLA COMPLETA PARA ESCANEAR
 * Muestra el QR en grande, con el brillo al máximo y la pantalla siempre encendida,
 * para que los participantes lo escaneen desde el teléfono del administrador.
 */
public class QrPantallaCompletaActivity extends AppCompatActivity {

    public static final String EXTRA_FORMULARIO = "formulario";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Dibuja detrás de las barras del sistema: íconos claros arriba (fondo azul noche)
        // y claros abajo (fondo azul noche)
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_qr_pantalla_completa);

        Formulario f = Repositorio.get().buscar(getIntent().getStringExtra(EXTRA_FORMULARIO));
        if (f == null) {
            finish();
            return;
        }

        // Brillo al máximo y pantalla siempre encendida
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        getWindow().setAttributes(lp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        View raiz = findViewById(R.id.raiz);
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        ((TextView) findViewById(R.id.txtTitulo)).setText(f.getTitulo());
        ((TextView) findViewById(R.id.txtEnlace)).setText(f.getEnlaceCorto());
        Bitmap qr = GeneradorQr.generar(this, f.getEnlace(), 900);
        ((ImageView) findViewById(R.id.imgQr)).setImageBitmap(qr);

        findViewById(R.id.btnCerrar).setOnClickListener(v -> finish());
    }
}
