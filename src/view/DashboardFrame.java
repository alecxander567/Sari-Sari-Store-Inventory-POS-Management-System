package view;

import model.User;
import config.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class DashboardFrame extends JFrame {

    private static final Color ACCENT      = new Color(0xC7, 0x4B, 0x1A);
    private static final Color ACCENT_DARK = new Color(0x8F, 0x31, 0x10);
    private static final Color LEFT_TOP    = new Color(0xB0, 0x3A, 0x12);
    private static final Color LEFT_BOT    = new Color(0x4A, 0x18, 0x08);
    private static final Color CARD        = Color.WHITE;
    private static final Color BG          = new Color(0xF5, 0xF0, 0xE8);
    private static final Color TEXT        = new Color(0x1A, 0x14, 0x10);
    private static final Color MUTED       = new Color(0x6B, 0x5E, 0x52);
    private static final Color BORDER_CLR  = new Color(0xD9, 0xCF, 0xC4);
    private static final Color GOLD        = new Color(0xFF, 0xD5, 0x80);
    private static final Color SIDEBAR_BG  = new Color(0x1A, 0x14, 0x10);
    private static final Color SIDEBAR_HVR = new Color(0x2C, 0x22, 0x1C);
    private static final Color SIDEBAR_ACT = new Color(0xC7, 0x4B, 0x1A);

    private static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  22);
    private static final Font FONT_SUB     = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_BTN     = new Font("SansSerif", Font.BOLD,  15);
    private static final Font FONT_CARD_N  = new Font("SansSerif", Font.BOLD,  28);
    private static final Font FONT_CARD_L  = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_SECTION = new Font("SansSerif", Font.BOLD,  15);

    private User   loggedInUser;
    private String storeName;

    private JPanel     contentCardPanel;
    private CardLayout cardLayout;

    private JPanel navDashboard;
    private JPanel navInventory;
    private JPanel navPOS;
    private JPanel navLogs;
    private JPanel navSales;

    private String         activeNav = "Dashboard";
    private InventoryPanel inventoryPanel;
    private SuppliersPanel suppliersPanel;

    private boolean posAdded          = false;
    private boolean salesHistoryAdded = false;
    private boolean categoriesAdded   = false;

    private JLabel lblTodaySales;
    private JLabel lblTransactions;
    private JLabel lblLowStock;
    private JLabel lblOutOfStock;

    private JLabel lblTopBarGreeting;
    private JLabel lblSidebarStoreName;
    private JLabel lblSidebarLogo;
    private JLabel lblHeroWelcome;     

    public DashboardFrame(User user, String storeName) {
        this.loggedInUser = user;
        this.storeName    = (storeName != null && !storeName.isEmpty()) ? storeName : "Your Store";

        setTitle("Sari-Sari Store POS - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 660);
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        root.add(buildSidebar(),  BorderLayout.WEST);
        root.add(buildMainArea(), BorderLayout.CENTER);

        refreshDashboardStats();
        setVisible(true);
    }

    private void refreshDashboardStats() {
        final int storeId = loggedInUser.getStoreId() != null ? loggedInUser.getStoreId() : -1;
        final int userId  = loggedInUser.getUserId();

        SwingUtilities.invokeLater(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {

                PreparedStatement s1 = conn.prepareStatement(
                    "SELECT COALESCE(SUM(total_amount), 0) AS total " +
                    "FROM sales WHERE user_id = ? AND DATE(created_at) = CURDATE()");
                s1.setInt(1, userId);
                ResultSet r1 = s1.executeQuery();
                double todayTotal = r1.next() ? r1.getDouble("total") : 0;

                PreparedStatement s2 = conn.prepareStatement(
                    "SELECT COUNT(*) AS cnt FROM sales " +
                    "WHERE user_id = ? AND DATE(created_at) = CURDATE()");
                s2.setInt(1, userId);
                ResultSet r2 = s2.executeQuery();
                int txCount = r2.next() ? r2.getInt("cnt") : 0;

                PreparedStatement s3 = conn.prepareStatement(
                    "SELECT COUNT(*) AS cnt FROM products " +
                    "WHERE store_id = ? AND stock_quantity > 0 AND stock_quantity <= 5");
                s3.setInt(1, storeId);
                ResultSet r3 = s3.executeQuery();
                int lowCount = r3.next() ? r3.getInt("cnt") : 0;

                PreparedStatement s4 = conn.prepareStatement(
                    "SELECT COUNT(*) AS cnt FROM products " +
                    "WHERE store_id = ? AND stock_quantity = 0");
                s4.setInt(1, storeId);
                ResultSet r4 = s4.executeQuery();
                int outCount = r4.next() ? r4.getInt("cnt") : 0;

                final String salesText = String.format("₱ %,.2f", todayTotal);
                final String txText    = String.valueOf(txCount);
                final String lowText   = String.valueOf(lowCount);
                final String outText   = String.valueOf(outCount);

                SwingUtilities.invokeLater(() -> {
                    if (lblTodaySales   != null) lblTodaySales.setText(salesText);
                    if (lblTransactions != null) lblTransactions.setText(txText);
                    if (lblLowStock     != null) lblLowStock.setText(lowText);
                    if (lblOutOfStock   != null) lblOutOfStock.setText(outText);
                });

            } catch (Exception ex) { ex.printStackTrace(); }
        });
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, LEFT_TOP, 0, getHeight(), LEFT_BOT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillOval(-60, getHeight() - 180, 200, 200);
                g2.dispose();
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setOpaque(false);
        sidebar.setBorder(new EmptyBorder(28, 0, 28, 0));

        JPanel logoArea = new JPanel();
        logoArea.setLayout(new BoxLayout(logoArea, BoxLayout.Y_AXIS));
        logoArea.setOpaque(false);
        logoArea.setBorder(new EmptyBorder(0, 14, 0, 14));
        logoArea.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblSidebarLogo = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                String logoPath = fetchStoreLogo(loggedInUser.getStoreId());
                if (!logoPath.isEmpty()) {
                    try {
                        java.io.File f = new java.io.File(logoPath);
                        if (f.exists()) {
                            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(f);
                            if (img != null) {
                                g2.setClip(new java.awt.geom.Ellipse2D.Float(2, 2, getWidth()-4, getHeight()-4));
                                g2.drawImage(img, 2, 2, getWidth()-4, getHeight()-4, null);
                                g2.setClip(null);
                                g2.setColor(GOLD); g2.setStroke(new BasicStroke(2f));
                                g2.drawOval(2, 2, getWidth()-5, getHeight()-5);
                                g2.dispose(); return;
                            }
                        }
                    } catch (Exception ignored) {}
                }
                g2.setColor(GOLD); g2.fillOval(2, 2, getWidth()-4, getHeight()-4);
                g2.dispose(); super.paintComponent(g);
            }
            @Override public Dimension getPreferredSize() { return new Dimension(72, 72); }
            @Override public Dimension getMinimumSize()   { return new Dimension(72, 72); }
            @Override public Dimension getMaximumSize()   { return new Dimension(72, 72); }
        };
        lblSidebarLogo.setText("\uD83C\uDFEA");
        lblSidebarLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblSidebarLogo.setVerticalAlignment(SwingConstants.CENTER);
        lblSidebarLogo.setFont(new Font("SansSerif", Font.PLAIN, 38));
        lblSidebarLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblSidebarStoreName = new JLabel(storeName);
        lblSidebarStoreName.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblSidebarStoreName.setForeground(Color.WHITE);
        lblSidebarStoreName.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSidebarStoreName.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel posLabel = new JLabel("Inventory & POS");
        posLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        posLabel.setForeground(new Color(255, 255, 255, 160));
        posLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        posLabel.setHorizontalAlignment(SwingConstants.CENTER);

        logoArea.add(lblSidebarLogo);
        logoArea.add(Box.createVerticalStrut(10));
        logoArea.add(lblSidebarStoreName);
        logoArea.add(Box.createVerticalStrut(3));
        logoArea.add(posLabel);

        JPanel logoWrapper = new JPanel(new BorderLayout());
        logoWrapper.setOpaque(false);
        logoWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        logoWrapper.add(logoArea, BorderLayout.CENTER);

        sidebar.add(logoWrapper);
        sidebar.add(Box.createVerticalStrut(24));
        sidebar.add(buildSidebarDivider());
        sidebar.add(Box.createVerticalStrut(16));

        navDashboard = buildNavItem("\uD83D\uDCCA", "Dashboard",      true);
        navInventory = buildNavItem("\uD83D\uDCE6", "Inventory",      false);
        navPOS       = buildNavItem("\uD83D\uDCB0", "POS / Sales",    false);
        navLogs      = buildNavItem("\uD83D\uDCCB", "Inventory Logs", false);
        navSales     = buildNavItem("\uD83D\uDCC8", "Sales History",  false);

        sidebar.add(navDashboard);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(navInventory);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(navPOS);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(navLogs);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(navSales);

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(buildSidebarDivider());
        sidebar.add(Box.createVerticalStrut(12));

        JPanel logoutWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        logoutWrap.setOpaque(false);
        logoutWrap.setBorder(new EmptyBorder(0, 14, 0, 14));
        logoutWrap.add(buildLogoutButton());
        sidebar.add(logoutWrap);

        return sidebar;
    }

    private JPanel buildSidebarDivider() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 18, 0, 18));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        p.setPreferredSize(new Dimension(220, 1));
        p.setBackground(new Color(255, 255, 255, 25));
        return p;
    }

    private JPanel buildNavItem(String emoji, String label, boolean initialActive) {
        final boolean[] hovered = {false};

        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                boolean isActive = activeNav.equals(label);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isActive) {
                    g2.setColor(ACCENT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                } else if (hovered[0]) {
                    g2.setColor(new Color(255, 255, 255, 28));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        item.setOpaque(false);
        item.setBorder(new EmptyBorder(8, 18, 8, 18));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel ico = new JLabel(emoji);
        ico.setFont(new Font("SansSerif", Font.PLAIN, 18));

        JLabel lbl = new JLabel(label) {
            @Override public Font getFont() {
                return activeNav.equals(label)
                    ? FONT_BTN.deriveFont(15f)
                    : new Font("SansSerif", Font.PLAIN, 15);
            }
            @Override public Color getForeground() {
                return activeNav.equals(label)
                    ? Color.WHITE
                    : new Color(255, 255, 255, 180);
            }
        };

        item.add(ico);
        item.add(lbl);

        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered[0] = true;  item.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hovered[0] = false; item.repaint(); }
            @Override public void mouseClicked(MouseEvent e) {
                switch (label) {
                    case "Dashboard":      navigateTo("Dashboard"); refreshDashboardStats(); break;
                    case "Inventory":      navigateTo("Inventory");   break;
                    case "POS / Sales":    openPOS();                 break;
                    case "Inventory Logs": openInventoryLogs();       break;
                    case "Sales History":  openSalesHistory();        break;
                }
            }
        });

        return item;
    }

    private JButton buildLogoutButton() {
        JButton btn = new JButton("Logout") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = new Color(255, 255, 255, 20);
                if (getModel().isRollover() || getModel().isPressed())
                    base = new Color(255, 255, 255, 35);
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(255, 255, 255, 50));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(new Color(255, 255, 255, 200));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(190, 36));
        btn.addActionListener(e -> logout());
        return btn;
    }

    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG);

        JPanel topBar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, LEFT_TOP, getWidth(), getHeight(), LEFT_BOT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(18, 28, 18, 28));

        JLabel pageTitle = new JLabel("Dashboard");
        pageTitle.setFont(FONT_TITLE);
        pageTitle.setForeground(Color.WHITE);

        JLabel greeting = new JLabel("Good day, " + loggedInUser.getFullName() + "!");
        greeting.setFont(FONT_SUB);
        greeting.setForeground(new Color(255, 255, 255, 170));
        lblTopBarGreeting = greeting;

        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);
        titleStack.add(pageTitle);
        titleStack.add(greeting);
        topBar.add(titleStack, BorderLayout.WEST);

        JButton btnProfile = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                Color circleBg = getModel().isRollover()
                    ? new Color(255, 255, 255, 60) : new Color(255, 255, 255, 30);
                g2.setColor(circleBg); g2.fillOval(0, 0, w, h);
                g2.setColor(GOLD); g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(1, 1, w - 3, h - 3);
                int headDiam = w / 3, headX = (w - headDiam) / 2, headY = h / 5;
                g2.setColor(Color.WHITE); g2.fillOval(headX, headY, headDiam, headDiam);
                int bodyW = (int)(w * 0.58), bodyH = (int)(h * 0.36);
                int bodyX = (w - bodyW) / 2, bodyY = h - bodyH - 3;
                Shape oldClip = g2.getClip();
                g2.setClip(0, h / 2, w, h);
                g2.fillOval(bodyX, bodyY, bodyW, bodyH);
                g2.setClip(oldClip);
                g2.dispose();
            }
        };
        btnProfile.setPreferredSize(new Dimension(44, 44));
        btnProfile.setContentAreaFilled(false);
        btnProfile.setBorderPainted(false);
        btnProfile.setFocusPainted(false);
        btnProfile.setOpaque(false);
        btnProfile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnProfile.setToolTipText("Profile — " + loggedInUser.getFullName());
        btnProfile.addActionListener(e -> openProfile());

        JPanel profileWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        profileWrap.setOpaque(false);
        profileWrap.add(btnProfile);
        topBar.add(profileWrap, BorderLayout.EAST);

        main.add(topBar, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentCardPanel = new JPanel(cardLayout);
        contentCardPanel.setBackground(BG);

        contentCardPanel.add(buildDashboardContent(), "Dashboard");
        contentCardPanel.add(buildInventoryContent(), "Inventory");
        contentCardPanel.add(buildSuppliersContent(), "Suppliers");

        main.add(contentCardPanel, BorderLayout.CENTER);
        return main;
    }

    private JScrollPane buildDashboardContent() {
        JPanel contentArea = new JPanel();
        contentArea.setLayout(new BoxLayout(contentArea, BoxLayout.Y_AXIS));
        contentArea.setBackground(BG);
        contentArea.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel heroPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, LEFT_TOP, getWidth(), getHeight(), LEFT_BOT));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(255, 255, 255, 12)); g2.fillOval(getWidth() - 140, -50, 200, 200);
                g2.setColor(new Color(255, 255, 255, 8));  g2.fillOval(getWidth() - 60, getHeight() - 60, 140, 140);
                g2.setColor(new Color(255, 255, 255, 10)); g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(-30, -30, 160, 160);
                g2.dispose();
            }
        };
        heroPanel.setLayout(new BorderLayout());
        heroPanel.setOpaque(false);
        heroPanel.setBorder(new EmptyBorder(24, 28, 24, 32));
        heroPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        heroPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel heroLogo = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,           RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,       RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                int pad = 3;
                int d = Math.min(getWidth(), getHeight()) - pad * 2;
                int x = (getWidth()  - d) / 2;
                int y = (getHeight() - d) / 2;
                String logoPath = fetchStoreLogo(loggedInUser.getStoreId());
                if (!logoPath.isEmpty()) {
                    try {
                        java.io.File f = new java.io.File(logoPath);
                        if (f.exists()) {
                            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(f);
                            if (img != null) {
                                java.awt.geom.Ellipse2D.Float circle =
                                    new java.awt.geom.Ellipse2D.Float(x, y, d, d);
                                g2.setClip(circle);
                                g2.drawImage(img, x, y, d, d, null);
                                g2.setClip(null);
                                g2.setColor(new Color(255, 255, 255, 120));
                                g2.setStroke(new BasicStroke(2.5f));
                                g2.draw(circle);
                                g2.dispose(); return;
                            }
                        }
                    } catch (Exception ignored) {}
                }
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillOval(x, y, d, d);
                g2.setColor(new Color(255, 255, 255, 80));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(x, y, d, d);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public Dimension getPreferredSize() { return new Dimension(90, 90); }
            @Override public Dimension getMinimumSize()   { return new Dimension(90, 90); }
            @Override public Dimension getMaximumSize()   { return new Dimension(90, 90); }
        };
        heroLogo.setText("\uD83C\uDFEA");
        heroLogo.setHorizontalAlignment(SwingConstants.CENTER);
        heroLogo.setVerticalAlignment(SwingConstants.CENTER);
        heroLogo.setFont(new Font("SansSerif", Font.PLAIN, 40));
        heroLogo.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel heroText = new JPanel();
        heroText.setLayout(new BoxLayout(heroText, BoxLayout.Y_AXIS));
        heroText.setOpaque(false);
        heroText.setBorder(new EmptyBorder(0, 24, 0, 0));

        JLabel heroBadge = new JLabel("  " + loggedInUser.getFullName() + "  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose(); super.paintComponent(g);
            }
        };
        heroBadge.setFont(new Font("SansSerif", Font.BOLD, 11));
        heroBadge.setForeground(GOLD);
        heroBadge.setBorder(new EmptyBorder(4, 10, 4, 10));
        heroBadge.setOpaque(false);
        heroBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblHeroWelcome = new JLabel("Welcome to " + storeName + "!");
        lblHeroWelcome.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblHeroWelcome.setForeground(Color.WHITE);
        lblHeroWelcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel heroSub = new JLabel("Your store is ready. Manage inventory, process sales, and track performance.");
        heroSub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        heroSub.setForeground(new Color(255, 255, 255, 170));
        heroSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        heroText.add(heroBadge);
        heroText.add(Box.createVerticalStrut(8));
        heroText.add(lblHeroWelcome);  
        heroText.add(Box.createVerticalStrut(4));
        heroText.add(heroSub);

        heroPanel.add(heroLogo, BorderLayout.WEST);
        heroPanel.add(heroText, BorderLayout.CENTER);

        JPanel heroWrapper = new JPanel(new BorderLayout());
        heroWrapper.setOpaque(false);
        heroWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        heroWrapper.add(heroPanel, BorderLayout.CENTER);
        contentArea.add(heroWrapper);
        contentArea.add(Box.createVerticalStrut(24));

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JPanel salesCard = buildStatCard("₱ 0.00", "Today's Sales",
            "\uD83D\uDCB0", new Color(0xFA, 0xE8, 0xDF), ACCENT);
        lblTodaySales = findValueLabel(salesCard);

        JPanel txCard = buildStatCard("0", "Transactions Today",
            "\uD83D\uDCCB", new Color(0xE1, 0xF0, 0xE8), new Color(0x2E, 0x7D, 0x52));
        lblTransactions = findValueLabel(txCard);

        JPanel lowCard = buildStatCard("0", "Low Stock Items",
            "\uD83D\uDCE6", new Color(0xFF, 0xF4, 0xE0), new Color(0xB0, 0x6E, 0x00));
        lblLowStock = findValueLabel(lowCard);

        JPanel outOfStockCard = buildStatCard("0", "Out of Stock",
            "\uD83D\uDEAB", new Color(0xFD, 0xED, 0xEB), new Color(0x9B, 0x2C, 0x1F));
        lblOutOfStock = findValueLabel(outOfStockCard);

        statsRow.add(salesCard);
        statsRow.add(txCard);
        statsRow.add(lowCard);
        statsRow.add(outOfStockCard);

        contentArea.add(statsRow);
        contentArea.add(Box.createVerticalStrut(24));

        JLabel quickLabel = new JLabel("Quick Actions");
        quickLabel.setFont(FONT_SECTION);
        quickLabel.setForeground(TEXT);
        quickLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentArea.add(quickLabel);
        contentArea.add(Box.createVerticalStrut(14));

        JPanel actionsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        actionsRow.setOpaque(false);
        actionsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        actionsRow.add(buildActionCard("\uD83D\uDCE6", "Inventory\nManagement",
            "Manage products & stock", ACCENT, e -> navigateTo("Inventory")));
        actionsRow.add(buildActionCard("\uD83D\uDCB0", "POS / Sales",
            "Process customer orders", new Color(0x2E, 0x7D, 0x52), e -> openPOS()));
        actionsRow.add(buildActionCard("\uD83D\uDCC8", "Sales History\n& Analytics",
            "View reports & trends", new Color(0xB0, 0x6E, 0x00), e -> openSalesHistory()));
        contentArea.add(actionsRow);
        contentArea.add(Box.createVerticalStrut(16));

        JPanel actionsRow2 = new JPanel(new GridLayout(1, 3, 16, 0));
        actionsRow2.setOpaque(false);
        actionsRow2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        actionsRow2.add(buildActionCard("\uD83D\uDE9A", "Suppliers",
            "Manage your suppliers", new Color(0x5C, 0x35, 0x8A), e -> openSuppliers()));
        actionsRow2.add(buildActionCard("\uD83C\uDFF7\uFE0F", "Categories",
            "Organize product categories", new Color(0x1A, 0x6B, 0x8A), e -> openCategories()));
        actionsRow2.add(buildActionCard("\uD83D\uDDD1\uFE0F", "Disposed Items",
            "Track disposed inventory", new Color(0x7A, 0x3B, 0x3B),
            e -> navigateTo("DisposedItems")));
        contentArea.add(actionsRow2);

        JScrollPane scroll = new JScrollPane(contentArea);
        scroll.setBorder(null);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        return scroll;
    }

    private JLabel findValueLabel(JPanel card) {
        for (Component c : card.getComponents()) {
            if (c instanceof JPanel) {
                JPanel inner = (JPanel) c;
                if (inner.getLayout() instanceof BoxLayout) {
                    for (Component child : inner.getComponents()) {
                        if (child instanceof JLabel) return (JLabel) child;
                    }
                }
            }
        }
        return new JLabel();
    }

    private JPanel buildInventoryContent() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG);
        inventoryPanel = new InventoryPanel(
            loggedInUser,
            () -> navigateTo("Dashboard"),
            () -> navigateTo("DisposedItems")
        );
        wrapper.add(inventoryPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildDisposedContent() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG);
        Disposeditemspanel disposedPanel = new Disposeditemspanel(
            loggedInUser,
            () -> {
                if (inventoryPanel != null) inventoryPanel.refresh();
                navigateTo("Inventory");
            }
        );
        wrapper.add(disposedPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildSuppliersContent() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG);
        suppliersPanel = new SuppliersPanel(loggedInUser, () -> navigateTo("Dashboard"));
        wrapper.add(suppliersPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private void navigateTo(String page) {
        activeNav = page;

        if (navDashboard != null) navDashboard.repaint();
        if (navInventory != null) navInventory.repaint();
        if (navPOS       != null) navPOS.repaint();
        if (navLogs      != null) navLogs.repaint();
        if (navSales     != null) navSales.repaint();

        if ("DisposedItems".equals(page)) {
            for (int i = 0; i < contentCardPanel.getComponentCount(); i++) {
                Component c = contentCardPanel.getComponent(i);
                if ("DisposedItems".equals(c.getName())) {
                    contentCardPanel.remove(c); break;
                }
            }
            JPanel fresh = buildDisposedContent();
            fresh.setName("DisposedItems");
            contentCardPanel.add(fresh, "DisposedItems");
            contentCardPanel.revalidate();
        }

        if ("Dashboard".equals(page)) refreshDashboardStats();

        cardLayout.show(contentCardPanel, page);
    }

    private JPanel buildStatCard(String value, String label, String emoji,
                                 Color bgColor, Color accentColor) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_CLR); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel valLabel = new JLabel(value);
        valLabel.setFont(FONT_CARD_N);
        valLabel.setForeground(TEXT);

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(FONT_CARD_L);
        lblLabel.setForeground(MUTED);

        left.add(valLabel);
        left.add(Box.createVerticalStrut(4));
        left.add(lblLabel);

        JLabel emojiBadge = new JLabel(emoji) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public Dimension getPreferredSize() { return new Dimension(44, 44); }
        };
        emojiBadge.setHorizontalAlignment(SwingConstants.CENTER);
        emojiBadge.setVerticalAlignment(SwingConstants.CENTER);
        emojiBadge.setFont(new Font("SansSerif", Font.PLAIN, 20));

        card.add(left,       BorderLayout.CENTER);
        card.add(emojiBadge, BorderLayout.EAST);
        return card;
    }

    private JPanel buildActionCard(String emoji, String title, String desc,
                                   Color accentColor, ActionListener action) {
        JPanel card = new JPanel() {
            boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) { action.actionPerformed(null); }
                });
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? accentColor : CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                if (!hovered) {
                    g2.setColor(BORDER_CLR); g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                }
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel icoLabel = new JLabel(emoji);
        icoLabel.setFont(new Font("SansSerif", Font.PLAIN, 28));
        icoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] titleLines = title.split("\n");
        JLabel titleLabel = new JLabel("<html>" + String.join("<br>", titleLines) + "</html>");
        titleLabel.setFont(FONT_BTN.deriveFont(15f));
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(FONT_SUB);
        descLabel.setForeground(MUTED);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(icoLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(descLabel);

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                titleLabel.setForeground(Color.WHITE);
                descLabel.setForeground(new Color(255, 255, 255, 180));
            }
            @Override public void mouseExited(MouseEvent e) {
                titleLabel.setForeground(TEXT);
                descLabel.setForeground(MUTED);
            }
        });

        return card;
    }

    private void openPOS() {
        if (!posAdded) {
            contentCardPanel.add(
                new POSPanel(loggedInUser, () -> navigateTo("Dashboard")),
                "POS / Sales"
            );
            posAdded = true;
        }
        navigateTo("POS / Sales");
    }

    private void openInventoryLogs() {
        for (int i = 0; i < contentCardPanel.getComponentCount(); i++) {
            Component c = contentCardPanel.getComponent(i);
            if (c instanceof JComponent && "InventoryLogs".equals(((JComponent) c).getName())) {
                contentCardPanel.remove(c); break;
            }
        }
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG);
        wrapper.setName("InventoryLogs");
        wrapper.add(new InventoryLogPanel(loggedInUser, () -> navigateTo("Dashboard")),
            BorderLayout.CENTER);
        contentCardPanel.add(wrapper, "Inventory Logs");
        contentCardPanel.revalidate();
        navigateTo("Inventory Logs");
    }

    private void openSalesHistory() {
        if (!salesHistoryAdded) {
            contentCardPanel.add(
                new SalesHistoryPanel(loggedInUser, () -> navigateTo("Dashboard")),
                "Sales History"
            );
            salesHistoryAdded = true;
        }
        navigateTo("Sales History");
    }

    private void openSuppliers() { navigateTo("Suppliers"); }

    private void openCategories() {
        if (!categoriesAdded) {
            JPanel catWrapper = new JPanel(new BorderLayout());
            catWrapper.setBackground(BG);
            catWrapper.setName("Categories");
            catWrapper.add(new CategoryPanel(loggedInUser, () -> navigateTo("Dashboard")),
                BorderLayout.CENTER);
            contentCardPanel.add(catWrapper, "Categories");
            contentCardPanel.revalidate();
            categoriesAdded = true;
        }
        navigateTo("Categories");
    }

    private String fetchStoreLogo(Integer storeId) {
        if (storeId == null) return "";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT store_logo FROM store WHERE store_id = ?")) {
            stmt.setInt(1, storeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) { String v = rs.getString("store_logo"); return v != null ? v : ""; }
        } catch (Exception ignored) {}
        return "";
    }

    private void refreshUserDisplay() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT store_name FROM store WHERE store_id = ?")) {
            if (loggedInUser.getStoreId() != null) {
                stmt.setInt(1, loggedInUser.getStoreId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) storeName = rs.getString("store_name");
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        if (lblTopBarGreeting   != null) lblTopBarGreeting.setText("Good day, " + loggedInUser.getFullName() + "!");
        if (lblSidebarStoreName != null) lblSidebarStoreName.setText(storeName);
        if (lblSidebarLogo      != null) lblSidebarLogo.repaint();

        if (lblHeroWelcome      != null) lblHeroWelcome.setText("Welcome to " + storeName + "!");

        repaint();
    }

    private void openProfile() {
        for (int i = 0; i < contentCardPanel.getComponentCount(); i++) {
            Component c = contentCardPanel.getComponent(i);
            if (c instanceof JComponent && "Profile".equals(((JComponent) c).getName())) {
                contentCardPanel.remove(c); break;
            }
        }
        ProfileSettingsPanel panel = new ProfileSettingsPanel(
            loggedInUser,
            () -> navigateTo("Dashboard"),
            () -> refreshUserDisplay()
        );
        panel.setName("Profile");
        contentCardPanel.add(panel, "Profile");
        contentCardPanel.revalidate();
        navigateTo("Profile");
    }

    private void logout() {
        dispose();
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}