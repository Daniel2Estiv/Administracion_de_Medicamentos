package Centro.Interfaz;

import Centro.dao.*;
import Centro.modelo.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel CRUD genérico para cualquier tipo de producto del inventario.
 * Muestra productos + sus lotes (LoteExistencia) en una vista aplanada.
 * Se instancia con el id_tipo del TipoProducto que corresponde.
 *
 * Usado por: PanelInsumos, PanelMedicamentos, PanelBienesDevolutivos
 */
public class PanelInventario extends JPanel {

    // ── Paleta ────────────────────────────────────────────────
    private static final Color AZUL_OSCURO   = new Color(10, 33, 80);
    private static final Color AZUL_BTN      = new Color(0, 102, 204);
    private static final Color VERDE_BTN     = new Color(0, 128, 64);
    private static final Color ROJO_BTN      = new Color(192, 0, 0);
    private static final Color VENCE_ROJO    = new Color(255, 200, 200);
    private static final Color VENCE_NARANJA = new Color(255, 235, 190);
    private static final Color FILA_PAR      = Color.WHITE;
    private static final Color FILA_IMPAR    = new Color(245, 248, 255);

    // ── Columnas de la tabla principal ────────────────────────
    private static final String[] COLUMNAS = {
        "ID Prod.", "Código", "INVIMA", "Nombre", "Presentación",
        "ID Lote", "Lote", "Vencimiento", "Existencias", "Costo", "Ubicación"
    };

    // ── Estado ────────────────────────────────────────────────
    private final int    idTipo;
    private final String tituloPanel;
    private int selectedIdProducto = -1;
    private int selectedIdLote     = -1;

    // ── DAOs ──────────────────────────────────────────────────
    private final ProductoDAO       productoDAO    = new ProductoDAO();
    private final LoteExistenciaDAO loteDAO        = new LoteExistenciaDAO();
    private final UbicacionDAO      ubicDAO        = new UbicacionDAO();
    private final PresentacionDAO   presentDAO     = new PresentacionDAO();
    private final TipoProductoDAO   tipoDAO        = new TipoProductoDAO();

    // ── Componentes UI ────────────────────────────────────────
    private JTable             tabla;
    private DefaultTableModel  modeloTabla;
    private JTextField         txtBuscar;

    // Formulario producto
    private JTextField         txtCodigo, txtInvima, txtNombre;
    private JComboBox<Presentacion> cmbPresentacion;

    // Formulario lote
    private JTextField         txtLote, txtVencimiento, txtExistencias, txtCosto;
    private JComboBox<Ubicacion>    cmbUbicacion;

    private JButton btnNuevoProducto, btnGuardarProducto, btnEliminarProducto;
    private JButton btnNuevoLote,     btnGuardarLote,     btnEliminarLote;

    // ── Constructor ───────────────────────────────────────────

    public PanelInventario(int idTipo, String tituloPanel) {
        this.idTipo      = idTipo;
        this.tituloPanel = tituloPanel;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        construirUI();
        cargarCombos();
        cargarTabla();
    }

    // ── Construcción de UI ────────────────────────────────────

    private void construirUI() {
        add(construirHeader(),   BorderLayout.NORTH);
        add(construirCuerpo(),   BorderLayout.CENTER);
    }

    private JPanel construirHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(AZUL_OSCURO);
        h.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel lbl = new JLabel(tituloPanel);
        // Fuente con soporte emoji: el título del panel puede llevar iconos (💊, 🩺, 📦)
        lbl.setFont(Centro.util.Tema.fontEmoji(Font.BOLD, 18));
        lbl.setForeground(Color.WHITE);

        JPanel busq = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        busq.setBackground(AZUL_OSCURO);
        txtBuscar = new JTextField(20);
        JButton btnBuscar = btn("🔍 Buscar", AZUL_BTN);
        btnBuscar.addActionListener(e -> buscar());
        JButton btnBorrar = btn("🧹 Borrar", new Color(60, 100, 180));
        btnBorrar.setToolTipText("Borra el texto de búsqueda y recarga la tabla completa");
        btnBorrar.addActionListener(e -> { txtBuscar.setText(""); cargarTabla(); });
        busq.add(txtBuscar); busq.add(btnBuscar); busq.add(btnBorrar);

