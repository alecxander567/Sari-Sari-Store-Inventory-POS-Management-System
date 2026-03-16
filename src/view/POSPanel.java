package view;

import model.SaleItem;
import model.User;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import config.DatabaseConnection;

import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.List;

public class POSPanel extends JPanel {

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

    private static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  22);
    private static final Font FONT_SUB     = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_BTN     = new Font("SansSerif", Font.BOLD,  13);
    private static final Font FONT_LABEL   = new Font("SansSerif", Font.BOLD,  11);
    private static final Font FONT_INPUT   = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_TABLE_H = new Font("SansSerif", Font.BOLD,  11);
    private static final Font FONT_TABLE   = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_MONO    = new Font("Monospaced", Font.BOLD, 13);

    private static final String SEARCH_PLACEHOLDER = "Search products...";
    private static final String ALL_CATEGORIES     = "All Categories";

    private final User loggedInUser;

    private DefaultTableModel productModel;
    private DefaultTableModel cartModel;
    private JTable            productTable;
    private JTable            cartTable;
    private JLabel            lblTotal;
    private JTextField        txtPayment;
    private JLabel            lblChange;
    private JTextField        searchField;
    private JComboBox<String> cbCategory;
    private JComboBox<String> cbSort;
    private List<SaleItem>    cartItems = new ArrayList<>();

    private final List<Object[]> allProductRows = new ArrayList<>();

    private int productHoveredRow = -1;
    private int cartHoveredRow    = -1;

    public POSPanel(User user, Runnable onBack) {
        this.loggedInUser = user;
        setLayout(new BorderLayout());
        setBackground(BG);
        add(buildTopBar(onBack), BorderLayout.NORTH);
        add(buildContent(),      BorderLayout.CENTER);
        add(buildBottomBar(),    BorderLayout.SOUTH);
        loadProducts();
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
        btnBack.addActionListener(e -> onBack.run());

        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);
        JLabel title = new JLabel("🛒 Point of Sale");
        title.setFont(FONT_TITLE); title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Search or double-click a product to add it to the cart");
        sub.setFont(FONT_SUB); sub.setForeground(new Color(255, 255, 255, 170));
        titleStack.add(title); titleStack.add(sub);

        left.add(btnBack);
        left.add(Box.createHorizontalStrut(6));
        left.add(titleStack);
        bar.add(left, BorderLayout.WEST);

        JButton btnRefresh = buildIconButton("🔄 Refresh", new Color(255, 255, 255, 30), Color.WHITE);
        btnRefresh.addActionListener(e -> { loadProducts(); showToast("✅  Products refreshed.", true); });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(btnRefresh);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildContent() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 16, 0));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(20, 28, 10, 28));
        panel.add(buildProductCard());
        panel.add(buildCartCard());
        return panel;
    }

    private JPanel buildProductCard() {
        JPanel card = buildCard();

        JPanel headerWrap = new JPanel(new BorderLayout());
        headerWrap.setOpaque(false);

        JPanel headerTop = new JPanel(new BorderLayout());
        headerTop.setOpaque(false);
        headerTop.setBorder(new EmptyBorder(16, 20, 10, 20));
        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);
        JLabel lTitle = new JLabel("Product List");
        lTitle.setFont(new Font("SansSerif", Font.BOLD, 15)); lTitle.setForeground(TEXT);
        JLabel lSub = new JLabel("Double-click a row to add to cart");
        lSub.setFont(FONT_SUB); lSub.setForeground(MUTED);
        stack.add(lTitle); stack.add(lSub);
        headerTop.add(stack, BorderLayout.WEST);

        JPanel filterRow = new JPanel(new BorderLayout(6, 0));
        filterRow.setOpaque(false);
        filterRow.setBorder(new EmptyBorder(0, 20, 12, 20));

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
        searchWrap.setPreferredSize(new Dimension(0, 36));

        JLabel searchIco = new JLabel("  🔍");
        searchIco.setFont(new Font("SansSerif", Font.PLAIN, 13));
        searchIco.setBorder(new EmptyBorder(0, 8, 0, 2));

        searchField = new JTextField();
        searchField.setFont(FONT_INPUT);
        searchField.setForeground(MUTED);
        searchField.setText(SEARCH_PLACEHOLDER);
        searchField.setOpaque(false);
        searchField.setBorder(new EmptyBorder(5, 4, 5, 10));
        searchField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(SEARCH_PLACEHOLDER)) {
                    searchField.setText(""); searchField.setForeground(TEXT);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (searchField.getText().trim().isEmpty()) {
                    searchField.setForeground(MUTED); searchField.setText(SEARCH_PLACEHOLDER);
                }
            }
        });
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { applyProductFilter(); }
        });
        searchWrap.add(searchIco,   BorderLayout.WEST);
        searchWrap.add(searchField, BorderLayout.CENTER);

        cbCategory = buildFilterCombo(new String[]{ ALL_CATEGORIES }, 145);
        cbCategory.addActionListener(e -> applyProductFilter());

        cbSort = buildFilterCombo(new String[]{ "Name A → Z", "Name Z → A" }, 120);
        cbSort.addActionListener(e -> applyProductFilter());

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightControls.setOpaque(false);
        rightControls.add(cbCategory);
        rightControls.add(cbSort);

        filterRow.add(searchWrap,    BorderLayout.CENTER);
        filterRow.add(rightControls, BorderLayout.EAST);

        JPanel divider = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BORDER_CLR); g.fillRect(0, 0, getWidth(), 1);
            }
        };
        divider.setPreferredSize(new Dimension(0, 1));
        divider.setOpaque(false);

        headerWrap.add(headerTop, BorderLayout.NORTH);
        headerWrap.add(filterRow, BorderLayout.CENTER);
        headerWrap.add(divider,   BorderLayout.SOUTH);
        card.add(headerWrap, BorderLayout.NORTH);

        String[] cols = {"ID", "Product", "Price (₱)", "Stock", "Category"};
        productModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        productTable = new JTable(productModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                boolean sel     = isRowSelected(row);
                boolean hovered = (row == productHoveredRow) && !sel;
                c.setBackground(sel ? ROW_SEL : hovered ? ROW_HOVER
                        : row % 2 == 0 ? ROW_EVEN : ROW_ODD);
                if (c instanceof JLabel) {
                    ((JLabel) c).setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, COL_BORDER),
                        new EmptyBorder(0, 14, 0, 14)));
                }
                return c;
            }
        };

        productTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int row = productTable.rowAtPoint(e.getPoint());
                if (row != productHoveredRow) { productHoveredRow = row; productTable.repaint(); }
            }
        });
        productTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e)  { productHoveredRow = -1; productTable.repaint(); }
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = productTable.getSelectedRow();
                    if (row == -1) return;
                    int    productId = (int)    productModel.getValueAt(row, 0);
                    String name      = (String) productModel.getValueAt(row, 1);
                    double price     = (double) productModel.getValueAt(row, 2);
                    int    stock     = (int)    productModel.getValueAt(row, 3);
                    if (stock <= 0) { showToast("❌  \"" + name + "\" is out of stock.", false); return; }
                    addToCart(productId, name, price, stock);  
                }
            }
        });

        styleTable(productTable);
        hideColumn(productTable, 0);
        hideColumn(productTable, 4);
        int[] widths = {0, 200, 100, 70, 0};
        for (int i = 0; i < widths.length; i++)
            productTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        productTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setText("₱ " + String.format("%.2f", val));
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(FONT_MONO);
                setForeground(new Color(0x1A, 0x52, 0x76));
                setBorder(new EmptyBorder(0, 8, 0, 14));
                return this;
            }
        });

        productTable.getColumnModel().getColumn(3).setCellRenderer(new TableCellRenderer() {
            final Color AMBER = new Color(0xB0, 0x6E, 0x00);
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                int qty = val instanceof Integer ? (Integer) val : 0;
                Color dot = qty == 0 ? RED : qty <= 5 ? AMBER : GREEN;
                JPanel cell = new JPanel(new GridBagLayout()) {
                    @Override protected void paintComponent(Graphics g) {
                        boolean isSelected = t.isRowSelected(row);
                        boolean isHovered  = (row == productHoveredRow) && !isSelected;
                        g.setColor(isSelected ? ROW_SEL : isHovered ? ROW_HOVER
                                : row % 2 == 0 ? ROW_EVEN : ROW_ODD);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        super.paintComponent(g);
                    }
                };
                cell.setOpaque(false);
                JPanel inner = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
                inner.setOpaque(false);
                JLabel dotLbl = new JLabel() {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(dot); g2.fillOval(0, 3, 8, 8); g2.dispose();
                    }
                    @Override public Dimension getPreferredSize() { return new Dimension(8, 14); }
                };
                JLabel num = new JLabel(String.valueOf(qty));
                num.setFont(new Font("SansSerif", Font.BOLD, 13));
                num.setForeground(qty == 0 ? RED : qty <= 5 ? AMBER : TEXT);
                inner.add(dotLbl); inner.add(num);
                cell.add(inner);
                return cell;
            }
        });

        card.add(buildScroll(productTable), BorderLayout.CENTER);
        return card;
    }

    private JComboBox<String> buildFilterCombo(String[] items, int width) {
        JComboBox<String> cb = new JComboBox<>(items) {
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
        cb.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cb.setForeground(TEXT);
        cb.setBackground(INPUT_BG);
        cb.setOpaque(false);
        cb.setBorder(new EmptyBorder(4, 8, 4, 8));
        cb.setPreferredSize(new Dimension(width, 36));
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return cb;
    }

    private JPanel buildCartCard() {
        JPanel card = buildCard();
        card.add(buildCardHeader("Cart", "Items added to the current sale"), BorderLayout.NORTH);

        String[] cols = {"Product", "Qty", "Price (₱)", "Subtotal (₱)"};
        cartModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        cartTable = new JTable(cartModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                boolean sel     = isRowSelected(row);
                boolean hovered = (row == cartHoveredRow) && !sel;
                c.setBackground(sel ? ROW_SEL : hovered ? ROW_HOVER
                        : row % 2 == 0 ? ROW_EVEN : ROW_ODD);
                if (c instanceof JLabel) {
                    ((JLabel) c).setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, COL_BORDER),
                        new EmptyBorder(0, 14, 0, 14)));
                }
                return c;
            }
        };

        cartTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int row = cartTable.rowAtPoint(e.getPoint());
                if (row != cartHoveredRow) { cartHoveredRow = row; cartTable.repaint(); }
            }
        });
        cartTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { cartHoveredRow = -1; cartTable.repaint(); }
        });

        styleTable(cartTable);

        int[] widths = {180, 50, 100, 110};
        for (int i = 0; i < widths.length; i++)
            cartTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        TableCellRenderer monoRight = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setText("₱ " + String.format("%.2f", val));
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(FONT_MONO);
                setForeground(new Color(0x1A, 0x52, 0x76));
                setBorder(new EmptyBorder(0, 8, 0, 14));
                return this;
            }
        };
        cartTable.getColumnModel().getColumn(2).setCellRenderer(monoRight);
        cartTable.getColumnModel().getColumn(3).setCellRenderer(monoRight);

        cartTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("SansSerif", Font.BOLD, 13));
                return this;
            }
        });

        card.add(buildScroll(cartTable), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildBottomBar() {
        JPanel outer = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BORDER_CLR);
                g2.drawLine(0, 0, getWidth(), 0);
                g2.dispose();
            }
        };
        outer.setBackground(CARD);
        outer.setBorder(new EmptyBorder(12, 28, 14, 28));

        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftBtns.setOpaque(false);
        JButton btnRemove = buildSmallButton("✕ Remove",      new Color(0xFD, 0xED, 0xEB), RED);
        JButton btnClear  = buildSmallButton("🗑️ Clear Cart", new Color(0xF0, 0xEB, 0xE4), MUTED);
        btnRemove.addActionListener(e -> removeSelectedItem());
        btnClear.addActionListener(e -> clearCart());
        leftBtns.add(btnRemove);
        leftBtns.add(btnClear);

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightSide.setOpaque(false);

        lblTotal = new JLabel("Total: ₱ 0.00");
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblTotal.setForeground(TEXT);

        JLabel payLbl = new JLabel("Payment:");
        payLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        payLbl.setForeground(TEXT);

        txtPayment = new JTextField(10) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                if (isFocusOwner()) {
                    g2.setColor(ACCENT); g2.setStroke(new BasicStroke(2f));
                } else {
                    g2.setColor(new Color(0xB0, 0x88, 0x68)); g2.setStroke(new BasicStroke(1.5f));
                }
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        txtPayment.setFont(FONT_INPUT);
        txtPayment.setOpaque(false);
        txtPayment.setBorder(new EmptyBorder(6, 10, 6, 10));
        txtPayment.setPreferredSize(new Dimension(130, 38));
        txtPayment.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { txtPayment.repaint(); }
            @Override public void focusLost(FocusEvent e)   { txtPayment.repaint(); }
        });

        lblChange = new JLabel("Change: ₱ 0.00");
        lblChange.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblChange.setForeground(GREEN);

        JButton btnPay = buildFilledButton("💳 Pay");
        btnPay.addActionListener(e -> processSale());

        rightSide.add(lblTotal);
        rightSide.add(payLbl);
        rightSide.add(txtPayment);
        rightSide.add(lblChange);
        rightSide.add(btnPay);

        outer.add(leftBtns,  BorderLayout.WEST);
        outer.add(rightSide, BorderLayout.EAST);
        return outer;
    }

    private void applyProductFilter() {
        String raw     = searchField.getText();
        String keyword = raw.equals(SEARCH_PLACEHOLDER) ? "" : raw.trim().toLowerCase();
        String selCat  = (String) cbCategory.getSelectedItem();
        boolean allCat = ALL_CATEGORIES.equals(selCat);
        String  sort   = cbSort != null ? (String) cbSort.getSelectedItem() : "Name A → Z";

        List<Object[]> filtered = new ArrayList<>();
        for (Object[] row : allProductRows) {
            String name = ((String) row[1]).toLowerCase();
            String cat  = row[4] != null ? (String) row[4] : "";
            boolean matchName = keyword.isEmpty() || name.contains(keyword);
            boolean matchCat  = allCat || cat.equals(selCat);
            if (matchName && matchCat) filtered.add(row);
        }

        filtered.sort((a, b) -> {
            String na = (String) a[1];
            String nb = (String) b[1];
            return "Name Z → A".equals(sort)
                ? nb.compareToIgnoreCase(na)
                : na.compareToIgnoreCase(nb);
        });

        productModel.setRowCount(0);
        for (Object[] row : filtered) productModel.addRow(row);
    }

    private void rebuildCategoryCombo() {
        String current = (String) cbCategory.getSelectedItem();
        cbCategory.removeAllItems();
        cbCategory.addItem(ALL_CATEGORIES);
        LinkedHashSet<String> cats = new LinkedHashSet<>();
        for (Object[] row : allProductRows) {
            if (row[4] != null && !((String) row[4]).isEmpty()) cats.add((String) row[4]);
        }
        for (String c : cats) cbCategory.addItem(c);
        if (current != null) cbCategory.setSelectedItem(current);
        if (cbCategory.getSelectedIndex() < 0) cbCategory.setSelectedIndex(0);
    }

    private void addToCart(int productId, String name, double price, int stock) {
        for (SaleItem item : cartItems) {
            if (item.getProductId() == productId) {
                if (item.getQuantity() >= stock) {  
                    showToast("❌  Not enough stock for \"" + name + "\". Only " + stock + " available.", false);
                    return;
                }
                item.setQuantity(item.getQuantity() + 1);
                refreshCartTable(); calculateTotal();
                showToast("➕  Added another \"" + name + "\" to cart.", true);
                return;
            }
        }
        // New item — stock must be at least 1 (already checked above, but safe to keep)
        SaleItem item = new SaleItem();
        item.setProductId(productId);
        item.setProductName(name);
        item.setQuantity(1);
        item.setPrice(BigDecimal.valueOf(price));
        cartItems.add(item);
        refreshCartTable(); calculateTotal();
        showToast("🛒  \"" + name + "\" added to cart.", true);
    }

    private void refreshCartTable() {
        cartModel.setRowCount(0);
        for (SaleItem item : cartItems) {
            double subtotal = item.getPrice().doubleValue() * item.getQuantity();
            cartModel.addRow(new Object[]{
                item.getProductName(), item.getQuantity(),
                item.getPrice().doubleValue(), subtotal
            });
        }
    }

    private void calculateTotal() {
        double total = 0;
        for (SaleItem item : cartItems)
            total += item.getPrice().doubleValue() * item.getQuantity();
        lblTotal.setText(String.format("Total:  ₱ %.2f", total));
    }

    private void processSale() {
        if (cartItems.isEmpty()) { showToast("❌  Cart is empty.", false); return; }

        double payment;
        try {
            payment = Double.parseDouble(txtPayment.getText().trim());
        } catch (NumberFormatException ex) {
            showToast("❌  Please enter a valid payment amount.", false);
            return;
        }

        double total = 0;
        for (SaleItem item : cartItems)
            total += item.getPrice().doubleValue() * item.getQuantity();

        if (payment < total) {
            showToast("❌  Payment is less than the total amount.", false);
            return;
        }
        double change = payment - total;
        lblChange.setText(String.format("Change:  ₱ %.2f", change));

        Connection conn = null;
        int saleId = -1;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement saleStmt = conn.prepareStatement(
                "INSERT INTO sales(user_id, total_amount) VALUES(?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            saleStmt.setInt(1, loggedInUser.getUserId());
            saleStmt.setDouble(2, total);
            saleStmt.executeUpdate();
            ResultSet rs = saleStmt.getGeneratedKeys();
            rs.next();
            saleId = rs.getInt(1);

            PreparedStatement itemStmt = conn.prepareStatement(
                "INSERT INTO sale_items(sale_id, product_id, quantity, price) VALUES(?, ?, ?, ?)");
            for (SaleItem item : cartItems) {
                itemStmt.setInt(1, saleId);
                itemStmt.setInt(2, item.getProductId());
                itemStmt.setInt(3, item.getQuantity());
                itemStmt.setBigDecimal(4, item.getPrice());
                itemStmt.addBatch();
            }
            itemStmt.executeBatch();

            PreparedStatement stockStmt = conn.prepareStatement(
                "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ?");
            for (SaleItem item : cartItems) {
                stockStmt.setInt(1, item.getQuantity());
                stockStmt.setInt(2, item.getProductId());
                stockStmt.addBatch();
            }
            stockStmt.executeBatch();
            conn.commit();

        } catch (Exception ex) {
            ex.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            showToast("❌  Sale failed. Please try again.", false);
            return;
        }

        generateReceipt(saleId, total, payment, change);
        showToast("✅  Sale completed! Transaction #" + saleId + " recorded.", true);
        cartItems.clear();
        refreshCartTable(); calculateTotal();
        lblChange.setText("Change:  ₱ 0.00");
        txtPayment.setText("");
        loadProducts();
    }

    private void loadProducts() {
        allProductRows.clear();
        int storeId = loggedInUser.getStoreId() != null ? loggedInUser.getStoreId() : -1;
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql =
                "SELECT p.product_id, p.product_name, p.price, p.stock_quantity, " +
                "COALESCE(c.category_name, '') AS category_name " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.category_id " +
                "WHERE p.store_id = ?";   // ← filtered by store
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, storeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                allProductRows.add(new Object[]{
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getDouble("price"),
                    rs.getInt("stock_quantity"),
                    rs.getString("category_name")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        rebuildCategoryCombo();
        applyProductFilter();
    }

    private void removeSelectedItem() {
        int row = cartTable.getSelectedRow();
        if (row == -1) { showToast("❌  Select an item in the cart first.", false); return; }
        String name = (String) cartModel.getValueAt(row, 0);
        cartItems.remove(row);
        refreshCartTable(); calculateTotal();
        showToast("✕  \"" + name + "\" removed from cart.", true);
    }

    private void clearCart() {
        if (cartItems.isEmpty()) return;
        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(parent, "Clear Cart", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(380, 200);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD);
        dlg.setContentPane(root);
        root.add(buildDialogHeader("🗑️ Clear Cart", "Remove all items from the current sale?"), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(CARD);
        body.setBorder(new EmptyBorder(18, 26, 10, 26));
        JLabel msg = new JLabel("This will remove all " + cartItems.size() + " item(s) from the cart.");
        msg.setFont(FONT_TABLE); msg.setForeground(TEXT);
        body.add(msg, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);

        final boolean[] ok = {false};
        root.add(buildDialogFooter(dlg, () -> ok[0] = true, "Clear All"), BorderLayout.SOUTH);
        dlg.setVisible(true);

        if (ok[0]) {
            cartItems.clear(); refreshCartTable(); calculateTotal();
            showToast("🗑️  Cart cleared.", true);
        }
    }

    private void generateReceipt(int saleId, double total, double payment, double change) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(parent, "Receipt", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(480, 560);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD);
        dlg.setContentPane(root);

        root.add(buildDialogHeader("🧾 Sale Receipt", "Transaction #" + saleId + "  ·  " +
            new SimpleDateFormat("MMM dd, yyyy  hh:mm a").format(new Date())), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(INPUT_BG);
        body.setBorder(new EmptyBorder(20, 28, 20, 28));

        body.add(makeReceiptHeaderRow());
        body.add(makeHRule(new Color(0xC8, 0xBE, 0xB2)));
        body.add(Box.createVerticalStrut(4));

        for (SaleItem item : cartItems) {
            double sub = item.getPrice().doubleValue() * item.getQuantity();
            body.add(makeReceiptItemRow(item.getProductName(), item.getQuantity(),
                item.getPrice().doubleValue(), sub));
            body.add(Box.createVerticalStrut(2));
        }

        body.add(Box.createVerticalStrut(8));
        body.add(makeHRule(new Color(0xC8, 0xBE, 0xB2)));
        body.add(Box.createVerticalStrut(10));
        body.add(makeSummaryRow("Subtotal", String.format("₱ %.2f", total),   false));
        body.add(Box.createVerticalStrut(4));
        body.add(makeSummaryRow("Payment",  String.format("₱ %.2f", payment), false));
        body.add(Box.createVerticalStrut(8));
        body.add(makeHRule(new Color(0xC8, 0xBE, 0xB2)));
        body.add(Box.createVerticalStrut(8));
        body.add(makeSummaryRow("Change",   String.format("₱ %.2f", change),  true));
        body.add(Box.createVerticalStrut(20));

        JLabel thanks = new JLabel("Thank you for your purchase!", SwingConstants.CENTER);
        thanks.setFont(new Font("SansSerif", Font.ITALIC, 13));
        thanks.setForeground(MUTED);
        thanks.setAlignmentX(Component.CENTER_ALIGNMENT);
        thanks.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        body.add(thanks);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setBackground(CARD);
        scroll.getViewport().setBackground(INPUT_BG);
        scroll.getViewport().setBorder(null);
        root.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(CARD); footer.setBorder(new EmptyBorder(8, 24, 16, 24));
        JButton btnClose = buildFilledButton("Close");
        btnClose.addActionListener(e -> dlg.dispose());
        footer.add(btnClose);
        root.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private JPanel makeReceiptHeaderRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        JLabel lProduct = new JLabel("PRODUCT");
        lProduct.setFont(new Font("SansSerif", Font.BOLD, 11)); lProduct.setForeground(MUTED);
        JPanel right = new JPanel(new GridLayout(1, 3, 0, 0));
        right.setOpaque(false); right.setPreferredSize(new Dimension(210, 26));
        for (String h : new String[]{"QTY", "PRICE", "SUBTOTAL"}) {
            JLabel l = new JLabel(h, SwingConstants.CENTER);
            l.setFont(new Font("SansSerif", Font.BOLD, 11)); l.setForeground(MUTED);
            right.add(l);
        }
        row.add(lProduct, BorderLayout.CENTER);
        row.add(right,    BorderLayout.EAST);
        return row;
    }

    private JPanel makeReceiptItemRow(String name, int qty, double price, double subtotal) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel lName = new JLabel(name);
        lName.setFont(new Font("SansSerif", Font.PLAIN, 13)); lName.setForeground(TEXT);
        JPanel right = new JPanel(new GridLayout(1, 3, 0, 0));
        right.setOpaque(false); right.setPreferredSize(new Dimension(210, 28));
        JLabel lQty   = new JLabel(String.valueOf(qty), SwingConstants.CENTER);
        lQty.setFont(new Font("SansSerif", Font.BOLD, 13)); lQty.setForeground(TEXT);
        JLabel lPrice = new JLabel(String.format("₱ %.2f", price), SwingConstants.CENTER);
        lPrice.setFont(FONT_MONO); lPrice.setForeground(new Color(0x1A, 0x52, 0x76));
        JLabel lSub   = new JLabel(String.format("₱ %.2f", subtotal), SwingConstants.CENTER);
        lSub.setFont(FONT_MONO); lSub.setForeground(new Color(0x1A, 0x52, 0x76));
        right.add(lQty); right.add(lPrice); right.add(lSub);
        row.add(lName, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JPanel makeSummaryRow(String label, String value, boolean highlight) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel lLabel = new JLabel(label);
        lLabel.setFont(highlight ? new Font("SansSerif", Font.BOLD, 15) : new Font("SansSerif", Font.PLAIN, 13));
        lLabel.setForeground(highlight ? TEXT : MUTED);
        JLabel lValue = new JLabel(value, SwingConstants.RIGHT);
        lValue.setFont(highlight ? new Font("Monospaced", Font.BOLD, 16) : FONT_MONO);
        lValue.setForeground(highlight ? GREEN : new Color(0x1A, 0x52, 0x76));
        row.add(lLabel, BorderLayout.WEST);
        row.add(lValue, BorderLayout.EAST);
        return row;
    }

    private JPanel makeHRule(Color color) {
        JPanel line = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(color); g.fillRect(0, 0, getWidth(), 1);
            }
        };
        line.setOpaque(false);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setPreferredSize(new Dimension(0, 1));
        return line;
    }

    private JPanel buildCard() {
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
        return card;
    }

    private JPanel buildCardHeader(String title, String sub) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(16, 20, 12, 20));
        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);
        JLabel lTitle = new JLabel(title);
        lTitle.setFont(new Font("SansSerif", Font.BOLD, 15)); lTitle.setForeground(TEXT);
        JLabel lSub = new JLabel(sub);
        lSub.setFont(FONT_SUB); lSub.setForeground(MUTED);
        stack.add(lTitle); stack.add(lSub);
        header.add(stack, BorderLayout.WEST);
        JPanel divider = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BORDER_CLR); g.fillRect(0, 0, getWidth(), 1);
            }
        };
        divider.setPreferredSize(new Dimension(0, 1)); divider.setOpaque(false);
        JPanel outer = new JPanel(new BorderLayout()); outer.setOpaque(false);
        outer.add(header, BorderLayout.NORTH); outer.add(divider, BorderLayout.SOUTH);
        return outer;
    }

    private JPanel buildDialogHeader(String title, String sub) {
        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, LEFT_TOP, getWidth(), getHeight(), LEFT_BOT));
                g2.fillRect(0, 0, getWidth(), getHeight()); g2.dispose();
            }
        };
        hdr.setOpaque(false); hdr.setBorder(new EmptyBorder(18, 24, 18, 24));
        JPanel stack = new JPanel(); stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS)); stack.setOpaque(false);
        JLabel t = new JLabel(title); t.setFont(new Font("SansSerif", Font.BOLD, 16)); t.setForeground(Color.WHITE);
        JLabel s = new JLabel(sub);   s.setFont(FONT_SUB); s.setForeground(new Color(255, 255, 255, 170));
        stack.add(t); stack.add(s); hdr.add(stack, BorderLayout.WEST);
        return hdr;
    }

    private JPanel buildDialogFooter(JDialog dlg, Runnable onConfirm, String confirmLabel) {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(CARD); footer.setBorder(new EmptyBorder(8, 24, 18, 24));
        JButton btnCancel  = buildSmallButton("Cancel", new Color(0xF0, 0xEB, 0xE4), MUTED);
        JButton btnConfirm = buildFilledButton(confirmLabel);
        btnCancel.addActionListener(e -> dlg.dispose());
        btnConfirm.addActionListener(e -> { onConfirm.run(); dlg.dispose(); });
        footer.add(btnCancel); footer.add(btnConfirm);
        return footer;
    }

    private static void hideColumn(JTable t, int col) {
        TableColumn tc = t.getColumnModel().getColumn(col);
        tc.setMinWidth(0); tc.setMaxWidth(0); tc.setWidth(0);
    }

    private void styleTable(JTable t) {
        t.setFont(FONT_TABLE); t.setRowHeight(44);
        t.setShowVerticalLines(true); t.setShowHorizontalLines(true);
        t.setGridColor(COL_BORDER);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFillsViewportHeight(true); t.setBackground(CARD);
        t.setSelectionBackground(ROW_SEL); t.setSelectionForeground(TEXT);
        t.setBorder(null); t.setIntercellSpacing(new Dimension(0, 1));

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

        JTableHeader header = t.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                JLabel lbl = new JLabel(val == null ? "" : val.toString().toUpperCase()) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setPaint(new GradientPaint(0, 0, HDR_TOP, 0, getHeight(), HDR_BOT));
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.setColor(new Color(255, 255, 255, 25));
                        g2.fillRect(getWidth() - 1, 4, 1, getHeight() - 8);
                        g2.setColor(GOLD); g2.setStroke(new BasicStroke(2f));
                        g2.drawLine(0, getHeight() - 2, getWidth(), getHeight() - 2);
                        g2.dispose(); super.paintComponent(g);
                    }
                };
                lbl.setFont(FONT_TABLE_H); lbl.setForeground(new Color(255, 255, 255, 210));
                lbl.setBorder(new EmptyBorder(0, 14, 0, 14)); lbl.setOpaque(false);
                return lbl;
            }
        });
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false); header.setBorder(null); header.setBackground(HDR_TOP);
        t.putClientProperty("cornerFill", cornerFill);
    }

    private JScrollPane buildScroll(JTable t) {
        JScrollPane scroll = new JScrollPane(t);
        scroll.setBorder(null); scroll.setViewportBorder(null);
        scroll.setOpaque(true); scroll.setBackground(CARD);
        scroll.getViewport().setOpaque(true); scroll.getViewport().setBackground(CARD);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        Component corner = (Component) t.getClientProperty("cornerFill");
        if (corner != null) scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, corner);
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
        panel.setOpaque(false); panel.setBorder(new EmptyBorder(12, 16, 12, 20));
        JLabel lbl = new JLabel(message);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13)); lbl.setForeground(Color.WHITE);
        panel.add(lbl);
        toast.setContentPane(panel); toast.pack();
        try {
            Point loc = getLocationOnScreen();
            toast.setLocation(loc.x + (getWidth() - toast.getWidth()) / 2,
                              loc.y + getHeight() - toast.getHeight() - 32);
        } catch (IllegalComponentStateException ignored) {}
        toast.setVisible(true);
        javax.swing.Timer fadeTimer = new javax.swing.Timer(30, null);
        final float[] alpha = {1.0f};
        final boolean[] fading = {false};
        javax.swing.Timer holdTimer = new javax.swing.Timer(2500, e -> fading[0] = true);
        holdTimer.setRepeats(false); holdTimer.start();
        fadeTimer.addActionListener(e -> {
            if (!fading[0]) return;
            alpha[0] -= 0.06f;
            if (alpha[0] <= 0f) { fadeTimer.stop(); toast.dispose(); }
            else toast.setOpacity(alpha[0]);
        });
        fadeTimer.start();
    }
}