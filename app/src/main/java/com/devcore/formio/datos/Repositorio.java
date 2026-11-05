package com.devcore.formio.datos;

import com.devcore.formio.modelos.Administrador;
import com.devcore.formio.modelos.EstadoFormulario;
import com.devcore.formio.modelos.Formulario;
import com.devcore.formio.modelos.Plantilla;
import com.devcore.formio.modelos.Pregunta;
import com.devcore.formio.modelos.Respuesta;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * REPOSITORIO: único lugar donde la app lee y guarda datos.
 *
 * Usa Firebase:
 *   - Firebase Authentication: registro, inicio de sesión y contraseñas.
 *   - Cloud Firestore: formularios, respuestas, perfil y plantillas propias.
 *
 * TIEMPO REAL: se escuchan los cambios de Firestore (snapshot listeners).
 * Cuando llega una respuesta desde la página web del QR, se avisa a las vistas
 * registradas como Oyente y se actualizan solas.
 */
public final class Repositorio {

    // ---- Oyentes y respuestas de las operaciones ----

    public interface Oyente {
        void alCambiarDatos();
    }

    public interface Resultado {
        void listo();
        void error(String mensaje);
    }

    public interface AvisoError {
        void error(String mensaje);
    }

    private static final String FORMULARIOS = "formularios";
    private static final String RESPUESTAS = "respuestas";
    private static final String USUARIOS = "usuarios";
    private static final String PLANTILLAS = "plantillas";

    private static Repositorio instancia;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Administrador admin;
    private final List<Formulario> formularios = new ArrayList<>();
    private final List<Plantilla> misPlantillas = new ArrayList<>();
    private final List<Oyente> oyentes = new ArrayList<>();
    private AvisoError avisoError;

    private ListenerRegistration escuchaFormularios;
    private ListenerRegistration escuchaPlantillas;
    private final Map<String, ListenerRegistration> escuchasRespuestas = new HashMap<>();
    private boolean formulariosCargados;


    private Repositorio() {
        // El enlace del QR usa el dominio de Firebase Hosting del proyecto: <proyecto>.web.app
        try {
            String proyecto = FirebaseApp.getInstance().getOptions().getProjectId();
            if (proyecto != null) Formulario.setDominio(proyecto + ".web.app/f/");
        } catch (IllegalStateException ignored) { }
    }

    public static synchronized Repositorio get() {
        if (instancia == null) instancia = new Repositorio();
        return instancia;
    }


    // =========================================================
    // TIEMPO REAL
    // =========================================================

    public void agregarOyente(Oyente o) {
        if (!oyentes.contains(o)) oyentes.add(o);
    }

    public void quitarOyente(Oyente o) {
        oyentes.remove(o);
    }

    public void setAvisoError(AvisoError aviso) {
        this.avisoError = aviso;
    }

    private void notificar() {
        for (Oyente o : new ArrayList<>(oyentes)) o.alCambiarDatos();
    }

    private void avisarError(Exception e) {
        if (avisoError != null) avisoError.error(mensajeDe(e));
    }

    public boolean isFormulariosCargados() { return formulariosCargados; }


    // =========================================================
    // MÓDULO 1: AUTENTICACIÓN (Firebase Authentication)
    // =========================================================

    public boolean haySesion() {
        return auth.getCurrentUser() != null;
    }

    // true cuando ya se cargó el perfil y se están escuchando los datos
    public boolean isSesionCargada() {
        return admin != null && escuchaFormularios != null;
    }

    // 1.2 Login
    public void iniciarSesion(String correo, String contrasena, Resultado r) {
        auth.signInWithEmailAndPassword(correo.trim(), contrasena)
                .addOnSuccessListener(x -> cargarSesion(r))
                .addOnFailureListener(e -> r.error(mensajeDe(e)));
    }

    // 1.1 Registro: crea la cuenta y su documento usuarios/{uid}
    public void registrar(String nombre, String correo, String contrasena, Resultado r) {
        auth.createUserWithEmailAndPassword(correo.trim(), contrasena)
                .addOnSuccessListener(x -> {
                    FirebaseUser u = auth.getCurrentUser();
                    if (u == null) { r.error("No se pudo crear la cuenta"); return; }
                    admin = new Administrador(u.getUid(), nombre.trim(), correo.trim());
                    db.collection(USUARIOS).document(u.getUid()).set(Mapeo.deAdmin(admin))
                            .addOnSuccessListener(y -> { iniciarEscuchas(); r.listo(); })
                            .addOnFailureListener(e -> r.error(mensajeDe(e)));
                })
                .addOnFailureListener(e -> r.error(mensajeDe(e)));
    }

