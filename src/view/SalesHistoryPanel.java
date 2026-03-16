package view;

import config.DatabaseConnection;
import model.User;
import org.knowm.xchart.*;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.PieStyler.LabelType;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class SalesHistoryPanel extends JPanel {

    private static final Color ACCENT      = new Color(0xC7, 0x4B, 0x1A);
    private static final Color LEFT_TOP    = new Color(0xB0, 0x3A, 0x12);
    private static final Color LEFT_BOT    = new Color(0x4A, 0x18, 0x08);
    private static final Color CARD        = Color.WHITE;
    private static final Color BG          = new Color(0xF5, 0xF0, 0xE8);
    private static final Color TEXT        = new Color(0x1A, 0x14, 0x10);
    private static final Color MUTED       = new Color(0x6B, 0x5E, 0x52);
    private static final Color BORDER_CLR  = new Color(0xE8, 0xE2, 0xD8);
    private static final Color COL_BORDER  = new Color(0xE0, 0xD8, 0xCC);
    private static final Color HDR_TOP     = new Color(0xB0, 0x3A, 0x12);
    private static final Color HDR_BOT     = new Color(0x6A, 0x22, 0x0C);
    private static final Color GOLD        = new Color(0xFF, 0xD5, 0x80);
    private static final Color INPUT_BG    = new Color(0xFA, 0xF7, 0xF3);
    private static final Color ROW_EVEN    = Color.WHITE;
    private static final Color ROW_ODD     = new Color(0xFD, 0xFB, 0xF8);
    private static final Color ROW_HOVER   = new Color(0xFB, 0xF0, 0xE8);
    private static final Color ROW_SEL     = new Color(0xF5, 0xE6, 0xDC);
    private static final Color BLUE        = new Color(0x1A, 0x52, 0x76);

    private static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  22);
    private static final Font FONT_SUB     = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_TABLE_H = new Font("SansSerif", Font.BOLD,  11);
    private static final Font FONT_TABLE   = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_MONO    = new Font("Monospaced", Font.BOLD, 13);
    private static final Font FONT_BTN     = new Font("SansSerif", Font.BOLD,  13);

    private static final SimpleDateFormat SQL_FMT = new SimpleDateFormat("yyyy-MM-dd");

    private JTable            salesTable;
    private JTable            itemsTable;
    private DefaultTableModel salesModel;
    private DefaultTableModel itemsModel;
    private JPanel            chartPanel;
    private int               hoveredSalesRow  = -1;
    private int               hoveredItemsRow  = -1;

    private JLabel chipTotalSales;
    private JLabel chipTotalRevenue;

    private JSpinner spFrom;
    private JSpinner spTo;

    private final Runnable onBack;
    private final User     loggedInUser;

    public SalesHistoryPanel(User user, Runnable onBack) {
        this.loggedInUser = user;
        this.onBack       = onBack;
        setLayout(new BorderLayout());
        setBackground(BG);
        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    public SalesHistoryPanel(Runnable onBack) { this(null, onBack); }
    public SalesHistoryPanel()               { this(null, null);   }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, LEFT_TOP, getWidth(), getHeight(), LEFT_BOT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(18, 28, 18, 28));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        JButton btnBack = buildIconButton("← Back", new Color(255, 255, 255, 30), Color.WHITE);
        btnBack.addActionListener(e -> { if (onBack != null) onBack.run(); });
        left.add(btnBack);
        left.add(Box.createHorizontalStrut(6));

        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);
        JLabel title = new JLabel("📊 Sales History");
        title.setFont(FONT_TITLE); title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("View transactions, items sold, and revenue trends");
        sub.setFont(FONT_SUB); sub.setForeground(new Color(255, 255, 255, 170));
        titleStack.add(title); titleStack.add(sub);
        left.add(titleStack);
        bar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        chipTotalSales   = buildHeaderChip("Sales: —");
        chipTotalRevenue = buildHeaderChip("Revenue: —");
        right.add(chipTotalSales);
        right.add(chipTotalRevenue);
        JButton btnRefresh = buildIconButton("🔄 Refresh", new Color(255, 255, 255, 30), Color.WHITE);
        btnRefresh.addActionListener(e -> { loadSales(); loadCharts(); });
        right.add(btnRefresh);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JLabel buildHeaderChip(String text) {
        JLabel chip = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 35));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose(); super.paintComponent(g);
            }
        };
        chip.setFont(new Font("SansSerif", Font.BOLD, 13));
        chip.setForeground(Color.WHITE);
        chip.setBorder(new EmptyBorder(6, 16, 6, 16));
        chip.setOpaque(false);
        return chip;
    }

    private JSplitPane buildContent() {
        JSplitPane main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildLeftPane(), buildRightPane());
        main.setDividerLocation(650);
        main.setDividerSize(6);
        main.setBorder(new EmptyBorder(20, 24, 20, 24));
        main.setBackground(BG);
        main.setOpaque(false);
        main.setContinuousLayout(true);
        return main;
    }

    private JSplitPane buildLeftPane() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                buildSalesCard(), buildItemsCard());
        split.setDividerLocation(300);
        split.setDividerSize(6);
        split.setOpaque(false);
        split.setBorder(null);
        split.setContinuousLayout(true);
        return split;
    }

    private JPanel buildSalesCard() {
        return buildTableCard("🧾 Transactions", buildSalesScrollPane(), true);
    }

    private JPanel buildItemsCard() {
        return buildTableCard("📦 Sale Items", buildItemsScrollPane(), false);
    }

    private JPanel buildTableCard(String title, JScrollPane scroll, boolean isSales) {
        JPanel cornerFill = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, HDR_TOP, 0, getHeight(), HDR_BOT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(GOLD); g2.setStroke(new BasicStroke(2f));
                g2.drawLine(0, getHeight() - 2, getWidth(), getHeight() - 2);
                g2.dispose();
            }
        };
        cornerFill.setOpaque(false);
        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, cornerFill);

        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fill rounded background
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BORDER_CLR);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
            @Override public boolean isOpaque() { return false; }
        };
        card.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(14, 20, 12, 20));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14)); lbl.setForeground(TEXT);
        header.add(lbl, BorderLayout.WEST);

        if (!isSales) {
            JLabel hint = new JLabel("← Select a transaction to see items");
            hint.setFont(new Font("SansSerif", Font.ITALIC, 11)); hint.setForeground(MUTED);
            header.add(hint, BorderLayout.EAST);
        }

        JPanel divider = new JPanel();
        divider.setBackground(BORDER_CLR);
        divider.setOpaque(true);
        divider.setPreferredSize(new Dimension(0, 1));

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(header,  BorderLayout.NORTH);
        north.add(divider, BorderLayout.SOUTH);
        card.add(north,  BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JScrollPane buildSalesScrollPane() {
        salesModel = new DefaultTableModel(
                new String[]{"_id", "#", "Cashier", "Total (₱)", "Date"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        salesTable = new JTable(salesModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                boolean sel = isRowSelected(row), hov = (row == hoveredSalesRow) && !sel;
                c.setBackground(sel ? ROW_SEL : hov ? ROW_HOVER : row % 2 == 0 ? ROW_EVEN : ROW_ODD);
                return c;
            }
        };
        styleTable(salesTable);

        salesTable.getColumnModel().getColumn(0).setMinWidth(0);
        salesTable.getColumnModel().getColumn(0).setMaxWidth(0);
        salesTable.getColumnModel().getColumn(0).setWidth(0);
        salesTable.getColumnModel().getColumn(1).setPreferredWidth(45);
        salesTable.getColumnModel().getColumn(1).setMaxWidth(55);
        salesTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        salesTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        salesTable.getColumnModel().getColumn(4).setPreferredWidth(180);

        salesTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("SansSerif", Font.BOLD, 12));
                setForeground(MUTED);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, COL_BORDER),
                    new EmptyBorder(0, 8, 0, 8)));
                return this;
            }
        });

        salesTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.LEFT);
                setFont(FONT_TABLE);
                setForeground(TEXT);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, COL_BORDER),
                    new EmptyBorder(0, 10, 0, 10)));
                return this;
            }
        });

        salesTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, "₱ " + val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.LEFT);
                setFont(FONT_MONO);
                setForeground(BLUE);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, COL_BORDER),
                    new EmptyBorder(0, 10, 0, 10)));
                return this;
            }
        });

        salesTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.LEFT);
                setFont(FONT_TABLE);
                setForeground(MUTED);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, COL_BORDER),
                    new EmptyBorder(0, 10, 0, 10)));
                return this;
            }
        });

        salesTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int row = salesTable.rowAtPoint(e.getPoint());
                if (row != hoveredSalesRow) { hoveredSalesRow = row; salesTable.repaint(); }
            }
        });
        salesTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { hoveredSalesRow = -1; salesTable.repaint(); }
        });
        salesTable.getSelectionModel().addListSelectionListener(e -> {
            int row = salesTable.getSelectedRow();
            if (!e.getValueIsAdjusting() && row != -1)
                loadSaleItems((int) salesModel.getValueAt(row, 0));
        });
        loadSales();
        return styledScrollPane(salesTable);
    }

    private JScrollPane buildItemsScrollPane() {
        itemsModel = new DefaultTableModel(
                new String[]{"Product", "Qty", "Price (₱)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        itemsTable = new JTable(itemsModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                boolean sel = isRowSelected(row), hov = (row == hoveredItemsRow) && !sel;
                c.setBackground(sel ? ROW_SEL : hov ? ROW_HOVER : row % 2 == 0 ? ROW_EVEN : ROW_ODD);
                return c;
            }
        };
        styleTable(itemsTable);
        itemsTable.setFillsViewportHeight(false);

        itemsTable.getColumnModel().getColumn(0).setPreferredWidth(260);
        itemsTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        itemsTable.getColumnModel().getColumn(1).setMaxWidth(80);
        itemsTable.getColumnModel().getColumn(2).setPreferredWidth(130);

        itemsTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.LEFT);
                setFont(new Font("SansSerif", Font.BOLD, 13));
                setForeground(TEXT);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, COL_BORDER),
                    new EmptyBorder(0, 10, 0, 10)));
                return this;
            }
        });

        itemsTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(FONT_TABLE);
                setForeground(TEXT);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, COL_BORDER),
                    new EmptyBorder(0, 8, 0, 8)));
                return this;
            }
        });

        itemsTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, "₱ " + val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.LEFT);
                setFont(FONT_MONO);
                setForeground(BLUE);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, COL_BORDER),
                    new EmptyBorder(0, 10, 0, 10)));
                return this;
            }
        });

        itemsTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int row = itemsTable.rowAtPoint(e.getPoint());
                if (row != hoveredItemsRow) { hoveredItemsRow = row; itemsTable.repaint(); }
            }
        });
        itemsTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { hoveredItemsRow = -1; itemsTable.repaint(); }
        });
        return styledScrollPane(itemsTable);
    }

    private JPanel buildRightPane() {
        JPanel outer = new JPanel(new BorderLayout(0, 10));
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(0, 12, 0, 0));
        outer.add(buildDateFilterBar(), BorderLayout.NORTH);

        chartPanel = new JPanel();
        chartPanel.setLayout(new BoxLayout(chartPanel, BoxLayout.Y_AXIS));
        chartPanel.setOpaque(false);
        loadCharts();

        JScrollPane chartScroll = new JScrollPane(chartPanel);
        chartScroll.setBorder(null);
        chartScroll.setOpaque(false);
        chartScroll.getViewport().setOpaque(false);
        chartScroll.getViewport().setBackground(BG);
        chartScroll.getVerticalScrollBar().setUnitIncrement(20);
        outer.add(chartScroll, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildDateFilterBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_CLR); g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(8, 14, 8, 14));

        Calendar calFrom = Calendar.getInstance();
        calFrom.set(Calendar.MONTH, Calendar.JANUARY);
        calFrom.set(Calendar.DAY_OF_MONTH, 1);
        calFrom.set(Calendar.HOUR_OF_DAY, 0);
        calFrom.set(Calendar.MINUTE, 0);
        calFrom.set(Calendar.SECOND, 0);
        calFrom.set(Calendar.MILLISECOND, 0);
        spFrom = buildDateSpinner(calFrom.getTime());
        spTo   = buildDateSpinner(new java.util.Date());

        JLabel ico = new JLabel("🗓");
        ico.setFont(new Font("SansSerif", Font.PLAIN, 13));
        ico.setBorder(new EmptyBorder(0, 0, 0, 8));

        JLabel lblFrom = makeFilterLabel("From");
        JLabel lblTo   = makeFilterLabel("To");
        lblTo.setBorder(new EmptyBorder(0, 10, 0, 6));

        JButton btnApply = buildFilterButton("Apply", ACCENT, Color.WHITE);
        btnApply.addActionListener(e -> loadCharts());

        JButton btnReset = buildFilterButton("Reset", INPUT_BG, MUTED);
        btnReset.addActionListener(e -> {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.MONTH, Calendar.JANUARY);
            c.set(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            ((SpinnerDateModel) spFrom.getModel()).setValue(c.getTime());
            ((SpinnerDateModel) spTo.getModel()).setValue(new java.util.Date());
            loadCharts();
        });

        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        fields.setOpaque(false);
        fields.add(ico);
        fields.add(lblFrom); fields.add(spFrom);
        fields.add(lblTo);   fields.add(spTo);
        bar.add(fields, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.setOpaque(false);
        btns.add(btnReset); btns.add(btnApply);
        bar.add(btns, BorderLayout.EAST);
        return bar;
    }

    private JSpinner buildDateSpinner(java.util.Date initial) {
        java.util.Date value = initial != null ? initial : new java.util.Date();
        SpinnerDateModel model = new SpinnerDateModel(value, null, null, Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "MMM dd, yyyy");
        spinner.setEditor(editor);
        editor.getTextField().setFont(new Font("SansSerif", Font.PLAIN, 12));
        editor.getTextField().setForeground(TEXT);
        editor.getTextField().setBackground(INPUT_BG);
        editor.getTextField().setBorder(new EmptyBorder(3, 8, 3, 4));
        editor.getTextField().setOpaque(true);
        spinner.setPreferredSize(new Dimension(130, 28));
        spinner.setBorder(BorderFactory.createLineBorder(BORDER_CLR, 1));
        spinner.setBackground(INPUT_BG);
        return spinner;
    }

    private JLabel makeFilterLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(MUTED);
        l.setBorder(new EmptyBorder(0, 0, 0, 6));
        return l;
    }

    private JButton buildFilterButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                if (fg == MUTED) {
                    g2.setColor(BORDER_CLR); g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 12)); btn.setForeground(fg);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        return btn;
    }

    private String buildDateWhere(String tableAlias) {
        if (spFrom == null || spTo == null) return buildUserOnlyWhere(tableAlias);
        java.util.Date from = (java.util.Date) spFrom.getValue();
        java.util.Date to   = (java.util.Date) spTo.getValue();
        if (from == null || to == null) return buildUserOnlyWhere(tableAlias);
        String userClause = (loggedInUser != null)
            ? tableAlias + ".user_id = " + loggedInUser.getUserId() + " AND " : "";
        return " WHERE " + userClause
             + "DATE(" + tableAlias + ".created_at) >= '" + SQL_FMT.format(from) + "'"
             + " AND DATE(" + tableAlias + ".created_at) <= '" + SQL_FMT.format(to) + "'";
    }

    private String buildPieBarWhere() {
        if (spFrom == null || spTo == null) return buildUserOnlyWhere("s");
        java.util.Date from = (java.util.Date) spFrom.getValue();
        java.util.Date to   = (java.util.Date) spTo.getValue();
        if (from == null || to == null) return buildUserOnlyWhere("s");
        String userClause = (loggedInUser != null)
            ? "s.user_id = " + loggedInUser.getUserId() + " AND " : "";
        return " WHERE " + userClause
             + "DATE(s.created_at) >= '" + SQL_FMT.format(from) + "'"
             + " AND DATE(s.created_at) <= '" + SQL_FMT.format(to) + "'";
    }

    private String buildUserOnlyWhere(String alias) {
        return (loggedInUser != null)
            ? " WHERE " + alias + ".user_id = " + loggedInUser.getUserId() : "";
    }

    private void loadSales() {
        salesModel.setRowCount(0);
        int count = 0; double revenue = 0;
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql;
            PreparedStatement stmt;
            if (loggedInUser != null) {
                sql  = "SELECT s.sale_id, u.username, s.total_amount, s.created_at" +
                       " FROM sales s JOIN users u ON s.user_id = u.user_id" +
                       " WHERE s.user_id = ? ORDER BY s.created_at ASC";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, loggedInUser.getUserId());
            } else {
                sql  = "SELECT s.sale_id, u.username, s.total_amount, s.created_at" +
                       " FROM sales s JOIN users u ON s.user_id = u.user_id" +
                       " ORDER BY s.created_at ASC";
                stmt = conn.prepareStatement(sql);
            }
            ResultSet rs = stmt.executeQuery();
            int rowNum = 1;
            while (rs.next()) {
                double total = rs.getDouble("total_amount");
                salesModel.addRow(new Object[]{
                    rs.getInt("sale_id"), rowNum++,
                    rs.getString("username"),
                    String.format("%.2f", total),
                    rs.getTimestamp("created_at")
                });
                count++; revenue += total;
            }
        } catch (Exception e) { e.printStackTrace(); }
        chipTotalSales.setText("Sales: " + count);
        chipTotalRevenue.setText(String.format("Revenue: ₱ %,.2f", revenue));
    }

    private void loadSaleItems(int saleId) {
        itemsModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT p.product_name, si.quantity, si.price" +
                         " FROM sale_items si JOIN products p ON si.product_id = p.product_id" +
                         " WHERE si.sale_id = ? ORDER BY p.product_name ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, saleId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                itemsModel.addRow(new Object[]{
                    rs.getString("product_name"),
                    rs.getInt("quantity"),
                    String.format("%.2f", rs.getDouble("price"))
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadCharts() {
        chartPanel.removeAll();
        String dateWhere = buildDateWhere("s");
        String pieWhere  = buildPieBarWhere();

        XYChart lineChart = new XYChartBuilder().width(360).height(220)
                .xAxisTitle("Day").yAxisTitle("Revenue (₱)").build();
        styleXYChart(lineChart);
        List<Double> xVals = new ArrayList<>(), yVals = new ArrayList<>();
        List<String> dateLabels = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT DATE(s.created_at) AS sale_date, SUM(s.total_amount) AS total" +
                         " FROM sales s" + dateWhere +
                         " GROUP BY DATE(s.created_at) ORDER BY sale_date ASC";
            ResultSet rs = conn.prepareStatement(sql).executeQuery();
            int idx = 0;
            while (rs.next()) {
                xVals.add((double) idx); yVals.add(rs.getDouble("total"));
                dateLabels.add(rs.getString("sale_date")); idx++;
            }
        } catch (Exception e) { e.printStackTrace(); }
        if (!xVals.isEmpty()) {
            lineChart.addSeries("Revenue", xVals, yVals);
            lineChart.setCustomXAxisTickLabelsFormatter(i -> {
                int idx = i.intValue();
                return (idx >= 0 && idx < dateLabels.size()) ? dateLabels.get(idx) : "";
            });
        }
        chartPanel.add(wrapChart(new XChartPanel<>(lineChart), "📈 Daily Revenue Trend"));
        chartPanel.add(Box.createVerticalStrut(12));

        PieChart pieChart = new PieChartBuilder().width(360).height(260).build();
        stylePieChart(pieChart);
        List<String> pieNames = new ArrayList<>();
        Color[] pieColors = {
            ACCENT, new Color(0xE8,0x8A,0x3C), new Color(0x2E,0x7D,0x52),
            new Color(0x1A,0x52,0x76), new Color(0xFF,0xD5,0x80),
            new Color(0x9B,0x2C,0x1F), new Color(0x6A,0x5A,0xCD), new Color(0x20,0x8A,0x8A)
        };
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT p.product_name, SUM(si.quantity) AS total_sold" +
                         " FROM sale_items si JOIN products p ON si.product_id = p.product_id" +
                         " JOIN sales s ON si.sale_id = s.sale_id" + pieWhere +
                         " GROUP BY p.product_name ORDER BY total_sold DESC";
            ResultSet rs = conn.prepareStatement(sql).executeQuery();
            boolean hasData = false;
            while (rs.next()) {
                String name = rs.getString("product_name");
                pieChart.addSeries(name, rs.getInt("total_sold"));
                pieNames.add(name); hasData = true;
            }
            if (!hasData) { pieChart.addSeries("No Data", 1); pieNames.add("No Data"); }
        } catch (Exception e) { e.printStackTrace(); }
        chartPanel.add(wrapPieChart(new XChartPanel<>(pieChart), "🥧 Product Mix", pieNames, pieColors));
        chartPanel.add(Box.createVerticalStrut(12));

        List<String> names = new ArrayList<>();
        List<Integer> qtys = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT p.product_name, SUM(si.quantity) AS total_sold" +
                         " FROM sale_items si JOIN products p ON si.product_id = p.product_id" +
                         " JOIN sales s ON si.sale_id = s.sale_id" + pieWhere +
                         " GROUP BY p.product_name ORDER BY total_sold DESC LIMIT 5";
            ResultSet rs = conn.prepareStatement(sql).executeQuery();
            while (rs.next()) {
                names.add(rs.getString("product_name")); qtys.add(rs.getInt("total_sold"));
            }
        } catch (Exception e) { e.printStackTrace(); }

        if (names.size() >= 2) {
            CategoryChart barChart = new CategoryChartBuilder().width(360).height(220)
                    .xAxisTitle("Product").yAxisTitle("Units Sold").build();
            styleCategoryChart(barChart);
            barChart.addSeries("Sold", names, qtys);
            chartPanel.add(wrapChart(new XChartPanel<>(barChart), "🏆 Top 5 Products"));
        } else {
            chartPanel.add(wrapEmptyChart("🏆 Top 5 Products",
                names.isEmpty() ? "No sales data for this period."
                                : "Need at least 2 products to display chart."));
        }

        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private void styleXYChart(XYChart chart) {
        var s = chart.getStyler();
        s.setChartBackgroundColor(CARD); s.setPlotBackgroundColor(new Color(0xFD,0xFB,0xF8));
        s.setChartFontColor(MUTED); s.setPlotBorderColor(BORDER_CLR);
        s.setSeriesColors(new Color[]{ ACCENT }); s.setLegendVisible(false);
        s.setAxisTickLabelsColor(MUTED); s.setChartTitleVisible(false);
    }

    private void stylePieChart(PieChart chart) {
        chart.getStyler().setChartBackgroundColor(CARD);
        chart.getStyler().setPlotBackgroundColor(CARD);
        chart.getStyler().setChartFontColor(MUTED);
        chart.getStyler().setSeriesColors(new Color[]{
            ACCENT, new Color(0xE8,0x8A,0x3C), new Color(0x2E,0x7D,0x52),
            new Color(0x1A,0x52,0x76), new Color(0xFF,0xD5,0x80),
            new Color(0x9B,0x2C,0x1F), new Color(0x6A,0x5A,0xCD), new Color(0x20,0x8A,0x8A),
        });
        chart.getStyler().setChartTitleVisible(false);
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setLabelsVisible(true);
        chart.getStyler().setLabelType(LabelType.Percentage);
        chart.getStyler().setLabelsDistance(0.68);
        chart.getStyler().setForceAllLabelsVisible(true);
        chart.getStyler().setCircular(true);
    }

    private void styleCategoryChart(CategoryChart chart) {
        var s = chart.getStyler();
        s.setChartBackgroundColor(CARD); s.setPlotBackgroundColor(new Color(0xFD,0xFB,0xF8));
        s.setChartFontColor(MUTED); s.setPlotBorderColor(BORDER_CLR);
        s.setSeriesColors(new Color[]{ ACCENT }); s.setLegendVisible(false);
        s.setAxisTickLabelsColor(MUTED); s.setChartTitleVisible(false);
    }

    private JPanel wrapEmptyChart(String label, String message) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_CLR); g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false); card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 270));

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false); hdr.setBorder(new EmptyBorder(10, 16, 8, 16));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12)); lbl.setForeground(TEXT);
        hdr.add(lbl, BorderLayout.WEST);

        JPanel div = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BORDER_CLR); g.fillRect(0, 0, getWidth(), 1);
            }
        };
        div.setPreferredSize(new Dimension(0, 1)); div.setOpaque(false);

        JPanel north = new JPanel(new BorderLayout()); north.setOpaque(false);
        north.add(hdr, BorderLayout.NORTH); north.add(div, BorderLayout.SOUTH);
        card.add(north, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        JLabel msg = new JLabel(message);
        msg.setFont(new Font("SansSerif", Font.ITALIC, 13));
        msg.setForeground(MUTED);
        body.add(msg);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel wrapChart(JPanel chartWidget, String label) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_CLR); g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, chartWidget.getPreferredSize().height + 50));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false); hdr.setBorder(new EmptyBorder(10, 16, 8, 16));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12)); lbl.setForeground(TEXT);
        hdr.add(lbl, BorderLayout.WEST);

        JPanel div = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BORDER_CLR); g.fillRect(0, 0, getWidth(), 1);
            }
        };
        div.setPreferredSize(new Dimension(0, 1)); div.setOpaque(false);

        JPanel north = new JPanel(new BorderLayout()); north.setOpaque(false);
        north.add(hdr, BorderLayout.NORTH); north.add(div, BorderLayout.SOUTH);
        card.add(north, BorderLayout.NORTH);
        card.add(chartWidget, BorderLayout.CENTER);
        return card;
    }

    private JPanel wrapPieChart(JPanel chartWidget, String label, List<String> names, Color[] colors) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_CLR); g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false); card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, chartWidget.getPreferredSize().height + 50));

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false); hdr.setBorder(new EmptyBorder(10, 16, 8, 16));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12)); lbl.setForeground(TEXT);
        hdr.add(lbl, BorderLayout.WEST);
        JPanel div = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BORDER_CLR); g.fillRect(0, 0, getWidth(), 1);
            }
        };
        div.setPreferredSize(new Dimension(0, 1)); div.setOpaque(false);
        JPanel north = new JPanel(new BorderLayout()); north.setOpaque(false);
        north.add(hdr, BorderLayout.NORTH); north.add(div, BorderLayout.SOUTH);
        card.add(north, BorderLayout.NORTH);
        card.add(chartWidget, BorderLayout.CENTER);

        JPanel legendItems = new JPanel();
        legendItems.setLayout(new BoxLayout(legendItems, BoxLayout.Y_AXIS));
        legendItems.setOpaque(true); legendItems.setBackground(CARD);
        legendItems.setBorder(new EmptyBorder(8, 10, 8, 10));
        for (int i = 0; i < names.size(); i++) {
            final Color dot   = colors[i % colors.length];
            final String name = names.get(i);
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
            row.setOpaque(false);
            JLabel dotLbl = new JLabel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(dot); g2.fillOval(0, 2, 10, 10); g2.dispose();
                }
                @Override public Dimension getPreferredSize() { return new Dimension(10, 14); }
            };
            JLabel nameLbl = new JLabel(name);
            nameLbl.setFont(new Font("SansSerif", Font.PLAIN, 11)); nameLbl.setForeground(TEXT);
            row.add(dotLbl); row.add(nameLbl); legendItems.add(row);
        }
        JScrollPane legendScroll = new JScrollPane(legendItems);
        legendScroll.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER_CLR));
        legendScroll.setOpaque(true); legendScroll.getViewport().setOpaque(true);
        legendScroll.getViewport().setBackground(CARD); legendScroll.setBackground(CARD);
        legendScroll.setPreferredSize(new Dimension(130, 0));
        legendScroll.getVerticalScrollBar().setUnitIncrement(12);
        legendScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        card.add(legendScroll, BorderLayout.EAST);
        return card;
    }

    private void styleTable(JTable table) {
        table.setFont(FONT_TABLE); table.setRowHeight(44);
        table.setShowVerticalLines(true); table.setShowHorizontalLines(true);
        table.setGridColor(COL_BORDER);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true); table.setBackground(CARD);
        table.setSelectionBackground(ROW_SEL); table.setSelectionForeground(TEXT);
        table.setBorder(null);
        table.setIntercellSpacing(new Dimension(1, 1));

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                JLabel lbl = new JLabel(val == null ? "" : val.toString().toUpperCase()) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setPaint(new GradientPaint(0, 0, HDR_TOP, 0, getHeight(), HDR_BOT));
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.setColor(new Color(255, 255, 255, 25));
                        g2.fillRect(getWidth()-1, 4, 1, getHeight()-8);
                        g2.setColor(GOLD); g2.setStroke(new BasicStroke(2f));
                        g2.drawLine(0, getHeight()-2, getWidth(), getHeight()-2);
                        g2.dispose(); super.paintComponent(g);
                    }
                };
                lbl.setFont(FONT_TABLE_H); lbl.setForeground(new Color(255, 255, 255, 210));
                lbl.setBorder(new EmptyBorder(0, 14, 0, 14)); lbl.setOpaque(false);
                return lbl;
            }
        });
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false); header.setBorder(null); header.setBackground(HDR_TOP);
    }

    private JScrollPane styledScrollPane(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.setViewportBorder(null);
        scroll.setOpaque(false);
        scroll.setBackground(CARD);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(CARD);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().addChangeListener(e -> scroll.getViewport().repaint());
        return scroll;
    }

    private JButton buildIconButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN); btn.setForeground(fg);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }
}