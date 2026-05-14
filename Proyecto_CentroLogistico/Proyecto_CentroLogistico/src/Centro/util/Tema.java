package Centro.util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centraliza la paleta de colores, las fuentes y el look &amp; feel del sistema.
 * <p>
 * Si FlatLaf está disponible en el classpath se aplica de forma transparente
 * mediante reflexión; si no, se cae al look &amp; feel del sistema operativo.
 * Esto permite que el proyecto compile y funcione exactamente igual aunque el
 * usuario aún no haya añadido el JAR de FlatLaf.
 * <p>
 * Cómo activar la apariencia moderna:
 * <ol>
 *   <li>Descargar {@code flatlaf-3.x.x.jar} desde
 *       <a href="https://github.com/JFormDesigner/FlatLaf/releases">JFormDesigner/FlatLaf</a>.</li>
 *   <li>En NetBeans: clic derecho en el proyecto &rarr; <em>Properties</em>
 *       &rarr; <em>Libraries</em> &rarr; <em>Add JAR/Folder</em> y seleccionar
 *       el archivo descargado.</li>
 *   <li>Ejecutar como siempre — el tema se carga automáticamente.</li>
 * </ol>
 */
public final class Tema {

    private static final Logger LOGGER = Logger.getLogger(Tema.class.getName());

    // ── Paleta corporativa ────────────────────────────────────────
    public static final Color AZUL_OSCURO    = new Color(10, 33, 80);
    public static final Color AZUL_PROFUNDO  = new Color(7, 24, 60);
    public static final Color AZUL_HOVER     = new Color(20, 60, 140);
    public static final Color AZUL_ACTIVO    = new Color(0, 102, 204);
    public static final Color AZUL_BG        = new Color(245, 248, 252);
    public static final Color AZUL_BG_FORM   = new Color(245, 248, 255);
    public static final Color AZUL_HEADER    = new Color(224, 235, 255);
    public static final Color AZUL_SELECCION = new Color(173, 214, 255);
    public static final Color AZUL_BORDE     = new Color(190, 210, 240);

    public static final Color VERDE_BTN      = new Color(0, 128, 64);
    public static final Color ROJO_BTN       = new Color(192, 0, 0);
    public static final Color GRIS_BTN       = new Color(120, 120, 120);

    public static final Color GRIS_TEXTO     = new Color(100, 120, 160);
    public static final Color BORDE_SUAVE    = new Color(210, 220, 235);

    public static final Color VENCE_ROJO     = new Color(255, 200, 200);
    public static final Color VENCE_NARANJA  = new Color(255, 235, 190);
    public static final Color FILA_PAR       = Color.WHITE;
    public static final Color FILA_IMPAR     = new Color(245, 248, 255);

    // ── Fuentes ───────────────────────────────────────────────────
    public static final Font FUENTE_BASE     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FUENTE_NEGRITA  = FUENTE_BASE.deriveFont(Font.BOLD);
    public static final Font FUENTE_TITULO   = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FUENTE_TITULO_S = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FUENTE_LBL      = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FUENTE_LBL_BIG  = new Font("Segoe UI", Font.BOLD, 12);
    /** Fuente de botones — usa automáticamente la mejor familia con soporte emoji disponible. */
    public static final Font FUENTE_BTN      = new Font(detectarFamiliaEmoji(), Font.BOLD, 11);
    public static final Font FUENTE_LIST     = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FUENTE_NOTA     = new Font("Segoe UI", Font.ITALIC, 11);

    /** Familia de fuente detectada para renderizar emojis modernos. */
    public static final String FAMILIA_EMOJI = detectarFamiliaEmoji();

    private Tema() {}

