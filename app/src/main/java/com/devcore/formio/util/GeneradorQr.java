package com.devcore.formio.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.core.content.ContextCompat;

import com.devcore.formio.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.EnumMap;
import java.util.Map;

/*
 * Genera el código QR de un formulario (módulo 4.1) con la librería ZXing.
 * El QR lleva el enlace del formulario y el logo de Form.io en el centro.
 */
public final class GeneradorQr {

    private GeneradorQr() { }

    public static Bitmap generar(Context context, String texto, int tamano) {
        try {
            Map<EncodeHintType, Object> opciones = new EnumMap<>(EncodeHintType.class);
            // Corrección de errores alta: el QR se sigue leyendo aunque el logo tape el centro
            opciones.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            opciones.put(EncodeHintType.MARGIN, 1);
            opciones.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix matriz = new QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, tamano, tamano, opciones);

            int oscuro = ContextCompat.getColor(context, R.color.azul_noche);
            int[] pixeles = new int[tamano * tamano];
            for (int y = 0; y < tamano; y++) {
                for (int x = 0; x < tamano; x++) {
                    pixeles[y * tamano + x] = matriz.get(x, y) ? oscuro : Color.WHITE;
                }
            }
            Bitmap qr = Bitmap.createBitmap(tamano, tamano, Bitmap.Config.ARGB_8888);
            qr.setPixels(pixeles, 0, tamano, 0, 0, tamano, tamano);
            ponerLogo(context, qr);
            return qr;
        } catch (WriterException e) {
            return null;
        }
    }

    // Logo de la app en el centro, sobre un recuadro blanco
    private static void ponerLogo(Context context, Bitmap qr) {
        Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_formio);
        if (logo == null) return;
        int tam = qr.getWidth();
        float lado = tam * 0.22f;
        float borde = tam * 0.025f;
        float inicio = (tam - lado) / 2f;

        Canvas canvas = new Canvas(qr);
        Paint blanco = new Paint(Paint.ANTI_ALIAS_FLAG);
        blanco.setColor(Color.WHITE);
        canvas.drawRoundRect(new RectF(inicio - borde, inicio - borde, inicio + lado + borde, inicio + lado + borde),
                lado * 0.25f, lado * 0.25f, blanco);
        Paint suave = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(logo, null, new RectF(inicio, inicio, inicio + lado, inicio + lado), suave);
    }
}
