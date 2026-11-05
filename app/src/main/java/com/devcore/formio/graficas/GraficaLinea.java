package com.devcore.formio.graficas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.devcore.formio.R;

/*
 * Gráfica de línea (respuestas por día). Se dibuja a mano con Canvas, sin librerías.
 */
public class GraficaLinea extends View {

    private int[] valores = new int[0];
    private String[] etiquetas = new String[0];
    private boolean claro;   // true = colores para fondo oscuro

    private final Paint linea = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint relleno = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint punto = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centro = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint texto = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valorTexto = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint guia = new Paint(Paint.ANTI_ALIAS_FLAG);

    public GraficaLinea(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        linea.setStyle(Paint.Style.STROKE);
        linea.setStrokeWidth(Paleta.dp(context, 3));
        linea.setStrokeCap(Paint.Cap.ROUND);
        linea.setStrokeJoin(Paint.Join.ROUND);
        relleno.setStyle(Paint.Style.FILL);
        texto.setTextSize(Paleta.dp(context, 11));
        texto.setTextAlign(Paint.Align.CENTER);
        valorTexto.setTextSize(Paleta.dp(context, 11));
        valorTexto.setTextAlign(Paint.Align.CENTER);
        valorTexto.setFakeBoldText(true);
        guia.setStrokeWidth(Paleta.dp(context, 1));
        aplicarColores();
    }

    public void setColoresClaros(boolean claro) {
        this.claro = claro;
        aplicarColores();
        invalidate();
    }

    private void aplicarColores() {
        Context c = getContext();
        int azul = ContextCompat.getColor(c, R.color.azul_noche);
        int menta = ContextCompat.getColor(c, R.color.verde_menta);
        int verde = ContextCompat.getColor(c, R.color.verde_azulado);
        int blanco = ContextCompat.getColor(c, R.color.blanco);
        linea.setColor(claro ? menta : verde);
        relleno.setColor(Paleta.alfa(claro ? menta : verde, 0x33));
        punto.setColor(claro ? menta : verde);
        centro.setColor(claro ? azul : blanco);
        texto.setColor(claro ? blanco : verde);
        valorTexto.setColor(claro ? blanco : azul);
        guia.setColor(Paleta.alfa(claro ? blanco : verde, 0x33));
    }

    public void setDatos(int[] valores, String[] etiquetas) {
        this.valores = valores;
        this.etiquetas = etiquetas;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int alto = (int) Paleta.dp(getContext(), 170);
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), resolveSize(alto, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int n = valores.length;
        if (n == 0) return;
        Context c = getContext();
        float izq = Paleta.dp(c, 16), der = getWidth() - Paleta.dp(c, 16);
        float arriba = Paleta.dp(c, 22), abajo = getHeight() - Paleta.dp(c, 26);

        int max = 1;
        for (int v : valores) max = Math.max(max, v);

        // líneas guía
        for (int i = 0; i <= 3; i++) {
            float y = arriba + (abajo - arriba) * i / 3f;
            canvas.drawLine(izq, y, der, y, guia);
        }

        float paso = n > 1 ? (der - izq) / (n - 1) : 0;
        Path camino = new Path();
        Path area = new Path();
        float[] xs = new float[n], ys = new float[n];
        for (int i = 0; i < n; i++) {
            xs[i] = n > 1 ? izq + paso * i : (izq + der) / 2;
            ys[i] = abajo - (abajo - arriba) * valores[i] / (float) max;
            if (i == 0) {
                camino.moveTo(xs[i], ys[i]);
                area.moveTo(xs[i], abajo);
                area.lineTo(xs[i], ys[i]);
            } else {
                // curva suave entre puntos
                float cx = (xs[i - 1] + xs[i]) / 2;
                camino.cubicTo(cx, ys[i - 1], cx, ys[i], xs[i], ys[i]);
                area.cubicTo(cx, ys[i - 1], cx, ys[i], xs[i], ys[i]);
            }
        }
        area.lineTo(xs[n - 1], abajo);
        area.close();
        canvas.drawPath(area, relleno);
        canvas.drawPath(camino, linea);

        float r = Paleta.dp(c, 5);
        for (int i = 0; i < n; i++) {
            canvas.drawCircle(xs[i], ys[i], r, punto);
            canvas.drawCircle(xs[i], ys[i], r * 0.5f, centro);
            canvas.drawText(String.valueOf(valores[i]), xs[i], ys[i] - Paleta.dp(c, 9), valorTexto);
            if (i < etiquetas.length) {
                canvas.drawText(etiquetas[i], xs[i], getHeight() - Paleta.dp(c, 6), texto);
            }
        }
    }
}
