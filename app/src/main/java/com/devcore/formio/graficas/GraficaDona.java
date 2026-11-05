package com.devcore.formio.graficas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.devcore.formio.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * Gráfica de dona (pastel con hueco) para opción múltiple, con su leyenda a la derecha.
 */
public class GraficaDona extends View {

    private final List<String> etiquetas = new ArrayList<>();
    private final List<Integer> valores = new ArrayList<>();
    private int total;

    private final Paint arco = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fondoArco = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint numero = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subtitulo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint leyenda = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint leyendaPct = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint punto = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int[] colores;

    public GraficaDona(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        int azul = ContextCompat.getColor(context, R.color.azul_noche);
        int verde = ContextCompat.getColor(context, R.color.verde_azulado);
        arco.setStyle(Paint.Style.STROKE);
        arco.setStrokeWidth(Paleta.dp(context, 22));
        fondoArco.setStyle(Paint.Style.STROKE);
        fondoArco.setStrokeWidth(Paleta.dp(context, 22));
        fondoArco.setColor(Paleta.alfa(verde, 0x26));
        numero.setTextAlign(Paint.Align.CENTER);
        numero.setTextSize(Paleta.dp(context, 22));
        numero.setFakeBoldText(true);
        numero.setColor(azul);
        subtitulo.setTextAlign(Paint.Align.CENTER);
        subtitulo.setTextSize(Paleta.dp(context, 11));
        subtitulo.setColor(verde);
        leyenda.setTextSize(Paleta.dp(context, 13));
        leyenda.setColor(azul);
        leyendaPct.setTextSize(Paleta.dp(context, 13));
        leyendaPct.setFakeBoldText(true);
        leyendaPct.setTextAlign(Paint.Align.RIGHT);
        leyendaPct.setColor(azul);
        colores = Paleta.colores(context);
    }

    public void setDatos(Map<String, Integer> datos) {
        etiquetas.clear();
        valores.clear();
        total = 0;
        for (Map.Entry<String, Integer> e : datos.entrySet()) {
            etiquetas.add(e.getKey());
            valores.add(e.getValue());
            total += e.getValue();
        }
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float filas = Paleta.dp(getContext(), 28) * etiquetas.size();
        int alto = (int) Math.max(Paleta.dp(getContext(), 150), filas + Paleta.dp(getContext(), 10));
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), alto);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Context c = getContext();
        float diametro = Paleta.dp(c, 130);
        float grosor = arco.getStrokeWidth();
        float cy = getHeight() / 2f;
        RectF caja = new RectF(grosor / 2, cy - diametro / 2 + grosor / 2,
                diametro - grosor / 2, cy + diametro / 2 - grosor / 2);

        canvas.drawArc(caja, 0, 360, false, fondoArco);
        float inicio = -90;
        for (int i = 0; i < valores.size(); i++) {
            if (total == 0 || valores.get(i) == 0) continue;
            float barrido = 360f * valores.get(i) / total;
            arco.setColor(colores[i % colores.length]);
            canvas.drawArc(caja, inicio, Math.max(1, barrido - 1.5f), false, arco);
            inicio += barrido;
        }
        canvas.drawText(String.valueOf(total), caja.centerX(), cy + Paleta.dp(c, 4), numero);
        canvas.drawText("respuestas", caja.centerX(), cy + Paleta.dp(c, 18), subtitulo);

        // Leyenda
        float x = diametro + Paleta.dp(c, 20);
        float ancho = getWidth() - x;
        float fila = Paleta.dp(c, 28);
        float y0 = cy - fila * etiquetas.size() / 2f + fila / 2f;
        for (int i = 0; i < etiquetas.size(); i++) {
            float y = y0 + fila * i;
            punto.setColor(colores[i % colores.length]);
            canvas.drawCircle(x + Paleta.dp(c, 6), y, Paleta.dp(c, 6), punto);
            int pct = total == 0 ? 0 : Math.round(valores.get(i) * 100f / total);
            String p = pct + "%";
            float anchoPct = leyendaPct.measureText(p) + Paleta.dp(c, 8);
            CharSequence e = TextUtils.ellipsize(etiquetas.get(i), leyenda,
                    ancho - Paleta.dp(c, 20) - anchoPct, TextUtils.TruncateAt.END);
            canvas.drawText(e, 0, e.length(), x + Paleta.dp(c, 18), y + Paleta.dp(c, 5), leyenda);
            canvas.drawText(p, getWidth(), y + Paleta.dp(c, 5), leyendaPct);
        }
    }
}
