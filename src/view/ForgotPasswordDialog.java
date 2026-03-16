package view;

import config.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Two-step forgot-password dialog:
 *   Step 1 — user enters their phone number; we look up the matching account.
 *   Step 2 — user sets a new password (with confirmation).
 */
public class ForgotPasswordDialog extends JDialog {

    private static final Color ACCENT     = new Color(0xC7, 0x4B, 0x1A);
    private static final Color LEFT_TOP   = new Color(0xB0, 0x3A, 0x12);
    private static final Color LEFT_BOT   = new Color(0x4A, 0x18, 0x08);
    private static final Color CARD       = Color.WHITE;
    private static final Color TEXT       = new Color(0x1A, 0x14, 0x10);
    private static final Color MUTED      = new Color(0x6B, 0x5E, 0x52);
    private static final Color BORDER_CLR = new Color(0xD9, 0xCF, 0xC4);
    private static final Color INPUT_BG   = new Color(0xFA, 0xF7, 0xF3);
    private static final Color SUCCESS    = new Color(0x2E, 0x7D, 0x52);
    private static final Color ERROR_CLR  = new Color(0x9B, 0x2C, 0x1F);

    private static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD,  18);
    private static final Font FONT_SUB   = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_LABEL = new Font("SansSerif", Font.BOLD,  11);
    private static final Font FONT_INPUT = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font FONT_BTN   = new Font("SansSerif", Font.BOLD,  13);

    private int    foundUserId   = -1;
    private String foundFullName = "";

    private JPanel     stepPhone;
    private JPanel     stepReset;
    private CardLayout cards;
    private JPanel     cardHost;

    private JTextField   txtPhone;
    private JLabel       lblPhoneError;

    private JLabel        lblFoundUser;
    private JPasswordField txtNewPass;
    private JPasswordField txtConfirmPass;
    private JLabel        lblResetError;

    public ForgotPasswordDialog(Frame owner) {
        super(owner, "Forgot Password", true);
        setSize(440, 460);
        setResizable(false);
        setLocationRelativeTo(owner);
        setBackground(CARD);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD);
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);

        cards    = new CardLayout();
        cardHost = new JPanel(cards);
        cardHost.setBackground(CARD);

        stepPhone = buildStepPhone();
        stepReset = buildStepReset();
        cardHost.add(stepPhone, "PHONE");
        cardHost.add(stepReset, "RESET");
        root.add(cardHost, BorderLayout.CENTER);

        cards.show(cardHost, "PHONE");
    }

    private JPanel buildHeader() {
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

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel title = new JLabel("🔐 Forgot Password");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Enter your registered phone number to reset your password");
        sub.setFont(FONT_SUB);
        sub.setForeground(new Color(255, 255, 255, 170));

        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(sub);
        header.add(left, BorderLayout.CENTER);
        return header;
    }

    private JPanel buildStepPhone() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD);
        panel.setBorder(new EmptyBorder(28, 32, 28, 32));

        JLabel stepLabel = new JLabel("STEP 1 OF 2  —  FIND YOUR ACCOUNT");
        stepLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        stepLabel.setForeground(MUTED);
        stepLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(stepLabel);
        panel.add(Box.createVerticalStrut(14));

        JLabel lbl = makeLabel("Phone Number");
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(6));

        txtPhone = buildField("e.g. +63 912 345 6789");
        panel.add(txtPhone);
        panel.add(Box.createVerticalStrut(6));

        lblPhoneError = new JLabel(" ");
        lblPhoneError.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblPhoneError.setForeground(ERROR_CLR);
        lblPhoneError.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblPhoneError);

        panel.add(Box.createVerticalGlue());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton btnCancel = buildOutlineButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnNext = buildFilledButton("Find Account →", ACCENT, Color.WHITE);
        btnNext.addActionListener(e -> doPhoneLookup());

        // Allow Enter key to trigger lookup
        txtPhone.addActionListener(e -> doPhoneLookup());

        btnRow.add(btnCancel);
        btnRow.add(btnNext);
        panel.add(btnRow);
        return panel;
    }

    private JPanel buildStepReset() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD);
        panel.setBorder(new EmptyBorder(20, 32, 28, 32));

        JLabel stepLabel = new JLabel("STEP 2 OF 2  —  SET NEW PASSWORD");
        stepLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        stepLabel.setForeground(MUTED);
        stepLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(stepLabel);
        panel.add(Box.createVerticalStrut(10));

        lblFoundUser = new JLabel();
        lblFoundUser.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblFoundUser.setForeground(SUCCESS);
        lblFoundUser.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblFoundUser.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, SUCCESS),
            new EmptyBorder(6, 10, 6, 10)
        ));
        panel.add(lblFoundUser);
        panel.add(Box.createVerticalStrut(16));

        panel.add(makeLabel("New Password"));
        panel.add(Box.createVerticalStrut(6));
        txtNewPass = buildPasswordField();
        panel.add(txtNewPass);
        panel.add(Box.createVerticalStrut(12));

        panel.add(makeLabel("Confirm New Password"));
        panel.add(Box.createVerticalStrut(6));
        txtConfirmPass = buildPasswordField();
        panel.add(txtConfirmPass);
        panel.add(Box.createVerticalStrut(6));

        lblResetError = new JLabel(" ");
        lblResetError.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblResetError.setForeground(ERROR_CLR);
        lblResetError.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblResetError);

        panel.add(Box.createVerticalGlue());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton btnBack = buildOutlineButton("← Back");
        btnBack.addActionListener(e -> {
            lblPhoneError.setText(" ");
            cards.show(cardHost, "PHONE");
            setTitle("Forgot Password");
        });

        JButton btnSave = buildFilledButton("Reset Password", ACCENT, Color.WHITE);
        btnSave.addActionListener(e -> doPasswordReset());

        txtConfirmPass.addActionListener(e -> doPasswordReset());

        btnRow.add(btnBack);
        btnRow.add(btnSave);
        panel.add(btnRow);
        return panel;
    }

    private void doPhoneLookup() {
        String phone = txtPhone.getText().trim();
        if (phone.isEmpty()) {
            lblPhoneError.setText("Please enter your phone number.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT user_id, full_name FROM users WHERE phone_number = ?")) {
            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                foundUserId   = rs.getInt("user_id");
                foundFullName = rs.getString("full_name");
                lblFoundUser.setText("✓  Account found: " + foundFullName);
                lblResetError.setText(" ");
                txtNewPass.setText("");
                txtConfirmPass.setText("");
                cards.show(cardHost, "RESET");
            } else {
                lblPhoneError.setText("No account found with that phone number.");
                foundUserId = -1;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            lblPhoneError.setText("Database error. Please try again.");
        }
    }

    private void doPasswordReset() {
        String newPass     = new String(txtNewPass.getPassword()).trim();
        String confirmPass = new String(txtConfirmPass.getPassword()).trim();

        if (newPass.isEmpty()) {
            lblResetError.setText("New password cannot be empty.");
            return;
        }
        if (newPass.length() < 6) {
            lblResetError.setText("Password must be at least 6 characters.");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            lblResetError.setText("Passwords do not match.");
            return;
        }
        if (foundUserId == -1) {
            lblResetError.setText("Session error. Please go back and try again.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE users SET password = ? WHERE user_id = ?")) {
            stmt.setString(1, dao.userDAO.hashPassword(newPass));
            stmt.setInt(2, foundUserId);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this,
                "Password reset successfully!\nYou can now log in with your new password.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
            lblResetError.setText("Failed to reset password. Please try again.");
        }
    }

    private JTextField buildField(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(isFocusOwner() ? ACCENT : BORDER_CLR);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 1.5f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setFont(FONT_INPUT);
        f.setForeground(TEXT);
        f.setOpaque(false);
        f.setBorder(new EmptyBorder(10, 14, 10, 14));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setToolTipText(placeholder);
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { f.repaint(); }
            @Override public void focusLost(FocusEvent e)   { f.repaint(); }
        });
        return f;
    }

    private JPasswordField buildPasswordField() {
        JPasswordField f = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(isFocusOwner() ? ACCENT : BORDER_CLR);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 1.5f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setFont(FONT_INPUT);
        f.setForeground(TEXT);
        f.setOpaque(false);
        f.setBorder(new EmptyBorder(10, 14, 10, 14));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { f.repaint(); }
            @Override public void focusLost(FocusEvent e)   { f.repaint(); }
        });
        return f;
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
        btn.setBorder(new EmptyBorder(9, 22, 9, 22));
        return btn;
    }

    private JButton buildOutlineButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                    ? new Color(0xFA, 0xE8, 0xDF) : new Color(0xF5, 0xF0, 0xE8));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_CLR);
                g2.setStroke(new BasicStroke(1f));
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
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        return btn;
    }
}