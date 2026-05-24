package Centro.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Selector de fecha con calendario emergente.
 * <p>
 * Funciona como un {@link JTextField} pero al hacer clic en el campo o en
 * el botón "📅" muestra un mini calendario para elegir la fecha. También
 * admite escritura directa (d/M/yyyy, dd/MM/yyyy o yyyy-MM-dd).
 */
public class DatePickerField extends JPanel {

    private static final Color AZUL_BTN   = new Color(0, 102, 204);
    private static final Color AZUL_HOVER = new Color(214, 232, 252);
    private static final Color GRIS_OTRO  = new Color(180, 180, 180);
    private static final Color ROJO_HOY   = new Color(220, 60, 60);
    private static final Color FONDO      = Color.WHITE;
    private static final Color BORDE      = new Color(210, 220, 240);

    private static final String[] DOW_LABELS = {"do","lu","ma","mi","ju","vi","sá"};
    private static final String[] MONTHS_ES  = {
        "enero","febrero","marzo","abril","mayo","junio",
        "julio","agosto","septiembre","octubre","noviembre","diciembre"
    };

    private static final DateTimeFormatter FMT_VIEW = DateTimeFormatter.ofPattern("d/M/yyyy");
    private static final DateTimeFormatter[] PARSERS = {
        DateTimeFormatter.ofPattern("d/M/yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd")
    };

    private final JTextField field;
    private final JButton    trigger;
    private LocalDate        selected;
    private JPopupMenu       popup;

    public DatePickerField() {
        super(new BorderLayout(2, 0));
        setOpaque(false);

        field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setToolTipText("Haz clic para abrir el calendario o escribe en formato d/M/yyyy");
        field.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) showPopup();
            }
        });
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { parseFromText(); }
        });

        trigger = new JButton("📅");
        trigger.setMargin(new Insets(0, 6, 0, 6));
        trigger.setFocusPainted(false);
        trigger.setBackground(new Color(245, 248, 255));
        trigger.setToolTipText("Abrir calendario");
        trigger.addActionListener(e -> showPopup());

        add(field,   BorderLayout.CENTER);
        add(trigger, BorderLayout.EAST);

        setSelectedDate(LocalDate.now());
    }

    public LocalDate getSelectedDate() { parseFromText(); return selected; }
    public void setSelectedDate(LocalDate d) {
        this.selected = d;
        field.setText(d == null ? "" : d.format(FMT_VIEW));
    }
    public String getIsoDate() {
        LocalDate d = getSelectedDate();
        return d == null ? "" : d.toString();
    }

    private void parseFromText() {
        String text = field.getText() == null ? "" : field.getText().trim();
        if (text.isEmpty()) { selected = null; return; }
        for (DateTimeFormatter p : PARSERS) {
            try {
                LocalDate d = LocalDate.parse(text, p);
                this.selected = d;
                field.setText(d.format(FMT_VIEW));
                return;
            } catch (DateTimeParseException ignored) { }
        }
        if (selected != null) field.setText(selected.format(FMT_VIEW));
    }

    private void showPopup() {
        if (popup != null && popup.isVisible()) { popup.setVisible(false); return; }
        YearMonth start = selected != null ? YearMonth.from(selected) : YearMonth.now();
        popup = buildPopup(start);
        popup.show(this, 0, getHeight());
    }

    private JPopupMenu buildPopup(YearMonth initial) {
        final JPopupMenu p = new JPopupMenu();
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createLineBorder(BORDE));
        p.setBackground(FONDO);

        final YearMonth[] view = { initial };

        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setBackground(FONDO);
        root.setBorder(new EmptyBorder(8, 10, 10, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        final JLabel lblTitulo = new JLabel();
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitulo.setForeground(new Color(40, 60, 100));

        JButton btnHoy  = mini("Hoy");
        JButton btnPrev = mini("‹");
        JButton btnNext = mini("›");
        btnHoy.addActionListener(e -> { setSelectedDate(LocalDate.now()); p.setVisible(false); });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        right.add(btnHoy); right.add(btnPrev); right.add(btnNext);

        header.add(lblTitulo, BorderLayout.WEST);
        header.add(right,     BorderLayout.EAST);

        final JPanel grid = new JPanel(new GridLayout(7, 7, 2, 2));
        grid.setBackground(FONDO);

        final Runnable[] refresh = new Runnable[1];
        refresh[0] = () -> {
            grid.removeAll();
            lblTitulo.setText(MONTHS_ES[view[0].getMonthValue() - 1] + " " + view[0].getYear());

            for (String d : DOW_LABELS) {
                JLabel l = new JLabel(d, SwingConstants.CENTER);
                l.setForeground(new Color(120, 120, 120));
                l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                grid.add(l);
            }

            LocalDate first   = view[0].atDay(1);
            int firstDow      = first.getDayOfWeek().getValue() % 7;
            LocalDate start   = first.minusDays(firstDow);
            LocalDate hoy     = LocalDate.now();
            YearMonth viewing = view[0];

            for (int i = 0; i < 42; i++) {
                LocalDate d = start.plusDays(i);
                grid.add(dayButton(d, viewing, hoy, p));
            }
            grid.revalidate();
            grid.repaint();
            p.pack();
        };

        btnPrev.addActionListener(e -> { view[0] = view[0].minusMonths(1); refresh[0].run(); });
        btnNext.addActionListener(e -> { view[0] = view[0].plusMonths(1);  refresh[0].run(); });

        root.add(header, BorderLayout.NORTH);
        root.add(grid,   BorderLayout.CENTER);
        p.add(root, BorderLayout.CENTER);

        refresh[0].run();
        return p;
    }

    private JButton dayButton(LocalDate d, YearMonth viewing, LocalDate today, JPopupMenu p) {
        final JButton btn = new JButton(String.valueOf(d.getDayOfMonth()));
        btn.setMargin(new Insets(2, 2, 2, 2));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setPreferredSize(new Dimension(34, 28));

        final boolean enMes = d.getMonth() == viewing.getMonth() && d.getYear() == viewing.getYear();
        if (!enMes) btn.setForeground(GRIS_OTRO);

        final boolean esHoy = d.equals(today);
        final boolean esSel = selected != null && d.equals(selected);

        if (esSel) {
            btn.setOpaque(true); btn.setContentAreaFilled(true);
            btn.setBackground(AZUL_BTN); btn.setForeground(Color.WHITE);
        } else if (esHoy) {
            btn.setOpaque(true); btn.setContentAreaFilled(true);
            btn.setBackground(ROJO_HOY); btn.setForeground(Color.WHITE);
        }

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!esSel && !esHoy) {
                    btn.setOpaque(true); btn.setContentAreaFilled(true);
                    btn.setBackground(AZUL_HOVER);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!esSel && !esHoy) {
                    btn.setOpaque(false); btn.setContentAreaFilled(false);
                }
            }
        });

        btn.addActionListener(e -> { setSelectedDate(d); p.setVisible(false); });
        return btn;
    }

    private JButton mini(String txt) {
        final JButton b = new JButton(txt);
        b.setMargin(new Insets(2, 8, 2, 8));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setForeground(new Color(60, 90, 150));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setForeground(AZUL_BTN); }
            @Override public void mouseExited(MouseEvent e)  { b.setForeground(new Color(60, 90, 150)); }
        });
        return b;
    }
}
