package com.devcore.formio.modelos;

/*
 * Modelo: la cuenta del administrador (único tipo de usuario de la app).
 * Se guarda en Firestore: usuarios/{uid}. La contraseña la maneja Firebase Authentication.
 */
public class Administrador {

    private String nombre;
    private String correo;
    private String telefono = "";
    private String organizacion = "";
    private String uid = "";             // id de la cuenta en Firebase Authentication
    private String foto = "";            // foto de perfil en JPG codificada en texto (base64), "" = sin foto

    public Administrador(String uid, String nombre, String correo) {
        this.uid = uid;
        this.nombre = nombre;
        this.correo = correo;
    }

    // Iniciales para el avatar: "René Hernández" -> "RH"
    public String getIniciales() {
        StringBuilder sb = new StringBuilder();
        for (String palabra : nombre.trim().split("\\s+")) {
            if (!palabra.isEmpty()) sb.append(Character.toUpperCase(palabra.charAt(0)));
            if (sb.length() == 2) break;
        }
        return sb.length() == 0 ? "A" : sb.toString();
    }

    public String getPrimerNombre() {
        String[] partes = nombre.trim().split("\\s+");
        return partes.length > 0 ? partes[0] : nombre;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getOrganizacion() { return organizacion; }
    public void setOrganizacion(String organizacion) { this.organizacion = organizacion; }
    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto == null ? "" : foto; }
    public boolean tieneFoto() { return !foto.isEmpty(); }
    public String getUid() { return uid; }
}
