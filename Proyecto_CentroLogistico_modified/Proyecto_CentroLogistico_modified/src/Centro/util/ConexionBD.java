package Centro.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton que gestiona la conexión a la base de datos SQLite.
 * <p>
 * Las tablas originales del sistema (Producto, TipoProducto, Presentacion,
 * Ubicacion, LoteExistencia) ya existen en la base y <strong>no</strong> se
 * recrean aquí. Sólo se garantiza la existencia de la tabla {@code usuarios}
 * para el login y se inserta el usuario admin por defecto la primera vez.
 * <p>
 * Mejoras respecto a la versión inicial:
 * <ul>
 *   <li>{@code INSERT OR IGNORE} en lugar de {@code INSERT OR REPLACE} para
 *       no sobrescribir la contraseña del admin cada vez que se arranca.</li>
 *   <li>{@code PRAGMA foreign_keys = ON} para que las claves foráneas se
 *       respeten (SQLite las desactiva por defecto).</li>
 *   <li>Logging del path absoluto para detectar problemas de directorio de
 *       trabajo.</li>
 *   <li>Reintento de conexión si la conexión interna se ha cerrado.</li>
 * </ul>
 */
public final class ConexionBD {

    private static final Logger LOGGER  = Logger.getLogger(ConexionBD.class.getName());
    private static final String DB_PATH = "src/Bd/inventario_general_2.db";
    private static final String URL     = "jdbc:sqlite:" + DB_PATH;

    private static volatile ConexionBD instancia;
    private Connection conexion;

    private ConexionBD() {
        conectar();
    }

    /** Devuelve el singleton (creación perezosa, doble-check con volatile). */
    public static ConexionBD getInstance() {
        ConexionBD ref = instancia;
        if (ref == null) {
            synchronized (ConexionBD.class) {
                ref = instancia;
                if (ref == null) {
                    instancia = ref = new ConexionBD();
                }
            }
        }
        return ref;
    }

    /** Devuelve la conexión activa, reabriéndola si fue cerrada externamente. */
    public Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                conectar();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar conexión", e);
        }
        return conexion;
    }

    // ── Inicialización ─────────────────────────────────────────────

    private void conectar() {
        try {
            Class.forName("org.sqlite.JDBC");
            conexion = DriverManager.getConnection(URL);
            conexion.setAutoCommit(true);
            File f = new File(DB_PATH);
            LOGGER.info(() -> "Conexión SQLite establecida: " + f.getAbsolutePath()
                    + " (existe=" + f.exists() + ")");

            habilitarClavesForaneas();
            crearTablaUsuariosSiNoExiste();
            insertarAdminPorDefectoSiFalta();
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE,
                    "Driver SQLite no encontrado. Agrega sqlite-jdbc al classpath.", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "No se pudo conectar a " + URL, e);
        }
    }

    private void habilitarClavesForaneas() {
        try (Statement st = conexion.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No se pudo activar PRAGMA foreign_keys", e);
        }
    }

    private void crearTablaUsuariosSiNoExiste() throws SQLException {
        final String sql =
            "CREATE TABLE IF NOT EXISTS usuarios (" +
            "  id_usuario    INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  username      TEXT    NOT NULL UNIQUE," +
            "  password_hash TEXT    NOT NULL," +
            "  rol           TEXT    NOT NULL DEFAULT 'OPERADOR' " +
            "                CHECK(rol IN ('ADMIN','OPERADOR','LECTURA'))," +
            "  activo        INTEGER NOT NULL DEFAULT 1" +
            ")";
        try (Statement st = conexion.createStatement()) {
            st.execute(sql);
            LOGGER.info("Tabla 'usuarios' verificada.");
        }
    }

    /**
     * Inserta el usuario admin sólo si no existe. Si el admin ya existe se
     * preserva la contraseña actual (a diferencia de la versión anterior que
     * la reseteaba en cada arranque).
     */
    private void insertarAdminPorDefectoSiFalta() {
        final String sql =
            "INSERT OR IGNORE INTO usuarios(username, password_hash, rol, activo) " +
            "VALUES(?,?,?,?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, "admin");
            ps.setString(2, Seguridad.hashSHA256("admin123"));
            ps.setString(3, "ADMIN");
            ps.setInt   (4, 1);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                LOGGER.info("Usuario admin creado (admin / admin123).");
            } else {
                LOGGER.fine("Usuario admin ya existía: contraseña preservada.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al preparar usuario admin por defecto", e);
        }
    }

    /** Cierra la conexión. Llamar en {@code shutdown}. */
    public void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                LOGGER.info("Conexión cerrada.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error al cerrar conexión", e);
        }
    }
}
