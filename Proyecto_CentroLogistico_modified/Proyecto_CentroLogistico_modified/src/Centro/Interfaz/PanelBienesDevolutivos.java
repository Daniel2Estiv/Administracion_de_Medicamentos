package Centro.Interfaz;

import Centro.dao.TipoProductoDAO;
import Centro.modelo.TipoProducto;
import Centro.modelo.Usuario;

/**
 * Panel de Bienes Devolutivos.
 */
public class PanelBienesDevolutivos extends PanelInventario {

    /** Constructor con usuario — aplica restricciones por rol. */
    public PanelBienesDevolutivos(Usuario usuario) {
        super(resolverIdTipo(), "📦  Bienes Devolutivos", usuario);
    }

    /** Constructor sin usuario (compatibilidad). */
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
