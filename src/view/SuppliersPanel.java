package view;

import model.User;
import dao.SupplierDAO;
import model.Supplier;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class SuppliersPanel extends JPanel {

    private static final Color ACCENT      = new Color(0xC7, 0x4B, 0x1A);
    private static final Color ACCENT_DARK = new Color(0x8F, 0x31, 0x10);
    private static final Color LEFT_TOP    = new Color(0xB0, 0x3A, 0x12);
    private static final Color LEFT_BOT    = new Color(0x4A, 0x18, 0x08);
    private static final Color BG          = new Color(0xF5, 0xF0, 0xE8);
    private static final Color CARD        = Color.WHITE;
    private static final Color TEXT        = new Color(0x1A, 0x14, 0x10);
    private static final Color MUTED       = new Color(0x6B, 0x5E, 0x52);
    private static final Color BORDER_CLR  = new Color(0xD9, 0xCF, 0xC4);
    private static final Color INPUT_BG    = new Color(0xFA, 0xF7, 0xF3);
    private static final Color GOLD        = new Color(0xFF, 0xD5, 0x80);
    private static final Color RED         = new Color(0xC0, 0x39, 0x2B);
    private static final Color RED_DARK    = new Color(0x96, 0x1A, 0x10);
    private static final Color GREEN       = new Color(0x2E, 0x7D, 0x52);
    private static final Color AVATAR_BG   = new Color(0xFA, 0xEC, 0xE7);
    private static final Color AVATAR_FG   = new Color(0x99, 0x3C, 0x1D);
    private static final Color CARD_FOOT   = new Color(0xFD, 0xFB, 0xF8);
    private static final Color EDIT_BG     = new Color(0xEE, 0xF6, 0xF1);
    private static final Color EDIT_FG     = new Color(0x2E, 0x7D, 0x52);
    private static final Color DEL_BG      = new Color(0xFD, 0xED, 0xEB);
    private static final Color DEL_FG      = new Color(0xC0, 0x39, 0x2B);

    private static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD,  22);
    private static final Font FONT_LABEL = new Font("SansSerif", Font.BOLD,  11);
    private static final Font FONT_INPUT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_BTN   = new Font("SansSerif", Font.BOLD,  13);

    private static final String PLACEHOLDER = "Search suppliers...";

    private final User     loggedInUser;
    private final Runnable onBack;

    private List<Supplier> allSuppliers = new ArrayList<>();
    private JPanel         cardsPanel;
    private JTextField     searchField;
    private JLabel         countLabel;

    public SuppliersPanel(User user, Runnable onBack) {
        this.loggedInUser = user;
        this.onBack       = onBack;
        setLayout(new BorderLayout());
        setBackground(BG);
        add(buildTopBar(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
        loadSuppliers();
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
        JLabel title = new JLabel("\uD83D\uDE9A  Suppliers");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Manage your supply partners");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(new Color(255, 255, 255, 170));
        titleStack.add(title);
        titleStack.add(sub);

        left.add(btnBack);
        left.add(Box.createHorizontalStrut(6));
        left.add(titleStack);

        JButton btnAdd = buildIconButton("+ Add Supplier", GOLD, new Color(0x1A, 0x14, 0x10));
        btnAdd.addActionListener(e -> showSupplierForm(null));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(btnAdd);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 0));
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(22, 28, 22, 28));
        body.add(buildToolbar(),     BorderLayout.NORTH);
        body.add(buildCardsScroll(), BorderLayout.CENTER);
        return body;
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 16, 0));

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

        JLabel searchIco = new JLabel("  \uD83D\uDD0D");
        searchIco.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchIco.setBorder(new EmptyBorder(0, 8, 0, 2));

        searchField = new JTextField();
        searchField.setFont(FONT_INPUT);
        searchField.setForeground(MUTED);
        searchField.setText(PLACEHOLDER);
        searchField.setOpaque(false);
        searchField.setBorder(new EmptyBorder(6, 4, 6, 10));

        searchField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(PLACEHOLDER)) {
                    searchField.setText(""); searchField.setForeground(TEXT);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (searchField.getText().trim().isEmpty()) {
                    searchField.setForeground(MUTED); searchField.setText(PLACEHOLDER);
                }
            }
        });
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { SwingUtilities.invokeLater(() -> filterCards()); }
            public void removeUpdate(DocumentEvent e)  { SwingUtilities.invokeLater(() -> filterCards()); }
            public void changedUpdate(DocumentEvent e) { SwingUtilities.invokeLater(() -> filterCards()); }
        });

        searchWrap.add(searchIco,   BorderLayout.WEST);
        searchWrap.add(searchField, BorderLayout.CENTER);

        countLabel = new JLabel("0 suppliers");
        countLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        countLabel.setForeground(MUTED);

        JPanel left  = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(searchWrap);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(countLabel);

        toolbar.add(left,  BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);
        return toolbar;
    }

    private JScrollPane buildCardsScroll() {
        cardsPanel = new JPanel();
        cardsPanel.setLayout(new GridLayout(0, 3, 16, 16));
        cardsPanel.setBackground(BG);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG);
        wrapper.add(cardsPanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        return scroll;
    }

    private void loadSuppliers() {
        allSuppliers = new SupplierDAO().getAllSuppliers(loggedInUser.getStoreId());
        filterCards();
    }

    private void filterCards() {
        String raw   = searchField.getText();
        String query = raw.equals(PLACEHOLDER) ? "" : raw.trim().toLowerCase();

        List<Supplier> filtered = new ArrayList<>();
        for (Supplier s : allSuppliers) {
            if (query.isEmpty()
                || s.getSupplierName().toLowerCase().contains(query)
                || (s.getContactNumber() != null && s.getContactNumber().toLowerCase().contains(query))
                || (s.getEmail()         != null && s.getEmail().toLowerCase().contains(query))
                || (s.getAddress()       != null && s.getAddress().toLowerCase().contains(query))) {
                filtered.add(s);
            }
        }

        cardsPanel.removeAll();
        if (filtered.isEmpty()) {
            JLabel empty = new JLabel("No suppliers found.");
            empty.setFont(new Font("SansSerif", Font.PLAIN, 14));
            empty.setForeground(MUTED);
            empty.setBorder(new EmptyBorder(32, 0, 0, 0));
            cardsPanel.add(empty);
        } else {
            for (int i = 0; i < filtered.size(); i++) {
                cardsPanel.add(buildSupplierCard(filtered.get(i), i + 1));
            }
        }

        int n = filtered.size();
        countLabel.setText(n + (n == 1 ? " supplier" : " suppliers"));
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private JPanel buildSupplierCard(final Supplier s, int displayIndex) {
        final boolean[] hovered = {false};

        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(hovered[0] ? BORDER_CLR.darker() : BORDER_CLR);
                g2.setStroke(new BasicStroke(hovered[0] ? 1.4f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
            @Override public boolean isOpaque() { return false; }
        };
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered[0] = true;  card.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hovered[0] = false; card.repaint(); }
        });

        JPanel stripe = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, LEFT_TOP, getWidth(), 0, ACCENT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        stripe.setPreferredSize(new Dimension(0, 4));
        stripe.setOpaque(false);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 18, 14, 18));

        JPanel headRow = new JPanel(new BorderLayout(12, 0));
        headRow.setOpaque(false);
        headRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        headRow.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel avatar = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AVATAR_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(44, 44));
        avatar.setMinimumSize(new Dimension(44, 44));
        avatar.setMaximumSize(new Dimension(44, 44));
        JLabel initialsLbl = new JLabel(getInitials(s.getSupplierName()));
        initialsLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        initialsLbl.setForeground(AVATAR_FG);
        avatar.add(initialsLbl);

        JPanel nameStack = new JPanel();
        nameStack.setLayout(new BoxLayout(nameStack, BoxLayout.Y_AXIS));
        nameStack.setOpaque(false);
        JLabel nameLabel = new JLabel(truncate(s.getSupplierName(), 28));
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        nameLabel.setForeground(TEXT);
        JLabel seqLabel = new JLabel("Supplier #" + displayIndex);
        seqLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        seqLabel.setForeground(MUTED);
        nameStack.add(nameLabel);
        nameStack.add(Box.createVerticalStrut(2));
        nameStack.add(seqLabel);

        headRow.add(avatar,    BorderLayout.WEST);
        headRow.add(nameStack, BorderLayout.CENTER);
        body.add(headRow);

        body.add(makeDivider());
        body.add(Box.createVerticalStrut(12));

        body.add(makeContactRow("📞", s.getContactNumber()));
        body.add(Box.createVerticalStrut(7));
        body.add(makeContactRow("✉",  s.getEmail()));
        body.add(Box.createVerticalStrut(7));
        body.add(makeContactRow("📍", s.getAddress()));

        JPanel footer = new JPanel(new GridLayout(1, 2, 8, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(CARD_FOOT);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_CLR),
            new EmptyBorder(10, 18, 12, 18)
        ));

        JButton btnEdit   = buildCardButton("✏  Edit",   EDIT_BG, EDIT_FG);
        JButton btnRemove = buildCardButton("🗑  Remove", DEL_BG,  DEL_FG);
        btnEdit.addActionListener(e -> showSupplierForm(s));
        btnRemove.addActionListener(e -> showDeleteDialog(s));
        footer.add(btnEdit);
        footer.add(btnRemove);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(stripe, BorderLayout.NORTH);
        north.add(body,   BorderLayout.CENTER);

        card.add(north,  BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private JPanel makeContactRow(String icon, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel ico = new JLabel(icon);
        ico.setFont(new Font("SansSerif", Font.PLAIN, 12));
        ico.setPreferredSize(new Dimension(16, 16));
        ico.setHorizontalAlignment(SwingConstants.CENTER);

        String display = (value == null || value.trim().isEmpty()) ? "—" : value.trim();
        if (display.length() > 34) display = display.substring(0, 31) + "…";
        JLabel val = new JLabel(display);
        val.setFont(new Font("SansSerif", Font.PLAIN, 12));
        val.setForeground(display.equals("—") ? new Color(0xC0, 0xB8, 0xB0) : MUTED);

        row.add(ico, BorderLayout.WEST);
        row.add(val, BorderLayout.CENTER);
        return row;
    }

    private JPanel makeDivider() {
        JPanel line = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BORDER_CLR); g.fillRect(0, 0, getWidth(), 1);
            }
        };
        line.setOpaque(false);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setPreferredSize(new Dimension(0, 1));
        return line;
    }

    private JButton buildCardButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.darker() : bg);
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
        btn.setBorder(new EmptyBorder(7, 12, 7, 12));
        return btn;
    }

    private void showSupplierForm(final Supplier existing) {
        final boolean isEdit = (existing != null);

        Window owner = SwingUtilities.getWindowAncestor(this);
        final JDialog dialog;
        if (owner instanceof Frame)       dialog = new JDialog((Frame)  owner, "", true);
        else if (owner instanceof Dialog) dialog = new JDialog((Dialog) owner, "", true);
        else                              dialog = new JDialog((Frame)   null, "", true);

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(460, 480);
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
        header.setBorder(new EmptyBorder(20, 24, 20, 24));
        JLabel hTitle = new JLabel(isEdit ? "✏  Edit Supplier" : "\uD83D\uDE9A  Add New Supplier");
        hTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        hTitle.setForeground(Color.WHITE);
        header.add(hTitle, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CARD);
        form.setBorder(new EmptyBorder(20, 28, 10, 28));

        final JTextField fName    = buildFormField(form, "Supplier Name *", isEdit ? existing.getSupplierName() : "");
        final JTextField fPhone   = buildFormField(form, "Phone",   isEdit && existing.getContactNumber() != null ? existing.getContactNumber() : "");
        final JTextField fEmail   = buildFormField(form, "Email",   isEdit && existing.getEmail()         != null ? existing.getEmail()         : "");
        final JTextField fAddress = buildFormField(form, "Address", isEdit && existing.getAddress()       != null ? existing.getAddress()       : "");

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.setBackground(CARD);
        formScroll.getViewport().setBackground(CARD);
        root.add(formScroll, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(CARD);
        btnRow.setBorder(new EmptyBorder(10, 24, 18, 24));

        JButton btnCancel = buildSmallButton("Cancel", new Color(0xF0, 0xEB, 0xE4), MUTED);
        JButton btnSave   = buildFilledButton(isEdit ? "Save Changes" : "Add Supplier");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String name = fName.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Supplier name is required.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            SupplierDAO dao = new SupplierDAO();
            boolean ok;
            if (isEdit) {
                Supplier upd = new Supplier();
                upd.setSupplierId(existing.getSupplierId());
                upd.setSupplierName(name);
                upd.setContactNumber(fPhone.getText().trim());
                upd.setEmail(fEmail.getText().trim());
                upd.setAddress(fAddress.getText().trim());
                ok = dao.updateSupplier(upd, loggedInUser.getStoreId());
            } else {
                ok = dao.addSupplier(new Supplier(name,
                    fPhone.getText().trim(), fEmail.getText().trim(),
                    fAddress.getText().trim()), loggedInUser.getStoreId());
            }
            if (ok) { dialog.dispose(); loadSuppliers(); }
            else JOptionPane.showMessageDialog(dialog,
                isEdit ? "Failed to update supplier." : "Failed to add supplier.",
                "Error", JOptionPane.ERROR_MESSAGE);
        });

        btnRow.add(btnCancel);
        btnRow.add(btnSave);
        root.add(btnRow, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showDeleteDialog(final Supplier s) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        final JDialog dialog;
        if (owner instanceof Frame)       dialog = new JDialog((Frame)  owner, "Confirm Removal", true);
        else if (owner instanceof Dialog) dialog = new JDialog((Dialog) owner, "Confirm Removal", true);
        else                              dialog = new JDialog((Frame)   null, "Confirm Removal", true);

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(500, 360);
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
        header.setBorder(new EmptyBorder(20, 24, 20, 24));
        JPanel hLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        hLeft.setOpaque(false);
        JLabel hIcon = new JLabel("\uD83D\uDDD1");
        hIcon.setFont(new Font("SansSerif", Font.PLAIN, 26));
        JPanel hStack = new JPanel();
        hStack.setLayout(new BoxLayout(hStack, BoxLayout.Y_AXIS));
        hStack.setOpaque(false);
        JLabel hTitle = new JLabel("Remove Supplier");
        hTitle.setFont(new Font("SansSerif", Font.BOLD, 17));
        hTitle.setForeground(Color.WHITE);
        JLabel hSub = new JLabel("This will permanently delete the supplier record");
        hSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        hSub.setForeground(new Color(255, 255, 255, 170));
        hStack.add(hTitle);
        hStack.add(Box.createVerticalStrut(3));
        hStack.add(hSub);
        hLeft.add(hIcon);
        hLeft.add(hStack);
        header.add(hLeft, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

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
                g2.dispose();
                super.paintComponent(g);
            }
        };
        infoCard.setOpaque(false);
        infoCard.setBorder(new EmptyBorder(16, 18, 16, 18));
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        String phone   = (s.getContactNumber() != null && !s.getContactNumber().trim().isEmpty()) ? s.getContactNumber() : "—";
        String email   = (s.getEmail()         != null && !s.getEmail().trim().isEmpty())         ? s.getEmail()         : "—";
        String address = (s.getAddress()       != null && !s.getAddress().trim().isEmpty())       ? s.getAddress()       : "—";

        infoCard.add(makeInfoCell("Supplier", s.getSupplierName()));
        infoCard.add(makeInfoCell("Phone",    phone));
        infoCard.add(makeInfoCell("Email",    email));
        infoCard.add(makeInfoCell("Address",  address));
        body.add(infoCard);
        body.add(Box.createVerticalStrut(16));

        JPanel warnBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8)) {
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
        warnBanner.setOpaque(false);
        warnBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        warnBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel wText = new JLabel("\u26A0  This supplier will be permanently removed and cannot be recovered.");
        wText.setFont(new Font("SansSerif", Font.PLAIN, 12));
        wText.setForeground(new Color(0x92, 0x58, 0x00));
        warnBanner.add(wText);
        body.add(warnBanner);
        root.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(CARD);
        footer.setBorder(new EmptyBorder(8, 26, 20, 26));

        JButton btnCancel  = buildSmallButton("Cancel", new Color(0xF0, 0xEB, 0xE4), MUTED);
        JButton btnConfirm = new JButton("\uD83D\uDDD1  Remove Supplier") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? RED_DARK : RED);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
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
        btnConfirm.addActionListener(e -> { confirmed[0] = true; dialog.dispose(); });

        footer.add(btnCancel);
        footer.add(btnConfirm);
        root.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);

        if (!confirmed[0]) return;
        // ── Pass store_id so only this store's supplier can be deleted ─────────
        if (new SupplierDAO().deleteSupplier(s.getSupplierId(), loggedInUser.getStoreId())) {
            loadSuppliers();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to remove supplier.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JTextField buildFormField(JPanel parent, String labelText, String value) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(lbl);
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
                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setFont(FONT_INPUT);
        field.setForeground(TEXT);
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(8, 12, 8, 12));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { field.repaint(); }
            @Override public void focusLost(FocusEvent e)   { field.repaint(); }
        });
        parent.add(field);
        parent.add(Box.createVerticalStrut(14));
        return field;
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
        cell.add(lbl);
        cell.add(Box.createVerticalStrut(3));
        cell.add(val);
        return cell;
    }

    private JButton buildSmallButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isRollover()
                    ? new Color(Math.max(0, bg.getRed()-15), Math.max(0, bg.getGreen()-15),
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
        btn.setBorder(new EmptyBorder(7, 16, 7, 16));
        return btn;
    }

    private JButton buildFilledButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_DARK : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        return btn;
    }

    private JButton buildIconButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isRollover()
                    ? new Color(Math.max(0, bg.getRed()-15), Math.max(0, bg.getGreen()-15),
                                Math.max(0, bg.getBlue()-15), bg.getAlpha()) : bg;
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

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}