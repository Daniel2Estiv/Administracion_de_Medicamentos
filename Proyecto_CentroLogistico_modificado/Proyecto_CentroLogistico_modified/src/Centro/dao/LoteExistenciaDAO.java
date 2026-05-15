package Centro.dao;

import Centro.modelo.LoteExistencia;
import Centro.util.ConexionBD;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para la tabla LoteExistencia.
 * Tabla real: LoteExistencia(id_lote, id_producto, id_ubicacion,
 *                              lote, fecha_vencimiento, existencias, costo)
 */
public class LoteExistenciaDAO {

    private static final Logger LOGGER = Logger.getLogger(LoteExistenciaDAO.class.getName());

    // ── CRUD ───────────────────────────────────────────────────

    public int insertar(LoteExistencia le) {
        String sql = "INSERT INTO LoteExistencia(id_producto, id_ubicacion, lote, " +
                     "fecha_vencimiento, existencias, costo) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, le.getIdProducto());
            ps.setInt(2, le.getIdUbicacion());
            ps.setString(3, le.getLote());
            ps.setString(4, le.getFechaVencimiento());
            ps.setInt(5, le.getExistencias());
            ps.setDouble(6, le.getCosto());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al insertar LoteExistencia", e);
            return -1;
        }
    }

    public boolean actualizar(LoteExistencia le) {
        String sql = "UPDATE LoteExistencia SET id_ubicacion=?, lote=?, " +
                     "fecha_vencimiento=?, existencias=?, costo=? WHERE id_lote=?";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setInt(1, le.getIdUbicacion());
            ps.setString(2, le.getLote());
            ps.setString(3, le.getFechaVencimiento());
            ps.setInt(4, le.getExistencias());
            ps.setDouble(5, le.getCosto());
            ps.setInt(6, le.getIdLote());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar LoteExistencia", e);
            return false;
        }
    }

    public boolean eliminar(int idLote) {
        String sql = "DELETE FROM LoteExistencia WHERE id_lote = ?";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setInt(1, idLote);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar LoteExistencia", e);
            return false;
        }
    }

    public LoteExistencia buscarPorId(int idLote) {
        String sql = "SELECT * FROM LoteExistencia WHERE id_lote = ?";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setInt(1, idLote);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar LoteExistencia", e);
        }
        return null;
    }

    /** Lista todos los lotes de un producto. */
    public List<LoteExistencia> listarPorProducto(int idProducto) {
        List<LoteExistencia> lista = new ArrayList<>();
        String sql = "SELECT le.*, COALESCE(u.descripcion,'Sin asignar') AS ubicacion_desc " +
                     "FROM LoteExistencia le " +
                     "LEFT JOIN Ubicacion u ON le.id_ubicacion = u.id_ubicacion " +
                     "WHERE le.id_producto = ? ORDER BY le.fecha_vencimiento";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LoteExistencia le = mapear(rs);
                le.setUbicacionDescripcion(rs.getString("ubicacion_desc"));
                lista.add(le);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar lotes por producto", e);
        }
        return lista;
    }

    /**
     * Lista los productos (con datos de lote) que están almacenados en una
     * ubicación concreta. Pensado para el panel de Ubicaciones.
     * <p>Columnas devueltas:
     * código, nombre, tipo, lote, fecha_vencimiento, existencias, costo.
     */
    public List<Object[]> listarProductosPorUbicacion(int idUbicacion) {
        List<Object[]> lista = new ArrayList<>();
        if (idUbicacion <= 0) return lista;
        final String sql =
            "SELECT p.codigo, p.nombre, tp.nombre AS tipo, " +
            "       le.lote, le.fecha_vencimiento, le.existencias, le.costo " +
            "FROM LoteExistencia le " +
            "JOIN Producto p      ON le.id_producto = p.id_producto " +
            "JOIN TipoProducto tp ON p.id_tipo      = tp.id_tipo " +
            "WHERE le.id_ubicacion = ? " +
            "ORDER BY p.nombre, le.fecha_vencimiento";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setInt(1, idUbicacion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("tipo"),
                        rs.getString("lote"),
                        rs.getString("fecha_vencimiento"),
                        rs.getInt("existencias"),
                        rs.getDouble("costo")
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar productos por ubicación", e);
        }
        return lista;
    }

    // ── Alertas de vencimiento ─────────────────────────────────

    /**
     * Retorna lotes de un tipo de producto que vencen en los próximos [dias] días.
     * Columnas: id_producto, nombre_producto, lote, fecha_vencimiento, existencias, ubicacion
     */
    public List<Object[]> alertasVencimientoPorTipo(int idTipo, int dias) {
        List<Object[]> lista = new ArrayList<>();
        String limite = LocalDate.now().plusDays(dias).toString();
        String sql =
            "SELECT p.id_producto, p.nombre, le.lote, le.fecha_vencimiento, " +
            "       le.existencias, COALESCE(u.descripcion,'Sin asignar') AS ubicacion " +
            "FROM LoteExistencia le " +
            "JOIN Producto p    ON le.id_producto  = p.id_producto " +
            "LEFT JOIN Ubicacion u ON le.id_ubicacion = u.id_ubicacion " +
            "WHERE p.id_tipo = ? " +
            "  AND le.fecha_vencimiento IS NOT NULL " +
            "  AND date(le.fecha_vencimiento) <= date(?) " +
            "ORDER BY le.fecha_vencimiento";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setInt(1, idTipo);
            ps.setString(2, limite);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new Object[]{
                    rs.getInt("id_producto"),
                    rs.getString("nombre"),
                    rs.getString("lote"),
                    rs.getString("fecha_vencimiento"),
                    rs.getInt("existencias"),
                    rs.getString("ubicacion")
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener alertas de vencimiento", e);
        }
        return lista;
    }

    /**
     * Alerta global para el dashboard.
     * Soporta fechas en formato DD/MM/YYYY (BD real) y YYYY-MM-DD (ISO).
     * Devuelve los [limite] primeros lotes próximos a vencer, ordenados por fecha.
     *
     * @param dias ventana de días hacia el futuro (usa 0 para mostrar todos)
     */
    public List<Object[]> alertasVencimientoGlobal(int dias) {
        List<Object[]> lista = new ArrayList<>();

        // Convierte DD/MM/YYYY → YYYY-MM-DD dentro de SQLite para poder comparar.
        // Si la fecha ya está en formato ISO la función substr deja valores incorrectos,
        // por eso se usa CASE para detectar el separador en la posición 3.
        String sql =
            "SELECT tp.nombre AS tipo, p.nombre AS producto, le.lote, " +
            "       le.fecha_vencimiento, le.existencias, " +
            "       CASE WHEN substr(le.fecha_vencimiento,3,1)='/' " +
            "            THEN substr(le.fecha_vencimiento,7,4)||'-'||substr(le.fecha_vencimiento,4,2)||'-'||substr(le.fecha_vencimiento,1,2) " +
            "            ELSE le.fecha_vencimiento END AS fec_iso " +
            "FROM LoteExistencia le " +
            "JOIN Producto p      ON le.id_producto = p.id_producto " +
            "JOIN TipoProducto tp ON p.id_tipo = tp.id_tipo " +
            "WHERE le.fecha_vencimiento IS NOT NULL " +
            "ORDER BY fec_iso " +
            "LIMIT 50";

        try (Statement st = ConexionBD.getInstance().getConexion().createStatement();
             ResultSet rs  = st.executeQuery(sql)) {

            LocalDate hoy    = LocalDate.now();
            LocalDate limite = dias > 0 ? hoy.plusDays(dias) : LocalDate.of(9999, 12, 31);

            while (rs.next()) {
                String fecIso = rs.getString("fec_iso");
                try {
                    LocalDate fec = LocalDate.parse(fecIso);
                    if (!fec.isAfter(limite)) {          // vence dentro de la ventana
                        lista.add(new Object[]{
                            rs.getString("tipo"), rs.getString("producto"),
                            rs.getString("lote"), rs.getString("fecha_vencimiento"),
                            rs.getInt("existencias")
                        });
                    }
                } catch (Exception ignored) {
                    // fecha con formato inesperado, se omite
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener alertas globales", e);
        }
        return lista;
    }

    // ── Mapeo ──────────────────────────────────────────────────

    private LoteExistencia mapear(ResultSet rs) throws SQLException {
        return new LoteExistencia(
            rs.getInt("id_lote"),
            rs.getInt("id_producto"),
            rs.getInt("id_ubicacion"),
            rs.getString("lote"),
            rs.getString("fecha_vencimiento"),
            rs.getInt("existencias"),
            rs.getDouble("costo")
        );
    }
}
