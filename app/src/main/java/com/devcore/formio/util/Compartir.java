package com.devcore.formio.util;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.devcore.formio.R;
import com.devcore.formio.modelos.Formulario;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/*
 * Acciones del módulo 4: copiar enlace, descargar QR y enviar por WhatsApp o correo.
 */
public final class Compartir {

    private Compartir() { }

    private static String mensaje(Context c, Formulario f) {
        return c.getString(R.string.compartir_mensaje, f.getTitulo(), f.getEnlace());
    }

    // 4.4 Copiar enlace
    public static void copiarEnlace(Context c, Formulario f) {
        ClipboardManager cm = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("Form.io", f.getEnlace()));
        Toast.makeText(c, R.string.msg_enlace_copiado, Toast.LENGTH_SHORT).show();
    }

    // 4.5 Enviar por WhatsApp
    public static void porWhatsApp(Context c, Formulario f) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, mensaje(c, f));
        i.setPackage("com.whatsapp");
        try {
            c.startActivity(i);
        } catch (ActivityNotFoundException e) {
            // Si no tiene WhatsApp, se abre el menú normal de compartir
            i.setPackage(null);
            c.startActivity(Intent.createChooser(i, c.getString(R.string.compartir_titulo)));
        }
    }

    // 4.5 Enviar por correo
    public static void porCorreo(Context c, Formulario f) {
        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        i.putExtra(Intent.EXTRA_SUBJECT, c.getString(R.string.compartir_asunto, f.getTitulo()));
        i.putExtra(Intent.EXTRA_TEXT, mensaje(c, f));
        try {
            c.startActivity(i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(c, R.string.msg_sin_correo, Toast.LENGTH_SHORT).show();
        }
    }

    // Otras apps
    public static void otros(Context c, Formulario f) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, mensaje(c, f));
        c.startActivity(Intent.createChooser(i, c.getString(R.string.compartir_titulo)));
    }

    // 4.3 Descargar el QR en la galería (PNG o JPG)
    public static void descargarQr(Context c, Formulario f, Bitmap qr, boolean png) {
        String nombre = "FormIO_" + f.getCodigo() + (png ? ".png" : ".jpg");
        Bitmap.CompressFormat formato = png ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues valores = new ContentValues();
                valores.put(MediaStore.Images.Media.DISPLAY_NAME, nombre);
                valores.put(MediaStore.Images.Media.MIME_TYPE, png ? "image/png" : "image/jpeg");
                valores.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Form.io");
                Uri uri = c.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, valores);
                if (uri == null) throw new Exception("sin uri");
                try (OutputStream out = c.getContentResolver().openOutputStream(uri)) {
                    if (out == null) throw new Exception("sin salida");
                    qr.compress(formato, 100, out);
                }
                Toast.makeText(c, c.getString(R.string.msg_qr_guardado, "Imágenes/Form.io"), Toast.LENGTH_LONG).show();
            } else {
                // Android 9 o anterior: carpeta de imágenes de la app (no necesita permisos)
                File carpeta = c.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File archivo = new File(carpeta, nombre);
                try (FileOutputStream out = new FileOutputStream(archivo)) {
                    qr.compress(formato, 100, out);
                }
                Toast.makeText(c, c.getString(R.string.msg_qr_guardado, archivo.getParent()), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(c, R.string.msg_qr_error, Toast.LENGTH_SHORT).show();
        }
    }
}
