package view;

import model.Category;
import model.User;
import dao.CategoryDAO;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class CategoryPanel extends JPanel {

    private static final Color ACCENT      = new Color(0xC7, 0x4B, 0x1A);
    private static final Color ACCENT_DARK = new Color(0x8F, 0x31, 0x10);
    private static final Color LEFT_TOP    = new Color(0xB0, 0x3A, 0x12);
    private static final Color LEFT_BOT    = new Color(0x4A, 0x18, 0x08);
    private static final Color BG          = new Color(0xF5, 0xF0, 0xE8);
    private static final Color CARD        = Color.WHITE;
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
    private static final Color RED_DARK    = new Color(0x96, 0x1A, 0x10);

    private static final Font FONT_TITLE  = new Font("SansSerif", Font.BOLD,  22);
    private static final Font FONT_SUB    = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_BTN    = new Font("SansSerif", Font.BOLD,  13);
    private static final Font FONT_LABEL  = new Font("SansSerif", Font.BOLD,  11);
    private static final Font FONT_INPUT  = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_TABLE  = new Font("SansSerif", Font.PLAIN, 13);

    private final User        loggedInUser;
    private final Runnable    onBack;
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        txtName;
    private JButton           btnAdd;
    private JButton           btnUpdate;
    private JButton           btnDelete;
    private int               hoveredRow  = -1;

    public CategoryPanel(User user, Runnable onBack) {
        this.loggedInUser = user;
        this.onBack       = onBack;

        setLayout(new BorderLayout());
        setBackground(BG);

        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        loadCategories();
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, LEFT_TOP, getWidth(), getHeight(), LEFT_BOT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillOval(getWidth() - 120, -60, 200, 200);
                g2.dispose();
            }
        };
        bar.setOpaque(true);
        bar.setBackground(LEFT_TOP);
        bar.setBorder(new EmptyBorder(18, 28, 18, 28));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JButton btnBack = buildIconButton("<- Back", new Color(255, 255, 255, 30), Color.WHITE);
        btnBack.addActionListener(e -> onBack.run());

        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);
        JLabel title = new JLabel("\uD83C\uDFF7  Categories");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Manage your product categories");
        sub.setFont(FONT_SUB);
        sub.setForeground(new Color(255, 255, 255, 170));
        titleStack.add(title);
        titleStack.add(sub);

        left.add(btnBack);
        left.add(Box.createHorizontalStrut(6));
        left.add(titleStack);

        bar.add(left, BorderLayout.WEST);
        return bar;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));
        content.add(buildFormCard(),  BorderLayout.NORTH);
        content.add(buildTableCard(), BorderLayout.CENTER);
        return content;
    }

    private JPanel buildFormCard() {
        JPanel card = buildCard();
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel dot = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                g2.fillOval(0, (getHeight() - 8) / 2, 8, 8);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(14, 20); }
        };
        JLabel sectionLabel = new JLabel("Category Details");
        sectionLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        sectionLabel.setForeground(TEXT);

        JPanel headerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        headerRow.setOpaque(false);
        headerRow.add(dot);
        headerRow.add(sectionLabel);

        JPanel inputRow = new JPanel(new GridBagLayout());
        inputRow.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();

        JPanel fieldWrap = new JPanel(new BorderLayout(0, 5));
        fieldWrap.setOpaque(false);
        JLabel lbl = new JLabel("Category Name *");
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(MUTED);
        txtName = buildInputField();
        txtName.addActionListener(e -> addCategory());
        fieldWrap.add(lbl,     BorderLayout.NORTH);
        fieldWrap.add(txtName, BorderLayout.CENTER);

        gc.gridx = 0; gc.gridy = 0;
        gc.weightx = 1.0; gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.SOUTH;
        gc.insets = new Insets(0, 0, 0, 12);
        inputRow.add(fieldWrap, gc);

        btnAdd    = buildFilledButton("+ Add Category");
        btnUpdate = buildSecondaryButton("✎  Update");
        btnDelete = buildDangerButton("\uD83D\uDDD1  Delete");
        JButton btnClear = buildGhostButton("Clear", e -> clearForm());

        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);

        btnAdd.addActionListener(e -> addCategory());
        btnUpdate.addActionListener(e -> updateCategory());
        btnDelete.addActionListener(e -> deleteCategory());

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnGroup.setOpaque(false);
        btnGroup.add(btnClear);
        btnGroup.add(btnDelete);
        btnGroup.add(btnUpdate);
        btnGroup.add(btnAdd);

        JPanel btnWrap = new JPanel(new BorderLayout(0, 5));
        btnWrap.setOpaque(false);
        JLabel spacer = new JLabel(" ");
        spacer.setFont(FONT_LABEL);
        btnWrap.add(spacer,   BorderLayout.NORTH);
        btnWrap.add(btnGroup, BorderLayout.CENTER);

        gc.gridx = 1; gc.gridy = 0;
        gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        gc.anchor = GridBagConstraints.SOUTH;
        gc.insets = new Insets(0, 0, 0, 0);
        inputRow.add(btnWrap, gc);

        card.add(headerRow, BorderLayout.NORTH);
        card.add(inputRow,  BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = buildCard();
        card.setLayout(new BorderLayout());

        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        cardHeader.setBorder(new EmptyBorder(16, 22, 12, 22));
        JLabel cardTitle = new JLabel("All Categories");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        cardTitle.setForeground(TEXT);
        cardHeader.add(cardTitle, BorderLayout.WEST);

        JPanel divider = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BORDER_CLR);
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        divider.setPreferredSize(new Dimension(0, 1));
        divider.setOpaque(false);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(cardHeader, BorderLayout.NORTH);
        north.add(divider,    BorderLayout.SOUTH);
        card.add(north, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "Category Name", "Created At"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                boolean selected = isRowSelected(row);
                boolean hov      = (row == hoveredRow) && !selected;
                if (selected)  c.setBackground(ROW_SEL);
                else if (hov)  c.setBackground(ROW_HOVER);
                else           c.setBackground(row % 2 == 0 ? ROW_EVEN : ROW_ODD);
                if (c instanceof JLabel) {
                    JLabel jl = (JLabel) c;
                    jl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, COL_BORDER),
                        new EmptyBorder(0, 14, 0, 14)));
                }
                return c;
            }
        };

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            if (row != -1) {
                txtName.setText(tableModel.getValueAt(row, 1).toString());
                btnUpdate.setEnabled(true);
                btnDelete.setEnabled(true);
                btnAdd.setEnabled(false);
                btnUpdate.repaint();
                btnDelete.repaint();
                btnAdd.repaint();
            }
        });

        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) { hoveredRow = row; table.repaint(); }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { hoveredRow = -1; table.repaint(); }
        });

        table.setFont(FONT_TABLE);
        table.setRowHeight(44);
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

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        table.getColumnModel().getColumn(1).setPreferredWidth(340);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);

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
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        JTableHeader th = table.getTableHeader();
        th.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                String text = val == null ? "" : val.toString().toUpperCase();
                JLabel lbl = new JLabel(text) {
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
                lbl.setHorizontalAlignment(col == 2 ? SwingConstants.CENTER : SwingConstants.LEFT);
                return lbl;
            }
        });
        th.setPreferredSize(new Dimension(0, 44));
        th.setReorderingAllowed(false);
        th.setBorder(null);
        th.setBackground(HDR_TOP);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.setViewportBorder(null);
        scroll.setBackground(CARD);
        scroll.getViewport().setBackground(CARD);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        JPanel scrollWrapper = new JPanel(new BorderLayout());
        scrollWrapper.setBackground(CARD);
        scrollWrapper.setBorder(new EmptyBorder(0, 0, 8, 0));
        scrollWrapper.add(scroll, BorderLayout.CENTER);
        card.add(scrollWrapper, BorderLayout.CENTER);
        return card;
    }

    private void loadCategories() {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        try {
            List<Category> list = categoryDAO.getAllCategories(loggedInUser.getStoreId());
            for (Category cat : list) {
                String dateStr = cat.getCreatedAt() != null ? sdf.format(cat.getCreatedAt()) : "—";
                tableModel.addRow(new Object[]{cat.getId(), cat.getName(), dateStr});
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load categories: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        clearForm();
    }

    private void addCategory() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Category name is required.",
                "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (categoryDAO.addCategory(name, loggedInUser.getStoreId())) {
            loadCategories();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add category.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCategory() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Category name is required.",
                "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        if (categoryDAO.updateCategory(id, name, loggedInUser.getStoreId())) {
            loadCategories();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update category.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCategory() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        String catName = tableModel.getValueAt(row, 1).toString();
        Window owner = SwingUtilities.getWindowAncestor(this);

        final JDialog dialog;
        if (owner instanceof Frame)       dialog = new JDialog((Frame)  owner, "Confirm Delete", true);
        else if (owner instanceof Dialog) dialog = new JDialog((Dialog) owner, "Confirm Delete", true);
        else                              dialog = new JDialog((Frame)   null, "Confirm Delete", true);

        dialog.setSize(460, 290);
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
        header.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel hLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        hLeft.setOpaque(false);
        JLabel hIcon = new JLabel("\uD83D\uDDD1");
        hIcon.setFont(new Font("SansSerif", Font.PLAIN, 24));
        JPanel hStack = new JPanel();
        hStack.setLayout(new BoxLayout(hStack, BoxLayout.Y_AXIS));
        hStack.setOpaque(false);
        JLabel hTitle = new JLabel("Delete Category");
        hTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        hTitle.setForeground(Color.WHITE);
        JLabel hSub = new JLabel("This will permanently delete the category");
        hSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        hSub.setForeground(new Color(255, 255, 255, 170));
        hStack.add(hTitle);
        hStack.add(Box.createVerticalStrut(2));
        hStack.add(hSub);
        hLeft.add(hIcon);
        hLeft.add(hStack);
        header.add(hLeft, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(CARD);
        body.setBorder(new EmptyBorder(20, 24, 10, 24));

        JPanel infoCard = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xFD, 0xF6, 0xF0));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(0xF0, 0xE4, 0xD8));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        infoCard.setOpaque(false);
        infoCard.setBorder(new EmptyBorder(12, 16, 12, 16));
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel catLbl = new JLabel("CATEGORY");
        catLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        catLbl.setForeground(new Color(0x9A, 0x8E, 0x84));
        JLabel catVal = new JLabel(catName);
        catVal.setFont(new Font("SansSerif", Font.BOLD, 14));
        catVal.setForeground(TEXT);
        JPanel catStack = new JPanel();
        catStack.setLayout(new BoxLayout(catStack, BoxLayout.Y_AXIS));
        catStack.setOpaque(false);
        catStack.add(catLbl);
        catStack.add(Box.createVerticalStrut(3));
        catStack.add(catVal);
        infoCard.add(catStack, BorderLayout.WEST);
        body.add(infoCard);
        body.add(Box.createVerticalStrut(14));

        JPanel warn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xFF, 0xF4, 0xE0));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(0xF0, 0xD8, 0x90));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        warn.setOpaque(false);
        warn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        warn.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel wText = new JLabel("\u26A0  This category will be permanently removed and cannot be recovered.");
        wText.setFont(new Font("SansSerif", Font.PLAIN, 12));
        wText.setForeground(new Color(0x92, 0x58, 0x00));
        warn.add(wText);
        body.add(warn);
        root.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(CARD);
        footer.setBorder(new EmptyBorder(6, 22, 18, 22));

        JButton btnCancel  = buildSmallButton("Cancel", new Color(0xF0, 0xEB, 0xE4), MUTED);
        JButton btnConfirm = new JButton("\uD83D\uDDD1  Delete Category") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? RED_DARK : RED);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnConfirm.setFont(FONT_BTN);
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setContentAreaFilled(false);
        btnConfirm.setBorderPainted(false);
        btnConfirm.setFocusPainted(false);
        btnConfirm.setOpaque(false);
        btnConfirm.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnConfirm.setBorder(new EmptyBorder(9, 20, 9, 20));

        final boolean[] confirmed = {false};
        btnCancel.addActionListener(e -> dialog.dispose());
        btnConfirm.addActionListener(e -> { confirmed[0] = true; dialog.dispose(); });

        footer.add(btnCancel);
        footer.add(btnConfirm);
        root.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);

        if (!confirmed[0]) return;

        int id = (int) tableModel.getValueAt(row, 0);
        if (categoryDAO.deleteCategory(id, loggedInUser.getStoreId())) {
            loadCategories();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete category.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtName.setText("");
        table.clearSelection();
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        btnAdd.setEnabled(true);
        btnUpdate.repaint();
        btnDelete.repaint();
        btnAdd.repaint();
        hoveredRow = -1;
    }

    private JPanel buildCard() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_CLR);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
            @Override public boolean isOpaque() { return false; }
        };
    }

    private JTextField buildInputField() {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(isFocusOwner() ? ACCENT : BORDER_CLR);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 1.5f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setFont(FONT_INPUT);
        f.setForeground(TEXT);
        f.setOpaque(false);
        f.setBorder(new EmptyBorder(9, 12, 9, 12));
        f.setPreferredSize(new Dimension(280, 40));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { f.repaint(); }
            @Override public void focusLost(FocusEvent e)   { f.repaint(); }
        });
        return f;
    }

    private JButton buildFilledButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(!isEnabled() ? new Color(0xD9, 0xCF, 0xC4)
                    : getModel().isRollover() ? ACCENT_DARK : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                setForeground(!isEnabled() ? new Color(255, 255, 255, 130) : Color.WHITE);
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        return btn;
    }

    private JButton buildSecondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(!isEnabled() ? new Color(0xF5, 0xF2, 0xEF)
                    : getModel().isRollover() ? new Color(0xF0, 0xE8, 0xDE)
                    : new Color(0xFA, 0xF7, 0xF3));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(isEnabled() ? ACCENT : BORDER_CLR);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                setForeground(isEnabled() ? ACCENT : BORDER_CLR);
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(ACCENT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        return btn;
    }

    private JButton buildDangerButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(!isEnabled() ? new Color(0xF5, 0xF2, 0xEF)
                    : getModel().isRollover() ? new Color(0xFD, 0xED, 0xEB)
                    : new Color(0xFF, 0xF5, 0xF4));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(isEnabled() ? RED : BORDER_CLR);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                setForeground(isEnabled() ? RED : BORDER_CLR);
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(RED);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        return btn;
    }

    private JButton buildGhostButton(String text, ActionListener action) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                    ? new Color(0xEE, 0xE8, 0xDE) : new Color(0xF5, 0xF0, 0xE8));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_CLR);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(MUTED);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 14, 9, 14));
        btn.addActionListener(action);
        return btn;
    }

    private JButton buildSmallButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isRollover()
                    ? new Color(Math.max(0, bg.getRed()-15),
                                Math.max(0, bg.getGreen()-15),
                                Math.max(0, bg.getBlue()-15)) : bg;
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
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private JButton buildIconButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isRollover()
                    ? new Color(Math.max(0, bg.getRed()-15),
                                Math.max(0, bg.getGreen()-15),
                                Math.max(0, bg.getBlue()-15),
                                bg.getAlpha()) : bg;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }
}