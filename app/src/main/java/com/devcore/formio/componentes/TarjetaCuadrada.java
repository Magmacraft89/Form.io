package com.devcore.formio.componentes;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;

/*
 * Tarjeta (CardView de Material) que siempre es cuadrada:
 * su alto es igual a su ancho. Se usa en los accesos del Dashboard y del Detalle.
 */
public class TarjetaCuadrada extends MaterialCardView {

    public TarjetaCuadrada(Context context) {
        super(context);
    }

    public TarjetaCuadrada(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TarjetaCuadrada(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // El alto toma la misma medida que el ancho
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
