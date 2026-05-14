package Centro.dao;

import Centro.modelo.TipoProducto;
import Centro.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO de solo lectura para la tabla TipoProducto.
 */
public class TipoProductoDAO {

    private static final Logger LOGGER = Logger.getLogger(TipoProductoDAO.class.getName());

    public List<TipoProducto> listarTodos() {
        List<TipoProducto> lista = new ArrayList<>();
        String sql = "SELECT id_tipo, nombre FROM TipoProducto ORDER BY nombre";
        try (Statement st = ConexionBD.getInstance().getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new TipoProducto(rs.getInt("id_tipo"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar TipoProducto", e);
        }
        return lista;
    }

    /** Busca un tipo por su nombre exacto (case-insensitive). */
    public TipoProducto buscarPorNombre(String nombre) {
        String sql = "SELECT id_tipo, nombre FROM TipoProducto WHERE LOWER(nombre) = LOWER(?)";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new TipoProducto(rs.getInt("id_tipo"), rs.getString("nombre"));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar TipoProducto por nombre", e);
        }
        return null;
    }

    public TipoProducto buscarPorId(int idTipo) {
        String sql = "SELECT id_tipo, nombre FROM TipoProducto WHERE id_tipo = ?";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setInt(1, idTipo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new TipoProducto(rs.getInt("id_tipo"), rs.getString("nombre"));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar TipoProducto por id", e);
        }
        return null;
    }
}
