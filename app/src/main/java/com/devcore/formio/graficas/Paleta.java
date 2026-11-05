package com.devcore.formio.graficas;

import android.content.Context;
import android.graphics.Color;

import androidx.core.content.ContextCompat;

import com.devcore.formio.R;

/*
 * Colores de las gráficas: los 3 colores de la paleta y sus tonos más claros.
 */
final class Paleta {

    private Paleta() { }

    static int[] colores(Context c) {
        int azul = ContextCompat.getColor(c, R.color.azul_noche);
        int verdeAzulado = ContextCompat.getColor(c, R.color.verde_azulado);
        int menta = ContextCompat.getColor(c, R.color.verde_menta);
        return new int[]{verdeAzulado, menta, azul, tono(verdeAzulado), tono(menta), tono(azul)};
    }

    // El mismo color, un poco transparente
    static int tono(int color) {
        return Color.argb(140, Color.red(color), Color.green(color), Color.blue(color));
    }

    // El mismo color con la transparencia indicada (0 = invisible, 255 = sólido)
    static int alfa(int color, int alfa) {
        return Color.argb(alfa, Color.red(color), Color.green(color), Color.blue(color));
    }

    static float dp(Context c, float valor) {
        return valor * c.getResources().getDisplayMetrics().density;
    }
}
