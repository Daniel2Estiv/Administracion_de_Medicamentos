package Centro.dao;

import Centro.modelo.Ubicacion;
import Centro.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para la tabla Ubicacion.
 * Tabla real: Ubicacion(id_ubicacion INTEGER PK, descripcion TEXT UNIQUE)
 */
public class UbicacionDAO {

    private static final Logger LOGGER = Logger.getLogger(UbicacionDAO.class.getName());

    public List<Ubicacion> listarTodas() {
        List<Ubicacion> lista = new ArrayList<>();
        String sql = "SELECT id_ubicacion, descripcion FROM Ubicacion ORDER BY descripcion";
        try (Statement st = ConexionBD.getInstance().getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Ubicacion(rs.getInt("id_ubicacion"), rs.getString("descripcion")));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar ubicaciones", e);
        }
        return lista;
    }

    public List<Object[]> listarParaTabla() {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT id_ubicacion, descripcion FROM Ubicacion ORDER BY descripcion";
        try (Statement st = ConexionBD.getInstance().getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Object[]{ rs.getInt("id_ubicacion"), rs.getString("descripcion") });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar ubicaciones para tabla", e);
        }
        return lista;
    }

    public boolean insertar(Ubicacion u) {
        String sql = "INSERT INTO Ubicacion(descripcion) VALUES(?)";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setString(1, u.getDescripcion());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al insertar ubicación", e);
            return false;
        }
    }

    public boolean actualizar(Ubicacion u) {
        String sql = "UPDATE Ubicacion SET descripcion = ? WHERE id_ubicacion = ?";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setString(1, u.getDescripcion());
            ps.setInt(2, u.getIdUbicacion());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar ubicación", e);
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM Ubicacion WHERE id_ubicacion = ?";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar ubicación (posiblemente en uso)", e);
            return false;
        }
    }

    public Ubicacion buscarPorId(int id) {
        String sql = "SELECT id_ubicacion, descripcion FROM Ubicacion WHERE id_ubicacion = ?";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new Ubicacion(rs.getInt("id_ubicacion"), rs.getString("descripcion"));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar ubicación", e);
        }
        return null;
    }
}
