package Centro.modelo;

/**
 * DTO para la entidad Usuario del sistema.
 */
public class Usuario {

    private int idUsuario;
    private String username;
    private String passwordHash;
    private String rol;      // ADMIN | OPERADOR | LECTURA
    private boolean activo;

    public Usuario() {}

    public Usuario(int idUsuario, String username, String passwordHash, String rol, boolean activo) {
        this.idUsuario    = idUsuario;
        this.username     = username;
        this.passwordHash = passwordHash;
        this.rol          = rol;
        this.activo       = activo;
    }

    // ── Getters / Setters ──────────────────────────────────────

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    /** Verifica si el usuario tiene permiso de administrador. */
    public boolean esAdmin() { return "ADMIN".equalsIgnoreCase(rol); }

    /** Verifica si el usuario es operador con permisos de edición. */
    public boolean esOperador() { return "OPERADOR".equalsIgnoreCase(rol); }

    /** Verifica si el usuario es de solo lectura (no puede editar nada). */
    public boolean esLectura() { return "LECTURA".equalsIgnoreCase(rol); }

    /** {@code true} si el usuario puede crear, modificar o eliminar registros. */
    public boolean puedeEditar() { return esAdmin() || esOperador(); }

    @Override
    public String toString() {
        return username + " [" + rol + "]";
    }
}
