package Centro.dao;

import Centro.modelo.Presentacion;
import Centro.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para la tabla Presentacion.
 */
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

    public boolean insertar(String descripcion) {
        String sql = "INSERT INTO Presentacion(descripcion) VALUES(?)";
        try (PreparedStatement ps = ConexionBD.getInstance().getConexion().prepareStatement(sql)) {
            ps.setString(1, descripcion);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al insertar Presentacion", e);
            return false;
        }
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
}
