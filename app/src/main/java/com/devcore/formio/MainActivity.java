package com.devcore.formio;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.devcore.formio.datos.Repositorio;
import com.devcore.formio.modelos.Administrador;
import com.devcore.formio.util.Alertas;
import com.devcore.formio.util.FotoPerfil;
import com.devcore.formio.vistas.CompartirFragment;
import com.devcore.formio.vistas.ConfiguracionFragment;
import com.devcore.formio.vistas.DashboardFragment;
import com.devcore.formio.vistas.DetalleFragment;
import com.devcore.formio.vistas.EditorFragment;
import com.devcore.formio.vistas.EstadisticasFragment;
import com.devcore.formio.vistas.FormulariosFragment;
import com.devcore.formio.vistas.NuevoFragment;
import com.devcore.formio.vistas.PerfilFragment;
import com.devcore.formio.vistas.PlantillasFragment;
import com.devcore.formio.vistas.RespuestaDetalleFragment;
import com.devcore.formio.vistas.RespuestasFragment;
import com.devcore.formio.vistas.VistaPreviaFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

/*
 * PANEL DEL ADMINISTRADOR
 * Única Activity del panel: tiene la Toolbar y el Navigation Drawer,
 * y cambia las vistas (Fragments) dentro de "contenedor_fragment".
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Dibuja detrás de las barras del sistema: íconos claros arriba (fondo azul noche)
        // y oscuros abajo (fondo blanco)
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        setContentView(R.layout.activity_main);

        // Espacio para la barra de estado (arriba) y la de navegación o el teclado (abajo)
        View contenido = findViewById(R.id.contenido_principal);
        View contenedor = findViewById(R.id.contenedor_fragment);
        ViewCompat.setOnApplyWindowInsetsListener(contenido, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(barras.left, barras.top, barras.right, 0);
            contenedor.setPadding(0, 0, 0, Math.max(barras.bottom, teclado.bottom));
            return insets;
        });

        // Toolbar como barra de la app
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Navigation Drawer + botón ☰
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.drawer_abrir, R.string.drawer_cerrar);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Botón atrás: si el menú está abierto, primero lo cierra
        OnBackPressedCallback cerrarMenu = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, cerrarMenu);
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                cerrarMenu.setEnabled(true);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                cerrarMenu.setEnabled(false);
            }
        });

        // Errores de Firebase en segundo plano (sin internet, permisos…)
        Repositorio.get().setAvisoError(mensaje -> {
            if (!isFinishing()) Alertas.error(this, getString(R.string.algo_salio_mal), mensaje);
        });

        // Si la app se abrió con la sesión recordada, se carga el perfil desde Firestore
        if (!Repositorio.get().haySesion()) {
            cerrarSesion();
            return;
        }
        if (!Repositorio.get().isSesionCargada()) {
            Repositorio.get().cargarSesion(new Repositorio.Resultado() {
                @Override public void listo() { actualizarCabeceraMenu(); }
                @Override public void error(String mensaje) {
                    if (!isFinishing()) Alertas.error(MainActivity.this, getString(R.string.algo_salio_mal), mensaje);
                }
            });
        }

        actualizarCabeceraMenu();

        if (savedInstanceState == null) {
            navegarA(R.id.nav_dashboard);
        }
    }

    @Override
    protected void onDestroy() {
        Repositorio.get().setAvisoError(null);
        super.onDestroy();
    }

    // Nombre, correo e iniciales del administrador en el menú lateral
    public void actualizarCabeceraMenu() {
        Administrador a = Repositorio.get().getAdmin();
        View cabecera = navigationView.getHeaderView(0);
        FotoPerfil.mostrar(a, cabecera.findViewById(R.id.imgAvatarMenu),
                cabecera.findViewById(R.id.txtInicialesMenu));
        ((TextView) cabecera.findViewById(R.id.txtNombreMenu)).setText(a.getNombre());
        ((TextView) cabecera.findViewById(R.id.txtCorreoMenu)).setText(a.getCorreo());
    }


    // =========================================================
    // MENÚ LATERAL
    // =========================================================

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        if (item.getItemId() == R.id.nav_salir) {
            confirmarCerrarSesion();
            return false;
        }
        abrirSeccion(item.getItemId());
        return true;
    }

    // Abre una sección principal y la marca en el menú
    public void navegarA(int idMenu) {
        navigationView.setCheckedItem(idMenu);
        abrirSeccion(idMenu);
    }

    private void abrirSeccion(int idMenu) {
        Fragment vista;
        if (idMenu == R.id.nav_formularios) {
            vista = FormulariosFragment.nueva(false);
        } else if (idMenu == R.id.nav_nuevo) {
            vista = new NuevoFragment();
        } else if (idMenu == R.id.nav_plantillas) {
            vista = new PlantillasFragment();
        } else if (idMenu == R.id.nav_archivados) {
            vista = FormulariosFragment.nueva(true);
        } else if (idMenu == R.id.nav_perfil) {
            vista = new PerfilFragment();
        } else {
            vista = new DashboardFragment();
        }

        FragmentManager fm = getSupportFragmentManager();
        // Al cambiar de sección se limpia el historial de subvistas
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.contenedor_fragment, vista)
                .commit();
    }

    // Abre una vista ENCIMA de la actual (el botón atrás regresa)
    public void abrirSubvista(Fragment vista) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.entrar_derecha, R.anim.salir_izquierda,
                        R.anim.entrar_izquierda, R.anim.salir_derecha)
                .replace(R.id.contenedor_fragment, vista)
                .addToBackStack(null)
                .commit();
    }

    public void regresar() {
        getSupportFragmentManager().popBackStack();
    }


    // =========================================================
    // ATAJOS PARA ABRIR CADA VISTA DE UN FORMULARIO
    // =========================================================

    public void abrirDetalle(String id) { abrirSubvista(DetalleFragment.nueva(id)); }
    public void abrirEditor(String id) { abrirSubvista(EditorFragment.nueva(id)); }
    public void abrirCompartir(String id, boolean recienPublicado) { abrirSubvista(CompartirFragment.nueva(id, recienPublicado)); }
    public void abrirRespuestas(String id) { abrirSubvista(RespuestasFragment.nueva(id)); }
    public void abrirRespuesta(String id, int indice) { abrirSubvista(RespuestaDetalleFragment.nueva(id, indice)); }
    public void abrirEstadisticas(String id) { abrirSubvista(EstadisticasFragment.nueva(id)); }
    public void abrirConfiguracion(String id) { abrirSubvista(ConfiguracionFragment.nueva(id)); }
    public void abrirVistaPrevia(String id) { abrirSubvista(VistaPreviaFragment.nueva(id)); }

    // Después de crear un formulario: Mis formularios -> Detalle -> Editor
    public void abrirFormularioNuevo(String id) {
        navegarA(R.id.nav_formularios);
        abrirDetalle(id);
        abrirEditor(id);
    }


    // =========================================================
    // MENÚ DE LA TOOLBAR
    // =========================================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_nuevo) {
            navegarA(R.id.nav_nuevo);
            return true;
        } else if (id == R.id.action_perfil) {
            navegarA(R.id.nav_perfil);
            return true;
        } else if (id == R.id.action_salir) {
            confirmarCerrarSesion();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // =========================================================
    // CERRAR SESIÓN
    // =========================================================

    public void confirmarCerrarSesion() {
        Alertas.confirmar(this, getString(R.string.cerrar_sesion_titulo),
                getString(R.string.cerrar_sesion_mensaje), getString(R.string.salir), this::cerrarSesion);
    }

    public void cerrarSesion() {
        Repositorio.get().cerrarSesion();
        Intent i = new Intent(this, AuthActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
