package Centro.Interfaz;

import Centro.dao.*;
import Centro.modelo.*;
import Centro.util.Tema;

import Centro.util.DatePickerField;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel CRUD genérico para cualquier tipo de producto del inventario.
 * Muestra productos + sus lotes (LoteExistencia) en una vista aplanada.
 * Se instancia con el id_tipo del TipoProducto que corresponde.
 *
 * El formulario lateral unifica Producto + Lote en un único panel
 * "Ingreso de Producto", que guarda ambas entidades en una sola acción.
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
    private final int     idTipo;
    private final String  tituloPanel;
    private final Usuario usuarioActual;
    private final boolean soloLectura;
    private int selectedIdProducto = -1;
    private int selectedIdLote     = -1;

    // ── DAOs ──────────────────────────────────────────────────
    private final ProductoDAO       productoDAO = new ProductoDAO();
    private final LoteExistenciaDAO loteDAO     = new LoteExistenciaDAO();
    private final UbicacionDAO      ubicDAO     = new UbicacionDAO();
    private final PresentacionDAO   presentDAO  = new PresentacionDAO();
    private final TipoProductoDAO   tipoDAO     = new TipoProductoDAO();

    // ── Componentes UI ────────────────────────────────────────
    private JTable             tabla;
    private DefaultTableModel  modeloTabla;
    private JTextField         txtBuscar;
    private JComboBox<String>  cmbCampoBusqueda;

    // Formulario unificado — datos de Producto
    private JTextField              txtCodigo, txtInvima, txtNombre;
    private JComboBox<Presentacion> cmbPresentacion;

    // Formulario unificado — datos de Lote
    private JTextField           txtLote, txtExistencias, txtCosto;
    /** Selector tipo calendario para la fecha de vencimiento. */
    private DatePickerField      dpVencimiento;
    private JComboBox<Ubicacion> cmbUbicacion;

    // Botones únicos del formulario unificado
    private JButton btnNuevo, btnGuardar, btnEliminar;

    // ── Constructor ───────────────────────────────────────────

    public PanelInventario(int idTipo, String tituloPanel, Usuario usuario) {
        this.idTipo        = idTipo;
        this.tituloPanel   = tituloPanel;
        this.usuarioActual = usuario;
        this.soloLectura   = usuario != null && usuario.esLectura();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        construirUI();
        cargarCombos();
        cargarTabla();

        // Refresca los combos (Presentación, Ubicación) cada vez que el panel
        // se hace visible, para reflejar altas hechas en otras pestañas.
        addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) {
                cargarCombos();
            }
        });
    }

    public PanelInventario(int idTipo, String tituloPanel) {
        this(idTipo, tituloPanel, null);
    }

    // ── Construcción de UI ────────────────────────────────────

    private void construirUI() {
        add(construirHeader(), BorderLayout.NORTH);
        add(construirCuerpo(), BorderLayout.CENTER);
    }

    private JPanel construirHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(AZUL_OSCURO);
        h.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel lbl = new JLabel(tituloPanel);
        lbl.setFont(Centro.util.Tema.fontEmoji(Font.BOLD, 18));
        lbl.setForeground(Color.WHITE);

        JPanel busq = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        busq.setBackground(AZUL_OSCURO);

        JLabel lblBuscarPor = new JLabel("Buscar por:");
        lblBuscarPor.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBuscarPor.setForeground(new Color(200, 220, 255));

        cmbCampoBusqueda = new JComboBox<>(new String[]{
            "Todos los campos", "Código", "Nombre", "INVIMA",
            "Lote", "Ubicación", "Presentación"
        });
        cmbCampoBusqueda.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbCampoBusqueda.setPreferredSize(new Dimension(160, 28));

        txtBuscar = new JTextField(20);
        txtBuscar.setToolTipText("Escribe el texto a buscar y pulsa Enter o el botón Buscar");
        txtBuscar.addActionListener(e -> buscar());

        JButton btnBuscar = btn("🔍 Buscar", AZUL_BTN);
        btnBuscar.addActionListener(e -> buscar());
        JButton btnBorrar = btn("🧹 Borrar", new Color(60, 100, 180));
        btnBorrar.setToolTipText("Borra el texto de búsqueda y recarga la tabla completa");
        btnBorrar.addActionListener(e -> {
            txtBuscar.setText("");
            cmbCampoBusqueda.setSelectedIndex(0);
            cargarTabla();
        });

        busq.add(lblBuscarPor);
        busq.add(cmbCampoBusqueda);
        busq.add(txtBuscar);
        busq.add(btnBuscar);
        busq.add(btnBorrar);

        h.add(lbl,  BorderLayout.WEST);
        h.add(busq, BorderLayout.EAST);
        return h;
    }

    private JPanel construirCuerpo() {
        JLabel alerta = new JLabel("  ⚠️  Rojo: vence ≤7 días  ·  Naranja: vence ≤30 días");
        alerta.setFont(Centro.util.Tema.fontEmoji(Font.PLAIN, 11));
        alerta.setBackground(new Color(255, 248, 220));
        alerta.setOpaque(true);
        alerta.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

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
                    String fec = (String) t.getModel().getValueAt(row, 7);
                    LocalDate d = parseFechaFlexible(fec);
                    if (d != null) {
                        LocalDate hoy = LocalDate.now();
                        if (!d.isAfter(hoy.plusDays(7)))       setBackground(VENCE_ROJO);
                        else if (!d.isAfter(hoy.plusDays(30))) setBackground(VENCE_NARANJA);
                        else setBackground(row % 2 == 0 ? FILA_PAR : FILA_IMPAR);
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

        JPanel cuerpo = new JPanel(new BorderLayout(8, 0));
        cuerpo.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        cuerpo.setBackground(Color.WHITE);
        cuerpo.add(centro, BorderLayout.CENTER);

        if (!soloLectura) {
            cuerpo.add(construirPanelLateral(), BorderLayout.EAST);
        }
        return cuerpo;
    }

    /**
     * Panel lateral con UN ÚNICO formulario unificado "Ingreso de Producto"
     * que contiene los campos del Producto y del Lote juntos.
     */
    private JPanel construirPanelLateral() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 248, 255));
        panel.setBorder(BorderFactory.createLineBorder(new Color(190, 210, 240)));
        panel.setPreferredSize(new Dimension(400, 0));
        panel.setMinimumSize(new Dimension(380, 0));

        JPanel form = construirFormUnificado();
        JScrollPane scroll = new JScrollPane(form,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(245, 248, 255));

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Formulario unificado: datos de Producto + datos del Lote
     * en un solo panel con título "📋 Ingreso de Producto".
     */
    private JPanel construirFormUnificado() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(245, 248, 255));
        p.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        GridBagConstraints g = new GridBagConstraints();
        g.fill      = GridBagConstraints.HORIZONTAL;
        g.insets    = new Insets(3, 2, 3, 2);
        g.gridwidth = 2;

        // ── Título ───────────────────────────────────────────
        JLabel tit = new JLabel("📋 Ingreso de Producto");
        tit.setFont(Centro.util.Tema.fontEmoji(Font.BOLD, 13));
        tit.setForeground(AZUL_OSCURO);
        g.gridx = 0; g.gridy = 0;
        p.add(tit, g);

        // ── Separador "Datos del Producto" ───────────────────
        g.gridy = 1;
        p.add(separador("— Datos del Producto —"), g);

        // Campos producto
        txtCodigo       = new JTextField(); txtCodigo.setToolTipText("Código interno");
        txtInvima       = new JTextField(); txtInvima.setToolTipText("Registro INVIMA");
        txtNombre       = new JTextField(); txtNombre.setToolTipText("Nombre del producto");
        cmbPresentacion = new JComboBox<>();

        String[]     etiqProd = {"Código:", "INVIMA:", "Nombre:", "Presentación:"};
        JComponent[] ctrlProd = {txtCodigo, txtInvima, txtNombre, cmbPresentacion};

        for (int i = 0; i < ctrlProd.length; i++) {
            g.gridwidth = 1; g.gridx = 0; g.gridy = i + 2; g.weightx = 0;
            JLabel l = new JLabel(etiqProd[i]);
            l.setFont(new Font("Segoe UI", Font.BOLD, 11));
            p.add(l, g);
            g.gridx = 1; g.weightx = 1;
            p.add(ctrlProd[i], g);
        }

        // ── Separador "Datos del Lote" ────────────────────────
        int baseRow = 2 + ctrlProd.length;
        g.gridwidth = 2; g.gridx = 0; g.gridy = baseRow; g.weightx = 0;
        g.insets = new Insets(8, 2, 3, 2);
        p.add(separador("— Datos del Lote / Existencia —"), g);
        g.insets = new Insets(3, 2, 3, 2);

        // Campos lote
        txtLote        = new JTextField(); txtLote.setToolTipText("Número de lote");
        dpVencimiento  = new DatePickerField();
        txtExistencias = new JTextField("0");
        txtCosto       = new JTextField("0.0");
        cmbUbicacion   = new JComboBox<>();

        String[]     etiqLote = {"Lote:", "Vencimiento:", "Existencias:", "Costo:", "Ubicación:"};
        JComponent[] ctrlLote = {txtLote, dpVencimiento, txtExistencias, txtCosto, cmbUbicacion};

        for (int i = 0; i < ctrlLote.length; i++) {
            g.gridwidth = 1; g.gridx = 0; g.gridy = baseRow + 1 + i; g.weightx = 0;
            JLabel l = new JLabel(etiqLote[i]);
            l.setFont(new Font("Segoe UI", Font.BOLD, 11));
            p.add(l, g);
            g.gridx = 1; g.weightx = 1;
            p.add(ctrlLote[i], g);
        }

        // ── Botones ───────────────────────────────────────────
        btnNuevo    = btn("➕ Nuevo",     AZUL_BTN);
        btnGuardar  = btn("💾 Guardar",   VERDE_BTN);
        btnEliminar = btn("🗑️ Eliminar", ROJO_BTN);

        btnNuevo.setToolTipText("Limpiar el formulario para ingresar un nuevo producto");
        btnGuardar.setToolTipText("Guardar producto y lote en un solo paso");
        btnEliminar.setToolTipText("Eliminar el producto seleccionado y todos sus lotes");

        btnNuevo.addActionListener(e    -> limpiarFormulario());
        btnGuardar.addActionListener(e  -> guardarProductoConLote());
        btnEliminar.addActionListener(e -> eliminarProducto());

        JPanel bts = new JPanel(new GridLayout(1, 3, 4, 0));
        bts.setBackground(new Color(245, 248, 255));
        bts.add(btnNuevo); bts.add(btnGuardar); bts.add(btnEliminar);

        int btnRow = baseRow + 1 + ctrlLote.length;
        g.gridwidth = 2; g.gridx = 0; g.gridy = btnRow;
        g.insets = new Insets(10, 2, 4, 2);
        p.add(bts, g);

        // Relleno inferior para que el GridBag no estire los campos
        g.gridy = btnRow + 1; g.weighty = 1;
        p.add(new JLabel(), g);

        return p;
    }

    /** Crea una etiqueta separadora de sección con estilo visual. */
    private JLabel separador(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(80, 110, 170));
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(190, 210, 240)));
        return lbl;
    }

    // ── Carga de datos ────────────────────────────────────────

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        for (Object[] f : productoDAO.listarConLotesParaTabla(idTipo)) {
            modeloTabla.addRow(f);
        }
    }

    private void cargarCombos() {
        if (soloLectura) return;
        cmbPresentacion.removeAllItems();
        for (Presentacion pr : presentDAO.listarTodas()) cmbPresentacion.addItem(pr);

        cmbUbicacion.removeAllItems();
        // Asegura que exista en BD una ubicación "Sin asignar" con un id
        // válido. LoteExistencia.id_ubicacion es FK NOT NULL en SQLite; si
        // usamos id=0 (que no existe), el INSERT del lote falla y aparece
        // el mensaje "Lote no guardado".
        List<Ubicacion> ubicaciones = ubicDAO.listarTodas();
        Ubicacion sinAsignar = null;
        for (Ubicacion u : ubicaciones) {
            if (u.getDescripcion() != null
                    && u.getDescripcion().equalsIgnoreCase("Sin asignar")) {
                sinAsignar = u; break;
            }
        }
        if (sinAsignar == null) {
            ubicDAO.insertar(new Ubicacion(0, "Sin asignar"));
            ubicaciones = ubicDAO.listarTodas();
            for (Ubicacion u : ubicaciones) {
                if (u.getDescripcion() != null
                        && u.getDescripcion().equalsIgnoreCase("Sin asignar")) {
                    sinAsignar = u; break;
                }
            }
        }
        if (sinAsignar != null) cmbUbicacion.addItem(sinAsignar);
        for (Ubicacion u : ubicaciones) {
            if (sinAsignar != null && u.getIdUbicacion() == sinAsignar.getIdUbicacion()) continue;
            cmbUbicacion.addItem(u);
        }
    }

    private void cargarFormularioDesdeTabla() {
        if (soloLectura) return;
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;

        // Datos del Producto
        selectedIdProducto = (int) modeloTabla.getValueAt(fila, 0);
        txtCodigo.setText(str(modeloTabla.getValueAt(fila, 1)));
        txtInvima.setText(str(modeloTabla.getValueAt(fila, 2)));
        txtNombre.setText(str(modeloTabla.getValueAt(fila, 3)));
        seleccionarEnCombo(cmbPresentacion, str(modeloTabla.getValueAt(fila, 4)));

        // Datos del Lote
        Object idLoteObj = modeloTabla.getValueAt(fila, 5);
        if (idLoteObj != null) {
            selectedIdLote = (int) idLoteObj;
            txtLote.setText(str(modeloTabla.getValueAt(fila, 6)));
            setFechaEnPicker(str(modeloTabla.getValueAt(fila, 7)));
            txtExistencias.setText(str(modeloTabla.getValueAt(fila, 8)));
            txtCosto.setText(str(modeloTabla.getValueAt(fila, 9)));
            seleccionarUbicacionEnCombo(str(modeloTabla.getValueAt(fila, 10)));
        } else {
            selectedIdLote = -1;
            limpiarCamposLote();
        }
    }

    // ── Operaciones ───────────────────────────────────────────

    /**
     * Guarda el producto y, si hay datos de lote, también el lote.
     * Ambas operaciones se hacen en una sola acción (botón "Guardar").
     */
    private void guardarProductoConLote() {
        // Validación mínima del producto
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "El nombre del producto es obligatorio.", "Validación",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        Presentacion pr = (Presentacion) cmbPresentacion.getSelectedItem();
        if (pr == null) {
            JOptionPane.showMessageDialog(this,
                "Selecciona una presentación.", "Validación",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── 1) Guardar / actualizar Producto ──────────────────
        ItemBodega prod = new ItemBodega(
            selectedIdProducto,
            txtCodigo.getText().trim(),
            txtInvima.getText().trim(),
            txtNombre.getText().trim(),
            pr.getIdPresentacion(),
            idTipo
        );

        boolean eraNuevoProducto = selectedIdProducto < 0;
        boolean okProducto;

        if (eraNuevoProducto) {
            int nuevoId = productoDAO.insertar(prod);
            okProducto = nuevoId > 0;
            if (okProducto) selectedIdProducto = nuevoId;
        } else {
            okProducto = productoDAO.actualizar(prod);
        }

        if (!okProducto) {
            JOptionPane.showMessageDialog(this,
                "❌ Error al guardar el producto.", "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ── 2) Guardar / actualizar Lote (si hay datos) ───────
        boolean tieneDatosLote = hayDatosDeLote();
        boolean okLote = true;

        if (tieneDatosLote) {
            okLote = guardarLoteInterno();
        }

        // ── 3) Mensaje de resultado ───────────────────────────
        if (tieneDatosLote) {
            if (okLote) {
                JOptionPane.showMessageDialog(this,
                    "✅ Producto y lote guardados correctamente.");
            } else {
                JOptionPane.showMessageDialog(this,
                    "⚠ Producto guardado, pero el lote no se pudo guardar.\n"
                    + "Verifique: Existencias y Costo deben ser números,\n"
                    + "y la Fecha en formato AAAA-MM-DD.",
                    "Lote no guardado", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "✅ Producto guardado.");
        }

        cargarTabla();
    }

    /**
     * Lógica interna para insertar o actualizar el lote.
     * Retorna true si la operación fue exitosa.
     */
    private boolean guardarLoteInterno() {
        if (selectedIdProducto < 0) return false;
        try {
            int    existencias = Integer.parseInt(txtExistencias.getText().trim());
            double costo       = Double.parseDouble(txtCosto.getText().trim());
            Ubicacion ub       = (Ubicacion) cmbUbicacion.getSelectedItem();
            int    idUb        = (ub != null && ub.getIdUbicacion() > 0)
                                    ? ub.getIdUbicacion() : 0;
            if (idUb <= 0) {
                // FK NOT NULL en BD: el lote no se puede guardar sin ubicación real
                return false;
            }

            LoteExistencia le = new LoteExistencia(
                selectedIdLote < 0 ? 0 : selectedIdLote,
                selectedIdProducto,
                idUb,
                txtLote.getText().trim(),
                fechaPickerComoIso(),
                existencias,
                costo
            );

            if (selectedIdLote < 0) {
                int nId = loteDAO.insertar(le);
                if (nId > 0) { selectedIdLote = nId; return true; }
                return false;
            } else {
                le.setIdLote(selectedIdLote);
                return loteDAO.actualizar(le);
            }
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /** Elimina el producto seleccionado y todos sus lotes (CASCADE en BD). */
    private void eliminarProducto() {
        if (selectedIdProducto < 0) {
            JOptionPane.showMessageDialog(this,
                "Selecciona un producto de la tabla primero.");
            return;
        }
        int resp = JOptionPane.showConfirmDialog(this,
            "¿Eliminar el producto seleccionado y todos sus lotes?",
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        if (resp == JOptionPane.YES_OPTION) {
            if (productoDAO.eliminar(selectedIdProducto)) {
                limpiarFormulario();
                cargarTabla();
            } else {
                JOptionPane.showMessageDialog(this,
                    "❌ No se pudo eliminar el producto.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void buscar() {
        String t = txtBuscar.getText().trim();
        if (t.isEmpty()) { cargarTabla(); return; }
        String campo = mapearCampoBusqueda((String) cmbCampoBusqueda.getSelectedItem());
        modeloTabla.setRowCount(0);
        for (Object[] f : productoDAO.buscarConLotesPorCampo(t, idTipo, campo)) {
            modeloTabla.addRow(f);
        }
        if (modeloTabla.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No se encontraron resultados para \"" + t + "\""
                + (campo.equals("TODOS") ? "."
                    : " en el campo " + cmbCampoBusqueda.getSelectedItem() + "."),
                "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private String mapearCampoBusqueda(String etiqueta) {
        if (etiqueta == null) return "TODOS";
        switch (etiqueta) {
            case "Código":       return "CODIGO";
            case "Nombre":       return "NOMBRE";
            case "INVIMA":       return "INVIMA";
            case "Lote":         return "LOTE";
            case "Ubicación":    return "UBICACION";
            case "Presentación": return "PRESENTACION";
            default:             return "TODOS";
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    /** ¿Tiene el usuario ingresado algún dato en la sección del Lote? */
    private boolean hayDatosDeLote() {
        if (soloLectura) return false;
        String lote = txtLote.getText() == null ? "" : txtLote.getText().trim();
        String venc = fechaPickerComoIso();
        String exis = txtExistencias.getText() == null ? "0" : txtExistencias.getText().trim();
        if (!lote.isEmpty() || !venc.isEmpty()) return true;
        try { return Integer.parseInt(exis) > 0; }
        catch (NumberFormatException ex) { return false; }
    }

    /** Limpia todo el formulario (producto + lote) y deselecciona la tabla. */
    private void limpiarFormulario() {
        selectedIdProducto = -1;
        selectedIdLote     = -1;
        txtCodigo.setText(""); txtInvima.setText(""); txtNombre.setText("");
        if (cmbPresentacion.getItemCount() > 0) cmbPresentacion.setSelectedIndex(0);
        limpiarCamposLote();
        tabla.clearSelection();
    }

    /** Limpia solo los campos del lote. */
    private void limpiarCamposLote() {
        selectedIdLote = -1;
        txtLote.setText("");
        resetPickerFecha();
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
        b.setFont(Centro.util.Tema.FUENTE_BTN);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ── Helpers de fecha (DatePickerField) ──────────────────────

    /**
     * Devuelve la fecha del calendario en formato {@code DD/MM/YYYY}.
     * Este formato se eligió para ser consistente con los datos históricos
     * que ya estaban almacenados en la BD. Devuelve "" si no hay fecha.
     */
    private String fechaPickerComoIso() {
        if (dpVencimiento == null) return "";
        LocalDate d = dpVencimiento.getSelectedDate();
        if (d == null) return "";
        return String.format("%02d/%02d/%04d", d.getDayOfMonth(), d.getMonthValue(), d.getYear());
    }

    /**
     * Parser tolerante de fechas. Acepta DD/MM/YYYY (formato histórico) y
     * YYYY-MM-DD (ISO). Devuelve null si no se reconoce.
     */
    private LocalDate parseFechaFlexible(String fec) {
        if (fec == null || fec.isBlank()) return null;
        String s = fec.trim();
        try {
            if (s.contains("/")) {
                String[] p = s.split("/");
                if (p.length == 3) {
                    return LocalDate.of(Integer.parseInt(p[2]),
                                        Integer.parseInt(p[1]),
                                        Integer.parseInt(p[0]));
                }
                return null;
            }
            return LocalDate.parse(s);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Asigna al calendario la fecha pasada como cadena.
     * Acepta DD/MM/YYYY o YYYY-MM-DD. Vacío/inválido → hoy.
     */
    private void setFechaEnPicker(String fecha) {
        if (dpVencimiento == null) return;
        LocalDate ld = parseFechaFlexible(fecha);
        dpVencimiento.setSelectedDate(ld != null ? ld : LocalDate.now());
    }

    /** Restablece el calendario al día de hoy. */
    private void resetPickerFecha() {
        if (dpVencimiento != null) dpVencimiento.setSelectedDate(LocalDate.now());
    }
}
