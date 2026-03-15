package view;

import dao.ProductDAO;
import model.Product;
import model.User;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class InventoryPanel extends JPanel {

    private static final Color ACCENT     = new Color(0xC7, 0x4B, 0x1A);
    private static final Color LEFT_TOP   = new Color(0xB0, 0x3A, 0x12);
    private static final Color LEFT_BOT   = new Color(0x4A, 0x18, 0x08);
    private static final Color CARD       = Color.WHITE;
    private static final Color BG         = new Color(0xF5, 0xF0, 0xE8);
    private static final Color TEXT       = new Color(0x1A, 0x14, 0x10);
    private static final Color MUTED      = new Color(0x6B, 0x5E, 0x52);
    private static final Color BORDER_CLR  = new Color(0xE8, 0xE2, 0xD8);
    private static final Color COL_BORDER  = new Color(0xE0, 0xD8, 0xCC);
    private static final Color HDR_TOP     = new Color(0xB0, 0x3A, 0x12);
    private static final Color HDR_BOT     = new Color(0x6A, 0x22, 0x0C);
    private static final Color GOLD       = new Color(0xFF, 0xD5, 0x80);
    private static final Color INPUT_BG   = new Color(0xFA, 0xF7, 0xF3);
    private static final Color ROW_EVEN   = Color.WHITE;
    private static final Color ROW_ODD    = new Color(0xFD, 0xFB, 0xF8);
    private static final Color ROW_HOVER  = new Color(0xFB, 0xF0, 0xE8);
    private static final Color ROW_SEL    = new Color(0xF5, 0xE6, 0xDC);
    private static final Color GREEN      = new Color(0x2E, 0x7D, 0x52);
    private static final Color RED        = new Color(0xC0, 0x39, 0x2B);
    private static final Color AMBER      = new Color(0xB0, 0x6E, 0x00);

    private static final Color PILL_GREEN_BG = new Color(0xE8, 0xF5, 0xED);
    private static final Color PILL_GREEN_FG = new Color(0x1A, 0x6B, 0x3C);
    private static final Color PILL_AMBER_BG = new Color(0xFF, 0xF4, 0xE0);
    private static final Color PILL_AMBER_FG = new Color(0x92, 0x58, 0x00);
    private static final Color PILL_RED_BG   = new Color(0xFD, 0xED, 0xEB);
    private static final Color PILL_RED_FG   = new Color(0x9B, 0x2C, 0x1F);

    private static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  22);
    private static final Font FONT_SUB     = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_BTN     = new Font("SansSerif", Font.BOLD,  13);
    private static final Font FONT_LABEL   = new Font("SansSerif", Font.BOLD,  11);
    private static final Font FONT_INPUT   = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_TABLE_H = new Font("SansSerif", Font.BOLD,  11);
    private static final Font FONT_TABLE   = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_MONO    = new Font("Monospaced", Font.BOLD, 13);
    private static final Font FONT_DATE    = new Font("SansSerif", Font.PLAIN, 13);

    private static final String SEARCH_PLACEHOLDER = "Search products...";

    private static final String[] COLUMNS = {
        "ID", "Product Name", "Category", "Stock", "Price (₱)", "Supplier", "Added On", "Status"
    };

    private final User        loggedInUser;
    private final Runnable    onBack;
    private final Runnable    onOpenDisposed;
    private final ProductDAO  dao = new ProductDAO();
    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        searchField;
    private JLabel            chipTotal;
    private JLabel            chipLow;
    private JLabel            chipOutStock;
    private JComboBox<String> cbCategoryFilter;
    private JComboBox<String> cbSort;
    private int               hoveredRow = -1;
    private JComboBox<String> cbStockFilter;

    public InventoryPanel(User user, Runnable onBack, Runnable onOpenDisposed) {
        this.loggedInUser   = user;
        this.onBack         = onBack;
        this.onOpenDisposed = onOpenDisposed;
        setLayout(new BorderLayout());
        setBackground(BG);
        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        loadProducts();
    }

    public void refresh() {
        loadProducts();
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
        JLabel title = new JLabel("📦 Inventory");
        title.setFont(FONT_TITLE); title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Manage your products and stock levels");
        sub.setFont(FONT_SUB); sub.setForeground(new Color(255, 255, 255, 170));
        titleStack.add(title); titleStack.add(sub);
        left.add(btnBack); left.add(Box.createHorizontalStrut(6)); left.add(titleStack);
        bar.add(left, BorderLayout.WEST);

        JButton btnAdd = buildIconButton("+ Add Product", GOLD, new Color(0x1A, 0x14, 0x10));
        btnAdd.addActionListener(e -> openProductDialog(null));

        JButton btnDisposed = buildIconButton("🗑️ Disposed Items", new Color(255, 255, 255, 30), Color.WHITE);
        btnDisposed.addActionListener(e -> { if (onOpenDisposed != null) onOpenDisposed.run(); });

        JButton btnRefresh = buildIconButton("🔄 Refresh", new Color(255, 255, 255, 30), Color.WHITE);
        btnRefresh.addActionListener(e -> {
            loadProducts();
            showToast("✅  Inventory refreshed.", true);
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(btnRefresh);
        right.add(btnDisposed);
        right.add(btnAdd);
        bar.add(right, BorderLayout.EAST);
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

    private JPanel buildToolbar() {
        JPanel t = new JPanel(new BorderLayout());
        t.setOpaque(false);

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
        searchWrap.setPreferredSize(new Dimension(300, 38));

        JLabel searchIco = new JLabel("  🔍");
        searchIco.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchIco.setBorder(new EmptyBorder(0, 8, 0, 2));

        searchField = new JTextField();
        searchField.setFont(FONT_INPUT);
        searchField.setForeground(MUTED);
        searchField.setText(SEARCH_PLACEHOLDER);
        searchField.setOpaque(false);
        searchField.setBorder(new EmptyBorder(6, 4, 6, 10));
        searchField.setToolTipText("Search by name, category or supplier...");
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
            @Override public void keyReleased(KeyEvent e) { applyFilters(); }
        });

        searchWrap.add(searchIco,   BorderLayout.WEST);
        searchWrap.add(searchField, BorderLayout.CENTER);
        t.add(searchWrap, BorderLayout.WEST);

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        chips.setOpaque(false);
        chipTotal = buildChip("Total: 0", new Color(0xE8, 0xF0, 0xFE), new Color(0x1A, 0x5C, 0xB8));
        chipTotal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chipTotal.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (cbStockFilter != null) { cbStockFilter.setSelectedItem("All Stock"); applyFilters(); }
            }
        });

        chipLow = buildChip("Low Stock: 0", new Color(0xFF, 0xF4, 0xE0), new Color(0xB0, 0x6E, 0x00));
        chipLow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chipLow.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (cbStockFilter != null) { cbStockFilter.setSelectedItem("Low Stock"); applyFilters(); }
            }
        });

        chipOutStock = buildChip("Out of Stock: 0", new Color(0xFD, 0xED, 0xEB), new Color(0x9B, 0x2C, 0x1F));
        chipOutStock.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chipOutStock.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (cbStockFilter != null) { cbStockFilter.setSelectedItem("Out of Stock"); applyFilters(); }
            }
        });
        
        chips.add(chipTotal);
        chips.add(chipLow);
        chips.add(chipOutStock);
        t.add(chips, BorderLayout.EAST);
        return t;
    }

    private JLabel buildChip(String text, Color bg, Color fg) {
        JLabel chip = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose(); super.paintComponent(g);
            }
        };
        chip.setFont(new Font("SansSerif", Font.BOLD, 14));
        chip.setForeground(fg); chip.setBorder(new EmptyBorder(6, 16, 6, 16));
        chip.setOpaque(false); return chip;
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

        JLabel cardTitle = new JLabel("Product List");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        cardTitle.setForeground(TEXT);
        cardHeader.add(cardTitle, BorderLayout.WEST);

        JPanel actBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actBtns.setOpaque(false);

        Map<Integer, String> catMap = dao.getCategoryMap();
        Vector<String> catItems = new Vector<>();
        catItems.add("All Categories");
        catItems.addAll(catMap.values());
        cbCategoryFilter = new JComboBox<>(catItems) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_CLR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cbCategoryFilter.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cbCategoryFilter.setForeground(TEXT);
        cbCategoryFilter.setBackground(INPUT_BG);
        cbCategoryFilter.setOpaque(false);
        cbCategoryFilter.setBorder(new EmptyBorder(4, 8, 4, 8));
        cbCategoryFilter.setPreferredSize(new Dimension(150, 32));
        cbCategoryFilter.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cbCategoryFilter.addActionListener(e -> applyFilters());

        cbSort = new JComboBox<>(new String[]{
            "Name A → Z", "Name Z → A", "Price ↑", "Price ↓", "Stock ↑", "Stock ↓"
        }) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_CLR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cbSort.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cbSort.setForeground(TEXT);
        cbSort.setBackground(INPUT_BG);
        cbSort.setOpaque(false);
        cbSort.setBorder(new EmptyBorder(4, 8, 4, 8));
        cbSort.setPreferredSize(new Dimension(130, 32));
        cbSort.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cbSort.addActionListener(e -> applyFilters());
        
        cbStockFilter = new JComboBox<>(new String[]{
        	    "All Stock", "In Stock", "Low Stock", "Out of Stock"
        	}) {
        	    @Override protected void paintComponent(Graphics g) {
        	        Graphics2D g2 = (Graphics2D) g.create();
        	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        	        g2.setColor(INPUT_BG);
        	        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        	        g2.setColor(BORDER_CLR);
        	        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
        	        g2.dispose();
        	        super.paintComponent(g);
        	    }
        	};
        	cbStockFilter.setFont(new Font("SansSerif", Font.PLAIN, 12));
        	cbStockFilter.setForeground(TEXT);
        	cbStockFilter.setBackground(INPUT_BG);
        	cbStockFilter.setOpaque(false);
        	cbStockFilter.setBorder(new EmptyBorder(4, 8, 4, 8));
        	cbStockFilter.setPreferredSize(new Dimension(130, 32));
        	cbStockFilter.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        	cbStockFilter.addActionListener(e -> applyFilters());

        JButton btnEdit   = buildSmallButton("✏️  Edit",    new Color(0xEE, 0xF6, 0xF1), GREEN);
        JButton btnDelete = buildSmallButton("🗑️  Dispose", new Color(0xFD, 0xED, 0xEB), RED);
        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        actBtns.add(cbCategoryFilter);
        actBtns.add(cbSort);
        actBtns.add(cbStockFilter);
        actBtns.add(btnEdit);
        actBtns.add(btnDelete);
        cardHeader.add(actBtns, BorderLayout.EAST);

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
        northSection.add(divider,    BorderLayout.SOUTH);
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
            @Override public void mouseExited(MouseEvent e)  { hoveredRow = -1; table.repaint(); }
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) editSelected();
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
                boolean centered = (col == 3 || col == 4 || col == 6 || col == 7);
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

        int[] widths = {0, 210, 120, 70, 110, 155, 105, 100};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

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

        table.getColumnModel().getColumn(3).setCellRenderer(new TableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                int qty = val instanceof Integer ? (Integer) val : 0;
                Color dotColor = qty == 0 ? RED : qty <= 5 ? AMBER : GREEN;
                JPanel cell = new JPanel(new GridBagLayout()) {
                    @Override protected void paintComponent(Graphics g) {
                        boolean isSelected = t.isRowSelected(row);
                        boolean isHovered  = (row == hoveredRow) && !isSelected;
                        g.setColor(isSelected ? ROW_SEL : isHovered ? ROW_HOVER
                                   : row % 2 == 0 ? ROW_EVEN : ROW_ODD);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        super.paintComponent(g);
                    }
                };
                cell.setOpaque(false);
                JPanel inner = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
                inner.setOpaque(false);
                JLabel dot = new JLabel() {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(dotColor);
                        g2.fillOval(0, 3, 8, 8);
                        g2.dispose();
                    }
                    @Override public Dimension getPreferredSize() { return new Dimension(8, 14); }
                };
                JLabel num = new JLabel(String.valueOf(qty));
                num.setFont(new Font("SansSerif", Font.BOLD, 13));
                num.setForeground(qty == 0 ? RED : qty <= 5 ? AMBER : TEXT);
                inner.add(dot); inner.add(num);
                cell.add(inner);
                return cell;
            }
        });

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setText("₱ " + val);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(FONT_MONO);
                setForeground(new Color(0x1A, 0x52, 0x76));
                setBorder(new EmptyBorder(0, 8, 0, 14));
                return this;
            }
        });

        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(FONT_TABLE);
                setForeground(TEXT);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                return this;
            }
        });

        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(FONT_DATE);
                setForeground(TEXT);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        table.getColumnModel().getColumn(7).setCellRenderer(new TableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                String status = val == null ? "" : val.toString();
                Color pillBg, pillFg;
                String label;
                switch (status) {
                    case "In Stock":
                        pillBg = PILL_GREEN_BG; pillFg = PILL_GREEN_FG; label = "● In Stock";  break;
                    case "Low Stock":
                        pillBg = PILL_AMBER_BG; pillFg = PILL_AMBER_FG; label = "● Low Stock"; break;
                    default:
                        pillBg = PILL_RED_BG;   pillFg = PILL_RED_FG;   label = "● Out";       break;
                }
                JPanel cell = new JPanel(new GridBagLayout()) {
                    @Override protected void paintComponent(Graphics g) {
                        boolean isSelected = t.isRowSelected(row);
                        boolean isHovered  = (row == hoveredRow) && !isSelected;
                        g.setColor(isSelected ? ROW_SEL : isHovered ? ROW_HOVER
                                   : row % 2 == 0 ? ROW_EVEN : ROW_ODD);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        super.paintComponent(g);
                    }
                };
                cell.setOpaque(false);
                final Color fPillBg = pillBg;
                JLabel pill = new JLabel(label) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(fPillBg);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                pill.setFont(new Font("SansSerif", Font.BOLD, 10));
                pill.setForeground(pillFg);
                pill.setBorder(new EmptyBorder(4, 10, 4, 10));
                pill.setOpaque(false);
                cell.add(pill);
                return cell;
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

    private void loadProducts() {
        if (loggedInUser.getStoreId() == null) return;
        if (cbCategoryFilter == null || cbSort == null) {
            populateTable(dao.getAllProducts(loggedInUser.getStoreId()));
        } else {
            applyFilters();
        }
        
        if (cbCategoryFilter == null || cbSort == null || cbStockFilter == null) {
            populateTable(dao.getAllProducts(loggedInUser.getStoreId()));
        } else {
            applyFilters();
        }
    }

    private void applyFilters() {
        if (loggedInUser.getStoreId() == null) return;
        String raw     = searchField.getText();
        String keyword = raw.equals(SEARCH_PLACEHOLDER) ? "" : raw.trim();
        String cat     = cbCategoryFilter != null ? (String) cbCategoryFilter.getSelectedItem() : "All Categories";
        String sort    = cbSort           != null ? (String) cbSort.getSelectedItem()           : "Name A → Z";

        List<Product> list = keyword.isEmpty()
            ? dao.getAllProducts(loggedInUser.getStoreId())
            : dao.searchProducts(keyword, loggedInUser.getStoreId());

        if (!"All Categories".equals(cat)) {
            List<Product> filtered = new java.util.ArrayList<>();
            for (Product p : list)
                if (cat.equals(p.getCategoryName())) filtered.add(p);
            list = filtered;
        }
        
        String stockFilter = cbStockFilter != null ? (String) cbStockFilter.getSelectedItem() : "All Stock";
        if (!"All Stock".equals(stockFilter)) {
            List<Product> stockFiltered = new java.util.ArrayList<>();
            for (Product p : list) {
                String status = p.getStockQuantity() == 0 ? "Out of Stock"
                              : p.getStockQuantity() <= 5 ? "Low Stock" : "In Stock";
                if (stockFilter.equals(status)) stockFiltered.add(p);
            }
            list = stockFiltered;
        }

        switch (sort) {
            case "Name A → Z": list.sort((a, b) -> a.getProductName().compareToIgnoreCase(b.getProductName())); break;
            case "Name Z → A": list.sort((a, b) -> b.getProductName().compareToIgnoreCase(a.getProductName())); break;
            case "Price ↑":    list.sort((a, b) -> a.getPrice().compareTo(b.getPrice()));  break;
            case "Price ↓":    list.sort((a, b) -> b.getPrice().compareTo(a.getPrice()));  break;
            case "Stock ↑":    list.sort((a, b) -> Integer.compare(a.getStockQuantity(), b.getStockQuantity())); break;
            case "Stock ↓":    list.sort((a, b) -> Integer.compare(b.getStockQuantity(), a.getStockQuantity())); break;
        }

        populateTable(list);
    }

    private void populateTable(List<Product> products) {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        int lowCount = 0;
        int outCount = 0;
        for (Product p : products) {
            String status = p.getStockQuantity() == 0 ? "Out of Stock"
                          : p.getStockQuantity() <= 5 ? "Low Stock" : "In Stock";
            if (status.equals("Low Stock"))    lowCount++;
            if (status.equals("Out of Stock")) outCount++;
            tableModel.addRow(new Object[]{
                p.getProductId(),
                p.getProductName(),
                p.getCategoryName() != null ? p.getCategoryName() : "—",
                p.getStockQuantity(),
                String.format("%.2f", p.getPrice()),
                p.getSupplierName() != null ? p.getSupplierName() : "—",
                p.getCreatedAt() != null ? sdf.format(p.getCreatedAt()) : "—",
                status
            });
        }
        chipTotal.setText("Total: " + products.size());
        chipLow.setText("Low Stock: " + lowCount);
        chipOutStock.setText("Out of Stock: " + outCount);
    }

    private void openProductDialog(Product existing) {
        boolean isEdit = existing != null;
        Map<Integer, String> categoryMap = dao.getCategoryMap();
        Map<Integer, String> supplierMap = dao.getSupplierMap();

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, isEdit ? "Edit Product" : "Add New Product",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(460, 520);
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
                g2.fillRect(0, 0, getWidth(), getHeight()); g2.dispose();
            }
        };
        header.setOpaque(false); header.setBorder(new EmptyBorder(20, 24, 20, 24));
        JLabel hTitle = new JLabel(isEdit ? "✏️ Edit Product" : "📦 Add New Product");
        hTitle.setFont(new Font("SansSerif", Font.BOLD, 16)); hTitle.setForeground(Color.WHITE);
        header.add(hTitle, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CARD); form.setBorder(new EmptyBorder(20, 28, 10, 28));

        JTextField fldName  = buildField(form, "Product Name *", isEdit ? existing.getProductName() : "");
        JTextField fldStock = buildField(form, "Stock Quantity *", isEdit ? String.valueOf(existing.getStockQuantity()) : "");
        JTextField fldPrice = buildField(form, "Price (₱) *", isEdit ? existing.getPrice().toPlainString() : "");

        form.add(makeLabel("Category"));
        form.add(Box.createVerticalStrut(5));
        JComboBox<String> cbCategory = buildCombo(categoryMap, isEdit ? existing.getCategoryId() : null);
        cbCategory.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cbCategory.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(cbCategory); form.add(Box.createVerticalStrut(14));

        form.add(makeLabel("Supplier"));
        form.add(Box.createVerticalStrut(5));
        JComboBox<String> cbSupplier = buildCombo(supplierMap, isEdit ? existing.getSupplierId() : null);
        cbSupplier.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cbSupplier.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(cbSupplier); form.add(Box.createVerticalStrut(14));

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null); formScroll.setBackground(CARD);
        formScroll.getViewport().setBackground(CARD);
        root.add(formScroll, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(CARD); btnRow.setBorder(new EmptyBorder(10, 24, 18, 24));
        JButton btnCancel = buildSmallButton("Cancel", new Color(0xF0, 0xEB, 0xE4), MUTED);
        JButton btnSave   = buildFilledButton(isEdit ? "Save Changes" : "Add Product");
        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String name     = fldName.getText().trim();
            String stockStr = fldStock.getText().trim();
            String priceStr = fldPrice.getText().trim();
            if (name.isEmpty() || stockStr.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name, Stock and Price are required.",
                    "Validation", JOptionPane.WARNING_MESSAGE); return;
            }
            int qty; BigDecimal price;
            try {
                qty   = Integer.parseInt(stockStr);
                price = new BigDecimal(priceStr);
                if (qty < 0 || price.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Stock must be a non-negative integer and Price a valid number.",
                    "Validation", JOptionPane.WARNING_MESSAGE); return;
            }
            Integer catId = getSelectedId(cbCategory, categoryMap);
            Integer supId = getSelectedId(cbSupplier, supplierMap);
            Product p = isEdit ? existing : new Product();
            p.setProductName(name); p.setPrice(price); p.setStockQuantity(qty);
            p.setCategoryId(catId); p.setSupplierId(supId);
            boolean ok = isEdit
                ? dao.updateProduct(p, loggedInUser.getUserId())
                : dao.addProduct(p, loggedInUser.getUserId(), loggedInUser.getStoreId());
            if (ok) {
                loadProducts();
                dialog.dispose();
                showToast(isEdit
                    ? "✅  \"" + name + "\" updated successfully."
                    : "✅  \"" + name + "\" added to inventory.", true);
            } else {
                showToast("❌  Failed to save product. Please try again.", false);
            }
        });

        btnRow.add(btnCancel); btnRow.add(btnSave);
        root.add(btnRow, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE); return;
        }
        int productId = (int) tableModel.getValueAt(row, 0);
        if (loggedInUser.getStoreId() == null) return;
        List<Product> all = dao.getAllProducts(loggedInUser.getStoreId());
        Product target = all.stream().filter(p -> p.getProductId() == productId).findFirst().orElse(null);
        if (target != null) openProductDialog(target);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to dispose.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE); return;
        }

        int    productId = (int) tableModel.getValueAt(row, 0);
        String name      = tableModel.getValueAt(row, 1).toString();
        String category  = tableModel.getValueAt(row, 2).toString();
        String stock     = tableModel.getValueAt(row, 3).toString();
        String price     = tableModel.getValueAt(row, 4).toString();

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Confirm Disposal", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(520, 430);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD);
        dialog.setContentPane(root);

        JPanel dlgHeader = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, LEFT_TOP, getWidth(), getHeight(), LEFT_BOT));
                g2.fillRect(0, 0, getWidth(), getHeight()); g2.dispose();
            }
        };
        dlgHeader.setOpaque(false);
        dlgHeader.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel hLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        hLeft.setOpaque(false);
        JLabel hIcon = new JLabel("🗑️");
        hIcon.setFont(new Font("SansSerif", Font.PLAIN, 28));
        JPanel hStack = new JPanel();
        hStack.setLayout(new BoxLayout(hStack, BoxLayout.Y_AXIS));
        hStack.setOpaque(false);
        JLabel hTitle = new JLabel("Dispose Product");
        hTitle.setFont(new Font("SansSerif", Font.BOLD, 18)); hTitle.setForeground(Color.WHITE);
        JLabel hSub = new JLabel("This will remove the item from inventory permanently");
        hSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        hSub.setForeground(new Color(255, 255, 255, 170));
        hStack.add(hTitle); hStack.add(Box.createVerticalStrut(3)); hStack.add(hSub);
        hLeft.add(hIcon); hLeft.add(hStack);
        dlgHeader.add(hLeft, BorderLayout.WEST);
        root.add(dlgHeader, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(CARD);
        body.setBorder(new EmptyBorder(22, 26, 10, 26));

        JPanel infoCard = new JPanel(new GridLayout(2, 2, 16, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xFD, 0xF6, 0xF0));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(0xF0, 0xE4, 0xD8));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose(); super.paintComponent(g);
            }
        };
        infoCard.setOpaque(false);
        infoCard.setBorder(new EmptyBorder(16, 18, 16, 18));
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoCard.add(makeInfoCell("Product", name));
        infoCard.add(makeInfoCell("Category", category.equals("—") ? "Uncategorized" : category));
        infoCard.add(makeInfoCell("Current Stock", stock + " units"));
        infoCard.add(makeInfoCell("Unit Price", "₱ " + price));
        body.add(infoCard);
        body.add(Box.createVerticalStrut(16));

        JPanel warnBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xFF, 0xF4, 0xE0));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(0xF0, 0xD8, 0x90));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        warnBanner.setOpaque(false);
        warnBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        warnBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel wIcon = new JLabel("⚠️");
        wIcon.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JLabel wText = new JLabel("Product will be logged as disposed and removed from inventory.");
        wText.setFont(new Font("SansSerif", Font.PLAIN, 12));
        wText.setForeground(new Color(0x92, 0x58, 0x00));
        warnBanner.add(wIcon); warnBanner.add(wText);
        body.add(warnBanner);
        body.add(Box.createVerticalStrut(18));

        JLabel reasonLbl = new JLabel("Reason for disposal *");
        reasonLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        reasonLbl.setForeground(MUTED);
        reasonLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(reasonLbl);
        body.add(Box.createVerticalStrut(7));

        JTextField reasonField = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(isFocusOwner() ? ACCENT : BORDER_CLR);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 1.5f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        reasonField.setFont(FONT_INPUT);
        reasonField.setForeground(TEXT);
        reasonField.setOpaque(false);
        reasonField.setBorder(new EmptyBorder(10, 12, 10, 12));
        reasonField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        reasonField.setAlignmentX(Component.LEFT_ALIGNMENT);
        reasonField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { reasonField.repaint(); }
            @Override public void focusLost(FocusEvent e)   { reasonField.repaint(); }
        });
        body.add(reasonField);
        root.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(CARD);
        footer.setBorder(new EmptyBorder(8, 26, 20, 26));

        JButton btnCancel = buildSmallButton("Cancel", new Color(0xF0, 0xEB, 0xE4), MUTED);
        JButton btnConfirm = new JButton("🗑️  Dispose Product") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(0x96, 0x1A, 0x10) : RED);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btnConfirm.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setContentAreaFilled(false);
        btnConfirm.setBorderPainted(false);
        btnConfirm.setFocusPainted(false);
        btnConfirm.setOpaque(false);
        btnConfirm.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnConfirm.setBorder(new EmptyBorder(9, 22, 9, 22));

        final boolean[] confirmed = {false};
        btnCancel.addActionListener(e -> dialog.dispose());
        btnConfirm.addActionListener(e -> {
            if (reasonField.getText().trim().isEmpty()) {
                reasonField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(RED, 1, true),
                    new EmptyBorder(10, 12, 10, 12)));
                reasonField.requestFocus();
                return;
            }
            confirmed[0] = true;
            dialog.dispose();
        });

        footer.add(btnCancel); footer.add(btnConfirm);
        root.add(footer, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(reasonField::requestFocusInWindow);
        dialog.setVisible(true);

        if (!confirmed[0]) return;

        String reason = reasonField.getText().trim();
        if (loggedInUser.getStoreId() == null) return;
        List<Product> all = dao.getAllProducts(loggedInUser.getStoreId());
        Product target = all.stream().filter(p -> p.getProductId() == productId).findFirst().orElse(null);
        if (target == null) { showToast("❌  Product not found.", false); return; }

        boolean ok = dao.disposeProduct(target, reason, loggedInUser.getStoreId(), loggedInUser.getUserId());
        if (ok) {
            loadProducts();
            showToast("🗑️  \"" + name + "\" moved to Disposed Items.", true);
        } else {
            showToast("❌  Failed to dispose product. Please try again.", false);
        }
    }

    private JPanel makeInfoCell(String label, String value) {
        JPanel cell = new JPanel();
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setOpaque(false);
        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(new Color(0x9A, 0x8E, 0x84));
        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.BOLD, 13));
        val.setForeground(TEXT);
        cell.add(lbl); cell.add(Box.createVerticalStrut(3)); cell.add(val);
        return cell;
    }

    private JComboBox<String> buildCombo(Map<Integer, String> map, Integer selectedId) {
        Vector<String> items = new Vector<>();
        items.add("— None —");
        items.addAll(map.values());
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_INPUT); cb.setBackground(INPUT_BG);
        if (selectedId != null && map.containsKey(selectedId))
            cb.setSelectedItem(map.get(selectedId));
        return cb;
    }

    private Integer getSelectedId(JComboBox<String> cb, Map<Integer, String> map) {
        String sel = (String) cb.getSelectedItem();
        if (sel == null || sel.equals("— None —")) return null;
        return map.entrySet().stream()
            .filter(e -> e.getValue().equals(sel))
            .map(Map.Entry::getKey).findFirst().orElse(null);
    }

    private JTextField buildField(JPanel parent, String labelText, String value) {
        parent.add(makeLabel(labelText));
        parent.add(Box.createVerticalStrut(5));
        JTextField field = new JTextField(value) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(isFocusOwner() ? ACCENT : BORDER_CLR);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 1.5f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        field.setFont(FONT_INPUT); field.setForeground(TEXT);
        field.setOpaque(false); field.setBorder(new EmptyBorder(8, 12, 8, 12));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { field.repaint(); }
            @Override public void focusLost(FocusEvent e)   { field.repaint(); }
        });
        parent.add(field); parent.add(Box.createVerticalStrut(14));
        return field;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL); l.setForeground(MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
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
        btn.setBorder(new EmptyBorder(7, 16, 7, 16));
        return btn;
    }

    private void showToast(String message, boolean success) {
        Color toastBg = success ? new Color(0x1A, 0x6B, 0x3C) : new Color(0x9B, 0x2C, 0x1F);
        JWindow toast = new JWindow(SwingUtilities.getWindowAncestor(this));
        toast.setBackground(new Color(0, 0, 0, 0));
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(toastBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(255, 255, 255, 60));
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.dispose(); super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 16, 12, 20));
        JLabel lbl = new JLabel(message);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(Color.WHITE);
        panel.add(lbl);
        toast.setContentPane(panel);
        toast.pack();
        Point loc = getLocationOnScreen();
        int tx = loc.x + (getWidth()  - toast.getWidth())  / 2;
        int ty = loc.y +  getHeight() - toast.getHeight() - 32;
        toast.setLocation(tx, ty);
        toast.setVisible(true);
        javax.swing.Timer fadeTimer = new javax.swing.Timer(30, null);
        final float[] alpha = {1.0f};
        final boolean[] fading = {false};
        javax.swing.Timer holdTimer = new javax.swing.Timer(2500, e -> fading[0] = true);
        holdTimer.setRepeats(false);
        holdTimer.start();
        fadeTimer.addActionListener(e -> {
            if (!fading[0]) return;
            alpha[0] -= 0.06f;
            if (alpha[0] <= 0f) { fadeTimer.stop(); toast.dispose(); }
            else toast.setOpacity(alpha[0]);
        });
        fadeTimer.start();
    }

    private JButton buildFilledButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(0x8F, 0x31, 0x10) : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose(); super.paintComponent(g);
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