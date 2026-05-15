package Centro.Interfaz;

import Centro.dao.UsuarioDAO;
import Centro.modelo.Usuario;
import Centro.util.Tema;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel de administración de usuarios (solo ADMIN).
 * <p>
 * Mejoras frente a la versión anterior:
 * <ul>
 *   <li>Validación de longitud mínima de contraseña ({@value #MIN_PASS}).</li>
 *   <li>Validación contra duplicados antes de insertar.</li>
 *   <li>Mensajes de error más claros si el guardado falla.</li>
 *   <li>Uso de la paleta y los componentes centralizados en {@link Tema}.</li>
 * </ul>
 */
public class PanelUsuarios extends JPanel {

    private static final int MIN_PASS = 4;

    private JTable             tabla;
    private DefaultTableModel  modeloTabla;
    private JTextField         txtUsername;
    private JPasswordField     txtPassword;
    private JComboBox<String>  cmbRol;
    private JCheckBox          chkActivo;
    private JButton            btnNuevo, btnGuardar, btnDesactivar, btnEliminar, btnLimpiar;
    private int                idSeleccionado = -1;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public PanelUsuarios() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        construirUI();
        cargarTabla();
    }

    private void construirUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Tema.AZUL_OSCURO);
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel titulo = new JLabel("👤  Administración de Usuarios");
        titulo.setFont(Tema.fontEmoji(Font.BOLD, 18));
        titulo.setForeground(Color.WHITE);
        header.add(titulo, BorderLayout.WEST);

        JLabel warning = new JLabel("⚠ Solo administradores  ");
        warning.setFont(Tema.fontEmoji(Font.ITALIC, 11));
        warning.setForeground(new Color(255, 220, 120));
        header.add(warning, BorderLayout.EAST);

        String[] cols = {"ID", "Usuario", "Rol", "Activo"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(26);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(Tema.AZUL_HEADER);
        tabla.setSelectionBackground(Tema.AZUL_SELECCION);

        // Ocultar columna ID (información técnica).
        tabla.getColumnModel().getColumn(0).setMinWidth(0);
        tabla.getColumnModel().getColumn(0).setMaxWidth(0);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarFormulario();
        });

        JScrollPane scroll = new JScrollPane(tabla);
        JPanel cuerpo = new JPanel(new BorderLayout(10, 0));
        cuerpo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cuerpo.setBackground(Color.WHITE);
        cuerpo.add(scroll, BorderLayout.CENTER);
        cuerpo.add(construirFormulario(), BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(cuerpo, BorderLayout.CENTER);
    }

    private JPanel construirFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Tema.AZUL_BG_FORM);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.AZUL_BORDE),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        panel.setPreferredSize(new Dimension(250, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 2, 5, 2);
        gbc.gridwidth = 2;

        JLabel lf = new JLabel("🔐 Crear / Editar Usuario");
        lf.setFont(Tema.fontEmoji(Font.BOLD, 12));
        lf.setForeground(Tema.AZUL_OSCURO);
        gbc.gridx = 0; gbc.gridy = 0; panel.add(lf, gbc);
        gbc.gridy = 1; panel.add(new JSeparator(), gbc);

        txtUsername = new JTextField();
        txtPassword = new JPasswordField();
        cmbRol      = new JComboBox<>(new String[]{"ADMIN", "OPERADOR", "LECTURA"});
        chkActivo   = new JCheckBox("Activo");
        chkActivo.setSelected(true);
        chkActivo.setBackground(Tema.AZUL_BG_FORM);

        String[]    etiquetas = {"Usuario:", "Contraseña:", "Rol:", "Estado:"};
        JComponent[] controles = {txtUsername, txtPassword, cmbRol, chkActivo};

        for (int i = 0; i < controles.length; i++) {
            gbc.gridwidth = 1; gbc.gridx = 0; gbc.gridy = i + 2; gbc.weightx = 0;
            JLabel lbl = new JLabel(etiquetas[i]);
            lbl.setFont(Tema.FUENTE_LBL);
            panel.add(lbl, gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            panel.add(controles[i], gbc);
        }

        JLabel nota = new JLabel("<html><font color='#718096' size='2'>"
                + "Al editar, deja la contraseña<br>en blanco para no cambiarla.<br>"
                + "Mínimo " + MIN_PASS + " caracteres al crear.</font></html>");
        gbc.gridwidth = 2; gbc.gridx = 0; gbc.gridy = controles.length + 2;
        panel.add(nota, gbc);

        gbc.gridy++;
        btnGuardar    = Tema.boton("💾 Guardar",     Tema.VERDE_BTN);
        btnNuevo      = Tema.boton("➕ Nuevo",        Tema.AZUL_ACTIVO);
        btnDesactivar = Tema.boton("🚫 Desactivar",  Tema.ROJO_BTN);
        btnEliminar   = Tema.boton("🗑️ Eliminar",   new Color(120, 0, 0));  // rojo oscuro: acción destructiva
        btnLimpiar    = Tema.boton("↺ Limpiar",      Tema.GRIS_BTN);

        btnGuardar.addActionListener(e -> guardar());
        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnDesactivar.addActionListener(e -> desactivar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> { limpiarFormulario(); cargarTabla(); });

        // Grid 3x2: Nuevo/Guardar · Desactivar/Eliminar · Limpiar/(vacío)
        JPanel btnP = new JPanel(new GridLayout(3, 2, 6, 6));
        btnP.setBackground(Tema.AZUL_BG_FORM);
        btnP.add(btnNuevo);     btnP.add(btnGuardar);
        btnP.add(btnDesactivar); btnP.add(btnEliminar);
        btnP.add(btnLimpiar);   btnP.add(new JLabel()); // celda vacía para mantener simetría

        gbc.gridy++;
        panel.add(btnP, gbc);
        return panel;
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        for (Usuario u : usuarioDAO.listarTodos()) {
            modeloTabla.addRow(new Object[]{
                u.getIdUsuario(),
                u.getUsername(),
                u.getRol(),
                u.isActivo() ? "✅ Activo" : "🚫 Inactivo"
            });
        }
    }

    private void cargarFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        idSeleccionado = (int) modeloTabla.getValueAt(fila, 0);
        txtUsername.setText((String) modeloTabla.getValueAt(fila, 1));
        cmbRol.setSelectedItem(modeloTabla.getValueAt(fila, 2));
        chkActivo.setSelected(modeloTabla.getValueAt(fila, 3).toString().startsWith("✅"));
        txtPassword.setText("");
    }

    // ── Operaciones ───────────────────────────────────────────

    private void guardar() {
        String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
        if (username.isEmpty()) {
            error("El nombre de usuario es obligatorio.");
            txtUsername.requestFocus();
            return;
        }
        if (username.length() > 64) {
            error("El nombre de usuario es demasiado largo (máx. 64).");
            return;
        }

        String pass = new String(txtPassword.getPassword());

        Usuario u = new Usuario();
        u.setIdUsuario(idSeleccionado);
        u.setUsername(username);
        u.setRol((String) cmbRol.getSelectedItem());
        u.setActivo(chkActivo.isSelected());

        boolean ok;
        if (idSeleccionado < 0) {
            // Alta: la contraseña es obligatoria y debe cumplir longitud mínima.
            if (pass.isEmpty()) {
                error("Ingresa una contraseña para el nuevo usuario.");
                txtPassword.requestFocus();
                return;
            }
            if (pass.length() < MIN_PASS) {
                error("La contraseña debe tener al menos " + MIN_PASS + " caracteres.");
                txtPassword.requestFocus();
                return;
            }
            if (usuarioDAO.existeUsername(username)) {
                error("Ya existe un usuario con ese nombre.");
                txtUsername.requestFocus();
                return;
            }
            u.setPasswordHash(pass);                 // el DAO se encarga de hashearla
            ok = usuarioDAO.insertar(u);
        } else {
            // Edición: si se introduce contraseña debe cumplir longitud mínima.
            if (!pass.isEmpty() && pass.length() < MIN_PASS) {
                error("La nueva contraseña debe tener al menos " + MIN_PASS + " caracteres.");
                txtPassword.requestFocus();
                return;
            }
            ok = usuarioDAO.actualizar(u);
            if (ok && !pass.isEmpty()) {
                usuarioDAO.cambiarPassword(idSeleccionado, pass);
            }
        }

        if (ok) {
            JOptionPane.showMessageDialog(this, "✅ Usuario guardado.");
            limpiarFormulario();
            cargarTabla();
        } else {
            error("No se pudo guardar el usuario.");
        }
    }

    private void desactivar() {
        if (idSeleccionado < 0) {
            error("Seleccione un usuario.");
            return;
        }
        int op = JOptionPane.showConfirmDialog(this,
                "¿Desactivar usuario " + txtUsername.getText() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) {
            if (usuarioDAO.eliminar(idSeleccionado)) {
                limpiarFormulario();
                cargarTabla();
            } else {
                error("No se pudo desactivar el usuario.");
            }
        }
    }

    /**
     * Borra definitivamente al usuario seleccionado. Acción irreversible:
     * usa doble confirmación y protege al usuario "admin" para evitar dejar
     * al sistema sin administrador inicial.
     */
    private void eliminar() {
        if (idSeleccionado < 0) {
            error("Seleccione un usuario.");
            return;
        }
        String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();

        // Protección: no permitir borrar el usuario "admin".
        if ("admin".equalsIgnoreCase(username)) {
            error("No se puede eliminar al usuario 'admin'.\n"
                + "Si necesita inhabilitarlo, use el botón Desactivar.");
            return;
        }

        // Primera confirmación.
        int op1 = JOptionPane.showConfirmDialog(this,
                "¿Eliminar PERMANENTEMENTE al usuario \"" + username + "\"?\n"
              + "Esta acción no se puede deshacer.",
                "Eliminar usuario", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (op1 != JOptionPane.YES_OPTION) return;

        // Segunda confirmación: el usuario debe escribir el nombre exacto.
        String confirm = JOptionPane.showInputDialog(this,
                "Para confirmar la eliminación, escriba el nombre del usuario:\n\""
              + username + "\"",
                "Confirmación final", JOptionPane.WARNING_MESSAGE);
        if (confirm == null) return;
        if (!confirm.trim().equals(username)) {
            error("El nombre no coincide. Operación cancelada.");
            return;
        }

        if (usuarioDAO.eliminarPermanente(idSeleccionado)) {
            JOptionPane.showMessageDialog(this,
                    "Usuario \"" + username + "\" eliminado correctamente.",
                    "Eliminación exitosa", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
            cargarTabla();
        } else {
            error("No se pudo eliminar el usuario.");
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = -1;
        txtUsername.setText("");
        txtPassword.setText("");
        cmbRol.setSelectedIndex(1);    // OPERADOR por defecto
        chkActivo.setSelected(true);
        tabla.clearSelection();
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Validación", JOptionPane.WARNING_MESSAGE);
    }
}