    // 1.3 Recuperar contraseña: Firebase envía el enlace al correo
    public void recuperarContrasena(String correo, Resultado r) {
        auth.sendPasswordResetEmail(correo.trim())
                .addOnSuccessListener(x -> r.listo())
                .addOnFailureListener(e -> r.error(mensajeDe(e)));
    }

    // Carga el perfil del usuario que ya inició sesión y empieza a escuchar sus datos
    public void cargarSesion(Resultado r) {
        FirebaseUser u = auth.getCurrentUser();
        if (u == null) { r.error("Inicia sesión de nuevo"); return; }
        db.collection(USUARIOS).document(u.getUid()).get()
                .addOnSuccessListener(doc -> {
                    String correo = u.getEmail() == null ? "" : u.getEmail();
                    if (doc.exists()) {
                        admin = Mapeo.aAdmin(u.getUid(), doc.getData(), correo);
                        admin.setCorreo(correo.isEmpty() ? admin.getCorreo() : correo);
                    } else {
                        String nombre = correo.contains("@") ? correo.substring(0, correo.indexOf('@')) : "Administrador";
                        admin = new Administrador(u.getUid(), nombre, correo);
                        db.collection(USUARIOS).document(u.getUid()).set(Mapeo.deAdmin(admin));
                    }
                    iniciarEscuchas();
                    r.listo();
                })
                .addOnFailureListener(e -> r.error(mensajeDe(e)));
    }

    public void cerrarSesion() {
        detenerEscuchas();
        formularios.clear();
        misPlantillas.clear();
        admin = null;
        auth.signOut();
    }

    public Administrador getAdmin() {
        if (admin == null) {
            FirebaseUser u = auth.getCurrentUser();
            admin = new Administrador(u == null ? "" : u.getUid(), "Administrador",
                    u == null || u.getEmail() == null ? "" : u.getEmail());
        }
        return admin;
    }

    // 1.4 Guardar los datos del perfil (nombre, teléfono, organización y foto)
    public void guardarPerfil(Resultado r) {
        Administrador a = getAdmin();
        db.collection(USUARIOS).document(a.getUid()).set(Mapeo.deAdmin(a), SetOptions.merge())
                .addOnSuccessListener(x -> { notificar(); r.listo(); })
                .addOnFailureListener(e -> r.error(mensajeDe(e)));
        notificar();
    }

    // 1.4 Cambiar contraseña: Firebase pide volver a confirmar la contraseña actual
    public void cambiarContrasena(String actual, String nueva, Resultado r) {
        FirebaseUser u = auth.getCurrentUser();
        if (u == null || u.getEmail() == null) { r.error("Inicia sesión de nuevo"); return; }
        u.reauthenticate(EmailAuthProvider.getCredential(u.getEmail(), actual))
                .addOnSuccessListener(x -> u.updatePassword(nueva)
                        .addOnSuccessListener(y -> r.listo())
                        .addOnFailureListener(e -> r.error(mensajeDe(e))))
                .addOnFailureListener(e -> r.error("La contraseña actual no es correcta"));
    }


    // =========================================================
    // ESCUCHAS EN TIEMPO REAL (snapshot listeners)
    // =========================================================

