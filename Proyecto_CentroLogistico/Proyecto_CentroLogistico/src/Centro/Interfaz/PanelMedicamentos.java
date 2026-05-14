package Centro.Interfaz;

import Centro.dao.TipoProductoDAO;
import Centro.modelo.TipoProducto;

/**
 * Panel de Medicamentos.
 */
public class PanelMedicamentos extends PanelInventario {

    public PanelMedicamentos() {
        super(resolverIdTipo(), "🩺  Medicamentos");
    }

    private static int resolverIdTipo() {
        TipoProductoDAO dao = new TipoProductoDAO();
        for (TipoProducto tp : dao.listarTodos()) {
            if (tp.getNombre().toLowerCase().contains("medic")) {
                return tp.getIdTipo();
            }
        }
        var todos = dao.listarTodos();
        return todos.size() > 1 ? todos.get(1).getIdTipo() : 1;
    }
}
