package Centro.Interfaz;

import Centro.dao.PresentacionDAO;
import Centro.modelo.Presentacion;
import Centro.modelo.Usuario;
import Centro.util.Tema;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Panel CRUD para la tabla {@code Presentacion}. */
public class PanelPresentaciones extends JPanel {

    private static final Color AZUL_OSCURO = new Color(10, 33, 80);
    private static final Color AZUL_BTN    = new Color(0, 102, 204);
    private static final Color VERDE_BTN   = new Color(0, 128, 64);
    private static final Color ROJO_BTN    = new Color(192, 0, 0);
    private static final Color FILA_PAR    = Color.WHITE;
    private static final Color FILA_IMPAR  = new Color(245, 248, 255);

    private final Usuario usuarioActual;
    private final boolean soloLectura;

    private JTable            tabla;
    private DefaultTableModel modelo;
    private JTextField        txtBuscar;
    private JTextField        txtDescripcion;
    private JButton           btnNuevo, btnGuardar, btnEliminar;
    private int               idSeleccionado = -1;

    private final PresentacionDAO presentDAO = new PresentacionDAO();

    public PanelPresentaciones(Usuario usuario) {
        this.usuarioActual = usuario;
        this.soloLectura   = usuario != null && usuario.esLectura();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        construirUI();
        cargarTabla();
    }

    public PanelPresentaciones() { this(null); }

    private void construirUI() {
        add(construirHeader(), BorderLayout.NORTH);
        add(construirCuerpo(), BorderLayout.CENTER);
    }

    private JPanel construirHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(AZUL_OSCURO);
        h.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel lbl = new JLabel("🏷  Presentaciones");
        lbl.setFont(Tema.fontEmoji(Font.BOLD, 18));
        lbl.setForeground(Color.WHITE);

        JPanel busq = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        busq.setOpaque(false);

        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBuscar.setForeground(new Color(200, 220, 255));

        txtBuscar = new JTextField(18);
        txtBuscar.addActionListener(e -> buscar());

        JButton btnBuscar = btn("🔍 Buscar", AZUL_BTN);
        btnBuscar.addActionListener(e -> buscar());
        JButton btnBorrar = btn("🧹 Borrar", new Color(60, 100, 180));
        btnBorrar.addActionListener(e -> { txtBuscar.setText(""); cargarTabla(); });

        busq.add(lblBuscar); busq.add(txtBuscar); busq.add(btnBuscar); busq.add(btnBorrar);

