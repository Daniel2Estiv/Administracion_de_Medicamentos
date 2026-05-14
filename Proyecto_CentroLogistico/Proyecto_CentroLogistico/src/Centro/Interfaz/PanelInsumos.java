package Centro.Interfaz;

import Centro.dao.TipoProductoDAO;
import Centro.modelo.TipoProducto;

/**
 * Panel de Insumos Médicos.
 * Resuelve el id_tipo desde la BD en lugar de hardcodearlo.
 */
public class PanelInsumos extends PanelInventario {

    public PanelInsumos() {
        super(resolverIdTipo(), "💊  Insumos Médicos");
    }

    private static int resolverIdTipo() {
        TipoProductoDAO dao = new TipoProductoDAO();
        // Busca el tipo cuyo nombre contenga "insumo" (case-insensitive)
        for (TipoProducto tp : dao.listarTodos()) {
            if (tp.getNombre().toLowerCase().contains("insumo")) {
                return tp.getIdTipo();
            }
        }
        // Fallback: primer tipo disponible
        var todos = dao.listarTodos();
        return todos.isEmpty() ? 1 : todos.get(0).getIdTipo();
    }
}
