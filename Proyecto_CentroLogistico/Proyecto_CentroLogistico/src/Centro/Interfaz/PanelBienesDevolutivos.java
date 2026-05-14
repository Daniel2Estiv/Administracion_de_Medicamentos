package Centro.Interfaz;

import Centro.dao.TipoProductoDAO;
import Centro.modelo.TipoProducto;

/**
 * Panel de Bienes Devolutivos.
 */
public class PanelBienesDevolutivos extends PanelInventario {

    public PanelBienesDevolutivos() {
        super(resolverIdTipo(), "📦  Bienes Devolutivos");
    }

    private static int resolverIdTipo() {
        TipoProductoDAO dao = new TipoProductoDAO();
        for (TipoProducto tp : dao.listarTodos()) {
            String n = tp.getNombre().toLowerCase();
            if (n.contains("bien") || n.contains("devol")) {
                return tp.getIdTipo();
            }
        }
        var todos = dao.listarTodos();
        return todos.size() > 2 ? todos.get(2).getIdTipo() : 1;
    }
}
