package Centro.dao;

import Centro.modelo.ItemBodega;
import Centro.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para la tabla Producto.
 * Tabla real: Producto(id_producto, codigo TEXT, invima, nombre, id_presentacion FK, id_tipo FK)
 *
 * Todos los métodos que retornan listas para JTable incluyen JOIN con
 * TipoProducto y Presentacion para mostrar nombres descriptivos.
 */
public class ProductoDAO {

    private static final Logger LOGGER = Logger.getLogger(ProductoDAO.class.getName());

    // ── Base JOIN query ────────────────────────────────────────
    private static final String SELECT_BASE =
        "SELECT p.id_producto, p.codigo, p.invima, p.nombre, " +
        "       p.id_presentacion, p.id_tipo, " +
        "       pr.descripcion AS presentacion, " +
        "       tp.nombre      AS tipo " +
        "FROM Producto p " +
        "JOIN Presentacion pr ON p.id_presentacion = pr.id_presentacion " +
        "JOIN TipoProducto tp ON p.id_tipo = tp.id_tipo ";

    // ── Listados ───────────────────────────────────────────────

    /** Retorna todos los productos de un tipo (por id_tipo). */
    public List<ItemBodega> listarPorTipo(int idTipo) {
        List<ItemBodega> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE p.id_tipo = ? ORDER BY p.nombre";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setInt(1, idTipo);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar productos por tipo", e);
        }
        return lista;
    }

    /** Retorna todos los productos sin filtro de tipo. */
    public List<ItemBodega> listarTodos() {
        List<ItemBodega> lista = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY tp.nombre, p.nombre";
        try (Statement st = ConexionBD.getInstance().getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar todos los productos", e);
        }
        return lista;
    }

    /**
     * Datos aplanados para JTable: incluye columnas de LoteExistencia.
     * Columnas: id_producto, codigo, invima, nombre, presentacion, tipo,
     *           id_lote, lote, fecha_vencimiento, existencias, costo, ubicacion
     */
    public List<Object[]> listarConLotesParaTabla(int idTipo) {
        List<Object[]> lista = new ArrayList<>();
        String sql =
            "SELECT p.id_producto, p.codigo, p.invima, p.nombre, " +
            "       pr.descripcion AS presentacion, " +
            "       le.id_lote, le.lote, le.fecha_vencimiento, le.existencias, le.costo, " +
            "       COALESCE(u.descripcion, 'Sin asignar') AS ubicacion " +
            "FROM Producto p " +
            "JOIN Presentacion pr  ON p.id_presentacion = pr.id_presentacion " +
            "JOIN TipoProducto tp  ON p.id_tipo = tp.id_tipo " +
            "LEFT JOIN LoteExistencia le ON p.id_producto = le.id_producto " +
            "LEFT JOIN Ubicacion u       ON le.id_ubicacion = u.id_ubicacion " +
            "WHERE p.id_tipo = ? " +
            "ORDER BY p.nombre, le.fecha_vencimiento";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setInt(1, idTipo);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new Object[]{
                    rs.getInt("id_producto"),
                    rs.getString("codigo"),
                    rs.getString("invima"),
                    rs.getString("nombre"),
                    rs.getString("presentacion"),
                    rs.getObject("id_lote"),           // puede ser null si no hay lote
                    rs.getString("lote"),
                    rs.getString("fecha_vencimiento"),
                    rs.getObject("existencias"),
                    rs.getObject("costo"),
                    rs.getString("ubicacion")
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar productos con lotes", e);
        }
        return lista;
    }

    /** Búsqueda por nombre o código dentro de un tipo. */
    public List<Object[]> buscarConLotes(String termino, int idTipo) {
        List<Object[]> lista = new ArrayList<>();
        String pat = "%" + termino.toLowerCase() + "%";
        String sql =
            "SELECT p.id_producto, p.codigo, p.invima, p.nombre, " +
            "       pr.descripcion AS presentacion, " +
            "       le.id_lote, le.lote, le.fecha_vencimiento, le.existencias, le.costo, " +
            "       COALESCE(u.descripcion, 'Sin asignar') AS ubicacion " +
            "FROM Producto p " +
            "JOIN Presentacion pr  ON p.id_presentacion = pr.id_presentacion " +
            "JOIN TipoProducto tp  ON p.id_tipo = tp.id_tipo " +
            "LEFT JOIN LoteExistencia le ON p.id_producto = le.id_producto " +
            "LEFT JOIN Ubicacion u       ON le.id_ubicacion = u.id_ubicacion " +
            "WHERE p.id_tipo = ? " +
            "  AND (LOWER(p.nombre) LIKE ? OR LOWER(p.codigo) LIKE ? OR LOWER(p.invima) LIKE ?) " +
            "ORDER BY p.nombre";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setInt(1, idTipo);
            ps.setString(2, pat); ps.setString(3, pat); ps.setString(4, pat);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new Object[]{
                    rs.getInt("id_producto"), rs.getString("codigo"),
                    rs.getString("invima"),   rs.getString("nombre"),
                    rs.getString("presentacion"),
                    rs.getObject("id_lote"),  rs.getString("lote"),
                    rs.getString("fecha_vencimiento"), rs.getObject("existencias"),
                    rs.getObject("costo"),    rs.getString("ubicacion")
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar productos", e);
        }
        return lista;
    }

    // ── CRUD ───────────────────────────────────────────────────

    /**
     * Inserta un producto. Devuelve el id_producto generado, o -1 si falla.
     */
    public int insertar(ItemBodega p) {
        String sql = "INSERT INTO Producto(codigo, invima, nombre, id_presentacion, id_tipo) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getInvima());
            ps.setString(3, p.getNombre());
            ps.setInt(4, p.getIdPresentacion());
            ps.setInt(5, p.getIdTipo());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al insertar producto", e);
            return -1;
        }
    }

    public boolean actualizar(ItemBodega p) {
        String sql = "UPDATE Producto SET codigo=?, invima=?, nombre=?, id_presentacion=? WHERE id_producto=?";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getInvima());
            ps.setString(3, p.getNombre());
            ps.setInt(4, p.getIdPresentacion());
            ps.setInt(5, p.getIdProducto());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar producto", e);
            return false;
        }
    }

    public boolean eliminar(int idProducto) {
        String sql = "DELETE FROM Producto WHERE id_producto = ?";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar producto", e);
            return false;
        }
    }

    public ItemBodega buscarPorId(int idProducto) {
        String sql = SELECT_BASE + "WHERE p.id_producto = ?";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar producto por id", e);
        }
        return null;
    }

    // ── Mapeo ──────────────────────────────────────────────────

    private ItemBodega mapear(ResultSet rs) throws SQLException {
        ItemBodega p = new ItemBodega(
            rs.getInt("id_producto"),
            rs.getString("codigo"),
            rs.getString("invima"),
            rs.getString("nombre"),
            rs.getInt("id_presentacion"),
            rs.getInt("id_tipo")
        );
        p.setPresentacion(rs.getString("presentacion"));
        p.setTipo(rs.getString("tipo"));
        return p;
    }
}
