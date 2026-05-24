package Centro.Interfaz;

import Centro.dao.LoteExistenciaDAO;
import Centro.dao.UbicacionDAO;
import Centro.modelo.Ubicacion;
import Centro.modelo.Usuario;
import Centro.util.Tema;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;

/**
 * Panel CRUD para la tabla {@code Ubicacion}.
 * <p>
 * Mejoras respecto a la versión anterior:
 * <ul>
 *   <li>Al seleccionar una ubicación de la tabla superior se cargan en la
 *       tabla inferior los productos almacenados en esa ubicación.</li>
 *   <li>El formulario lateral de edición se oculta cuando el usuario tiene
 *       rol {@code LECTURA}, dejando el panel en modo consulta.</li>
 * </ul>
 */
public class PanelUbicacion extends JPanel {

    private static final Color AZUL_OSCURO = new Color(10, 33, 80);
    private static final Color AZUL_BTN    = new Color(0, 102, 204);
    private static final Color ROJO_BTN    = new Color(192, 0, 0);
    private static final Color VERDE_BTN   = new Color(0, 128, 64);

    // Estado y permisos
    private final Usuario usuarioActual;
    private final boolean soloLectura;

    // Tabla principal de ubicaciones
    private JTable            tablaUbic;
    private DefaultTableModel modeloUbic;

    // Tabla secundaria de productos en la ubicación seleccionada
    private JTable            tablaProductos;
    private DefaultTableModel modeloProductos;

    // Formulario lateral
    private JTextField        txtDescripcion;
    private JButton           btnNuevo, btnGuardar, btnEliminar;
    private int               idSeleccionado = -1;

    private final UbicacionDAO       ubicDAO = new UbicacionDAO();
    private final LoteExistenciaDAO  loteDAO = new LoteExistenciaDAO();

    /** Constructor con usuario — aplica restricciones por rol. */
    public PanelUbicacion(Usuario usuario) {
        this.usuarioActual = usuario;
        this.soloLectura   = usuario != null && usuario.esLectura();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        construirUI();
        cargarTabla();
    }

    /** Constructor sin usuario (compatibilidad). */
    public PanelUbicacion() {
        this(null);
    }

    // ── UI ────────────────────────────────────────────────────

    private void construirUI() {
        // Encabezado con título y acción "Mapa de la bodega"
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AZUL_OSCURO);
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel titulo = new JLabel("📍  Ubicaciones");
        titulo.setFont(Tema.fontEmoji(Font.BOLD, 18));
        titulo.setForeground(Color.WHITE);
        header.add(titulo, BorderLayout.WEST);

        // Panel derecho con la nota informativa y el botón "Mapa de bodega"
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        derecha.setOpaque(false);

        JLabel nota = new JLabel(
            soloLectura
                ? "Selecciona una ubicación para ver los productos almacenados (modo consulta)"
                : "Selecciona una ubicación para ver sus productos · Edita el formulario lateral");
        nota.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        nota.setForeground(new Color(180, 210, 255));
        derecha.add(nota);

        JButton btnMapa = new JButton("🗺  Mapa de la bodega");
        btnMapa.setFont(Tema.fontEmoji(Font.BOLD, 12));
        btnMapa.setBackground(new Color(0, 102, 204));
        btnMapa.setForeground(Color.WHITE);
        btnMapa.setBorderPainted(false);
        btnMapa.setFocusPainted(false);
        btnMapa.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnMapa.setToolTipText("Abre el plano de distribución de la bodega para consultar la ubicación física");
        btnMapa.addActionListener(e -> mostrarMapaBodega());
        derecha.add(btnMapa);

        header.add(derecha, BorderLayout.EAST);