        h.add(lbl,  BorderLayout.WEST);
        h.add(busq, BorderLayout.EAST);
        return h;
    }

    private JPanel construirCuerpo() {
        // Alerta vencimiento
        JLabel alerta = new JLabel("  ⚠️  Rojo: vence ≤7 días  ·  Naranja: vence ≤30 días");
        alerta.setFont(Centro.util.Tema.fontEmoji(Font.PLAIN, 11));
        alerta.setBackground(new Color(255, 248, 220));
        alerta.setOpaque(true);
        alerta.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        // Tabla
        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(24);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(new Color(224, 235, 255));
        tabla.setSelectionBackground(new Color(173, 214, 255));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Ocultar columnas de ID técnicas
        tabla.getColumnModel().getColumn(0).setMinWidth(0);
        tabla.getColumnModel().getColumn(0).setMaxWidth(0);
        tabla.getColumnModel().getColumn(5).setMinWidth(0);
        tabla.getColumnModel().getColumn(5).setMaxWidth(0);

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    String fec = (String) t.getModel().getValueAt(row, 7); // col 7 = Vencimiento
                    if (fec != null && !fec.isBlank()) {
                        try {
                            LocalDate d = LocalDate.parse(fec);
                            LocalDate hoy = LocalDate.now();
                            if (!d.isAfter(hoy.plusDays(7)))       setBackground(VENCE_ROJO);
                            else if (!d.isAfter(hoy.plusDays(30))) setBackground(VENCE_NARANJA);
                            else setBackground(row % 2 == 0 ? FILA_PAR : FILA_IMPAR);
                        } catch (Exception ex) {
                            setBackground(row % 2 == 0 ? FILA_PAR : FILA_IMPAR);
                        }
                    } else {
                        setBackground(row % 2 == 0 ? FILA_PAR : FILA_IMPAR);
                    }
                }
                return this;
            }
        });

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarFormularioDesdeTabla();
        });

        JScrollPane scroll = new JScrollPane(tabla);

        JPanel centro = new JPanel(new BorderLayout(0, 4));
        centro.setBackground(Color.WHITE);
        centro.add(alerta, BorderLayout.NORTH);
        centro.add(scroll, BorderLayout.CENTER);

        // Panel lateral con dos secciones: Producto y Lote
        JPanel lateral = construirPanelLateral();

        JPanel cuerpo = new JPanel(new BorderLayout(8, 0));
        cuerpo.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        cuerpo.setBackground(Color.WHITE);
        cuerpo.add(centro,  BorderLayout.CENTER);
        cuerpo.add(lateral, BorderLayout.EAST);
        return cuerpo;
    }

    private JPanel construirPanelLateral() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 248, 255));
        panel.setBorder(BorderFactory.createLineBorder(new Color(190, 210, 240)));
        panel.setPreferredSize(new Dimension(400, 0));   // ampliado de 270 → 400 para que quepan los textos completos
        panel.setMinimumSize(new Dimension(380, 0));

        panel.add(construirFormProducto());
        panel.add(Box.createVerticalStrut(6));
        panel.add(construirFormLote());
        return panel;
    }

    private JPanel construirFormProducto() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(245, 248, 255));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0, new Color(200,215,240)),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(3, 2, 3, 2);
        g.gridwidth = 2;

        JLabel tit = new JLabel("📦 Producto");
        tit.setFont(Centro.util.Tema.fontEmoji(Font.BOLD, 12));
        tit.setForeground(AZUL_OSCURO);
        g.gridx = 0; g.gridy = 0; p.add(tit, g);

        txtCodigo  = new JTextField(); txtCodigo.setToolTipText("Código interno");
        txtInvima  = new JTextField(); txtInvima.setToolTipText("Registro INVIMA");
        txtNombre  = new JTextField(); txtNombre.setToolTipText("Nombre del producto");
        cmbPresentacion = new JComboBox<>();

        String[][] campos = {{"Código:",""},{"INVIMA:",""},{"Nombre:",""},{"Presentación:",""}};
        JComponent[] ctrls = {txtCodigo, txtInvima, txtNombre, cmbPresentacion};

        for (int i = 0; i < ctrls.length; i++) {
            g.gridwidth = 1; g.gridx = 0; g.gridy = i+1; g.weightx = 0;
            JLabel l = new JLabel(campos[i][0]);
            l.setFont(new Font("Segoe UI", Font.BOLD, 11));
            p.add(l, g);
            g.gridx = 1; g.weightx = 1;
            p.add(ctrls[i], g);
        }

        btnNuevoProducto    = btn("➕ Nuevo",      AZUL_BTN);
        btnGuardarProducto  = btn("💾 Guardar",    VERDE_BTN);
        btnEliminarProducto = btn("🗑️ Eliminar", ROJO_BTN);

        btnNuevoProducto.addActionListener(e -> limpiarFormProducto());
        btnGuardarProducto.addActionListener(e -> guardarProducto());
        btnEliminarProducto.addActionListener(e -> eliminarProducto());

        JPanel bts = new JPanel(new GridLayout(1, 3, 4, 0));
        bts.setBackground(new Color(245, 248, 255));
        bts.add(btnNuevoProducto); bts.add(btnGuardarProducto); bts.add(btnEliminarProducto);

        g.gridwidth = 2; g.gridx = 0; g.gridy = ctrls.length + 1; g.insets = new Insets(6,2,2,2);
        p.add(bts, g);
        return p;
    }

    private JPanel construirFormLote() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(245, 248, 255));
        p.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(3, 2, 3, 2);
        g.gridwidth = 2;

        JLabel tit = new JLabel("🗂️ Lote / Existencia");
        tit.setFont(Centro.util.Tema.fontEmoji(Font.BOLD, 12));
        tit.setForeground(AZUL_OSCURO);
        g.gridx = 0; g.gridy = 0; p.add(tit, g);

        txtLote        = new JTextField(); txtLote.setToolTipText("Número de lote");
        txtVencimiento = new JTextField(); txtVencimiento.setToolTipText("AAAA-MM-DD");
        txtExistencias = new JTextField("0");
        txtCosto       = new JTextField("0.0");
        cmbUbicacion   = new JComboBox<>();

        String[] etiq = {"Lote:","Vencimiento:","Existencias:","Costo:","Ubicación:"};
        JComponent[] ctrls = {txtLote, txtVencimiento, txtExistencias, txtCosto, cmbUbicacion};

        for (int i = 0; i < ctrls.length; i++) {
            g.gridwidth = 1; g.gridx = 0; g.gridy = i+1; g.weightx = 0;
            JLabel l = new JLabel(etiq[i]);
            l.setFont(new Font("Segoe UI", Font.BOLD, 11));
            p.add(l, g);
            g.gridx = 1; g.weightx = 1;
            p.add(ctrls[i], g);
        }

        btnNuevoLote    = btn("➕ Nuevo lote",      AZUL_BTN);
        btnGuardarLote  = btn("💾 Guardar",         VERDE_BTN);
        btnEliminarLote = btn("🗑️ Eliminar Lote", ROJO_BTN);

        btnNuevoLote.addActionListener(e -> limpiarFormLote());
        btnGuardarLote.addActionListener(e -> guardarLote());
        btnEliminarLote.addActionListener(e -> eliminarLote());

        JPanel bts = new JPanel(new GridLayout(1, 3, 4, 0));
        bts.setBackground(new Color(245, 248, 255));
        bts.add(btnNuevoLote); bts.add(btnGuardarLote); bts.add(btnEliminarLote);

        g.gridwidth = 2; g.gridx = 0; g.gridy = ctrls.length + 1; g.insets = new Insets(6,2,2,2);
        p.add(bts, g);
        return p;
    }

    // ── Carga de datos ────────────────────────────────────────

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        for (Object[] f : productoDAO.listarConLotesParaTabla(idTipo)) {
            modeloTabla.addRow(f);
        }
    }

    private void cargarCombos() {
        cmbPresentacion.removeAllItems();
        for (Presentacion pr : presentDAO.listarTodas()) cmbPresentacion.addItem(pr);

        cmbUbicacion.removeAllItems();
        Ubicacion sinAsignar = new Ubicacion(0, "Sin asignar");
        cmbUbicacion.addItem(sinAsignar);
        for (Ubicacion u : ubicDAO.listarTodas()) cmbUbicacion.addItem(u);
    }

    private void cargarFormularioDesdeTabla() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;

        // Producto
        selectedIdProducto = (int) modeloTabla.getValueAt(fila, 0);
        txtCodigo.setText(str(modeloTabla.getValueAt(fila, 1)));
        txtInvima.setText(str(modeloTabla.getValueAt(fila, 2)));
        txtNombre.setText(str(modeloTabla.getValueAt(fila, 3)));
        seleccionarEnCombo(cmbPresentacion, str(modeloTabla.getValueAt(fila, 4)));

        // Lote
        Object idLoteObj = modeloTabla.getValueAt(fila, 5);
        if (idLoteObj != null) {
            selectedIdLote = (int) idLoteObj;
            txtLote.setText(str(modeloTabla.getValueAt(fila, 6)));
            txtVencimiento.setText(str(modeloTabla.getValueAt(fila, 7)));
            txtExistencias.setText(str(modeloTabla.getValueAt(fila, 8)));
            txtCosto.setText(str(modeloTabla.getValueAt(fila, 9)));
            seleccionarUbicacionEnCombo(str(modeloTabla.getValueAt(fila, 10)));
        } else {
            selectedIdLote = -1;
            limpiarFormLote();
        }
    }

    // ── Operaciones ───────────────────────────────────────────

    private void guardarProducto() {
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del producto es obligatorio.");
            return;
        }
        Presentacion pr = (Presentacion) cmbPresentacion.getSelectedItem();
        if (pr == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una presentación.");
            return;
        }
        ItemBodega prod = new ItemBodega(
            selectedIdProducto,
            txtCodigo.getText().trim(),
            txtInvima.getText().trim(),
            txtNombre.getText().trim(),
            pr.getIdPresentacion(),
            idTipo
        );
        boolean ok;
        if (selectedIdProducto < 0) {
            int nuevoId = productoDAO.insertar(prod);
            ok = nuevoId > 0;
            if (ok) selectedIdProducto = nuevoId;
        } else {
            ok = productoDAO.actualizar(prod);
        }
        if (ok) {
            JOptionPane.showMessageDialog(this, "✅ Producto guardado.");
            cargarTabla();
        } else {
            JOptionPane.showMessageDialog(this, "❌ Error al guardar producto.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarProducto() {
        if (selectedIdProducto < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la tabla primero.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "¿Eliminar el producto seleccionado y todos sus lotes?",
                "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (productoDAO.eliminar(selectedIdProducto)) {
                limpiarFormProducto(); limpiarFormLote(); cargarTabla();
            } else {
                JOptionPane.showMessageDialog(this, "❌ No se pudo eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void guardarLote() {
        if (selectedIdProducto < 0) {
            JOptionPane.showMessageDialog(this, "Primero selecciona o guarda un producto.");
            return;
        }
        if (txtVencimiento.getText().trim().isEmpty() && txtLote.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa al menos el Lote o la fecha de Vencimiento.");
            return;
        }
        try {
            int existencias = Integer.parseInt(txtExistencias.getText().trim());
            double costo    = Double.parseDouble(txtCosto.getText().trim());
            Ubicacion ub    = (Ubicacion) cmbUbicacion.getSelectedItem();
            int idUb        = (ub != null && ub.getIdUbicacion() > 0) ? ub.getIdUbicacion() : 0;

            LoteExistencia le = new LoteExistencia(
                selectedIdLote < 0 ? 0 : selectedIdLote,
                selectedIdProducto,
                idUb,
                txtLote.getText().trim(),
                txtVencimiento.getText().trim(),
                existencias,
                costo
            );

            boolean ok;
            if (selectedIdLote < 0) {
                int nId = loteDAO.insertar(le);
                ok = nId > 0;
                if (ok) selectedIdLote = nId;
            } else {
                le.setIdLote(selectedIdLote);
                ok = loteDAO.actualizar(le);
            }

            if (ok) {
                JOptionPane.showMessageDialog(this, "✅ Lote guardado.");
                cargarTabla();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Error al guardar el lote.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Existencias y Costo deben ser números.", "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarLote() {
        if (selectedIdLote < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una fila con lote para eliminar.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar solo este lote?",
                "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (loteDAO.eliminar(selectedIdLote)) {
                limpiarFormLote(); cargarTabla();
            }
        }
    }

    private void buscar() {
        String t = txtBuscar.getText().trim();
        if (t.isEmpty()) { cargarTabla(); return; }
        modeloTabla.setRowCount(0);
        for (Object[] f : productoDAO.buscarConLotes(t, idTipo)) modeloTabla.addRow(f);
    }

    // ── Helpers ───────────────────────────────────────────────

    private void limpiarFormProducto() {
        selectedIdProducto = -1;
        txtCodigo.setText(""); txtInvima.setText(""); txtNombre.setText("");
        if (cmbPresentacion.getItemCount() > 0) cmbPresentacion.setSelectedIndex(0);
        tabla.clearSelection();
    }

    private void limpiarFormLote() {
        selectedIdLote = -1;
        txtLote.setText(""); txtVencimiento.setText("");
        txtExistencias.setText("0"); txtCosto.setText("0.0");
        if (cmbUbicacion.getItemCount() > 0) cmbUbicacion.setSelectedIndex(0);
    }

    private void seleccionarEnCombo(JComboBox<Presentacion> cmb, String desc) {
        for (int i = 0; i < cmb.getItemCount(); i++) {
            if (cmb.getItemAt(i).getDescripcion().equalsIgnoreCase(desc)) {
                cmb.setSelectedIndex(i); return;
            }
        }
    }

    private void seleccionarUbicacionEnCombo(String desc) {
        for (int i = 0; i < cmbUbicacion.getItemCount(); i++) {
            if (cmbUbicacion.getItemAt(i).getDescripcion().equalsIgnoreCase(desc)) {
                cmbUbicacion.setSelectedIndex(i); return;
            }
        }
        cmbUbicacion.setSelectedIndex(0);
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }

    private JButton btn(String txt, Color bg) {
        JButton b = new JButton(txt);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        // Usar la fuente del tema que tiene soporte automático para emojis
        b.setFont(Centro.util.Tema.FUENTE_BTN);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
