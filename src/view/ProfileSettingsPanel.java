package view;

import model.User;
import config.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ProfileSettingsPanel extends JPanel {

    private static final Color ACCENT     = new Color(0xC7, 0x4B, 0x1A);
    private static final Color LEFT_TOP   = new Color(0xB0, 0x3A, 0x12);
    private static final Color LEFT_BOT   = new Color(0x4A, 0x18, 0x08);
    private static final Color CARD       = Color.WHITE;
    private static final Color BG         = new Color(0xF5, 0xF0, 0xE8);
    private static final Color TEXT       = new Color(0x1A, 0x14, 0x10);
    private static final Color MUTED      = new Color(0x6B, 0x5E, 0x52);
    private static final Color BORDER_CLR = new Color(0xE8, 0xE2, 0xD8);
    private static final Color INPUT_BG   = new Color(0xFA, 0xF7, 0xF3);
    private static final Color GOLD       = new Color(0xFF, 0xD5, 0x80);
    private static final Color GREEN      = new Color(0x2E, 0x7D, 0x52);

    private static final Color TOAST_SUCCESS = new Color(0x1A, 0x6B, 0x3C);
    private static final Color TOAST_ERROR   = new Color(0x9B, 0x2C, 0x1F);

    private static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  22);
    private static final Font FONT_SUB     = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_BTN     = new Font("SansSerif", Font.BOLD,  13);
    private static final Font FONT_LABEL   = new Font("SansSerif", Font.BOLD,  11);
    private static final Font FONT_INPUT   = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_SECTION = new Font("SansSerif", Font.BOLD,  13);

    private final User     user;
    private final Runnable onBack;
    private final Runnable onSaved;

    private JTextField txtFullName;
    private JTextField txtUsername;
    private JTextField txtPhone;       
    private JTextField txtStoreName;
    private JTextField txtStoreLogo;
    private JLabel     logoPreview;

    public ProfileSettingsPanel(User user, Runnable onBack) {
        this(user, onBack, null);
    }

    public ProfileSettingsPanel(User user, Runnable onBack, Runnable onSaved) {
        this.user    = user;
        this.onBack  = onBack;
        this.onSaved = onSaved;
        setLayout(new BorderLayout());
        setBackground(BG);
        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
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
        left.add(btnBack);
        left.add(Box.createHorizontalStrut(6));

        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);
        JLabel title = new JLabel("👤 Profile & Settings");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Manage your account and store information");
        sub.setFont(FONT_SUB);
        sub.setForeground(new Color(255, 255, 255, 170));
        titleStack.add(title);
        titleStack.add(sub);
        left.add(titleStack);
        bar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(buildHeaderChip("👤  " + user.getFullName()));
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
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setFont(new Font("SansSerif", Font.BOLD, 13));
        chip.setForeground(Color.WHITE);
        chip.setBorder(new EmptyBorder(6, 16, 6, 16));
        chip.setOpaque(false);
        return chip;
    }

    private JScrollPane buildContent() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(BG);
        page.setBorder(new EmptyBorder(28, 32, 32, 32));

        JPanel accountCard = buildCard();
        JPanel accountForm = new JPanel();
        accountForm.setLayout(new BoxLayout(accountForm, BoxLayout.Y_AXIS));
        accountForm.setOpaque(false);
        accountForm.setBorder(new EmptyBorder(20, 24, 24, 24));
        accountForm.add(buildSectionHeader("👤", "Account Information"));
        accountForm.add(Box.createVerticalStrut(18));

        JPanel row1 = new JPanel(new GridLayout(1, 2, 20, 0));
        row1.setOpaque(false);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel fnPanel = new JPanel();
        fnPanel.setLayout(new BoxLayout(fnPanel, BoxLayout.Y_AXIS));
        fnPanel.setOpaque(false);
        fnPanel.add(makeLabel("Full Name"));
        fnPanel.add(Box.createVerticalStrut(5));
        txtFullName = buildField(user.getFullName());
        fnPanel.add(txtFullName);

        JPanel unPanel = new JPanel();
        unPanel.setLayout(new BoxLayout(unPanel, BoxLayout.Y_AXIS));
        unPanel.setOpaque(false);
        unPanel.add(makeLabel("Username"));
        unPanel.add(Box.createVerticalStrut(5));
        txtUsername = buildField(user.getUsername());
        unPanel.add(txtUsername);

        row1.add(fnPanel);
        row1.add(unPanel);
        accountForm.add(row1);
        accountForm.add(Box.createVerticalStrut(14));

        JPanel row1b = new JPanel(new GridLayout(1, 2, 20, 0));
        row1b.setOpaque(false);
        row1b.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel phonePanel = new JPanel();
        phonePanel.setLayout(new BoxLayout(phonePanel, BoxLayout.Y_AXIS));
        phonePanel.setOpaque(false);
        phonePanel.add(makeLabel("Phone Number"));
        phonePanel.add(Box.createVerticalStrut(5));
        txtPhone = buildField(fetchPhoneNumber(user.getUserId()));
        txtPhone.putClientProperty("JTextField.placeholderText", "e.g. +63 912 345 6789");
        phonePanel.add(txtPhone);

        JLabel phoneHint = new JLabel("Used for password recovery. Optional.");
        phoneHint.setFont(new Font("SansSerif", Font.PLAIN, 10));
        phoneHint.setForeground(MUTED);
        phoneHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        phonePanel.add(Box.createVerticalStrut(4));
        phonePanel.add(phoneHint);

        JPanel phoneSpacer = new JPanel();
        phoneSpacer.setOpaque(false);

        row1b.add(phonePanel);
        row1b.add(phoneSpacer);
        accountForm.add(row1b);
        accountCard.add(accountForm, BorderLayout.CENTER);
        page.add(accountCard);
        page.add(Box.createVerticalStrut(18));

        JPanel storeCard = buildCard();
        JPanel storeForm = new JPanel();
        storeForm.setLayout(new BoxLayout(storeForm, BoxLayout.Y_AXIS));
        storeForm.setOpaque(false);
        storeForm.setBorder(new EmptyBorder(20, 24, 24, 24));
        storeForm.add(buildSectionHeader("🏪", "Store Information"));
        storeForm.add(Box.createVerticalStrut(18));

        JPanel row2 = new JPanel(new GridLayout(1, 2, 20, 0));
        row2.setOpaque(false);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JPanel snPanel = new JPanel();
        snPanel.setLayout(new BoxLayout(snPanel, BoxLayout.Y_AXIS));
        snPanel.setOpaque(false);
        snPanel.add(makeLabel("Store Name"));
        snPanel.add(Box.createVerticalStrut(5));
        txtStoreName = buildField(fetchStoreName(user.getStoreId()));
        txtStoreName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        snPanel.add(txtStoreName);
        snPanel.add(Box.createVerticalGlue()); 

        JPanel slPanel = new JPanel();
        slPanel.setLayout(new BoxLayout(slPanel, BoxLayout.Y_AXIS));
        slPanel.setOpaque(false);
        slPanel.add(makeLabel("Store Logo"));
        slPanel.add(Box.createVerticalStrut(8));

        String currentLogo = fetchStoreLogo(user.getStoreId());

        logoPreview = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                String path = txtStoreLogo != null ? txtStoreLogo.getText().trim() : "";
                if (!path.isEmpty()) {
                    try {
                        java.io.File f = new java.io.File(path);
                        if (f.exists()) {
                            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(f);
                            if (img != null) {
                                g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, getWidth(), getHeight()));
                                g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                                g2.setClip(null);
                                g2.setColor(BORDER_CLR);
                                g2.setStroke(new java.awt.BasicStroke(1.5f));
                                g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                                g2.dispose();
                                return;
                            }
                        }
                    } catch (Exception ignored) {}
                }
                g2.setColor(new Color(0xFA, 0xE8, 0xDF));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(BORDER_CLR);
                g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setColor(ACCENT);
                g2.setFont(new Font("SansSerif", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                String icon = "🏪";
                g2.drawString(icon, (getWidth() - fm.stringWidth(icon)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(72, 72); }
            @Override public Dimension getMinimumSize()   { return new Dimension(72, 72); }
            @Override public Dimension getMaximumSize()   { return new Dimension(72, 72); }
        };
        logoPreview.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtStoreLogo = buildField(currentLogo);
        txtStoreLogo.setEditable(false);
        txtStoreLogo.setForeground(MUTED);
        txtStoreLogo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton btnBrowse = buildFilledButton("📁  Browse Image", ACCENT, Color.WHITE);
        btnBrowse.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnBrowse.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Select Store Logo");
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Image Files (*.png, *.jpg, *.jpeg, *.gif)", "png", "jpg", "jpeg", "gif"));
            if (!txtStoreLogo.getText().isEmpty()) {
                java.io.File cur = new java.io.File(txtStoreLogo.getText());
                if (cur.getParentFile() != null && cur.getParentFile().exists())
                    fc.setCurrentDirectory(cur.getParentFile());
            }
            int result = fc.showOpenDialog(ProfileSettingsPanel.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                String path = fc.getSelectedFile().getAbsolutePath();
                txtStoreLogo.setText(path);
                txtStoreLogo.setForeground(TEXT);
                logoPreview.repaint();
            }
        });

        JPanel logoRow = new JPanel();
        logoRow.setLayout(new BoxLayout(logoRow, BoxLayout.X_AXIS));
        logoRow.setOpaque(false);
        logoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        logoRow.add(logoPreview);
        logoRow.add(Box.createHorizontalStrut(14));
        logoRow.add(btnBrowse);
        logoRow.add(Box.createHorizontalGlue());

        slPanel.add(logoRow);
        slPanel.add(Box.createVerticalStrut(8));
        slPanel.add(txtStoreLogo);

        row2.add(snPanel);
        row2.add(slPanel);
        storeForm.add(row2);
        storeCard.add(storeForm, BorderLayout.CENTER);
        page.add(storeCard);
        page.add(Box.createVerticalStrut(18));

        JPanel actionsCard = buildCard();
        JPanel actionsBody = new JPanel();
        actionsBody.setLayout(new BoxLayout(actionsBody, BoxLayout.Y_AXIS));
        actionsBody.setOpaque(false);
        actionsBody.setBorder(new EmptyBorder(20, 24, 24, 24));
        actionsBody.add(buildSectionHeader("⚙️", "Actions"));
        actionsBody.add(Box.createVerticalStrut(18));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSave    = buildFilledButton("💾  Save Changes",    ACCENT,                       Color.WHITE);
        JButton btnBackup  = buildFilledButton("🗄️  Backup Database",  GREEN,                        Color.WHITE);
        JButton btnRestore = buildFilledButton("🔄  Restore Backup",   new Color(0x1A, 0x5C, 0x8A), Color.WHITE);

        btnSave.addActionListener(e -> {
            user.setFullName(txtFullName.getText().trim());
            user.setUsername(txtUsername.getText().trim());
            updateUser(user, txtPhone.getText().trim());
            updateStore(user.getStoreId(), txtStoreName.getText().trim(), txtStoreLogo.getText().trim());
            showToast("✅  Profile & Store updated successfully.", true);
            if (onSaved != null) onSaved.run();
        });
        btnBackup.addActionListener(e -> backupDatabase());
        btnRestore.addActionListener(e -> restoreBackup());

        btnRow.add(btnSave);
        btnRow.add(btnBackup);
        btnRow.add(btnRestore);
        actionsBody.add(btnRow);
        actionsCard.add(actionsBody, BorderLayout.CENTER);
        page.add(actionsCard);

        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(null);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                int w = scroll.getViewport().getWidth();
                page.setMaximumSize(new Dimension(w, Integer.MAX_VALUE));
                page.setPreferredSize(new Dimension(w, page.getPreferredSize().height));
                page.revalidate();
            }
        });
        return scroll;
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
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private JPanel buildSectionHeader(String emoji, String title) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel badge = new JLabel(emoji) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xFA, 0xE8, 0xDF));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("SansSerif", Font.PLAIN, 16));
        badge.setBorder(new EmptyBorder(4, 8, 4, 8));
        badge.setOpaque(false);
        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_SECTION);
        lbl.setForeground(TEXT);
        left.add(badge);
        left.add(lbl);
        wrap.add(left, BorderLayout.WEST);
        JPanel divider = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BORDER_CLR);
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                g2.dispose();
            }
        };
        divider.setOpaque(false);
        divider.setPreferredSize(new Dimension(0, 1));
        JPanel stack = new JPanel(new BorderLayout());
        stack.setOpaque(false);
        stack.add(wrap,    BorderLayout.NORTH);
        stack.add(divider, BorderLayout.SOUTH);
        stack.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        stack.setAlignmentX(Component.LEFT_ALIGNMENT);
        return stack;
    }

    private JTextField buildField(String value) {
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
        field.setBorder(new EmptyBorder(9, 12, 9, 12));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { field.repaint(); }
            @Override public void focusLost(FocusEvent e)   { field.repaint(); }
        });
        return field;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(FONT_LABEL);
        l.setForeground(MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JButton buildFilledButton(String text, Color bg, Color fg) {
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
        btn.setFont(FONT_BTN);
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 22, 10, 22));
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

    private void showToast(String message, boolean success) {
        Color toastBg = success ? TOAST_SUCCESS : TOAST_ERROR;
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
                g2.dispose();
                super.paintComponent(g);
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
        try {
            Point loc = getLocationOnScreen();
            toast.setLocation(
                loc.x + (getWidth()  - toast.getWidth())  / 2,
                loc.y +  getHeight() - toast.getHeight() - 32
            );
        } catch (Exception ignored) {}
        toast.setVisible(true);
        final float[]   alpha  = {1.0f};
        final boolean[] fading = {false};
        javax.swing.Timer holdTimer = new javax.swing.Timer(2500, e -> fading[0] = true);
        holdTimer.setRepeats(false);
        holdTimer.start();
        javax.swing.Timer fadeTimer = new javax.swing.Timer(30, null);
        fadeTimer.addActionListener(e -> {
            if (!fading[0]) return;
            alpha[0] -= 0.06f;
            if (alpha[0] <= 0f) { fadeTimer.stop(); toast.dispose(); }
            else toast.setOpacity(alpha[0]);
        });
        fadeTimer.start();
    }

    private String fetchStoreName(Integer storeId) {
        if (storeId == null) return "";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT store_name FROM store WHERE store_id = ?")) {
            stmt.setInt(1, storeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("store_name");
        } catch (Exception e) { e.printStackTrace(); }
        return "";
    }

    private String fetchStoreLogo(Integer storeId) {
        if (storeId == null) return "";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT store_logo FROM store WHERE store_id = ?")) {
            stmt.setInt(1, storeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String v = rs.getString("store_logo");
                return v != null ? v : "";
            }
        } catch (Exception e) { /* column may not exist yet */ }
        return "";
    }

    private String fetchPhoneNumber(int userId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT phone_number FROM users WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String v = rs.getString("phone_number");
                return v != null ? v : "";
            }
        } catch (Exception e) { /* column may not exist yet */ }
        return "";
    }

    private void updateUser(User u, String phone) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE users SET full_name = ?, username = ?, phone_number = ? WHERE user_id = ?")) {
            stmt.setString(1, u.getFullName());
            stmt.setString(2, u.getUsername());
            stmt.setString(3, phone.isEmpty() ? null : phone);
            stmt.setInt(4, u.getUserId());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            showToast("❌  Failed to update account info.", false);
        }
    }

    private void updateStore(Integer storeId, String name, String logoPath) {
        if (storeId == null) return;
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE store SET store_name = ?, store_logo = ? WHERE store_id = ?")) {
                stmt.setString(1, name);
                stmt.setString(2, logoPath);
                stmt.setInt(3, storeId);
                stmt.executeUpdate();
                return;
            } catch (SQLException ex) { /* store_logo may not exist */ }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE store SET store_name = ? WHERE store_id = ?")) {
                stmt.setString(1, name);
                stmt.setInt(2, storeId);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast("❌  Failed to update store info.", false);
        }
    }

    private void backupDatabase() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Backup As");
        fc.setSelectedFile(new java.io.File("backup_store_" + user.getStoreId() + ".sql"));
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("SQL Files (*.sql)", "sql"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File outputFile = fc.getSelectedFile();
        if (!outputFile.getName().endsWith(".sql"))
            outputFile = new java.io.File(outputFile.getAbsolutePath() + ".sql");
        final java.io.File finalOutput = outputFile;

        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 java.io.PrintWriter writer = new java.io.PrintWriter(
                         new java.io.FileWriter(finalOutput))) {

                Integer storeId = user.getStoreId();
                int     userId  = user.getUserId();

                writer.println("-- ============================================================");
                writer.println("-- Store Backup for: " + txtStoreName.getText().trim());
                writer.println("-- Generated:        " + new java.util.Date());
                writer.println("-- Store ID:         " + storeId);
                writer.println("-- User ID:          " + userId);
                writer.println("-- ============================================================");
                writer.println();

                writer.println("-- STORE");
                PreparedStatement s1 = conn.prepareStatement("SELECT * FROM store WHERE store_id = ?");
                s1.setInt(1, storeId);
                dumpTable(writer, s1.executeQuery(), "store");

                writer.println("-- USER");
                PreparedStatement s2 = conn.prepareStatement("SELECT * FROM users WHERE user_id = ?");
                s2.setInt(1, userId);
                dumpTable(writer, s2.executeQuery(), "users");

                writer.println("-- CATEGORIES");
                PreparedStatement s3 = conn.prepareStatement("SELECT * FROM categories WHERE store_id = ?");
                s3.setInt(1, storeId);
                dumpTable(writer, s3.executeQuery(), "categories");

                writer.println("-- SUPPLIER");
                PreparedStatement s4 = conn.prepareStatement("SELECT * FROM supplier WHERE store_id = ?");
                s4.setInt(1, storeId);
                dumpTable(writer, s4.executeQuery(), "supplier");

                writer.println("-- PRODUCTS");
                PreparedStatement s5 = conn.prepareStatement("SELECT * FROM products WHERE store_id = ?");
                s5.setInt(1, storeId);
                dumpTable(writer, s5.executeQuery(), "products");

                writer.println("-- SALES");
                PreparedStatement s6 = conn.prepareStatement("SELECT * FROM sales WHERE user_id = ?");
                s6.setInt(1, userId);
                dumpTable(writer, s6.executeQuery(), "sales");

                writer.println("-- SALE ITEMS");
                PreparedStatement s7 = conn.prepareStatement(
                    "SELECT si.* FROM sale_items si " +
                    "JOIN sales s ON si.sale_id = s.sale_id WHERE s.user_id = ?");
                s7.setInt(1, userId);
                dumpTable(writer, s7.executeQuery(), "sale_items");

                writer.println("-- INVENTORY LOGS");
                PreparedStatement s8 = conn.prepareStatement(
                    "SELECT * FROM inventory_logs WHERE user_id = ?");
                s8.setInt(1, userId);
                dumpTable(writer, s8.executeQuery(), "inventory_logs");

                writer.println("-- DISPOSED ITEMS");
                PreparedStatement s9 = conn.prepareStatement(
                    "SELECT * FROM disposed_items WHERE store_id = ?");
                s9.setInt(1, storeId);
                dumpTable(writer, s9.executeQuery(), "disposed_items");

                writer.println();
                writer.println("-- Backup complete.");
                writer.flush();

                SwingUtilities.invokeLater(() ->
                    showToast("✅  Backup saved to: " + finalOutput.getName(), true));

            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() ->
                    showToast("❌  Backup failed: " + ex.getMessage(), false));
            }
        }).start();
    }

    private void restoreBackup() {
        final boolean[] confirmed = {false};

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog;
        if (owner instanceof Frame)       dialog = new JDialog((Frame)  owner, "Confirm Restore", true);
        else if (owner instanceof Dialog) dialog = new JDialog((Dialog) owner, "Confirm Restore", true);
        else                              dialog = new JDialog((Frame)   null,  "Confirm Restore", true);

        dialog.setSize(480, 310);
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

        JPanel hLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        hLeft.setOpaque(false);
        JLabel hIcon = new JLabel("🔄");
        hIcon.setFont(new Font("SansSerif", Font.PLAIN, 26));
        JPanel hStack = new JPanel();
        hStack.setLayout(new BoxLayout(hStack, BoxLayout.Y_AXIS));
        hStack.setOpaque(false);
        JLabel hTitle = new JLabel("Restore Backup");
        hTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        hTitle.setForeground(Color.WHITE);
        JLabel hSub = new JLabel("This action will overwrite your current store data");
        hSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        hSub.setForeground(new Color(255, 255, 255, 170));
        hStack.add(hTitle);
        hStack.add(Box.createVerticalStrut(3));
        hStack.add(hSub);
        hLeft.add(hIcon);
        hLeft.add(hStack);
        header.add(hLeft, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(2, 1, 0, 12));
        body.setBackground(CARD);
        body.setBorder(new EmptyBorder(20, 24, 14, 24));

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
        infoCard.setBorder(new EmptyBorder(10, 16, 10, 16));

        JLabel infoLbl = new JLabel("RESTORING TO");
        infoLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        infoLbl.setForeground(new Color(0x9A, 0x8E, 0x84));
        JLabel infoVal = new JLabel(user.getFullName() + "  —  " + fetchStoreName(user.getStoreId()));
        infoVal.setFont(new Font("SansSerif", Font.BOLD, 14));
        infoVal.setForeground(TEXT);
        JPanel infoStack = new JPanel();
        infoStack.setLayout(new BoxLayout(infoStack, BoxLayout.Y_AXIS));
        infoStack.setOpaque(false);
        infoStack.add(infoLbl);
        infoStack.add(Box.createVerticalStrut(4));
        infoStack.add(infoVal);
        infoCard.add(infoStack, BorderLayout.WEST);

        JPanel warn = new JPanel(new BorderLayout()) {
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
        warn.setBorder(new EmptyBorder(10, 14, 10, 14));
        JLabel wText = new JLabel("⚠  All current data will be deleted and replaced. This cannot be undone.");
        wText.setFont(new Font("SansSerif", Font.PLAIN, 12));
        wText.setForeground(new Color(0x92, 0x58, 0x00));
        warn.add(wText, BorderLayout.WEST);

        body.add(infoCard);
        body.add(warn);
        root.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(CARD);
        footer.setBorder(new EmptyBorder(6, 22, 18, 22));

        JButton btnCancel = new JButton("Cancel") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                    ? new Color(0xE8, 0xE2, 0xD8) : new Color(0xF0, 0xEB, 0xE4));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnCancel.setFont(FONT_BTN);
        btnCancel.setForeground(MUTED);
        btnCancel.setContentAreaFilled(false);
        btnCancel.setBorderPainted(false);
        btnCancel.setFocusPainted(false);
        btnCancel.setOpaque(false);
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.setBorder(new EmptyBorder(9, 20, 9, 20));

        JButton btnConfirm = new JButton("🔄  Restore") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                    ? new Color(0x0F, 0x4A, 0x6E) : new Color(0x1A, 0x5C, 0x8A));
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

        btnCancel.addActionListener(e -> dialog.dispose());
        btnConfirm.addActionListener(e -> { confirmed[0] = true; dialog.dispose(); });

        footer.add(btnCancel);
        footer.add(btnConfirm);
        root.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);

        if (!confirmed[0]) return;

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select Backup File to Restore");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "SQL Backup Files (*.sql)", "sql"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        final java.io.File backupFile = fc.getSelectedFile();
        showToast("⏳  Restoring backup, please wait...", true);

        new Thread(() -> {
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                conn.setAutoCommit(false);

                Integer storeId = user.getStoreId();
                int     userId  = user.getUserId();

                PreparedStatement d1 = conn.prepareStatement(
                    "DELETE si FROM sale_items si JOIN sales s ON si.sale_id = s.sale_id WHERE s.user_id = ?");
                d1.setInt(1, userId); d1.executeUpdate();

                PreparedStatement d2 = conn.prepareStatement("DELETE FROM sales WHERE user_id = ?");
                d2.setInt(1, userId); d2.executeUpdate();

                PreparedStatement d3 = conn.prepareStatement("DELETE FROM inventory_logs WHERE user_id = ?");
                d3.setInt(1, userId); d3.executeUpdate();

                PreparedStatement d4 = conn.prepareStatement("DELETE FROM disposed_items WHERE store_id = ?");
                d4.setInt(1, storeId); d4.executeUpdate();

                PreparedStatement d5 = conn.prepareStatement("DELETE FROM products WHERE store_id = ?");
                d5.setInt(1, storeId); d5.executeUpdate();

                PreparedStatement d6 = conn.prepareStatement("DELETE FROM categories WHERE store_id = ?");
                d6.setInt(1, storeId); d6.executeUpdate();

                PreparedStatement d7 = conn.prepareStatement("DELETE FROM supplier WHERE store_id = ?");
                d7.setInt(1, storeId); d7.executeUpdate();

                PreparedStatement d8 = conn.prepareStatement("DELETE FROM store WHERE store_id = ?");
                d8.setInt(1, storeId); d8.executeUpdate();

                PreparedStatement d9 = conn.prepareStatement("DELETE FROM users WHERE user_id = ?");
                d9.setInt(1, userId); d9.executeUpdate();

                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.FileReader(backupFile));
                String line;
                StringBuilder stmtBuilder = new StringBuilder();
                int restoredCount = 0;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("--")) continue;
                    stmtBuilder.append(line).append(" ");
                    if (line.endsWith(";")) {
                        String sql = stmtBuilder.toString().trim();
                        stmtBuilder.setLength(0);
                        if (!sql.toUpperCase().startsWith("INSERT")) continue;
                        try (PreparedStatement ps = conn.prepareStatement(sql)) {
                            ps.executeUpdate();
                            restoredCount++;
                        } catch (SQLException ex) {
                            if (ex.getErrorCode() != 1062) throw ex;
                        }
                    }
                }
                reader.close();
                conn.commit();

                final int count = restoredCount;
                SwingUtilities.invokeLater(() ->
                    showToast("✅  Restore complete! " + count + " records restored.", true));

            } catch (Exception ex) {
                ex.printStackTrace();
                if (conn != null) try { conn.rollback(); } catch (Exception ignored) {}
                SwingUtilities.invokeLater(() ->
                    showToast("❌  Restore failed: " + ex.getMessage(), false));
            } finally {
                if (conn != null) try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception ignored) {}
            }
        }).start();
    }

    private void dumpTable(java.io.PrintWriter writer, ResultSet rs, String tableName)
            throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();

        StringBuilder cols = new StringBuilder("INSERT INTO `").append(tableName).append("` (");
        for (int i = 1; i <= colCount; i++) {
            cols.append("`").append(meta.getColumnName(i)).append("`");
            if (i < colCount) cols.append(", ");
        }
        cols.append(") VALUES ");

        boolean hasRows = false;
        while (rs.next()) {
            hasRows = true;
            StringBuilder row = new StringBuilder(cols).append("(");
            for (int i = 1; i <= colCount; i++) {
                Object val = rs.getObject(i);
                if (val == null) {
                    row.append("NULL");
                } else {
                    String escaped = val.toString().replace("\\", "\\\\").replace("'", "\\'");
                    row.append("'").append(escaped).append("'");
                }
                if (i < colCount) row.append(", ");
            }
            row.append(");");
            writer.println(row);
        }

        if (!hasRows) writer.println("-- (no data for " + tableName + ")");
        writer.println();
    }
}