        // SplitPane vertical: ubicaciones arriba, productos de la ubicación abajo
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                construirTablaUbicaciones(),
                construirTablaProductosUbicacion());
        split.setResizeWeight(0.45);
        split.setDividerLocation(280);
        split.setBorder(null);

        // Cuerpo: split a la izquierda + formulario a la derecha (si no es LECTURA)
        JPanel cuerpo = new JPanel(new BorderLayout(10, 0));
        cuerpo.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        cuerpo.setBackground(Color.WHITE);
        cuerpo.add(split, BorderLayout.CENTER);
        if (!soloLectura) {
            cuerpo.add(construirFormulario(), BorderLayout.EAST);
        }

        add(header, BorderLayout.NORTH);
        add(cuerpo, BorderLayout.CENTER);
    }

    private JPanel construirTablaUbicaciones() {
        modeloUbic = new DefaultTableModel(new String[]{"ID", "Descripción"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaUbic = new JTable(modeloUbic);
        tablaUbic.setRowHeight(26);
        tablaUbic.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaUbic.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tablaUbic.getTableHeader().setBackground(Tema.AZUL_HEADER);
        tablaUbic.setSelectionBackground(Tema.AZUL_SELECCION);

        // Ocultar columna ID
        tablaUbic.getColumnModel().getColumn(0).setMinWidth(0);
        tablaUbic.getColumnModel().getColumn(0).setMaxWidth(0);

        tablaUbic.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarFormulario();
                cargarProductosDeUbicacionSeleccionada();
            }
        });

        JScrollPane scroll = new JScrollPane(tablaUbic);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 215, 235)));

        JPanel cont = new JPanel(new BorderLayout(0, 4));
        cont.setBackground(Color.WHITE);
        JLabel encabezado = new JLabel("Ubicaciones registradas");
        encabezado.setFont(new Font("Segoe UI", Font.BOLD, 12));
        encabezado.setForeground(AZUL_OSCURO);
        encabezado.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 0));
        cont.add(encabezado, BorderLayout.NORTH);
        cont.add(scroll, BorderLayout.CENTER);
        return cont;
    }

    private JPanel construirTablaProductosUbicacion() {
        String[] cols = {"Código", "Producto", "Tipo", "Lote", "Vencimiento", "Existencias", "Costo"};
        modeloProductos = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaProductos = new JTable(modeloProductos);
        tablaProductos.setRowHeight(24);
        tablaProductos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaProductos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tablaProductos.getTableHeader().setBackground(new Color(255, 248, 220));
        tablaProductos.setSelectionBackground(new Color(220, 235, 255));

        JScrollPane scroll = new JScrollPane(tablaProductos);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 200, 150)));

        JPanel cont = new JPanel(new BorderLayout(0, 4));
        cont.setBackground(Color.WHITE);
        JLabel encabezado = new JLabel("📦  Productos en la ubicación seleccionada");
        encabezado.setFont(Tema.fontEmoji(Font.BOLD, 12));
        encabezado.setForeground(AZUL_OSCURO);
        encabezado.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 0));
        cont.add(encabezado, BorderLayout.NORTH);
        cont.add(scroll, BorderLayout.CENTER);
        return cont;
    }

    private JPanel construirFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 248, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(190, 210, 240)),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        panel.setPreferredSize(new Dimension(400, 0));
        panel.setMinimumSize(new Dimension(380, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 2, 5, 2);
        g.gridwidth = 2;

        JLabel tit = new JLabel("📋 Nueva Ubicación");
        tit.setFont(Tema.fontEmoji(Font.BOLD, 13));
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

    // ── Carga de datos ────────────────────────────────────────

    private void cargarTabla() {
        modeloUbic.setRowCount(0);
        for (Object[] f : ubicDAO.listarParaTabla()) modeloUbic.addRow(f);
    }

    private void cargarFormulario() {
        if (soloLectura) {
            // Aún sin formulario, registramos el id seleccionado para los productos.
            int fila = tablaUbic.getSelectedRow();
            idSeleccionado = (fila < 0) ? -1 : (int) modeloUbic.getValueAt(fila, 0);
            return;
        }
        int fila = tablaUbic.getSelectedRow();
        if (fila < 0) return;
        idSeleccionado = (int) modeloUbic.getValueAt(fila, 0);
        txtDescripcion.setText((String) modeloUbic.getValueAt(fila, 1));
    }

    private void cargarProductosDeUbicacionSeleccionada() {
        modeloProductos.setRowCount(0);
        if (idSeleccionado <= 0) return;
        for (Object[] row : loteDAO.listarProductosPorUbicacion(idSeleccionado)) {
            modeloProductos.addRow(row);
        }
    }

    // ── Operaciones ───────────────────────────────────────────

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
                "❌ Error al guardar. La descripción puede estar duplicada.",
                "Error", JOptionPane.ERROR_MESSAGE);
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
                limpiar(); cargarTabla(); modeloProductos.setRowCount(0);
            } else {
                JOptionPane.showMessageDialog(this,
                    "No se puede eliminar: tiene lotes asignados o error en BD.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limpiar() {
        idSeleccionado = -1;
        if (txtDescripcion != null) txtDescripcion.setText("");
        tablaUbic.clearSelection();
        modeloProductos.setRowCount(0);
    }

    // ── Mapa de la bodega ─────────────────────────────────────

    /**
     * Abre un diálogo modal con el boceto de la bodega como guía visual.
     * El usuario puede consultar a qué zona física corresponde cada ubicación
     * registrada en el sistema.
     * <p>
     * La ventana ocupa el 92 % del tamaño de la pantalla y la imagen se
     * escala manteniendo proporción con interpolación de alta calidad.
     */
    private void mostrarMapaBodega() {
        // Localizar la imagen: primero como recurso embebido del classpath,
        // luego en src/Imagenes/ por si se ejecuta desde el IDE sin empaquetar.
        Image imagen = null;
        URL recurso = getClass().getResource("/Imagenes/boceto_bodega.png");
        if (recurso != null) {
            imagen = new ImageIcon(recurso).getImage();
        } else {
            java.io.File f = new java.io.File("src/Imagenes/boceto_bodega.png");
            if (f.exists()) imagen = new ImageIcon(f.getAbsolutePath()).getImage();
        }

        if (imagen == null || imagen.getWidth(null) <= 0) {
            JOptionPane.showMessageDialog(this,
                "No se encontró el archivo del mapa.\n"
              + "Verifique que exista 'src/Imagenes/boceto_bodega.png'.",
                "Mapa no disponible", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Calcular tamaño relativo a la pantalla (92 %)
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        int anchoDialog = (int) (pantalla.width  * 0.92);
        int altoDialog  = (int) (pantalla.height * 0.92);

        // Diálogo modal
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Mapa de la bodega — Centro Logístico",
                                     Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(anchoDialog, altoDialog);
        dialog.setMinimumSize(new Dimension(900, 600));
        dialog.setLocationRelativeTo(owner);

        // Encabezado compacto (ocupa poco para dejar más espacio a la imagen)
        JPanel cab = new JPanel(new BorderLayout());
        cab.setBackground(AZUL_OSCURO);
        cab.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 8));
        JLabel lblTit = new JLabel("🗺  Mapa de distribución de la bodega");
        lblTit.setFont(Tema.fontEmoji(Font.BOLD, 16));
        lblTit.setForeground(Color.WHITE);
        cab.add(lblTit, BorderLayout.WEST);

        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        der.setOpaque(false);
        JLabel lblNota = new JLabel("Pulsa Esc o el botón ✕ para cerrar");
        lblNota.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblNota.setForeground(new Color(180, 210, 255));
        der.add(lblNota);

        // Botón cerrar tipo X compacto en el header
        JButton btnX = new JButton("✕");
        btnX.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnX.setBackground(new Color(160, 30, 30));
        btnX.setForeground(Color.WHITE);
        btnX.setBorderPainted(false);
        btnX.setFocusPainted(false);
        btnX.setMargin(new Insets(2, 8, 2, 8));
        btnX.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnX.setToolTipText("Cerrar (Esc)");
        btnX.addActionListener(e -> dialog.dispose());
        der.add(btnX);
        cab.add(der, BorderLayout.EAST);

        // Panel con la imagen escalada — sin padding para aprovechar todo el espacio
        ImagenEscalada panelImagen = new ImagenEscalada(imagen);
        panelImagen.setBackground(Color.WHITE);
        panelImagen.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        dialog.add(cab,         BorderLayout.NORTH);
        dialog.add(panelImagen, BorderLayout.CENTER);
        // (Se elimina la barra inferior para que la imagen aproveche más espacio)

        // Permitir cerrar con Esc
        dialog.getRootPane().registerKeyboardAction(
            e -> dialog.dispose(),
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialog.setVisible(true);
    }

    /**
     * Panel que pinta una imagen escalada al espacio disponible manteniendo
     * la proporción original (sin distorsión).
     * <p>
     * Optimizaciones:
     * <ul>
     *   <li>Usa {@link Image#SCALE_SMOOTH} para pre-renderizar una versión
     *       escalada de alta calidad cuando cambia el tamaño del panel.</li>
     *   <li>Cachea el resultado para no reescalar la imagen en cada repintado
     *       (lo cual sería lento en imágenes grandes).</li>
     *   <li>En el dibujado final aplica interpolación BICUBIC + render de
     *       calidad para obtener bordes y textos nítidos.</li>
     * </ul>
     */
    private static final class ImagenEscalada extends JPanel {
        private final Image imagen;
        private Image imagenCacheada;   // versión pre-escalada
        private int   anchoCacheado  = -1;
        private int   altoCacheado   = -1;

        ImagenEscalada(Image imagen) {
            this.imagen = imagen;
            setBackground(Color.WHITE);
            setOpaque(true);
        }

        private Image obtenerImagenEscalada(int destW, int destH) {
            if (imagen == null || destW <= 0 || destH <= 0) return null;
            // Si ya tenemos la imagen cacheada al tamaño actual, la devolvemos.
            if (imagenCacheada != null && anchoCacheado == destW && altoCacheado == destH) {
                return imagenCacheada;
            }
            // Generar nueva versión suave (Image.SCALE_SMOOTH usa
            // ReplicateScaleFilter+AreaAveragingScaleFilter internamente).
            imagenCacheada = imagen.getScaledInstance(destW, destH, Image.SCALE_SMOOTH);
            anchoCacheado  = destW;
            altoCacheado   = destH;
            return imagenCacheada;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (imagen == null) return;
            int iw = imagen.getWidth(this);
            int ih = imagen.getHeight(this);
            if (iw <= 0 || ih <= 0) return;

            int w = getWidth();
            int h = getHeight();
            double escala = Math.min((double) w / iw, (double) h / ih);
            int nw = Math.max(1, (int) Math.round(iw * escala));
            int nh = Math.max(1, (int) Math.round(ih * escala));
            int x  = (w - nw) / 2;
            int y  = (h - nh) / 2;

            // Obtenemos la versión pre-escalada (cacheada) y la pintamos con
            // interpolación bicúbica para máxima calidad final.
            Image escalada = obtenerImagenEscalada(nw, nh);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                                RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(escalada != null ? escalada : imagen, x, y, nw, nh, this);
            g2.dispose();
        }
    }

    private JButton crearBoton(String txt, Color c) {
        JButton b = new JButton(txt);
        b.setBackground(c); b.setForeground(Color.WHITE);
        // Fuente con soporte emoji
        b.setFont(Tema.FUENTE_BTN);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
