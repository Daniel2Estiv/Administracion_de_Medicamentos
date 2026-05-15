package Centro.Interfaz;

import Centro.dao.LoteExistenciaDAO;
import Centro.modelo.Usuario;
import Centro.util.Tema;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Ventana principal del sistema con navegación lateral y CardLayout.
 * <p>
 * Mejoras frente a la versión anterior:
 * <ul>
 *   <li>El panel de administración de usuarios (solo ADMIN) ya está integrado
 *       en la navegación.</li>
 *   <li>Toda la apariencia se delega en {@link Tema} para coherencia visual.</li>
 *   <li>Carga del dashboard separada para no bloquear el EDT.</li>
 *   <li>Limpieza de duplicación en la creación de botones de menú.</li>
 * </ul>
 */
public class MenuPrincipal extends JFrame {

    private static final long serialVersionUID = 1L;

    private final Usuario   usuarioActual;
    private JPanel          panelContenido;
    private CardLayout      cardLayout;
    private JButton         btnActivo;

    public MenuPrincipal(Usuario usuario) {
        this.usuarioActual = usuario;
        setTitle("Sistema de Gestión — Centro Logístico");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 660);
        setMinimumSize(new Dimension(900, 550));
        setLocationRelativeTo(null);
        construirUI();
    }

    /** Constructor sin usuario (compatibilidad retroactiva). */
    public MenuPrincipal() {
        this(new Usuario(0, "admin", "", "ADMIN", true));
    }

    // ── UI ─────────────────────────────────────────────────────

    private void construirUI() {
        setLayout(new BorderLayout());

        add(construirTopBar(),       BorderLayout.NORTH);
        add(construirMenuLateral(),  BorderLayout.WEST);
        add(construirPanelContenido(), BorderLayout.CENTER);
    }

    private JPanel construirTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Tema.AZUL_PROFUNDO);
        topBar.setPreferredSize(new Dimension(0, 38));
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        JLabel lblSistema = new JLabel("🏥 Centro Logístico — Sistema de Insumos Médicos");
        lblSistema.setFont(Tema.fontEmoji(Font.BOLD, 13));
        lblSistema.setForeground(new Color(180, 210, 255));

        JLabel lblUsuario = new JLabel("👤 " + usuarioActual.getUsername()
                + " (" + usuarioActual.getRol() + ")");
        lblUsuario.setFont(Tema.fontEmoji(Font.PLAIN, 12));
        lblUsuario.setForeground(new Color(200, 220, 255));

        topBar.add(lblSistema, BorderLayout.WEST);
        topBar.add(lblUsuario, BorderLayout.EAST);
        return topBar;
    }

    private JPanel construirPanelContenido() {
        cardLayout     = new CardLayout();
        panelContenido = new JPanel(cardLayout);
        panelContenido.setBackground(Color.WHITE);

        panelContenido.add(construirDashboard(),                       "INICIO");
        panelContenido.add(new PanelInsumos(usuarioActual),            "INSUMOS");
        panelContenido.add(new PanelMedicamentos(usuarioActual),       "MEDICAMENTOS");
        panelContenido.add(new PanelBienesDevolutivos(usuarioActual),  "BIENES");
        panelContenido.add(new PanelUbicacion(usuarioActual),          "UBICACIONES");

        // Sólo los administradores pueden ver/usar el panel de usuarios.
        if (usuarioActual.esAdmin()) {
            panelContenido.add(new PanelUsuarios(), "USUARIOS");
        }

        cardLayout.show(panelContenido, "INICIO");
        return panelContenido;
    }

    // ── Menú lateral ───────────────────────────────────────────

    private JPanel construirMenuLateral() {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(Tema.AZUL_OSCURO);
        menu.setPreferredSize(new Dimension(190, 0));

        menu.add(construirCabeceraLogo());

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(40, 65, 120));
        sep.setMaximumSize(new Dimension(190, 2));
        menu.add(sep);

        menu.add(crearSeccion("MENÚ PRINCIPAL"));

        JButton btnInicio = crearBtnMenu("🏠  Inicio",             "INICIO");
        menu.add(btnInicio);
        menu.add(crearBtnMenu("💊  Insumos",            "INSUMOS"));
        menu.add(crearBtnMenu("🩺  Medicamentos",       "MEDICAMENTOS"));
        menu.add(crearBtnMenu("📦  Bienes Devolutivos", "BIENES"));
        menu.add(crearBtnMenu("📍  Ubicaciones",        "UBICACIONES"));

        if (usuarioActual.esAdmin()) {
            menu.add(crearSeccion("ADMINISTRACIÓN"));
            menu.add(crearBtnMenu("👤  Usuarios", "USUARIOS"));
        }

        menu.add(Box.createVerticalGlue());

        JSeparator sep2 = new JSeparator();
        sep2.setForeground(new Color(40, 65, 120));
        sep2.setMaximumSize(new Dimension(190, 2));
        menu.add(sep2);

        JButton btnCerrar = new JButton("⏻  Cerrar Sesión");
        btnCerrar.setBackground(new Color(130, 20, 20));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFont(Tema.fontEmoji(Font.BOLD, 12));
        btnCerrar.setBorderPainted(false);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setMaximumSize(new Dimension(190, 40));
        btnCerrar.setPreferredSize(new Dimension(190, 40));
        btnCerrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCerrar.setHorizontalAlignment(SwingConstants.LEFT);
        btnCerrar.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 8));
        btnCerrar.addActionListener(e -> cerrarSesion());
        menu.add(btnCerrar);

        activarBoton(btnInicio);
        return menu;
    }

    private JPanel construirCabeceraLogo() {
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(Tema.AZUL_PROFUNDO);
        logoPanel.setMaximumSize(new Dimension(190, 90));
        logoPanel.setPreferredSize(new Dimension(190, 90));

        JLabel lblLogo = new JLabel("CL", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 34));
        lblLogo.setForeground(Tema.AZUL_ACTIVO);

        JLabel lblSub = new JLabel("INSUMOS MED.", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblSub.setForeground(new Color(160, 180, 220));

        JPanel inner = new JPanel(new GridLayout(2, 1));
        inner.setBackground(Tema.AZUL_PROFUNDO);
        inner.add(lblLogo);
        inner.add(lblSub);
        logoPanel.add(inner, BorderLayout.CENTER);
        return logoPanel;
    }

    private JButton crearBtnMenu(String texto, String card) {
        JButton btn = new JButton(texto);
        btn.setBackground(Tema.AZUL_OSCURO);
        btn.setForeground(Color.WHITE);
        // Fuente con soporte emoji para que se vean los iconos del menú lateral
        btn.setFont(Tema.fontEmoji(Font.PLAIN, 13));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(190, 42));
        btn.setPreferredSize(new Dimension(190, 42));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 8));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn != btnActivo) btn.setBackground(Tema.AZUL_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn != btnActivo) btn.setBackground(Tema.AZUL_OSCURO);
            }
        });

        btn.addActionListener(e -> {
            cardLayout.show(panelContenido, card);
            activarBoton(btn);
        });
        return btn;
    }

    private void activarBoton(JButton btn) {
        if (btnActivo != null) btnActivo.setBackground(Tema.AZUL_OSCURO);
        btnActivo = btn;
        btn.setBackground(Tema.AZUL_ACTIVO);
    }

    private JLabel crearSeccion(String texto) {
        JLabel lbl = new JLabel("  " + texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lbl.setForeground(new Color(130, 160, 210));
        lbl.setMaximumSize(new Dimension(190, 28));
        lbl.setPreferredSize(new Dimension(190, 28));
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 0));
        return lbl;
    }

    // ── Dashboard ──────────────────────────────────────────────

    private JPanel construirDashboard() {
        JPanel dash = new JPanel(new BorderLayout(0, 0));
        dash.setBackground(Tema.AZUL_BG);

        // Encabezado
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDE_SUAVE),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));
        JLabel lblH = new JLabel("🏠 Panel de Inicio");
        lblH.setFont(Tema.fontEmoji(Font.BOLD, 20));
        lblH.setForeground(Tema.AZUL_OSCURO);
        JLabel lblFecha = new JLabel("Sistema de Gestión de Insumos Médicos");
        lblFecha.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblFecha.setForeground(Tema.GRIS_TEXTO);
        header.add(lblH,    BorderLayout.WEST);
        header.add(lblFecha, BorderLayout.EAST);

        // Tarjetas de resumen
        JPanel tarjetas = new JPanel(new GridLayout(1, 4, 16, 0));
        tarjetas.setBackground(Tema.AZUL_BG);
        tarjetas.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
        tarjetas.add(crearTarjeta("💊", "Insumos",       "Registrados en BD",          new Color(0, 102, 204)));
        tarjetas.add(crearTarjeta("🩺", "Medicamentos",  "Con control de vencimiento", new Color(0, 140, 70)));
        tarjetas.add(crearTarjeta("📦", "Bienes Dev.",   "Por estado y responsable",   new Color(150, 60, 0)));
        tarjetas.add(crearTarjeta("📍", "Ubicaciones",   "Bodega / Pasillo / Rack",    new Color(80, 30, 140)));

        // Alertas de vencimiento
        JPanel alertPanel = new JPanel(new BorderLayout());
        alertPanel.setBackground(Tema.AZUL_BG);
        alertPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));

        JLabel lblAlerta = new JLabel("⚠️  Próximos a vencer — ordenados por fecha más cercana");
        lblAlerta.setFont(Tema.fontEmoji(Font.BOLD, 14));
        lblAlerta.setForeground(Tema.AZUL_OSCURO);
        lblAlerta.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        String[] colsAlerta = {"Tipo", "Producto", "Lote", "Vencimiento", "Existencias"};
        JTable tablaAlerta = new JTable(new javax.swing.table.DefaultTableModel(colsAlerta, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        tablaAlerta.setRowHeight(24);
        tablaAlerta.setFont(Tema.FUENTE_LIST);
        tablaAlerta.getTableHeader().setFont(Tema.FUENTE_LBL_BIG);
        tablaAlerta.getTableHeader().setBackground(new Color(255, 248, 220));

        // Carga las alertas en background para no bloquear el EDT.
        SwingWorker<java.util.List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override protected java.util.List<Object[]> doInBackground() {
                return new LoteExistenciaDAO().alertasVencimientoGlobal(0);
            }
            @Override protected void done() {
                try {
                    javax.swing.table.DefaultTableModel m =
                            (javax.swing.table.DefaultTableModel) tablaAlerta.getModel();
                    for (Object[] row : get()) m.addRow(row);
                } catch (Exception ignored) { /* no afecta al funcionamiento */ }
            }
        };
        worker.execute();

        JScrollPane scrollAlerta = new JScrollPane(tablaAlerta);
        scrollAlerta.setPreferredSize(new Dimension(0, 180));
        scrollAlerta.setBorder(BorderFactory.createLineBorder(new Color(220, 200, 150)));

        alertPanel.add(lblAlerta,    BorderLayout.NORTH);
        alertPanel.add(scrollAlerta, BorderLayout.CENTER);

        // Pie de bienvenida
        JPanel bienvenida = new JPanel(new BorderLayout());
        bienvenida.setBackground(Color.WHITE);
        bienvenida.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE_SUAVE),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));

        JLabel lblBien = new JLabel("👋 Bienvenido, " + usuarioActual.getUsername()
                + " — Rol: " + usuarioActual.getRol()
                + " · Use el menú lateral para navegar.");
        lblBien.setFont(Tema.fontEmoji(Font.PLAIN, 13));
        lblBien.setForeground(new Color(60, 80, 120));
        bienvenida.add(lblBien, BorderLayout.CENTER);

        JPanel centro = new JPanel(new BorderLayout(0, 0));
        centro.setBackground(Tema.AZUL_BG);
        centro.add(tarjetas,   BorderLayout.NORTH);
        centro.add(alertPanel, BorderLayout.CENTER);
        centro.add(bienvenida, BorderLayout.SOUTH);

        dash.add(header, BorderLayout.NORTH);
        dash.add(centro, BorderLayout.CENTER);
        return dash;
    }

    private JPanel crearTarjeta(String icono, String titulo, String subtitulo, Color color) {
        JPanel t = new JPanel(new BorderLayout(0, 4));
        t.setBackground(Color.WHITE);
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 240)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        JLabel ico = new JLabel(icono);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        JPanel linea = new JPanel();
        linea.setBackground(color);
        linea.setPreferredSize(new Dimension(4, 0));
        top.add(ico,   BorderLayout.WEST);
        top.add(linea, BorderLayout.EAST);

        JLabel tit = new JLabel(titulo);
        tit.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tit.setForeground(Tema.AZUL_OSCURO);

        JLabel sub = new JLabel(subtitulo);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(120, 140, 170));

        t.add(top, BorderLayout.NORTH);
        t.add(tit, BorderLayout.CENTER);
        t.add(sub, BorderLayout.SOUTH);
        return t;
    }

    // ── Sesión ─────────────────────────────────────────────────

    private void cerrarSesion() {
        int op = JOptionPane.showConfirmDialog(this,
                "¿Desea cerrar sesión?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) {
            dispose();
            new Login().setVisible(true);
        }
    }
}
