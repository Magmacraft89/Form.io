package com.devcore.formio.graficas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.devcore.formio.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * Gráfica de barras horizontales (casillas y escalas): etiqueta, barra y porcentaje.
 */
public class GraficaBarras extends View {

    private final List<String> etiquetas = new ArrayList<>();
    private final List<Integer> valores = new ArrayList<>();
    private int base;   // total para calcular el porcentaje

    private final TextPaint texto = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint porcentaje = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fondoBarra = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barra = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int[] colores;

    public GraficaBarras(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        texto.setTextSize(Paleta.dp(context, 13));
        texto.setColor(ContextCompat.getColor(context, R.color.azul_noche));
        porcentaje.setTextSize(Paleta.dp(context, 13));
        porcentaje.setFakeBoldText(true);
        porcentaje.setTextAlign(Paint.Align.RIGHT);
        porcentaje.setColor(ContextCompat.getColor(context, R.color.azul_noche));
        fondoBarra.setColor(Paleta.alfa(ContextCompat.getColor(context, R.color.verde_azulado), 0x26));
        colores = Paleta.colores(context);
    }

    // base = número de respuestas (casillas) o suma (escala)
    public void setDatos(Map<String, Integer> datos, int base) {
        etiquetas.clear();
        valores.clear();
        for (Map.Entry<String, Integer> e : datos.entrySet()) {
            etiquetas.add(e.getKey());
            valores.add(e.getValue());
        }
        this.base = Math.max(1, base);
        requestLayout();
        invalidate();
    }

    private float altoFila() {
        return Paleta.dp(getContext(), 46);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int alto = (int) (altoFila() * etiquetas.size());
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), alto);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Context c = getContext();
        float ancho = getWidth();
        float grosor = Paleta.dp(c, 10);
        float radio = grosor / 2;
        for (int i = 0; i < etiquetas.size(); i++) {
            float y = i * altoFila();
            int pct = Math.round(valores.get(i) * 100f / base);
            String derecha = valores.get(i) + " · " + pct + "%";
            float anchoDerecha = porcentaje.measureText(derecha) + Paleta.dp(c, 8);
            CharSequence etiqueta = TextUtils.ellipsize(etiquetas.get(i), texto, ancho - anchoDerecha, TextUtils.TruncateAt.END);
            canvas.drawText(etiqueta, 0, etiqueta.length(), 0, y + Paleta.dp(c, 16), texto);
            canvas.drawText(derecha, ancho, y + Paleta.dp(c, 16), porcentaje);

            float top = y + Paleta.dp(c, 24);
            canvas.drawRoundRect(new RectF(0, top, ancho, top + grosor), radio, radio, fondoBarra);
            barra.setColor(colores[i % 3]);
            float largo = Math.max(grosor, ancho * pct / 100f);
            if (valores.get(i) > 0) canvas.drawRoundRect(new RectF(0, top, largo, top + grosor), radio, radio, barra);
        }
    }
}