    private void iniciarEscuchas() {
        detenerEscuchas();
        String uid = getAdmin().getUid();

        // Formularios del administrador
        escuchaFormularios = db.collection(FORMULARIOS).whereEqualTo("creadorId", uid)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) { avisarError(e); return; }
                    if (snap == null) return;
                    Set<String> vistos = new HashSet<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        vistos.add(doc.getId());
                        Formulario f = buscar(doc.getId());
                        if (f == null) {
                            formularios.add(Mapeo.aFormulario(doc.getId(), doc.getData()));
                        } else if (!doc.getMetadata().hasPendingWrites()) {
                            Mapeo.actualizar(f, doc.getData());
                        }
                        escucharRespuestas(doc.getId());
                    }
                    // Quitar los que se borraron
                    for (Formulario f : new ArrayList<>(formularios)) {
                        if (!vistos.contains(f.getId())) {
                            formularios.remove(f);
                            ListenerRegistration reg = escuchasRespuestas.remove(f.getId());
                            if (reg != null) reg.remove();
                        }
                    }
                    formulariosCargados = true;
                    notificar();
                });

        // Plantillas propias
        escuchaPlantillas = db.collection(USUARIOS).document(uid).collection(PLANTILLAS)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;
                    misPlantillas.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        misPlantillas.add(Mapeo.aPlantilla(doc.getId(), doc.getData()));
                    }
                    notificar();
                });
    }

    // Respuestas de un formulario (llegan al instante cuando alguien responde por el QR)
    private void escucharRespuestas(String idFormulario) {
        if (escuchasRespuestas.containsKey(idFormulario)) return;
        ListenerRegistration reg = db.collection(FORMULARIOS).document(idFormulario).collection(RESPUESTAS)
                .orderBy("numero")
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;
                    Formulario f = buscar(idFormulario);
                    if (f == null) return;
                    List<Respuesta> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) lista.add(Mapeo.aRespuesta(doc.getData()));
                    f.getRespuestas().clear();
                    f.getRespuestas().addAll(lista);
                    notificar();
                });
        escuchasRespuestas.put(idFormulario, reg);
    }

    private void detenerEscuchas() {
        if (escuchaFormularios != null) escuchaFormularios.remove();
        if (escuchaPlantillas != null) escuchaPlantillas.remove();
        for (ListenerRegistration r : escuchasRespuestas.values()) r.remove();
        escuchasRespuestas.clear();
        escuchaFormularios = null;
        escuchaPlantillas = null;
        formulariosCargados = false;
    }


    // =========================================================
    // MÓDULO 2: GESTIÓN DE FORMULARIOS
    // =========================================================

    private DocumentReference ref(Formulario f) {
        return db.collection(FORMULARIOS).document(f.getId());
    }

    // Lista principal (sin archivados), del más nuevo al más viejo
    public List<Formulario> getFormularios() {
        List<Formulario> lista = new ArrayList<>();
        for (Formulario f : formularios) if (!f.isArchivado()) lista.add(f);
        ordenarPorFecha(lista);
        return lista;
    }

    public List<Formulario> getArchivados() {
        List<Formulario> lista = new ArrayList<>();
        for (Formulario f : formularios) if (f.isArchivado()) lista.add(f);
        ordenarPorFecha(lista);
        return lista;
    }

    public List<Formulario> getPorEstado(EstadoFormulario estado) {
        List<Formulario> lista = new ArrayList<>();
        for (Formulario f : getFormularios()) if (f.getEstado() == estado) lista.add(f);
        return lista;
    }

    private void ordenarPorFecha(List<Formulario> lista) {
        Collections.sort(lista, (a, b) -> Long.compare(b.getFechaCreacion(), a.getFechaCreacion()));
    }

    public Formulario buscar(String id) {
        if (id == null) return null;
        for (Formulario f : formularios) if (f.getId().equals(id)) return f;
        return null;
    }

    // Guarda un formulario nuevo: se ve al instante y Firestore lo sube en segundo plano
    private Formulario agregarNuevo(Formulario f) {
        f.setCreadorId(getAdmin().getUid());
        formularios.add(f);
        ref(f).set(Mapeo.deFormularioNuevo(f)).addOnFailureListener(this::avisarError);
        escucharRespuestas(f.getId());
        notificar();
        return f;
    }

    // 2.1 Crear
    public Formulario crear(String titulo, String descripcion) {
        return agregarNuevo(new Formulario(titulo.trim(), descripcion.trim(), System.currentTimeMillis()));
    }

    // 3.4 Crear desde una plantilla (básica o propia)
    public Formulario crearDesdePlantilla(Plantilla p) {
        Formulario f = new Formulario(p.getNombre(), p.getDescripcion(), System.currentTimeMillis());
        for (Pregunta pr : p.getPreguntas()) f.getPreguntas().add(pr.copia());
        return agregarNuevo(f);
    }

    // 2.3 Duplicar: copia la estructura (no las respuestas) como Borrador
    public Formulario duplicar(Formulario original, String nuevoTitulo) {
        Formulario f = new Formulario(nuevoTitulo.trim(), original.getDescripcion(), System.currentTimeMillis());
        for (Pregunta p : original.getPreguntas()) f.getPreguntas().add(p.copia());
        f.setLimiteRespuestas(original.getLimiteRespuestas());
        f.setMensajeLimite(original.getMensajeLimite());
        f.setUnaPorDispositivo(original.isUnaPorDispositivo());
        return agregarNuevo(f);
    }

    // Guarda los cambios de edición o configuración (sin tocar los contadores)
    public void guardar(Formulario f) {
        ref(f).set(Mapeo.deFormulario(f), SetOptions.merge()).addOnFailureListener(this::avisarError);
        notificar();
    }

    // 2.4 Archivar / restaurar
    public void archivar(Formulario f, boolean archivar) {
        f.setArchivado(archivar);
        ref(f).update("archivado", archivar).addOnFailureListener(this::avisarError);
        notificar();
    }

    // 2.5 Eliminar el formulario con todas sus respuestas
    public void eliminar(Formulario f, Resultado r) {
        ref(f).collection(RESPUESTAS).get()
                .addOnSuccessListener(snap -> {
                    List<DocumentReference> refs = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) refs.add(d.getReference());
                    refs.add(ref(f));
                    borrarEnLotes(refs, r);
                })
                .addOnFailureListener(e -> r.error(mensajeDe(e)));
    }

    // Firestore permite máximo 500 operaciones por lote
    private void borrarEnLotes(List<DocumentReference> refs, Resultado r) {
        if (refs.isEmpty()) { r.listo(); return; }
        int fin = Math.min(450, refs.size());
        WriteBatch lote = db.batch();
        for (DocumentReference d : refs.subList(0, fin)) lote.delete(d);
        List<DocumentReference> resto = new ArrayList<>(refs.subList(fin, refs.size()));
        lote.commit().addOnSuccessListener(x -> borrarEnLotes(resto, r))
                .addOnFailureListener(e -> r.error(mensajeDe(e)));
    }

    // 4.1 Publicar: pasa a Activo y queda listo su QR
    public void publicar(Formulario f, Resultado r) {
        f.setPublicado(true);
        f.setAbierto(true);
        Map<String, Object> cambios = new HashMap<>();
        cambios.put("publicado", true);
        cambios.put("abierto", true);
        cambios.put("preguntas", Mapeo.dePreguntas(f.getPreguntas()));
        ref(f).update(cambios).addOnSuccessListener(x -> r.listo()).addOnFailureListener(e -> r.error(mensajeDe(e)));
        notificar();
    }

    // 2.6 Abrir y cerrar
    public void setAbierto(Formulario f, boolean abierto) {
        f.setAbierto(abierto);
        ref(f).update("abierto", abierto).addOnFailureListener(this::avisarError);
        notificar();
    }


    // =========================================================
    // MÓDULO 5: RESPUESTAS
    // =========================================================

    // Cuenta una visita (para la tasa de abandono)
    public void registrarVisita(Formulario f) {
        f.setVisitas(f.getVisitas() + 1);
        ref(f).update("visitas", FieldValue.increment(1)).addOnFailureListener(this::avisarError);
        notificar();
    }

    // Guarda una respuesta con una transacción: revisa que el formulario siga abierto,
    // le da el número siguiente y suma 1 al total (igual que la página web).
    public void agregarRespuesta(Formulario f, Map<String, List<String>> valores, int duracionSegundos, Resultado r) {
        DocumentReference refForm = ref(f);
        DocumentReference refNueva = refForm.collection(RESPUESTAS).document();
        db.runTransaction(tx -> {
            DocumentSnapshot doc = tx.get(refForm);
            Formulario actual = Mapeo.aFormulario(doc.getId(), doc.getData());
            if (!actual.recibeRespuestas()) {
                throw new FirebaseFirestoreException("cerrado", FirebaseFirestoreException.Code.ABORTED);
            }
            int numero = (int) Mapeo.numero(doc.getData(), "totalRespuestas", 0) + 1;
            Respuesta nueva = new Respuesta(numero, System.currentTimeMillis(), duracionSegundos);
            nueva.getValores().putAll(valores);
            tx.update(refForm, "totalRespuestas", (long) numero);
            tx.set(refNueva, Mapeo.deRespuesta(nueva));
            return numero;
        }).addOnSuccessListener(numero -> {
            if (f.isUnaPorDispositivo()) f.setEsteDispositivoRespondio(true);
            r.listo();
        }).addOnFailureListener(e -> r.error(
                e instanceof FirebaseFirestoreException && "cerrado".equals(e.getMessage())
                        ? "El formulario ya no recibe respuestas" : mensajeDe(e)));
    }


    // =========================================================
    // PLANTILLAS PROPIAS (usuarios/{uid}/plantillas)
    // =========================================================

    public List<Plantilla> getMisPlantillas() {
        return misPlantillas;
    }

    private CollectionReference plantillasRef() {
        return db.collection(USUARIOS).document(getAdmin().getUid()).collection(PLANTILLAS);
    }

    public void guardarComoPlantilla(Formulario f, String nombre, Resultado r) {
        plantillasRef().add(Mapeo.dePlantilla(nombre.trim(), f.getDescripcion(), f.getPreguntas()))
                .addOnSuccessListener(x -> r.listo())
                .addOnFailureListener(e -> r.error(mensajeDe(e)));
    }

    public void eliminarPlantilla(Plantilla p, Resultado r) {
        plantillasRef().document(p.getId()).delete()
                .addOnSuccessListener(x -> r.listo())
                .addOnFailureListener(e -> r.error(mensajeDe(e)));
    }


    // =========================================================
    // DATOS DE EJEMPLO (para probar o presentar la app)
    // =========================================================

    public void crearEjemplos(Resultado r) {
        WriteBatch lote = db.batch();
        for (Formulario f : DatosEjemplo.crear()) {
            f.setCreadorId(getAdmin().getUid());
            f.setTotalRespuestas(f.getRespuestas().size());
            DocumentReference refForm = db.collection(FORMULARIOS).document(f.getId());
            lote.set(refForm, Mapeo.deFormularioNuevo(f));
            for (Respuesta resp : f.getRespuestas()) {
                lote.set(refForm.collection(RESPUESTAS).document(), Mapeo.deRespuesta(resp));
            }
        }
        lote.commit().addOnSuccessListener(x -> r.listo()).addOnFailureListener(e -> r.error(mensajeDe(e)));
    }


    // =========================================================
    // MÓDULO 7: DASHBOARD
    // =========================================================

    public int respuestasDelDia(int diasAtras) {
        long[] rango = rangoDia(diasAtras);
        int n = 0;
        for (Formulario f : formularios) {
            for (Respuesta resp : f.getRespuestas()) {
                if (resp.getFecha() >= rango[0] && resp.getFecha() < rango[1]) n++;
            }
        }
        return n;
    }

    public int[] respuestasSemana() {
        int[] dias = new int[7];
        for (int i = 0; i < 7; i++) dias[6 - i] = respuestasDelDia(i);
        return dias;
    }

    public int totalRespuestas() {
        int n = 0;
        for (Formulario f : getFormularios()) n += f.getRespuestas().size();
        return n;
    }

    // 7.3 Por vencer: cierran en menos de 3 días o llevan 80 % de su límite
    public List<Formulario> getPorVencer() {
        List<Formulario> lista = new ArrayList<>();
        long ahora = System.currentTimeMillis();
        long tresDias = 3L * 24 * 60 * 60 * 1000;
        for (Formulario f : getPorEstado(EstadoFormulario.ACTIVO)) {
            boolean porFecha = f.getFechaLimite() > 0 && f.getFechaLimite() - ahora < tresDias;
            boolean porLimite = f.getLimiteRespuestas() > 0
                    && f.getTotalRespuestas() >= f.getLimiteRespuestas() * 0.8;
            if (porFecha || porLimite) lista.add(f);
        }
        return lista;
    }

    public static long[] rangoDia(int diasAtras) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.DAY_OF_YEAR, -diasAtras);
        long inicio = c.getTimeInMillis();
        c.add(Calendar.DAY_OF_YEAR, 1);
        return new long[]{inicio, c.getTimeInMillis()};
    }


    // =========================================================
    // MENSAJES DE ERROR EN ESPAÑOL
    // =========================================================

    public static String mensajeDe(Exception e) {
        if (e instanceof FirebaseNetworkException) return "Sin conexión a internet. Revisa tu red e intenta de nuevo.";
        if (e instanceof FirebaseAuthUserCollisionException) return "Ya existe una cuenta con este correo.";
        if (e instanceof FirebaseAuthWeakPasswordException) return "La contraseña es muy débil: usa al menos 6 caracteres.";
        if (e instanceof FirebaseAuthInvalidUserException) return "No hay ninguna cuenta con este correo.";
        if (e instanceof FirebaseAuthInvalidCredentialsException) return "Correo o contraseña incorrectos.";
        if (e instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException.Code c = ((FirebaseFirestoreException) e).getCode();
            if (c == FirebaseFirestoreException.Code.PERMISSION_DENIED) return "No tienes permiso para esta acción. Revisa las reglas de Firestore.";
            if (c == FirebaseFirestoreException.Code.UNAVAILABLE) return "Sin conexión a internet. Los cambios se guardarán al reconectar.";
        }
        return e.getMessage() == null ? "Ocurrió un error inesperado." : e.getMessage();
    }
}
