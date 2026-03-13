package view;

import dao.userDAO;
import model.User;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class RegisterFrame extends JFrame {

    private static final Color ACCENT      = new Color(0xC7, 0x4B, 0x1A);
    private static final Color ACCENT_DARK = new Color(0x8F, 0x31, 0x10);
    private static final Color LEFT_TOP    = new Color(0xB0, 0x3A, 0x12);
    private static final Color LEFT_BOT    = new Color(0x4A, 0x18, 0x08);
    private static final Color CARD        = Color.WHITE;
    private static final Color TEXT        = new Color(0x1A, 0x14, 0x10);
    private static final Color MUTED       = new Color(0x6B, 0x5E, 0x52);
    private static final Color BORDER_CLR  = new Color(0xD9, 0xCF, 0xC4);
    private static final Color INPUT_BG    = new Color(0xFA, 0xF7, 0xF3);
    private static final Color GOLD        = new Color(0xFF, 0xD5, 0x80);

    private static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD,  28);
    private static final Font FONT_SUB   = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_LABEL = new Font("SansSerif", Font.BOLD,  11);
    private static final Font FONT_INPUT = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font FONT_BTN   = new Font("SansSerif", Font.BOLD,  14);

    private JTextField     txtUsername;
    private JTextField     txtFullName;
    private JTextField     txtStoreName;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JButton        btnRegister;
    private JButton        btnBack;

    public RegisterFrame() {
        setTitle("Sari-Sari Store POS - Register");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(980, 640);
        setMinimumSize(new Dimension(780, 520));
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel root = new JPanel(new GridLayout(1, 2));
        setContentPane(root);
        root.add(buildLeftPanel());
        root.add(buildRightPanel());

        setVisible(true);
    }

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, LEFT_TOP, getWidth(), getHeight(), LEFT_BOT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 18));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(-80, -80, 340, 340);
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillOval(getWidth() - 130, getHeight() - 130, 220, 220);
                g2.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(40, 36, 40, 36));

        panel.add(Box.createVerticalGlue());

        JLabel icon = new JLabel("\uD83C\uDFEA") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(GOLD);
                g2.fillOval(4, 4, getWidth() - 8, getHeight() - 8);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public Dimension getPreferredSize() { return new Dimension(80, 80); }
            @Override public Dimension getMinimumSize()   { return new Dimension(80, 80); }
            @Override public Dimension getMaximumSize()   { return new Dimension(80, 80); }
        };
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setVerticalAlignment(SwingConstants.CENTER);
        icon.setFont(new Font("SansSerif", Font.PLAIN, 38));
        icon.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(icon);
        panel.add(Box.createVerticalStrut(20));

        JLabel h1a = new JLabel("Sari-Sari Store");
        h1a.setFont(FONT_TITLE.deriveFont(Font.BOLD, 42f));
        h1a.setForeground(Color.WHITE);
        h1a.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel h1b = new JLabel("Inventory & POS System");
        h1b.setFont(FONT_TITLE.deriveFont(Font.BOLD, 42f));
        h1b.setForeground(GOLD);
        h1b.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("<html>Create your account to start<br>managing your store today.</html>");
        sub.setFont(FONT_SUB);
        sub.setForeground(new Color(255, 255, 255, 155));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setBorder(new EmptyBorder(10, 0, 0, 0));

        panel.add(h1a);
        panel.add(h1b);
        panel.add(sub);

        panel.add(Box.createVerticalGlue());

        JPanel steps = buildStepsHint();
        steps.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(steps);

        return panel;
    }

    private JPanel buildStepsHint() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        String[] steps = { "Create your account", "Set up your store", "Start selling!" };
        for (int i = 0; i < steps.length; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            row.setOpaque(false);

            final int num = i + 1;
            JLabel circle = new JLabel(String.valueOf(num)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.fillOval(0, 0, 24, 24);
                    g2.dispose();
                    super.paintComponent(g);
                }
                @Override public Dimension getPreferredSize() { return new Dimension(24, 24); }
            };
            circle.setHorizontalAlignment(SwingConstants.CENTER);
            circle.setFont(new Font("SansSerif", Font.BOLD, 11));
            circle.setForeground(GOLD);

            JLabel lbl = new JLabel(steps[i]);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
            lbl.setForeground(new Color(255, 255, 255, 180));

            row.add(circle);
            row.add(lbl);
            p.add(row);
            if (i < steps.length - 1) p.add(Box.createVerticalStrut(6));
        }
        return p;
    }

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD);
        panel.setLayout(new GridBagLayout());

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel title = new JLabel("Create Account");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Fill in your details to get started");
        subtitle.setFont(FONT_SUB);
        subtitle.setForeground(MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(4, 0, 22, 0));

        form.add(title);
        form.add(subtitle);

        form.add(buildFieldLabel("FULL NAME"));
        txtFullName = new RoundTextField(14);
        txtFullName.setFont(FONT_INPUT);
        styleInput(txtFullName);
        form.add(txtFullName);
        form.add(Box.createVerticalStrut(12));

        form.add(buildFieldLabel("STORE NAME"));
        txtStoreName = new RoundTextField(14);
        txtStoreName.setFont(FONT_INPUT);
        styleInput(txtStoreName);
        form.add(txtStoreName);
        form.add(Box.createVerticalStrut(12));

        form.add(buildFieldLabel("USERNAME"));
        txtUsername = new RoundTextField(14);
        txtUsername.setFont(FONT_INPUT);
        styleInput(txtUsername);
        form.add(txtUsername);
        form.add(Box.createVerticalStrut(12));

        form.add(buildFieldLabel("PASSWORD"));
        txtPassword = new JPasswordField(14);
        txtPassword.setFont(FONT_INPUT);
        styleInput(txtPassword);
        form.add(txtPassword);
        form.add(Box.createVerticalStrut(20));

        btnRegister = buildButton("Create Account", ACCENT, Color.WHITE, true);
        btnRegister.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRegister.addActionListener(e -> registerUser());
        form.add(btnRegister);
        form.add(Box.createVerticalStrut(10));

        form.add(buildDivider());
        form.add(Box.createVerticalStrut(10));

        btnBack = buildButton("Back to Login", CARD, ACCENT, false);
        btnBack.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnBack.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(LoginFrame::new);
        });
        form.add(btnBack);

        panel.add(form);
        return panel;
    }

    private JLabel buildFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 5, 0));
        return lbl;
    }

    private void styleInput(JTextField field) {
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT);
        field.setCaretColor(ACCENT);
        field.setPreferredSize(new Dimension(340, 44));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(BORDER_CLR, 10, 1.5f),
            new EmptyBorder(0, 14, 0, 14)
        ));
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(ACCENT, 10, 1.5f),
                    new EmptyBorder(0, 14, 0, 14)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(BORDER_CLR, 10, 1.5f),
                    new EmptyBorder(0, 14, 0, 14)
                ));
            }
        });
    }

    private JButton buildButton(String text, Color bg, Color fg, boolean filled) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = filled ? bg : CARD;
                if (getModel().isPressed())
                    base = filled ? ACCENT_DARK : new Color(0xFA, 0xE8, 0xDF);
                else if (getModel().isRollover())
                    base = filled ? ACCENT_DARK : new Color(0xFA, 0xE8, 0xDF);
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                if (!filled) {
                    g2.setColor(ACCENT);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(340, 48));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        return btn;
    }

    private JPanel buildDivider() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        JSeparator left = new JSeparator();
        left.setForeground(BORDER_CLR);
        p.add(left, c);

        c.weightx = 0;
        c.insets = new Insets(0, 10, 0, 10);
        JLabel or = new JLabel("or");
        or.setFont(FONT_SUB);
        or.setForeground(MUTED);
        p.add(or, c);

        c.weightx = 1;
        c.insets = new Insets(0, 0, 0, 0);
        JSeparator right = new JSeparator();
        right.setForeground(BORDER_CLR);
        p.add(right, c);

        return p;
    }

    private void registerUser() {
        String username  = txtUsername.getText().trim();
        String password  = new String(txtPassword.getPassword()).trim();
        String fullName  = txtFullName.getText().trim();
        String storeName = txtStoreName.getText().trim();

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || storeName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!", "Missing Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setFullName(fullName);

        userDAO userDAO = new userDAO();
        boolean success = userDAO.createUserWithStore(user, storeName);

        if (success) {
            JOptionPane.showMessageDialog(this, "Account created successfully! Please log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            SwingUtilities.invokeLater(LoginFrame::new);
        } else {
            JOptionPane.showMessageDialog(this, "Registration failed. Username might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static class RoundTextField extends JTextField {
        public RoundTextField(int cols) { super(cols); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class RoundBorder extends AbstractBorder {
        private final Color color;
        private final int radius;
        private final float thickness;
        RoundBorder(Color color, int radius, float thickness) {
            this.color = color; this.radius = radius; this.thickness = thickness;
        }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
            g2.dispose();
        }
        @Override
        public Insets getBorderInsets(Component c) { return new Insets(1, 1, 1, 1); }
    }
}