package view;

import model.InventoryLog;

import model.User;
import dao.InventoryLogDAO;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.awt.print.*;
import java.time.format.DateTimeFormatter;

public class InventoryLogPanel extends JPanel {

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
    private static final Color GREEN       = new Color(0x2E, 0x7D, 0x52);
    private static final Color RED         = new Color(0xC0, 0x39, 0x2B);
    private static final Color AMBER       = new Color(0xB0, 0x6E, 0x00);

    private static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  22);
    private static final Font FONT_SUB     = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_BTN     = new Font("SansSerif", Font.BOLD,  13);
    private static final Font FONT_TABLE   = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_DATE    = new Font("SansSerif", Font.PLAIN, 13);

    private static final String SEARCH_PLACEHOLDER = "Search product or user...";

    private static final String[] COLUMNS = {
        "Product Name", "Modified By", "Operation", "Qty Change", "Date & Time"
    };

    private final User            loggedInUser;
    private final InventoryLogDAO logDAO = new InventoryLogDAO();
    private DefaultTableModel     logModel;
    private JTable                logTable;
    private int                   hoveredRow = -1;

    private JComboBox<String> cbOperationFilter;
    private JTextField        searchField;
    private JComboBox<String> cbMonth;
    private JComboBox<String> cbYear;

    public InventoryLogPanel(User user, Runnable onBack) {
        this.loggedInUser = user;
        setLayout(new BorderLayout());
        setBackground(BG);
        add(buildTopBar(onBack), BorderLayout.NORTH);
        add(buildContent(),      BorderLayout.CENTER);
        loadLogs();
    }

    private JPanel buildTopBar(Runnable onBack) {
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
        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);
        JLabel title = new JLabel("📋 Inventory Logs");
        title.setFont(FONT_TITLE); title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Track all product modifications and operations");
        sub.setFont(FONT_SUB); sub.setForeground(new Color(255, 255, 255, 170));
        titleStack.add(title); titleStack.add(sub);
        left.add(btnBack);
        left.add(Box.createHorizontalStrut(6));
        left.add(titleStack);
        bar.add(left, BorderLayout.WEST);

        JButton btnRefresh = buildIconButton("🔄 Refresh", new Color(255, 255, 255, 30), Color.WHITE);
        btnRefresh.addActionListener(e -> loadLogs());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(btnRefresh);
        bar.add(right, BorderLayout.EAST);
        
        JButton btnExport = buildIconButton("📥 Export CSV", new Color(255, 255, 255, 30), Color.WHITE);
        btnExport.addActionListener(e -> exportToCSV());

        JButton btnPrint = buildIconButton("🖨️ Print", new Color(255, 255, 255, 30), Color.WHITE);
        btnPrint.addActionListener(e -> printLogs());

        right.add(btnExport);
        right.add(btnPrint);
        right.add(btnRefresh);

        return bar;
    }

    private JPanel buildContent() {
        JPanel c = new JPanel(new BorderLayout(0, 16));
        c.setBackground(BG);
        c.setBorder(new EmptyBorder(24, 28, 24, 28));
        c.add(buildTableCard(), BorderLayout.CENTER);
        return c;
    }

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(10, 22, 12, 22));

        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftSide.setOpaque(false);

        JPanel searchWrap = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_CLR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        searchWrap.setOpaque(false);
        searchWrap.setPreferredSize(new Dimension(240, 34));
        JLabel searchIco = new JLabel("  🔍");
        searchIco.setFont(new Font("SansSerif", Font.PLAIN, 13));
        searchField = new JTextField();
        searchField.setFont(FONT_TABLE);
        searchField.setForeground(MUTED);
        searchField.setText(SEARCH_PLACEHOLDER);
        searchField.setOpaque(false);
        searchField.setBorder(new EmptyBorder(4, 4, 4, 8));
        searchField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(SEARCH_PLACEHOLDER)) {
                    searchField.setText(""); searchField.setForeground(TEXT);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (searchField.getText().trim().isEmpty()) {
                    searchField.setForeground(MUTED);
                    searchField.setText(SEARCH_PLACEHOLDER);
                }
            }
        });
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { applyFilter(); }
        });
        searchWrap.add(searchIco,   BorderLayout.WEST);
        searchWrap.add(searchField, BorderLayout.CENTER);
        leftSide.add(searchWrap);

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightSide.setOpaque(false);

        JLabel opLbl = new JLabel("Operation:");
        opLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        opLbl.setForeground(MUTED);
        cbOperationFilter = new JComboBox<>(new String[]{
            "All Operations", "ADD", "UPDATE", "DISPOSE"
        });
        cbOperationFilter.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cbOperationFilter.setBackground(INPUT_BG);
        cbOperationFilter.setForeground(TEXT);
        cbOperationFilter.setPreferredSize(new Dimension(140, 34));
        cbOperationFilter.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cbOperationFilter.addActionListener(e -> applyFilter());

        JLabel dateLbl = new JLabel("Date:");
        dateLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        dateLbl.setForeground(MUTED);

        cbMonth = new JComboBox<>(new String[]{
            "All Months", "January", "February", "March", "April",
            "May", "June", "July", "August", "September",
            "October", "November", "December"
        });
        cbMonth.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cbMonth.setBackground(INPUT_BG);
        cbMonth.setForeground(TEXT);
        cbMonth.setPreferredSize(new Dimension(120, 34));
        cbMonth.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cbMonth.addActionListener(e -> applyFilter());

        int currentYear = LocalDate.now().getYear();
        String[] years = new String[currentYear - 2022 + 1];
        years[0] = "All Years";
        for (int i = 1; i < years.length; i++)
            years[i] = String.valueOf(2023 + i - 1);

        cbYear = new JComboBox<>(years);
        cbYear.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cbYear.setBackground(INPUT_BG);
        cbYear.setForeground(TEXT);
        cbYear.setPreferredSize(new Dimension(90, 34));
        cbYear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cbYear.addActionListener(e -> applyFilter());

        JButton btnClear = buildSmallButton("✕ Clear", new Color(0xF0, 0xEB, 0xE4), MUTED);
        btnClear.addActionListener(e -> {
            cbOperationFilter.setSelectedIndex(0);
            searchField.setText(SEARCH_PLACEHOLDER);
            searchField.setForeground(MUTED);
            cbMonth.setSelectedIndex(0);
            cbYear.setSelectedIndex(0);
            applyFilter();
        });

        rightSide.add(opLbl);
        rightSide.add(cbOperationFilter);
        rightSide.add(Box.createHorizontalStrut(4));
        rightSide.add(dateLbl);
        rightSide.add(cbMonth);
        rightSide.add(cbYear);
        rightSide.add(btnClear);

        bar.add(leftSide,  BorderLayout.WEST);
        bar.add(rightSide, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(BORDER_CLR);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);

        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        cardHeader.setBorder(new EmptyBorder(18, 22, 14, 22));
        JLabel cardTitle = new JLabel("Activity Log");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        cardTitle.setForeground(TEXT);
        cardHeader.add(cardTitle, BorderLayout.WEST);

        JPanel divider = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BORDER_CLR); g.fillRect(0, 0, getWidth(), 1);
            }
        };
        divider.setPreferredSize(new Dimension(0, 1));
        divider.setOpaque(false);

        JPanel northSection = new JPanel(new BorderLayout());
        northSection.setOpaque(false);
        northSection.add(cardHeader, BorderLayout.NORTH);
        northSection.add(divider,    BorderLayout.SOUTH);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(northSection,     BorderLayout.NORTH);
        top.add(buildFilterBar(), BorderLayout.SOUTH);
        card.add(top, BorderLayout.NORTH);

        logModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        logTable = new JTable(logModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                boolean selected = isRowSelected(row);
                boolean hovered  = (row == hoveredRow) && !selected;
                if (selected)     c.setBackground(ROW_SEL);
                else if (hovered) c.setBackground(ROW_HOVER);
                else              c.setBackground(row % 2 == 0 ? ROW_EVEN : ROW_ODD);
                if (c instanceof JLabel) {
                    JLabel lbl = (JLabel) c;
                    Border inner = lbl.getBorder() instanceof EmptyBorder
                        ? lbl.getBorder() : new EmptyBorder(0, 14, 0, 14);
                    lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, COL_BORDER), inner));
                }
                return c;
            }
        };

        logTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int row = logTable.rowAtPoint(e.getPoint());
                if (row != hoveredRow) { hoveredRow = row; logTable.repaint(); }
            }
        });
        logTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { hoveredRow = -1; logTable.repaint(); }
        });

        logTable.setFont(FONT_TABLE);
        logTable.setRowHeight(48);
        logTable.setShowVerticalLines(true);
        logTable.setShowHorizontalLines(true);
        logTable.setGridColor(COL_BORDER);
        logTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        logTable.setFillsViewportHeight(true);
        logTable.setBackground(CARD);
        logTable.setSelectionBackground(ROW_SEL);
        logTable.setSelectionForeground(TEXT);
        logTable.setBorder(null);
        logTable.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = logTable.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                String labelText = val == null ? "" : val.toString().toUpperCase();
                JLabel lbl = new JLabel(labelText) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setPaint(new GradientPaint(0, 0, HDR_TOP, 0, getHeight(), HDR_BOT));
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.setColor(new Color(255, 255, 255, 25));
                        g2.fillRect(getWidth() - 1, 4, 1, getHeight() - 8);
                        g2.setColor(GOLD);
                        g2.setStroke(new BasicStroke(2f));
                        g2.drawLine(0, getHeight() - 2, getWidth(), getHeight() - 2);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
                lbl.setForeground(new Color(255, 255, 255, 210));
                lbl.setBorder(new EmptyBorder(0, 14, 0, 14));
                lbl.setOpaque(false);
                boolean centered = (col == 2 || col == 3 || col == 4);
                lbl.setHorizontalAlignment(centered ? SwingConstants.CENTER : SwingConstants.LEFT);
                return lbl;
            }
        });
        header.setPreferredSize(new Dimension(0, 46));
        header.setReorderingAllowed(false);
        header.setBorder(null);
        header.setBackground(HDR_TOP);

        JPanel cornerFill = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, HDR_TOP, 0, getHeight(), HDR_BOT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(GOLD);
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(0, getHeight() - 2, getWidth(), getHeight() - 2);
                g2.dispose();
            }
        };
        cornerFill.setOpaque(false);

        int[] widths = {220, 160, 130, 100, 180};
        for (int i = 0; i < widths.length; i++)
            logTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        logTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(new Font("SansSerif", Font.BOLD, 13));
                setForeground(TEXT);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                return this;
            }
        });

        logTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setText("👤 " + (val != null ? val.toString() : "—"));
                setFont(FONT_TABLE);
                setForeground(TEXT);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                return this;
            }
        });

        logTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String op = val == null ? "" : val.toString().toUpperCase();
                Color fg;
                switch (op) {
                    case "ADD":     fg = GREEN; break;
                    case "REMOVE":
                    case "DISPOSE": fg = RED;   break;
                    case "UPDATE":  fg = AMBER; break;
                    default:        fg = TEXT;  break;
                }
                setText(op);
                setFont(new Font("SansSerif", Font.BOLD, 13));
                setForeground(fg);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        logTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                int qty = val instanceof Integer ? (Integer) val : 0;
                setText((qty > 0 ? "+" : "") + qty);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Monospaced", Font.BOLD, 13));
                setForeground(qty > 0 ? GREEN : qty < 0 ? RED : TEXT);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        logTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            private final DateTimeFormatter fmt =
                DateTimeFormatter.ofPattern("MMM dd, yyyy  HH:mm");
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (val instanceof LocalDateTime) {
                    setText(((LocalDateTime) val).format(fmt));
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(FONT_DATE);
                setForeground(TEXT);
                setBorder(new EmptyBorder(0, 8, 0, 14));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(logTable);
        scroll.setBorder(null);
        scroll.setViewportBorder(null);
        scroll.setOpaque(true);
        scroll.setBackground(CARD);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(CARD);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, cornerFill);

        JPanel scrollWrapper = new JPanel(new BorderLayout());
        scrollWrapper.setOpaque(true);
        scrollWrapper.setBackground(CARD);
        scrollWrapper.setBorder(new EmptyBorder(0, 0, 8, 0));
        scrollWrapper.add(scroll, BorderLayout.CENTER);
        card.add(scrollWrapper, BorderLayout.CENTER);

        return card;
    }

    private void applyFilter() {
        if (cbOperationFilter == null || searchField == null
                || cbMonth == null || cbYear == null) return;

        String selectedOp    = (String) cbOperationFilter.getSelectedItem();
        String keyword       = searchField.getText().equals(SEARCH_PLACEHOLDER)
                               ? "" : searchField.getText().trim().toLowerCase();
        int    selectedMonth = cbMonth.getSelectedIndex(); 
        String selectedYear  = (String) cbYear.getSelectedItem();

        logModel.setRowCount(0);
        List<InventoryLog> logs = logDAO.getAllLogs();

        for (InventoryLog log : logs) {
            if (!"All Operations".equals(selectedOp)
                    && !log.getOperation().equalsIgnoreCase(selectedOp)) continue;

            if (!keyword.isEmpty()) {
                boolean matchProduct  = log.getProductName() != null
                                        && log.getProductName().toLowerCase().contains(keyword);
                boolean matchUsername = log.getUsername() != null
                                        && log.getUsername().toLowerCase().contains(keyword);
                if (!matchProduct && !matchUsername) continue;
            }

            if (log.getCreatedAt() != null) {
                LocalDate logDate = log.getCreatedAt().toLocalDate();
                if (selectedMonth != 0 && logDate.getMonthValue() != selectedMonth) continue;
                if (!"All Years".equals(selectedYear)
                        && logDate.getYear() != Integer.parseInt(selectedYear)) continue;
            }

            logModel.addRow(new Object[]{
                log.getProductName(),
                log.getUsername(),
                log.getOperation(),
                log.getQuantityChanged(),
                log.getCreatedAt()
            });
        }
    }

    private void loadLogs() {
        applyFilter();
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
    
    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save CSV File");
        fileChooser.setSelectedFile(new java.io.File("inventory_logs.csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) return;

        java.io.File fileToSave = fileChooser.getSelectedFile();
        String path = fileToSave.getAbsolutePath();
        if (!path.endsWith(".csv")) path += ".csv";

        try (FileWriter fw = new FileWriter(path)) {
            fw.write("Product Name,Modified By,Operation,Qty Change,Date & Time\n");

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            for (int i = 0; i < logModel.getRowCount(); i++) {
                String productName = logModel.getValueAt(i, 0) != null
                                     ? logModel.getValueAt(i, 0).toString() : "";
                String modifiedBy  = logModel.getValueAt(i, 1) != null
                                     ? logModel.getValueAt(i, 1).toString() : "";
                String operation   = logModel.getValueAt(i, 2) != null
                                     ? logModel.getValueAt(i, 2).toString() : "";
                String qty         = logModel.getValueAt(i, 3) != null
                                     ? logModel.getValueAt(i, 3).toString() : "";
                String date        = "";
                if (logModel.getValueAt(i, 4) instanceof LocalDateTime) {
                    date = ((LocalDateTime) logModel.getValueAt(i, 4)).format(fmt);
                }

                fw.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    productName, modifiedBy, operation, qty, date));
            }

            JOptionPane.showMessageDialog(this,
                "✅ Exported successfully to:\n" + path,
                "Export Complete", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "❌ Failed to export: " + ex.getMessage(),
                "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printLogs() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Inventory Logs");

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;

            Graphics2D g2 = (Graphics2D) graphics;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double x      = pageFormat.getImageableX();
            double y      = pageFormat.getImageableY();
            double width  = pageFormat.getImageableWidth();

            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.setColor(Color.BLACK);
            g2.drawString("Inventory Logs", (int) x, (int) y + 20);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            g2.setColor(Color.GRAY);
            String printedOn = "Printed on: " + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
            g2.drawString(printedOn, (int) x, (int) y + 35);

            String[] headers = {"Product Name", "Modified By", "Operation", "Qty", "Date & Time"};
            int[]    colW    = {180, 120, 80, 50, 130};
            int      rowH    = 18;
            int      startY  = (int) y + 55;

            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            g2.setColor(Color.BLACK);
            int colX = (int) x;
            for (int i = 0; i < headers.length; i++) {
                g2.drawString(headers[i], colX + 2, startY);
                colX += colW[i];
            }

            startY += 4;
            g2.drawLine((int) x, startY, (int) (x + width), startY);
            startY += rowH;

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            for (int i = 0; i < logModel.getRowCount(); i++) {
                colX = (int) x;
                String[] values = {
                    logModel.getValueAt(i, 0) != null ? logModel.getValueAt(i, 0).toString() : "",
                    logModel.getValueAt(i, 1) != null ? logModel.getValueAt(i, 1).toString() : "",
                    logModel.getValueAt(i, 2) != null ? logModel.getValueAt(i, 2).toString() : "",
                    logModel.getValueAt(i, 3) != null ? logModel.getValueAt(i, 3).toString() : "",
                    logModel.getValueAt(i, 4) instanceof LocalDateTime
                        ? ((LocalDateTime) logModel.getValueAt(i, 4)).format(fmt) : ""
                };
                for (int j = 0; j < values.length; j++) {
                    String text = values[j];
                    while (text.length() > 0 &&
                           g2.getFontMetrics().stringWidth(text) > colW[j] - 4) {
                        text = text.substring(0, text.length() - 1);
                    }
                    g2.drawString(text, colX + 2, startY);
                    colX += colW[j];
                }
                startY += rowH;
            }

            return Printable.PAGE_EXISTS;
        });

        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "❌ Print failed: " + ex.getMessage(),
                    "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JButton buildSmallButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 12)); btn.setForeground(fg);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        return btn;
    }
}