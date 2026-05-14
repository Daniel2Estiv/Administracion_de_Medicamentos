package Centro.dao;

import Centro.modelo.Usuario;
import Centro.util.ConexionBD;
import Centro.util.Seguridad;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para operaciones CRUD sobre la tabla {@code usuarios}.
 * <p>
 * Mejoras frente a la versión anterior:
 * <ul>
 *   <li>Listado de columnas explícito (sin {@code SELECT *}).</li>
 *   <li>{@link ResultSet} cerrado mediante {@code try-with-resources}.</li>
 *   <li>Validación de {@code username} y {@code rol} antes de tocar la BD.</li>
 *   <li>Verificación de duplicados antes de insertar para devolver un error
 *       limpio en lugar de explotar con una {@code UNIQUE constraint failed}.</li>
 * </ul>
 */
public class UsuarioDAO {

    private static final Logger LOGGER = Logger.getLogger(UsuarioDAO.class.getName());
    private static final String COLS = "id_usuario, username, password_hash, rol, activo";

    // ── Autenticación ──────────────────────────────────────────

    /**
     * Verifica las credenciales contra la BD.
     *
     * @return el {@link Usuario} si la autenticación es correcta o {@code null}
     *         si las credenciales son inválidas o el usuario está desactivado.
     */
    public Usuario autenticar(String username, String passwordPlano) {
        if (username == null || username.isBlank() || passwordPlano == null) {
            return null;
        }
        final String sql = "SELECT " + COLS +
                " FROM usuarios WHERE username = ? AND activo = 1";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashGuardado = rs.getString("password_hash");
                    if (Seguridad.verificar(passwordPlano, hashGuardado)) {
                        return mapear(rs);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al autenticar usuario", e);
        }
        return null;
    }

    // ── CRUD ───────────────────────────────────────────────────

    /**
     * Inserta un usuario nuevo. La contraseña debe llegar en claro en el campo
     * {@code passwordHash} del DTO (se hashea antes de persistir).
     *
     * @return {@code true} si se insertó; {@code false} si el username ya
     *         existe o hubo un error de BD.
     */
    public boolean insertar(Usuario u) {
        if (u == null || u.getUsername() == null || u.getUsername().isBlank()) return false;
        if (u.getPasswordHash() == null || u.getPasswordHash().isBlank())     return false;
        if (existeUsername(u.getUsername())) {
            LOGGER.warning("Intento de insertar usuario duplicado: " + u.getUsername());
            return false;
        }
        final String sql = "INSERT INTO usuarios(username, password_hash, rol, activo) VALUES(?,?,?,?)";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setString(1, u.getUsername().trim());
            ps.setString(2, Seguridad.hashSHA256(u.getPasswordHash()));
            ps.setString(3, normalizarRol(u.getRol()));
            ps.setInt   (4, u.isActivo() ? 1 : 0);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al insertar usuario", e);
            return false;
        }
    }

    public boolean actualizar(Usuario u) {
        if (u == null || u.getIdUsuario() <= 0) return false;
        final String sql = "UPDATE usuarios SET rol = ?, activo = ? WHERE id_usuario = ?";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setString(1, normalizarRol(u.getRol()));
            ps.setInt   (2, u.isActivo() ? 1 : 0);
            ps.setInt   (3, u.getIdUsuario());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar usuario", e);
            return false;
        }
    }

    public boolean cambiarPassword(int idUsuario, String nuevaPasswordPlana) {
        if (idUsuario <= 0 || nuevaPasswordPlana == null || nuevaPasswordPlana.isEmpty()) {
            return false;
        }
        final String sql = "UPDATE usuarios SET password_hash = ? WHERE id_usuario = ?";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setString(1, Seguridad.hashSHA256(nuevaPasswordPlana));
            ps.setInt   (2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al cambiar contraseña", e);
            return false;
        }
    }

    /** Baja lógica (marca {@code activo = 0}). */
    public boolean eliminar(int idUsuario) {
        if (idUsuario <= 0) return false;
        final String sql = "UPDATE usuarios SET activo = 0 WHERE id_usuario = ?";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al desactivar usuario", e);
            return false;
        }
    }

    /**
     * Baja física: borra definitivamente el registro de la tabla {@code usuarios}.
     * <p>
     * <strong>Precaución:</strong> esta operación no se puede deshacer. Para una
     * baja reversible, usar {@link #eliminar(int)} (que sólo marca
     * {@code activo = 0}).
     *
     * @return {@code true} si el usuario fue eliminado; {@code false} si no
     *         existe o hay error en la BD.
     */
    public boolean eliminarPermanente(int idUsuario) {
        if (idUsuario <= 0) return false;
        final String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar permanentemente el usuario", e);
            return false;
        }
    }

    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        final String sql = "SELECT " + COLS + " FROM usuarios ORDER BY username";
        try (Statement st = ConexionBD.getInstance().getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar usuarios", e);
        }
        return lista;
    }

    public Usuario buscarPorId(int id) {
        if (id <= 0) return null;
        final String sql = "SELECT " + COLS + " FROM usuarios WHERE id_usuario = ?";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar usuario", e);
        }
        return null;
    }

    /** {@code true} si ya existe un usuario con ese username. */
    public boolean existeUsername(String username) {
        if (username == null || username.isBlank()) return false;
        final String sql = "SELECT 1 FROM usuarios WHERE username = ? LIMIT 1";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar duplicado de username", e);
            return false;
        }
    }

    // ── Helpers ────────────────────────────────────────────────

    /** Normaliza el rol: si viene null o vacío usa OPERADOR; mayúsculas siempre. */
    private static String normalizarRol(String rol) {
        if (rol == null || rol.isBlank()) return "OPERADOR";
        String r = rol.trim().toUpperCase();
        return switch (r) {
            case "ADMIN", "OPERADOR", "LECTURA" -> r;
            default -> "OPERADOR";
        };
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(
            rs.getInt   ("id_usuario"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("rol"),
            rs.getInt   ("activo") == 1
        );
    }
}
