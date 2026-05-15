package Centro.Interfaz;

import Centro.dao.UsuarioDAO;
import Centro.modelo.Usuario;
import Centro.util.ConexionBD;
import Centro.util.Tema;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Ventana de inicio de sesión.
 * <p>
 * Diseño con panel izquierdo de marca y formulario en el panel derecho.
 * Toda la apariencia (colores, fuentes y look &amp; feel) se obtiene de
 * {@link Tema}, lo que permite cambiar el tema desde un único punto y activar
 * FlatLaf de forma transparente sin tocar este archivo.
 */
public class Login extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextField     txtUsuario;
    private JPasswordField txtContrasena;
    private JButton        btnIngresar;
    private JLabel         lblError;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Login() {
        // Inicializar BD al arrancar (singleton perezoso).
        ConexionBD.getInstance();

        setTitle("Centro Logístico — Iniciar Sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 440);
        setResizable(false);
        setLocationRelativeTo(null);
        construirUI();
    }

    // ── UI ─────────────────────────────────────────────────────

    private void construirUI() {
        setLayout(new BorderLayout());
        add(construirPanelBranding(), BorderLayout.WEST);
        add(construirPanelFormulario(), BorderLayout.CENTER);
    }

    private JPanel construirPanelBranding() {
        JPanel izquierdo = new JPanel(new GridBagLayout());
        izquierdo.setBackground(Tema.AZUL_OSCURO);
        izquierdo.setPreferredSize(new Dimension(280, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 20, 6, 20);

        JPanel logo = new JPanel() {
            @Override protected void paintComponent(Graphics gr) {
                super.paintComponent(gr);
                Graphics2D g2 = (Graphics2D) gr;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Tema.AZUL_ACTIVO);
                g2.fillOval(10, 10, 80, 80);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 32));
                g2.drawString("CL", 26, 60);
            }
        };
        logo.setPreferredSize(new Dimension(100, 100));
        logo.setOpaque(false);
        g.gridx = 0; g.gridy = 0; g.anchor = GridBagConstraints.CENTER;
        izquierdo.add(logo, g);

        JLabel lblTitulo = new JLabel("Sistema de Gestión", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);
        g.gridy = 1; izquierdo.add(lblTitulo, g);

        JLabel lblSubtitulo = new JLabel("Centro Logístico", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitulo.setForeground(new Color(180, 200, 240));
        g.gridy = 2; izquierdo.add(lblSubtitulo, g);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(50, 80, 140));
        g.gridy = 3; g.insets = new Insets(16, 20, 16, 20);
        izquierdo.add(sep, g);

        String[] bullets = {
            "✔  Insumos médicos",
            "✔  Medicamentos",
            "✔  Bienes devolutivos",
            "✔  Control de ubicaciones"
        };
        g.insets = new Insets(3, 24, 3, 20);
        for (int i = 0; i < bullets.length; i++) {
            JLabel b = new JLabel(bullets[i]);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            b.setForeground(new Color(190, 215, 250));
            g.gridy = 4 + i; izquierdo.add(b, g);
        }
        return izquierdo;
    }

    private JPanel construirPanelFormulario() {
        JPanel derecho = new JPanel(new GridBagLayout());
        derecho.setBackground(Tema.AZUL_BG);
        derecho.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        GridBagConstraints gf = new GridBagConstraints();
        gf.fill = GridBagConstraints.HORIZONTAL;
        gf.insets = new Insets(6, 0, 6, 0);

        JLabel lblBienvenido = new JLabel("Bienvenido");
        lblBienvenido.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblBienvenido.setForeground(Tema.AZUL_OSCURO);
        gf.gridx = 0; gf.gridy = 0; gf.gridwidth = 2;
        derecho.add(lblBienvenido, gf);

        JLabel lblIngresa = new JLabel("Ingresa tus credenciales para continuar");
        lblIngresa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblIngresa.setForeground(Tema.GRIS_TEXTO);
        gf.gridy = 1; derecho.add(lblIngresa, gf);

        gf.gridy = 2; gf.insets = new Insets(10, 0, 10, 0);
        derecho.add(new JSeparator(), gf);
        gf.insets = new Insets(6, 0, 6, 0);

        // Usuario
        JLabel lblUser = new JLabel("Usuario");
        lblUser.setFont(Tema.FUENTE_LBL_BIG);
        gf.gridy = 3; gf.gridwidth = 1; gf.weightx = 0.3;
        derecho.add(lblUser, gf);

        txtUsuario = new JTextField(18);
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUsuario.setBorder(Tema.bordeCampo());
        gf.gridy = 4; gf.gridwidth = 2; gf.weightx = 1;
        derecho.add(txtUsuario, gf);

        // Contraseña
        JLabel lblPass = new JLabel("Contraseña");
        lblPass.setFont(Tema.FUENTE_LBL_BIG);
        gf.gridy = 5; gf.gridwidth = 1;
        derecho.add(lblPass, gf);

        txtContrasena = new JPasswordField(18);
        txtContrasena.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtContrasena.setBorder(Tema.bordeCampo());
        gf.gridy = 6; gf.gridwidth = 2;
        derecho.add(txtContrasena, gf);

        // Error
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblError.setForeground(new Color(180, 0, 0));
        gf.gridy = 7; derecho.add(lblError, gf);

        // Botón ingresar
        btnIngresar = Tema.boton("INGRESAR", Tema.AZUL_ACTIVO);
        btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnIngresar.setPreferredSize(new Dimension(0, 42));
        btnIngresar.addActionListener(e -> autenticar());
        gf.gridy = 8; gf.insets = new Insets(10, 0, 6, 0);
        derecho.add(btnIngresar, gf);

        JLabel lblNota = new JLabel("Usuario por defecto: admin / admin123", SwingConstants.CENTER);
        lblNota.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblNota.setForeground(new Color(150, 170, 200));
        gf.gridy = 9; gf.insets = new Insets(4, 0, 0, 0);
        derecho.add(lblNota, gf);

        // Enter envía el formulario.
        KeyAdapter enter = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) autenticar();
            }
        };
        txtUsuario.addKeyListener(enter);
        txtContrasena.addKeyListener(enter);

        return derecho;
    }

    // ── Autenticación ──────────────────────────────────────────

    private void autenticar() {
        final String username = txtUsuario.getText() == null ? "" : txtUsuario.getText().trim();
        final String password = new String(txtContrasena.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("⚠ Ingresa usuario y contraseña.");
            return;
        }
        if (username.length() > 64 || password.length() > 128) {
            // Defensa adicional contra entradas anómalas.
            lblError.setText("⚠ Datos demasiado largos.");
            return;
        }

        lblError.setText(" ");
        btnIngresar.setText("Verificando...");
        btnIngresar.setEnabled(false);

        // Autenticación asíncrona para no bloquear el EDT.
        SwingWorker<Usuario, Void> worker = new SwingWorker<>() {
            @Override protected Usuario doInBackground() {
                return usuarioDAO.autenticar(username, password);
            }
            @Override protected void done() {
                try {
                    Usuario usuario = get();
                    if (usuario != null) {
                        dispose();
                        new MenuPrincipal(usuario).setVisible(true);
                    } else {
                        mostrarError("✗ Usuario o contraseña incorrectos.");
                    }
                } catch (Exception ex) {
                    mostrarError("Error de conexión con la base de datos.");
                }
            }
        };
        worker.execute();
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        txtContrasena.setText("");
        btnIngresar.setText("INGRESAR");
        btnIngresar.setEnabled(true);
        txtUsuario.requestFocus();
    }

    // ── Entry point ────────────────────────────────────────────

    public static void main(String[] args) {
        // Aplica FlatLaf si está disponible; si no, look & feel del sistema.
        Tema.aplicarLookAndFeel();
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}
