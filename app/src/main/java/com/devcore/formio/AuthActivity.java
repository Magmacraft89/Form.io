package com.devcore.formio;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.devcore.formio.datos.Repositorio;
import com.devcore.formio.datos.Sesion;
import com.devcore.formio.vistas.LoginFragment;
import com.devcore.formio.vistas.RecuperarFragment;
import com.devcore.formio.vistas.RegistroFragment;

/*
 * MÓDULO 1: AUTENTICACIÓN
 * Pantalla de inicio de la app. Cambia entre los Fragments de
 * Login (1.2), Registro (1.1) y Recuperar contraseña (1.3).
 */
public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Dibuja detrás de las barras del sistema: íconos claros arriba (fondo azul noche)
        // y oscuros abajo (fondo blanco)
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        setContentView(R.layout.activity_auth);

        // Espacio para la barra de estado, la de navegación y el teclado
        View contenedor = findViewById(R.id.contenedor_auth);
        ViewCompat.setOnApplyWindowInsetsListener(contenedor, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(barras.left, barras.top, barras.right, Math.max(barras.bottom, teclado.bottom));
            return insets;
        });

        // Firebase recuerda la sesión. Si el usuario NO marcó "Recordarme", se cierra al volver a abrir.
        if (savedInstanceState == null && Repositorio.get().haySesion()) {
            if (Sesion.recordar(this)) {
                abrirPanel();
                return;
            }
            Repositorio.get().cerrarSesion();
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contenedor_auth, new LoginFragment())
                    .commit();
        }
    }


    // =========================================================
    // NAVEGACIÓN ENTRE LAS VISTAS DE ACCESO
    // =========================================================

    public void mostrarRegistro() {
        abrir(new RegistroFragment());
    }

    public void mostrarRecuperar() {
        abrir(new RecuperarFragment());
    }

    public void volverALogin() {
        getSupportFragmentManager().popBackStack();
    }

    private void abrir(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.entrar_derecha, R.anim.salir_izquierda,
                        R.anim.entrar_izquierda, R.anim.salir_derecha)
                .replace(R.id.contenedor_auth, f)
                .addToBackStack(null)
                .commit();
    }

    // Después de iniciar sesión o registrarse
    public void entrar(boolean recordar) {
        Sesion.setRecordar(this, recordar);
        abrirPanel();
    }

    private void abrirPanel() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
