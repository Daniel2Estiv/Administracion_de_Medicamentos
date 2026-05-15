package Centro.Interfaz;

import Centro.dao.TipoProductoDAO;
import Centro.modelo.TipoProducto;
import Centro.modelo.Usuario;

/**
 * Panel de Medicamentos.
 */
public class PanelMedicamentos extends PanelInventario {

    /** Constructor con usuario — aplica restricciones por rol. */
    public PanelMedicamentos(Usuario usuario) {
        super(resolverIdTipo(), "🩺  Medicamentos", usuario);
    }

    /** Constructor sin usuario (compatibilidad). */
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
