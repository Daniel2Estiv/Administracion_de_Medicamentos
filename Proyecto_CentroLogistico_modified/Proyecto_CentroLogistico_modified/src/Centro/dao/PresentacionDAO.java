package Centro.dao;

import Centro.modelo.Presentacion;
import Centro.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** DAO para la tabla Presentacion. CRUD completo. */
public class PresentacionDAO {

    private static final Logger LOGGER = Logger.getLogger(PresentacionDAO.class.getName());

    public List<Presentacion> listarTodas() {
        List<Presentacion> lista = new ArrayList<>();
        String sql = "SELECT id_presentacion, descripcion FROM Presentacion ORDER BY descripcion";
        try (Statement st = ConexionBD.getInstance().getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Presentacion(rs.getInt("id_presentacion"), rs.getString("descripcion")));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar Presentacion", e);
        }
        return lista;
    }

    public int insertar(String descripcion) {
        String sql = "INSERT INTO Presentacion(descripcion) VALUES(?)";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, descripcion);
            if (ps.executeUpdate() == 0) return -1;
            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al insertar Presentacion", e);
            return -1;
        }
    }

    public boolean actualizar(int idPresentacion, String descripcion) {
        String sql = "UPDATE Presentacion SET descripcion = ? WHERE id_presentacion = ?";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setString(1, descripcion);
            ps.setInt(2, idPresentacion);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar Presentacion", e);
            return false;
        }
    }

    public boolean eliminar(int idPresentacion) {
        String sql = "DELETE FROM Presentacion WHERE id_presentacion = ?";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setInt(1, idPresentacion);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar Presentacion", e);
            return false;
        }
    }

    public int contarProductosAsociados(int idPresentacion) {
        String sql = "SELECT COUNT(*) FROM Producto WHERE id_presentacion = ?";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setInt(1, idPresentacion);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al contar productos asociados", e);
        }
        return 0;
    }

    public Presentacion buscarPorId(int id) {
        String sql = "SELECT id_presentacion, descripcion FROM Presentacion WHERE id_presentacion = ?";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new Presentacion(rs.getInt("id_presentacion"), rs.getString("descripcion"));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar Presentacion", e);
        }
        return null;
    }

    public Presentacion buscarPorDescripcion(String descripcion) {
        String sql = "SELECT id_presentacion, descripcion FROM Presentacion " +
                     "WHERE LOWER(descripcion) = LOWER(?)";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setString(1, descripcion);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new Presentacion(rs.getInt("id_presentacion"), rs.getString("descripcion"));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar Presentacion por descripción", e);
        }
        return null;
    }
}
