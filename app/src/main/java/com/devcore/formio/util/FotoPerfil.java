package com.devcore.formio.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.devcore.formio.modelos.Administrador;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/*
 * Foto de perfil del administrador.
 * Se recorta en cuadrado, se reduce a 256 px y se guarda como texto (Base64)
 * dentro del documento usuarios/{uid} de Firestore. Así no se necesita
 * Firebase Storage (que pide el plan de pago).
 */
public final class FotoPerfil {

    private static final int TAMANO = 256;

    private FotoPerfil() { }

    // Convierte la imagen elegida en texto Base64 (o null si falla)
    public static String codificar(Context c, Uri uri) {
        try {
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inJustDecodeBounds = true;
            try (InputStream in = c.getContentResolver().openInputStream(uri)) {
                BitmapFactory.decodeStream(in, null, op);
            }
            int escala = 1;
            while (op.outWidth / (escala * 2) >= TAMANO && op.outHeight / (escala * 2) >= TAMANO) escala *= 2;

            BitmapFactory.Options op2 = new BitmapFactory.Options();
            op2.inSampleSize = escala;
            Bitmap original;
            try (InputStream in = c.getContentResolver().openInputStream(uri)) {
                original = BitmapFactory.decodeStream(in, null, op2);
            }
            if (original == null) return null;

            int lado = Math.min(original.getWidth(), original.getHeight());
            Bitmap cuadrada = Bitmap.createBitmap(original, (original.getWidth() - lado) / 2,
                    (original.getHeight() - lado) / 2, lado, lado);
            float factor = Math.min(1f, (float) TAMANO / lado);
            Matrix m = new Matrix();
            m.postScale(factor, factor);
            Bitmap reducida = Bitmap.createBitmap(cuadrada, 0, 0, lado, lado, m, true);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            reducida.compress(Bitmap.CompressFormat.JPEG, 82, out);
            return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }
    }

    // Muestra la foto si existe; si no, deja visibles las iniciales
    public static void mostrar(Administrador a, ImageView foto, TextView iniciales) {
        iniciales.setText(a.getIniciales());
        if (a.tieneFoto()) {
            try {
                byte[] bytes = Base64.decode(a.getFoto(), Base64.DEFAULT);
                Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (b != null) {
                    foto.setImageBitmap(b);
                    foto.setVisibility(View.VISIBLE);
                    return;
                }
            } catch (IllegalArgumentException ignored) { }
        }
        foto.setVisibility(View.GONE);
    }
}
