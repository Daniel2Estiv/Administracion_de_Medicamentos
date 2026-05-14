package Centro.Interfaz;

import Centro.dao.UbicacionDAO;
import Centro.modelo.Ubicacion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel CRUD para la tabla Ubicacion.
 * Cada ubicación tiene solo una descripción libre (ej. "Bodega A - Pasillo 3").
 */
public class PanelUbicacion extends JPanel {

    private static final Color AZUL_OSCURO = new Color(10, 33, 80);
    private static final Color AZUL_BTN    = new Color(0, 102, 204);
    private static final Color ROJO_BTN    = new Color(192, 0, 0);
    private static final Color VERDE_BTN   = new Color(0, 128, 64);

    private JTable            tabla;
    private DefaultTableModel modeloTabla;
    private JTextField        txtDescripcion;
    private JButton           btnNuevo, btnGuardar, btnEliminar;
    private int               idSeleccionado = -1;

    private final UbicacionDAO ubicDAO = new UbicacionDAO();

    public PanelUbicacion() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        construirUI();
        cargarTabla();
    }

    private void construirUI() {
        // Encabezado
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AZUL_OSCURO);
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel titulo = new JLabel("📍  Ubicaciones");
        titulo.setFont(Centro.util.Tema.fontEmoji(Font.BOLD, 18));
        titulo.setForeground(Color.WHITE);
        header.add(titulo, BorderLayout.WEST);

        JLabel nota = new JLabel("Descripción libre de cada punto de almacenamiento  ");
        nota.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        nota.setForeground(new Color(180, 210, 255));
        header.add(nota, BorderLayout.EAST);

        // Tabla
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Descripción"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(26);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(new Color(224, 235, 255));
        tabla.setSelectionBackground(new Color(173, 214, 255));

        // Ocultar columna ID
        tabla.getColumnModel().getColumn(0).setMinWidth(0);
        tabla.getColumnModel().getColumn(0).setMaxWidth(0);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarFormulario();
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 215, 235)));

        // Formulario
        JPanel form = construirFormulario();

        JPanel cuerpo = new JPanel(new BorderLayout(10, 0));
        cuerpo.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        cuerpo.setBackground(Color.WHITE);
        cuerpo.add(scroll, BorderLayout.CENTER);
        cuerpo.add(form,   BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(cuerpo, BorderLayout.CENTER);
    }

    private JPanel construirFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 248, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(190, 210, 240)),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        panel.setPreferredSize(new Dimension(400, 0));   // ampliado de 260 → 400 para que quepan los textos completos
        panel.setMinimumSize(new Dimension(380, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 2, 5, 2);
        g.gridwidth = 2;

        JLabel tit = new JLabel("📋 Nueva Ubicación");
        tit.setFont(Centro.util.Tema.fontEmoji(Font.BOLD, 13));
        tit.setForeground(AZUL_OSCURO);
        g.gridx = 0; g.gridy = 0; panel.add(tit, g);
        g.gridy = 1; panel.add(new JSeparator(), g);

        // Campo descripción
        g.gridwidth = 1; g.gridx = 0; g.gridy = 2; g.weightx = 0;
        JLabel lDesc = new JLabel("Descripción:");
        lDesc.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lDesc, g);

        txtDescripcion = new JTextField();
        txtDescripcion.setToolTipText("Ej: Bodega A - Pasillo 3 - Rack 2");
        g.gridx = 1; g.weightx = 1;
        panel.add(txtDescripcion, g);

        // Nota
        g.gridwidth = 2; g.gridx = 0; g.gridy = 3;
        JLabel nota = new JLabel("<html><font color='#718096' size='2'>" +
            "Ingresa una descripción clara que<br>" +
            "identifique el punto físico de<br>" +
            "almacenamiento (bodega, pasillo,<br>" +
            "estante, etc.).</font></html>");
        panel.add(nota, g);

        // Botones
        btnGuardar  = crearBoton("💾 Guardar",  VERDE_BTN);
        btnNuevo    = crearBoton("➕ Nuevo",     AZUL_BTN);
        btnEliminar = crearBoton("🗑️ Eliminar", ROJO_BTN);

        btnGuardar.addActionListener(e -> guardar());
        btnNuevo.addActionListener(e -> limpiar());
        btnEliminar.addActionListener(e -> eliminar());

        JPanel bts = new JPanel(new GridLayout(1, 3, 6, 0));
        bts.setBackground(new Color(245, 248, 255));
        bts.add(btnNuevo); bts.add(btnGuardar); bts.add(btnEliminar);

        g.gridy = 4; g.insets = new Insets(12, 2, 2, 2);
        panel.add(bts, g);

        return panel;
    }

    // ── Operaciones ───────────────────────────────────────────

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        for (Object[] f : ubicDAO.listarParaTabla()) modeloTabla.addRow(f);
    }

    private void cargarFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        idSeleccionado = (int) modeloTabla.getValueAt(fila, 0);
        txtDescripcion.setText((String) modeloTabla.getValueAt(fila, 1));
    }

    private void guardar() {
        String desc = txtDescripcion.getText().trim();
        if (desc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La descripción es obligatoria.");
            txtDescripcion.requestFocus();
            return;
        }
        Ubicacion u = new Ubicacion(idSeleccionado, desc);
        boolean ok  = idSeleccionado < 0 ? ubicDAO.insertar(u) : ubicDAO.actualizar(u);
        if (ok) {
            JOptionPane.showMessageDialog(this, "✅ Ubicación guardada.");
            limpiar(); cargarTabla();
        } else {
            JOptionPane.showMessageDialog(this,
                "❌ Error al guardar. La descripción puede estar duplicada.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminar() {
        if (idSeleccionado < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una ubicación de la lista.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "¿Eliminar la ubicación seleccionada?\n" +
                "Solo es posible si no tiene lotes asignados.",
                "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (ubicDAO.eliminar(idSeleccionado)) {
                limpiar(); cargarTabla();
            } else {
                JOptionPane.showMessageDialog(this,
                    "No se puede eliminar: tiene lotes asignados o error en BD.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limpiar() {
        idSeleccionado = -1;
        txtDescripcion.setText("");
        tabla.clearSelection();
    }

    private JButton crearBoton(String txt, Color c) {
        JButton b = new JButton(txt);
        b.setBackground(c); b.setForeground(Color.WHITE);
        // Usar la fuente del tema que tiene soporte automático para emojis
        b.setFont(Centro.util.Tema.FUENTE_BTN);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