    /**
     * Detecta la mejor fuente con soporte de emojis disponible en el sistema.
     * <ul>
     *   <li>Windows 10/11 → <em>Segoe UI Emoji</em></li>
     *   <li>macOS        → <em>Apple Color Emoji</em></li>
     *   <li>Linux        → <em>Noto Color Emoji</em> / <em>Symbola</em> / <em>DejaVu Sans</em></li>
     *   <li>Fallback     → <em>Dialog</em> (fuente lógica de Java)</li>
     * </ul>
     * Java Swing solo puede mostrar los glifos de emojis si la fuente del
     * componente los contiene; "Segoe UI" no los incluye, "Segoe UI Emoji" sí.
     */
    private static String detectarFamiliaEmoji() {
        String[] candidatas = {
            "Segoe UI Emoji",       // Windows 10/11
            "Apple Color Emoji",    // macOS
            "Noto Color Emoji",     // Linux
            "Symbola",              // Linux (paquete texlive-fonts-extra)
            "Segoe UI Symbol",      // Windows fallback
            "DejaVu Sans"           // Linux genérico
        };
        try {
            java.util.Set<String> set = new java.util.HashSet<>(java.util.Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
            for (String c : candidatas) {
                if (set.contains(c)) {
                    LOGGER.info("Fuente con soporte emoji detectada: " + c);
                    return c;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudieron enumerar las fuentes del sistema", e);
        }
        LOGGER.info("Ninguna fuente con soporte emoji disponible; se usará 'Dialog'.");
        return "Dialog";
    }

    /**
     * Crea una {@link Font} usando la familia con soporte emoji detectada.
     * Útil cuando un componente Swing (botón, etiqueta, etc.) muestra texto
     * que contiene emojis y necesita una fuente que sí pueda renderizarlos.
     *
     * @param style {@link Font#PLAIN}, {@link Font#BOLD} o {@link Font#ITALIC}
     * @param size  tamaño en puntos
     */
    public static Font fontEmoji(int style, int size) {
        return new Font(FAMILIA_EMOJI, style, size);
    }

    // ── Look &amp; Feel ─────────────────────────────────────────────

    /**
     * Aplica el mejor look &amp; feel disponible. Llamar antes de instanciar
     * cualquier {@code JFrame}.
     */
    public static void aplicarLookAndFeel() {
        // 1) FlatLaf (si está en el classpath). Carga por reflexión para que
        //    el proyecto compile sin la dependencia.
        String[] candidatos = {
            "com.formdev.flatlaf.FlatLightLaf",
            "com.formdev.flatlaf.FlatIntelliJLaf"
        };
        for (String cls : candidatos) {
            try {
                Class<?> c = Class.forName(cls);
                LookAndFeel laf = (LookAndFeel) c.getDeclaredConstructor().newInstance();
                UIManager.setLookAndFeel(laf);
                aplicarDefaults();
                LOGGER.info("Look & feel: " + cls);
                return;
            } catch (ClassNotFoundException ignored) {
                // FlatLaf no está en el classpath: probar siguiente.
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "No se pudo cargar " + cls, e);
            }
        }

        // 2) Fallback: look & feel del sistema operativo.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            LOGGER.info("Look & feel: sistema (FlatLaf no encontrado en el classpath).");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo aplicar el look & feel del sistema", e);
        }
        aplicarDefaults();
    }

    /** Defaults de UIManager — los valores propios de FlatLaf se ignoran si no está cargado. */
    private static void aplicarDefaults() {
        UIManager.put("Table.rowHeight",            26);
        UIManager.put("Table.gridColor",            new Color(225, 232, 244));
        UIManager.put("Table.selectionBackground",  AZUL_SELECCION);
        UIManager.put("Table.selectionForeground",  Color.BLACK);
        UIManager.put("TableHeader.font",           FUENTE_LBL_BIG);
        UIManager.put("Table.font",                 FUENTE_LIST);

        // Estos sólo aplican con FlatLaf — son ignorados de forma segura por otros L&F.
        UIManager.put("Button.arc",                 8);
        UIManager.put("Component.arc",              8);
        UIManager.put("ProgressBar.arc",            8);
        UIManager.put("TextComponent.arc",          6);
        UIManager.put("ScrollBar.thumbArc",         999);
        UIManager.put("ScrollBar.thumbInsets",      new Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.showButtons",      false);
        UIManager.put("ScrollBar.width",            12);
    }

    // ── Factory de componentes ────────────────────────────────────

    /** Crea un botón rectangular plano con fondo de color sólido y texto blanco. */
    public static JButton boton(String texto, Color fondo) {
        JButton b = new JButton(texto);
        b.setBackground(fondo);
        b.setForeground(Color.WHITE);
        b.setFont(FUENTE_BTN);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Borde compuesto típico para campos de texto (línea fina + padding interior). */
    public static Border bordeCampo() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 230)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10));
    }
}
