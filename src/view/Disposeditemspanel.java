package view;

import dao.Disposeditemdao;
import model.Disposeditem;
import model.User;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Disposeditemspanel extends JPanel {

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
    private static final Color RED         = new Color(0xC0, 0x39, 0x2B);
    private static final Color GREEN_DARK  = new Color(0x27, 0x6B, 0x3A);
    private static final Color GREEN_MID   = new Color(0x1E, 0x8A, 0x4A);
    private static final Color GRAY_MED    = new Color(0x6B, 0x5E, 0x52);
    private static final Color GRAY_LIGHT  = new Color(0x9A, 0x8E, 0x84);

    private static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  22);
    private static final Font FONT_SUB     = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_BTN     = new Font("SansSerif", Font.BOLD,  13);
    private static final Font FONT_INPUT   = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_TABLE   = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_LABEL   = new Font("SansSerif", Font.BOLD,  11);
    private static final Font FONT_MONO    = new Font("Monospaced", Font.BOLD, 13);

    private static final String[] COLUMNS = {
        "ID", "Product Name", "Category", "Supplier", "Qty Disposed",
        "Price (₱)", "Reason", "Disposed On"
    };

    private final User            loggedInUser;
    private final Runnable        onBack;
    private final Disposeditemdao dao = new Disposeditemdao();

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        searchField;
    private JLabel            chipTotal;
    private JLabel            chipQty;
    private int               hoveredRow = -1;

    public Disposeditemspanel(User user, Runnable onBack) {
        this.loggedInUser = user;
        this.onBack       = onBack;
        setLayout(new BorderLayout());
        setBackground(BG);
        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        loadItems();
    }

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

        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);
        JLabel title = new JLabel("🗑️ Disposed Items");
        title.setFont(FONT_TITLE); title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("View all products that have been disposed of");
        sub.setFont(FONT_SUB); sub.setForeground(new Color(255, 255, 255, 170));
        titleStack.add(title);
        titleStack.add(sub);

        left.add(btnBack);
        left.add(Box.createHorizontalStrut(6));
        left.add(titleStack);
        bar.add(left, BorderLayout.WEST);
        return bar;
    }

    private JPanel buildContent() {
        JPanel c = new JPanel(new BorderLayout(0, 16));
        c.setBackground(BG);
        c.setBorder(new EmptyBorder(24, 28, 24, 28));
        c.add(buildToolbar(),   BorderLayout.NORTH);
        c.add(buildTableCard(), BorderLayout.CENTER);
        return c;
    }

    private static final String SEARCH_PLACEHOLDER = "Search disposed items...";

    private JPanel buildToolbar() {
        JPanel t = new JPanel(new BorderLayout(12, 0));
        t.setOpaque(false);
        t.setPreferredSize(new Dimension(0, 46));

        JPanel searchWrap = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(BORDER_CLR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        searchWrap.setOpaque(false);
        searchWrap.setPreferredSize(new Dimension(320, 38));

        JLabel searchIco = new JLabel("  🔍");
        searchIco.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchIco.setBorder(new EmptyBorder(0, 8, 0, 2));

        searchField = new JTextField();
        searchField.setFont(FONT_INPUT);
        searchField.setForeground(MUTED);
        searchField.setText(SEARCH_PLACEHOLDER);
        searchField.setOpaque(false);
        searchField.setBorder(new EmptyBorder(6, 4, 6, 10));
        searchField.setToolTipText("Search by product, category, supplier or reason...");
        searchField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(SEARCH_PLACEHOLDER)) {
                    searchField.setText("");
                    searchField.setForeground(TEXT);
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
            @Override public void keyReleased(KeyEvent e) { loadItems(); }
        });

        searchWrap.add(searchIco,   BorderLayout.WEST);
        searchWrap.add(searchField, BorderLayout.CENTER);
        t.add(searchWrap, BorderLayout.WEST);

        JPanel chipPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        chipPanel.setOpaque(false);
        chipTotal = buildChip("Records: 0",   new Color(0xFD, 0xED, 0xEB), new Color(0x9B, 0x2C, 0x1F));
        chipQty   = buildChip("Total Qty: 0", new Color(0xFF, 0xF4, 0xE0), new Color(0xB0, 0x6E, 0x00));
        chipPanel.add(chipTotal);
        chipPanel.add(chipQty);
        t.add(chipPanel, BorderLayout.EAST);

        return t;
    }

    private JLabel buildChip(String text, Color bg, Color fg) {
        JLabel chip = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setFont(new Font("SansSerif", Font.BOLD, 13));
        chip.setForeground(fg);
        chip.setBorder(new EmptyBorder(6, 16, 6, 16));
        chip.setOpaque(false);
        return chip;
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

        JPanel cardHeader = new JPanel(new BorderLayout(12, 0));
        cardHeader.setOpaque(false);
        cardHeader.setBorder(new EmptyBorder(14, 22, 14, 22));

        JLabel cardTitle = new JLabel("Disposed Product Records");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        cardTitle.setForeground(TEXT);
        cardHeader.add(cardTitle, BorderLayout.WEST);

        JPanel cardBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        cardBtnPanel.setOpaque(false);

        JButton btnSelectAll   = buildToolbarButton("☑ Select All",   GRAY_MED,   Color.WHITE);
        JButton btnUnselectAll = buildToolbarButton("☐ Unselect All", GRAY_LIGHT, Color.WHITE);
        JButton btnRestore     = buildToolbarButton("↩ Restore",      GREEN_DARK, Color.WHITE);
        JButton btnRestoreAll  = buildToolbarButton("↩ Restore All",  GREEN_MID,  Color.WHITE);
        JButton btnDelete      = buildToolbarButton("🗑 Delete",       RED,        Color.WHITE);
        JButton btnDeleteAll   = buildToolbarButton("🗑 Delete All",   new Color(0x96, 0x1A, 0x10), Color.WHITE);

        btnSelectAll.addActionListener(e -> {
            table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            table.selectAll();
        });

        btnUnselectAll.addActionListener(e -> {
            table.clearSelection();
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        });

        btnRestore.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this,
                    "Please select a row to restore.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                "Restore selected item back to inventory?",
                "Confirm Restore", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                int id = (int) tableModel.getValueAt(row, 0);
                boolean ok = dao.restoreItem(id);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Item restored successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Restore failed. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
                loadItems();
            }
        });

        btnRestoreAll.addActionListener(e -> {
            int count = tableModel.getRowCount();
            if (count == 0) {
                JOptionPane.showMessageDialog(this, "No records to restore.", "Empty", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                "Restore ALL " + count + " disposed item(s) back to inventory?",
                "Confirm Restore All", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                int successCount = 0;
                List<Integer> ids = new ArrayList<>();
                for (int i = 0; i < tableModel.getRowCount(); i++)
                    ids.add((int) tableModel.getValueAt(i, 0));
                for (int id : ids)
                    if (dao.restoreItem(id)) successCount++;
                loadItems();
                JOptionPane.showMessageDialog(this,
                    successCount + " of " + ids.size() + " item(s) restored successfully.");
            }
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this,
                    "Please select a row to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                "Permanently delete this disposed record? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                int id = (int) tableModel.getValueAt(row, 0);
                dao.deleteDisposedItem(id);
                loadItems();
            }
        });

        btnDeleteAll.addActionListener(e -> {
            int count = tableModel.getRowCount();
            if (count == 0) {
                JOptionPane.showMessageDialog(this, "No records to delete.", "Empty", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                "Permanently delete ALL " + count + " disposed record(s)? This cannot be undone.",
                "Confirm Delete All", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                dao.deleteAllDisposedItems(loggedInUser.getStoreId());
                loadItems();
            }
        });

        cardBtnPanel.add(btnSelectAll);
        cardBtnPanel.add(btnUnselectAll);
        cardBtnPanel.add(btnRestore);
        cardBtnPanel.add(btnRestoreAll);
        cardBtnPanel.add(btnDelete);
        cardBtnPanel.add(btnDeleteAll);

        cardHeader.add(cardBtnPanel, BorderLayout.EAST);
        card.add(cardHeader, BorderLayout.NORTH);

        JPanel divider = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BORDER_CLR);
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        divider.setPreferredSize(new Dimension(0, 1));
        divider.setOpaque(false);

        JPanel northSection = new JPanel(new BorderLayout());
        northSection.setOpaque(false);
        northSection.add(cardHeader, BorderLayout.NORTH);
        northSection.add(divider, BorderLayout.SOUTH);
        card.add(northSection, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel) {
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

        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) { hoveredRow = row; table.repaint(); }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { hoveredRow = -1; table.repaint(); }
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) showDetailDialog();
            }
        });

        table.setFont(FONT_TABLE);
        table.setRowHeight(48);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setGridColor(COL_BORDER);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setBackground(CARD);
        table.setSelectionBackground(ROW_SEL);
        table.setSelectionForeground(TEXT);
        table.setBorder(null);
        table.setIntercellSpacing(new Dimension(0, 1));

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
                boolean centered = (col == 4 || col == 5 || col == 7);
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

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        int[] widths = {0, 190, 110, 140, 90, 100, 180, 115};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Col 1 — Product Name (bold, TEXT)
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(new Font("SansSerif", Font.BOLD, 13));
                setForeground(TEXT);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                return this;
            }
        });

        // Col 2 — Category (plain text, matches date column style)
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(FONT_TABLE);
                setForeground(TEXT);
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                return this;
            }
        });

        // Col 3 — Supplier (TEXT for readability, was MUTED)
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(FONT_TABLE);
                setForeground(TEXT);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                return this;
            }
        });

        // Col 4 — Qty Disposed badge
        table.getColumnModel().getColumn(4).setCellRenderer(new TableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                int qty = val instanceof Integer ? (Integer) val : 0;
                JPanel cell = new JPanel(new GridBagLayout()) {
                    @Override protected void paintComponent(Graphics g) {
                        boolean isSel = t.isRowSelected(row);
                        boolean isHov = (row == hoveredRow) && !isSel;
                        g.setColor(isSel ? ROW_SEL : isHov ? ROW_HOVER
                                   : row % 2 == 0 ? ROW_EVEN : ROW_ODD);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        super.paintComponent(g);
                    }
                };
                cell.setOpaque(false);
                JLabel badge = new JLabel(String.valueOf(qty)) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(0xFD, 0xED, 0xEB));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                badge.setFont(new Font("SansSerif", Font.BOLD, 12));
                badge.setForeground(RED);
                badge.setBorder(new EmptyBorder(3, 12, 3, 12));
                badge.setOpaque(false);
                cell.add(badge);
                return cell;
            }
        });

        // Col 5 — Price (monospaced, TEXT)
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setText(val != null ? "₱ " + val : "—");
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(FONT_MONO);
                setForeground(new Color(0x1A, 0x52, 0x76));
                setBorder(new EmptyBorder(0, 8, 0, 14));
                return this;
            }
        });

        // Col 6 — Reason (italic, TEXT for readability, was MUTED)
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String reason = val == null ? "—" : val.toString();
                setText(reason);
                setToolTipText(reason);
                setFont(new Font("SansSerif", Font.ITALIC, 13));
                setForeground(TEXT);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                return this;
            }
        });

        // Col 7 — Disposed On (centered, TEXT)
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("SansSerif", Font.PLAIN, 13));
                setForeground(TEXT);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
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

    private void loadItems() {
        if (loggedInUser.getStoreId() == null) return;
        String raw     = searchField != null ? searchField.getText() : "";
        String keyword = raw.equals(SEARCH_PLACEHOLDER) ? "" : raw.trim();
        List<Disposeditem> items = keyword.isEmpty()
            ? dao.getAllDisposedItems(loggedInUser.getStoreId())
            : dao.searchDisposedItems(keyword, loggedInUser.getStoreId());
        populateTable(items);
    }

    private void populateTable(List<Disposeditem> items) {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
        int totalQty = 0;
        for (Disposeditem d : items) {
            totalQty += d.getQuantity();
            tableModel.addRow(new Object[]{
                d.getDisposeId(),
                d.getProductName(),
                d.getCategoryName() != null ? d.getCategoryName() : "—",
                d.getSupplierName() != null ? d.getSupplierName() : "—",
                d.getQuantity(),
                d.getPrice() != null ? String.format("%.2f", d.getPrice()) : "—",
                d.getReason()     != null ? d.getReason()     : "—",
                d.getDisposedAt() != null ? sdf.format(d.getDisposedAt()) : "—"
            });
        }
        chipTotal.setText("Records: " + items.size());
        chipQty.setText("Total Qty: " + totalQty);
    }

    private void showDetailDialog() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        String name     = tableModel.getValueAt(row, 1).toString();
        String category = tableModel.getValueAt(row, 2).toString();
        String supplier = tableModel.getValueAt(row, 3).toString();
        String qty      = tableModel.getValueAt(row, 4).toString();
        String price    = tableModel.getValueAt(row, 5).toString();
        String reason   = tableModel.getValueAt(row, 6).toString();
        String date     = tableModel.getValueAt(row, 7).toString();

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Disposal Details",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD);
        dialog.setContentPane(root);

        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, LEFT_TOP, getWidth(), getHeight(), LEFT_BOT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(18, 24, 18, 24));
        JLabel hTitle = new JLabel("🗑️ Disposal Details");
        hTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        hTitle.setForeground(Color.WHITE);
        header.add(hTitle, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(CARD);
        body.setBorder(new EmptyBorder(20, 28, 20, 28));

        addDetailRow(body, "Product Name",  name);
        addDetailRow(body, "Category",      category);
        addDetailRow(body, "Supplier",      supplier);
        addDetailRow(body, "Qty Disposed",  qty);
        addDetailRow(body, "Price at Time", "₱ " + price);
        addDetailRow(body, "Reason",        reason);
        addDetailRow(body, "Disposed On",   date);
        root.add(body, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(CARD);
        btnRow.setBorder(new EmptyBorder(8, 24, 16, 24));
        JButton btnClose = buildFilledButton("Close");
        btnClose.addActionListener(e -> dialog.dispose());
        btnRow.add(btnClose);
        root.add(btnRow, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel parent, String labelText, String value) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(CARD);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(MUTED);
        lbl.setPreferredSize(new Dimension(120, 24));

        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.PLAIN, 13));
        val.setForeground(TEXT);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.CENTER);
        parent.add(row);
        parent.add(Box.createVerticalStrut(10));
    }

    private JButton buildToolbarButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed()  ? bg.darker().darker()
                        : getModel().isRollover() ? bg.darker()
                        : bg;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 13, 7, 13));
        return btn;
    }

    private JButton buildIconButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN); btn.setForeground(fg);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private JButton buildFilledButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color accent = new Color(0xC7, 0x4B, 0x1A);
                g2.setColor(getModel().isRollover() ? new Color(0x8F, 0x31, 0x10) : accent);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 13)); btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        return btn;
    }
}