        h.add(lbl, BorderLayout.WEST);
        h.add(busq, BorderLayout.EAST);
        return h;
    }

    private JPanel construirCuerpo() {
        String[] cols = {"ID", "Descripción"};
        modelo = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(26);
        tabla.setFont(Tema.FUENTE_LIST);
        tabla.getTableHeader().setFont(Tema.FUENTE_LBL_BIG);
        tabla.getTableHeader().setBackground(Tema.AZUL_HEADER);
        tabla.setSelectionBackground(Tema.AZUL_SELECCION);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getColumnModel().getColumn(0).setMinWidth(0);
        tabla.getColumnModel().getColumn(0).setMaxWidth(0);
        tabla.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                    boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) setBackground(row % 2 == 0 ? FILA_PAR : FILA_IMPAR);
                return this;
            }
        });
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarFormularioDesdeTabla();
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(Tema.AZUL_BORDE));

        JPanel cuerpo = new JPanel(new BorderLayout(8, 0));
        cuerpo.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        cuerpo.setBackground(Color.WHITE);
        cuerpo.add(scroll, BorderLayout.CENTER);

        if (!soloLectura) cuerpo.add(construirFormulario(), BorderLayout.EAST);
        return cuerpo;
    }

    private JPanel construirFormulario() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Tema.AZUL_BG_FORM);
        panel.setBorder(BorderFactory.createLineBorder(Tema.AZUL_BORDE));
        panel.setPreferredSize(new Dimension(320, 0));
        panel.setMinimumSize(new Dimension(300, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Tema.AZUL_BG_FORM);
        form.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 2, 4, 2);

        JLabel tit = new JLabel("🏷  Datos de la Presentación");
        tit.setFont(Tema.fontEmoji(Font.BOLD, 13));
        tit.setForeground(AZUL_OSCURO);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        form.add(tit, g);

        g.gridy = 1;
        JLabel sep = new JLabel("— Información —");
        sep.setFont(new Font("Segoe UI", Font.BOLD, 10));
        sep.setForeground(new Color(80, 110, 170));
        sep.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.AZUL_BORDE));
        form.add(sep, g);

        g.gridwidth = 1;
        g.gridx = 0; g.gridy = 2; g.weightx = 0;
        JLabel lbl = new JLabel("Descripción:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        form.add(lbl, g);

        g.gridx = 1; g.weightx = 1;
        txtDescripcion = new JTextField();
        txtDescripcion.setToolTipText("Ej.: Caja x 10, Frasco 500 ml, Sobre x 5");
        form.add(txtDescripcion, g);

        g.gridx = 0; g.gridy = 3; g.gridwidth = 2; g.weightx = 1;
        g.insets = new Insets(10, 2, 4, 2);
        JLabel nota = new JLabel("<html><i>Ej.: Caja x 10, Frasco 500 ml, Ampolla x 5, Sobre, Blister</i></html>");
        nota.setFont(Tema.FUENTE_NOTA);
        nota.setForeground(new Color(110, 130, 170));
        form.add(nota, g);

        btnNuevo    = btn("➕ Nuevo",     AZUL_BTN);
        btnGuardar  = btn("💾 Guardar",   VERDE_BTN);
        btnEliminar = btn("🗑 Eliminar",  ROJO_BTN);
        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());

        JPanel bts = new JPanel(new GridLayout(1, 3, 4, 0));
        bts.setBackground(Tema.AZUL_BG_FORM);
        bts.add(btnNuevo); bts.add(btnGuardar); bts.add(btnEliminar);

        g.gridx = 0; g.gridy = 4; g.gridwidth = 2;
        g.insets = new Insets(14, 2, 4, 2);
        form.add(bts, g);

        g.gridy = 5; g.weighty = 1;
        form.add(new JLabel(), g);

        panel.add(form, BorderLayout.NORTH);
        return panel;
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        for (Presentacion p : presentDAO.listarTodas()) {
            modelo.addRow(new Object[]{p.getIdPresentacion(), p.getDescripcion()});
        }
    }

    private void buscar() {
        String t = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        if (t.isEmpty()) { cargarTabla(); return; }
        modelo.setRowCount(0);
        int n = 0;
        for (Presentacion p : presentDAO.listarTodas()) {
            if (p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(t)) {
                modelo.addRow(new Object[]{p.getIdPresentacion(), p.getDescripcion()});
                n++;
            }
        }
        if (n == 0) JOptionPane.showMessageDialog(this,
                "No se encontraron presentaciones que contengan \"" + t + "\".",
                "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cargarFormularioDesdeTabla() {
        if (soloLectura) return;
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        idSeleccionado = (int) modelo.getValueAt(fila, 0);
        txtDescripcion.setText(String.valueOf(modelo.getValueAt(fila, 1)));
    }

    private void guardar() {
        String desc = txtDescripcion.getText() == null ? "" : txtDescripcion.getText().trim();
        if (desc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La descripción es obligatoria.",
                    "Validación", JOptionPane.WARNING_MESSAGE); return;
        }
        if (desc.length() > 80) {
            JOptionPane.showMessageDialog(this, "Máx. 80 caracteres.",
                    "Validación", JOptionPane.WARNING_MESSAGE); return;
        }
        Presentacion dup = presentDAO.buscarPorDescripcion(desc);
        if (dup != null && dup.getIdPresentacion() != idSeleccionado) {
            JOptionPane.showMessageDialog(this, "Ya existe una presentación con esa descripción.",
                    "Duplicado", JOptionPane.WARNING_MESSAGE); return;
        }
        boolean ok;
        if (idSeleccionado < 0) {
            int nuevoId = presentDAO.insertar(desc);
            ok = nuevoId > 0;
            if (ok) idSeleccionado = nuevoId;
        } else {
            ok = presentDAO.actualizar(idSeleccionado, desc);
        }
        if (ok) {
            JOptionPane.showMessageDialog(this, "✅ Presentación guardada.");
            cargarTabla();
            limpiarFormulario();
        } else {
            JOptionPane.showMessageDialog(this, "❌ No se pudo guardar.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminar() {
        if (idSeleccionado < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una presentación primero.",
                    "Selección requerida", JOptionPane.INFORMATION_MESSAGE); return;
        }
        int asociados = presentDAO.contarProductosAsociados(idSeleccionado);
        if (asociados > 0) {
            JOptionPane.showMessageDialog(this,
                    "No se puede eliminar: está asociada a " + asociados + " producto(s).",
                    "Presentación en uso", JOptionPane.WARNING_MESSAGE); return;
        }
        int resp = JOptionPane.showConfirmDialog(this, "¿Eliminar la presentación seleccionada?",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (resp == JOptionPane.YES_OPTION) {
            if (presentDAO.eliminar(idSeleccionado)) {
                JOptionPane.showMessageDialog(this, "✅ Presentación eliminada.");
                limpiarFormulario();
                cargarTabla();
            } else {
                JOptionPane.showMessageDialog(this, "❌ No se pudo eliminar.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = -1;
        txtDescripcion.setText("");
        tabla.clearSelection();
        txtDescripcion.requestFocus();
    }

    private JButton btn(String txt, Color bg) {
        JButton b = new JButton(txt);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(Tema.FUENTE_BTN);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
