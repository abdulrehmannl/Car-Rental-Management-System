// File: src/com/carrental/RegistrationPage.java
package com.carrental;

import java.util.Objects;
import com.carrental.model.User;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;


public class RegistrationPage {

    private JFrame frame;
    private JTextField usernameField;
    private JTextField emailField;
    private JTextField fullNameField;
    private JTextField phoneField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JCheckBox showPassword;
    private JCheckBox termsCheckBox;

    // Validation labels
    private JLabel usernameValidationLabel;
    private JLabel emailValidationLabel;
    private JLabel passwordValidationLabel; // For password mismatch
    private JLabel fullNameValidationLabel;
    private JLabel phoneValidationLabel;

    // REMOVED: private boolean isMaximized = false;
    // REMOVED: private int oldX, oldY, oldWidth, oldHeight;
    // REMOVED: private JButton maximizeButton;

    // THEME COLORS (UPDATED for dark text fields and white text)
    private static final Color THEME_YELLOW = new Color(255, 193, 7);
    private static final Color THEME_YELLOW_DARK = new Color(255, 179, 0);
    private static final Color THEME_DARK_OVERLAY = new Color(0, 0, 0, 180);
    private static final Color THEME_BACKGROUND_FIELD = new Color(30, 30, 30); // Dark background for text fields
    private static final Color THEME_TEXT_PRIMARY = Color.WHITE;
    private static final Color THEME_TEXT_ACCENT = Color.WHITE; // White text on dark fields (used for field text)
    private static final Color THEME_BORDER_COLOR = new Color(70, 70, 70); // Darker border for dark fields
    private static final Color THEME_ERROR_RED = new Color(255, 90, 90); // For validation messages

    // REMOVED: Control button characters
    // REMOVED: private static final String MINIMIZE_CHAR = "\u2014";
    // REMOVED: private static final String MAXIMIZE_CHAR = "\u25A1";
    // REMOVED: private static final String RESTORE_CHAR = "\u25A3";
    // REMOVED: private static final String CLOSE_CHAR = "X";

    public RegistrationPage() {
        frame = new JFrame("Register - The Rental Car");
        frame.setSize(1000, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        // REMOVED: frame.setUndecorated(true); // <--- THIS IS THE KEY CHANGE

        // Set the frame to maximized state by default
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // <--- ADDED THIS

        JPanel rootPanel = new JPanel(new BorderLayout()) {
            private BufferedImage backgroundImage;
            {
                try {
                    backgroundImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/Images/123.jpg")));
                } catch (IOException e) {
                    System.err.println("Background image not found: " + e.getMessage());
                }
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                if (backgroundImage != null) {
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g2d.setColor(new Color(30, 30, 30));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
                g2d.setColor(THEME_DARK_OVERLAY);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        rootPanel.setOpaque(false); // Make sure this is false if painting custom background
        frame.setContentPane(rootPanel);

        // REMOVED: Custom header panel and control buttons as native controls are now used
        // JPanel headerPanel = new JPanel(new BorderLayout());
        // headerPanel.setOpaque(false);
        // headerPanel.setPreferredSize(new Dimension(frame.getWidth(), 40));

        // REMOVED: MouseAdapter for dragging undecorated frame
        // MouseAdapter ma = new MouseAdapter() { ... };
        // headerPanel.addMouseListener(ma);
        // headerPanel.addMouseMotionListener(ma);

        // REMOVED: Button panel and custom control buttons
        // JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
        // buttonPanel.setOpaque(false);
        // JButton minimizeButton = createControlButton(MINIMIZE_CHAR);
        // this.maximizeButton = createControlButton(MAXIMIZE_CHAR);
        // JButton closeButton = createControlButton(CLOSE_CHAR);
        // minimizeButton.addActionListener(e -> frame.setState(Frame.ICONIFIED));
        // this.maximizeButton.addActionListener(e -> toggleMaximize());
        // closeButton.addActionListener(e -> System.exit(0));
        // buttonPanel.add(minimizeButton);
        // buttonPanel.add(this.maximizeButton);
        // buttonPanel.add(closeButton);
        // headerPanel.add(buttonPanel, BorderLayout.EAST);
        // REMOVED: rootPanel.add(headerPanel, BorderLayout.NORTH); // <--- Remove this as well

        JPanel registrationFormPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(THEME_DARK_OVERLAY);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 30, 30));
                g2.dispose();
            }
        };
        registrationFormPanel.setOpaque(false);
        registrationFormPanel.setPreferredSize(new Dimension(700, 600));
        registrationFormPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 50, 50));

        GridBagConstraints gbcMain = new GridBagConstraints();
        gbcMain.gridwidth = GridBagConstraints.REMAINDER;
        gbcMain.fill = GridBagConstraints.HORIZONTAL;
        gbcMain.insets = new Insets(0, 0, 0, 0);

        JLabel titleLabel = new JLabel("Create New Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(THEME_TEXT_PRIMARY);
        registrationFormPanel.add(titleLabel, gbcMain);

        registrationFormPanel.add(Box.createRigidArea(new Dimension(0, 20)), gbcMain);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);

        GridBagConstraints gbcFields = new GridBagConstraints();
        gbcFields.fill = GridBagConstraints.HORIZONTAL;
        gbcFields.weightx = 0.5;
        gbcFields.insets = new Insets(8, 10, 0, 10);

        int row = 0;

        // Row 0: Full Name & Username
        gbcFields.gridx = 0; gbcFields.gridy = row;
        fieldsPanel.add(createLabeledField("Full Name", fullNameField = createTextField("")), gbcFields);
        gbcFields.gridx = 1; gbcFields.gridy = row;
        fieldsPanel.add(createLabeledField("Username", usernameField = createTextField("")), gbcFields);
        row++;

        gbcFields.gridx = 0; gbcFields.gridy = row; gbcFields.insets = new Insets(0, 10, 5, 10);
        fullNameValidationLabel = createValidationLabel();
        fieldsPanel.add(fullNameValidationLabel, gbcFields);
        gbcFields.gridx = 1; gbcFields.gridy = row;
        usernameValidationLabel = createValidationLabel();
        fieldsPanel.add(usernameValidationLabel, gbcFields);
        row++;

        gbcFields.insets = new Insets(8, 10, 0, 10);

        // Row 1: Email & Phone Number
        gbcFields.gridx = 0; gbcFields.gridy = row;
        fieldsPanel.add(createLabeledField("Email", emailField = createTextField("")), gbcFields);
        gbcFields.gridx = 1; gbcFields.gridy = row;
        fieldsPanel.add(createLabeledField("Phone Number", phoneField = createTextField("")), gbcFields);
        row++;

        gbcFields.gridx = 0; gbcFields.gridy = row; gbcFields.insets = new Insets(0, 10, 5, 10);
        emailValidationLabel = createValidationLabel();
        fieldsPanel.add(emailValidationLabel, gbcFields);
        gbcFields.gridx = 1; gbcFields.gridy = row;
        phoneValidationLabel = createValidationLabel();
        fieldsPanel.add(phoneValidationLabel, gbcFields);
        row++;

        gbcFields.insets = new Insets(8, 10, 0, 10);

        // Row 2: Password & Confirm Password
        gbcFields.gridx = 0; gbcFields.gridy = row; gbcFields.gridwidth = 2;
        fieldsPanel.add(createLabeledField("Password", passwordField = createPasswordField("")), gbcFields);
        row++;

        gbcFields.gridx = 0; gbcFields.gridy = row; gbcFields.gridwidth = 2; gbcFields.insets = new Insets(0, 10, 5, 10);
        passwordValidationLabel = createValidationLabel();
        fieldsPanel.add(passwordValidationLabel, gbcFields);
        row++;

        gbcFields.gridx = 0; gbcFields.gridy = row; gbcFields.gridwidth = 2; gbcFields.insets = new Insets(8, 10, 0, 10);
        fieldsPanel.add(createLabeledField("Confirm Password", confirmPasswordField = createPasswordField("")), gbcFields);
        row++;

        // Add the fieldsPanel to the main registrationFormPanel
        gbcMain.gridwidth = GridBagConstraints.REMAINDER;
        gbcMain.fill = GridBagConstraints.HORIZONTAL;
        gbcMain.insets = new Insets(0, 0, 0, 0);
        registrationFormPanel.add(fieldsPanel, gbcMain);

        // --- Checkboxes and Buttons (Below Two-Column Fields) ---
        gbcMain.insets = new Insets(10, 0, 5, 0);
        gbcMain.fill = GridBagConstraints.NONE;
        gbcMain.anchor = GridBagConstraints.CENTER;

        showPassword = new JCheckBox("Show password");
        showPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        showPassword.setForeground(THEME_TEXT_PRIMARY);
        showPassword.setOpaque(false);
        showPassword.addActionListener(e -> {
            char echoChar = showPassword.isSelected() ? '\0' : '•';
            passwordField.setEchoChar(echoChar);
            confirmPasswordField.setEchoChar(echoChar);
        });
        registrationFormPanel.add(showPassword, gbcMain);

        termsCheckBox = new JCheckBox("I agree to the Terms & Conditions");
        termsCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        termsCheckBox.setForeground(THEME_TEXT_PRIMARY);
        termsCheckBox.setOpaque(false);
        gbcMain.insets = new Insets(5, 0, 20, 0);
        registrationFormPanel.add(termsCheckBox, gbcMain);

        JButton registerButton = createStyledButton("Create", THEME_YELLOW);
        gbcMain.insets = new Insets(0, 0, 0, 0);
        gbcMain.fill = GridBagConstraints.HORIZONTAL;

        registrationFormPanel.add(registerButton, gbcMain);

        JButton backToLoginButton = createTextButton("Already have an account? Login here");
        gbcMain.insets = new Insets(10, 0, 0, 0);
        backToLoginButton.addActionListener(e -> {
            frame.dispose();
            new LoginPage();
        });
        registrationFormPanel.add(backToLoginButton, gbcMain);

        JPanel centerWrapperPanel = new JPanel(new GridBagLayout());
        centerWrapperPanel.setOpaque(false);
        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.gridx = 0; gbcForm.gridy = 0; gbcForm.weightx = 0.0;
        gbcForm.weighty = 0.0; gbcForm.fill = GridBagConstraints.NONE;
        gbcForm.anchor = GridBagConstraints.CENTER;
        centerWrapperPanel.add(registrationFormPanel, gbcForm);
        rootPanel.add(centerWrapperPanel, BorderLayout.CENTER);

        registerButton.addActionListener(e -> handleRegistration());

        fullNameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validateFullName();
            }
        });
        usernameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validateUsername();
            }
        });
        emailField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validateEmail();
            }
        });
        phoneField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validatePhone();
            }
        });
        confirmPasswordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validatePasswords();
            }
        });
        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validatePasswords();
            }
        });

        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                // Apply rounded corners only when not maximized
                if (frame.getExtendedState() == JFrame.NORMAL) { // <--- MODIFIED CONDITION
                    frame.setShape(new RoundRectangle2D.Double(0, 0, frame.getWidth(), frame.getHeight(), 20, 20));
                } else {
                    frame.setShape(null); // Remove custom shape when maximized
                }
            }
        });

        // Apply initial rounded corners if not maximized by default (though we are now maximizing)
        // This will be handled by the componentResized listener when the frame is restored
        // frame.setShape(new RoundRectangle2D.Double(0, 0, frame.getWidth(), frame.getHeight(), 20, 20));
        frame.setVisible(true);
    }

    private void handleRegistration() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phoneNumber = phoneField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        boolean isValid = true;

        // Reset all validation labels
        fullNameValidationLabel.setText("");
        usernameValidationLabel.setText("");
        emailValidationLabel.setText("");
        phoneValidationLabel.setText("");
        passwordValidationLabel.setText("");

        // Validate Full Name
        if (fullName.isEmpty()) {
            fullNameValidationLabel.setText("Full name cannot be empty.");
            isValid = false;
        }

        // Validate Username
        if (username.isEmpty()) {
            usernameValidationLabel.setText("Username cannot be empty.");
            isValid = false;
        } else if (LoginPage.users.stream().anyMatch(u -> u.getUsername().equals(username))) {
            usernameValidationLabel.setText("Username already taken.");
            isValid = false;
        }

        // Validate Email
        if (email.isEmpty()) {
            emailValidationLabel.setText("Email cannot be empty.");
            isValid = false;
        } else if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            emailValidationLabel.setText("Invalid email format.");
            isValid = false;
        }

        // Validate Phone Number (UPDATED: 11 digits exact match)
        if (phoneNumber.isEmpty()) {
            phoneValidationLabel.setText("Phone number cannot be empty.");
            isValid = false;
        } else if (!phoneNumber.matches("^\\d{11}$")) { // Exactly 11 digits
            phoneValidationLabel.setText("Phone number must be 11 digits.");
            isValid = false;
        }

        // Validate Passwords
        if (password.isEmpty()) {
            passwordValidationLabel.setText("Password cannot be empty.");
            isValid = false;
        } else if (confirmPassword.isEmpty()) {
            passwordValidationLabel.setText("Confirm password cannot be empty.");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            passwordValidationLabel.setText("Passwords do not match.");
            isValid = false;
        }

        if (!termsCheckBox.isSelected()) {
            JOptionPane.showMessageDialog(frame, "You must agree to the Terms & Conditions.", "Registration Error", JOptionPane.WARNING_MESSAGE);
            isValid = false;
        }

        if (isValid) {
            User newUser = new User(username, password, "User", fullName, phoneNumber, email); // Default role "User"

            LoginPage.users.add(newUser);
            LoginPage.saveUsers(LoginPage.users);

            System.out.println("New user added: " + username + " (Full Name: " + fullName + ", Phone: " + phoneNumber + ", Email: " + email + ")");

            JOptionPane.showMessageDialog(frame, "Registration successful! You can now login.", "Success", JOptionPane.INFORMATION_MESSAGE);
            frame.dispose();
            new LoginPage();
        }
    }

    private void validateFullName() {
        String fullName = fullNameField.getText().trim();
        if (fullName.isEmpty()) {
            fullNameValidationLabel.setText(""); // Clear if empty
        } else {
            fullNameValidationLabel.setText("Looks good.");
            fullNameValidationLabel.setForeground(new Color(120, 255, 120));
        }
    }

    private void validateUsername() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            usernameValidationLabel.setText(""); // Clear if empty
        } else if (LoginPage.users.stream().anyMatch(u -> u.getUsername().equals(username))) {
            usernameValidationLabel.setText("Username already taken.");
            usernameValidationLabel.setForeground(THEME_ERROR_RED);
        } else {
            usernameValidationLabel.setText("Username is valid.");
            usernameValidationLabel.setForeground(new Color(120, 255, 120));
        }
    }

    private void validateEmail() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            emailValidationLabel.setText(""); // Clear if empty
        } else if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            emailValidationLabel.setText("Invalid email format.");
            emailValidationLabel.setForeground(THEME_ERROR_RED);
        } else {
            emailValidationLabel.setText("Email format is valid.");
            emailValidationLabel.setForeground(new Color(120, 255, 120));
        }
    }

    private void validatePhone() {
        String phoneNumber = phoneField.getText().trim();
        if (phoneNumber.isEmpty()) {
            phoneValidationLabel.setText(""); // Clear if empty
        } else if (!phoneNumber.matches("^\\d{11}$")) { // Exactly 11 digits for validation
            phoneValidationLabel.setText("Phone number must be 11 digits.");
            phoneValidationLabel.setForeground(THEME_ERROR_RED);
        } else {
            phoneValidationLabel.setText("Looks good.");
            phoneValidationLabel.setForeground(new Color(120, 255, 120));
        }
    }

    private void validatePasswords() {
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (password.isEmpty() || confirmPassword.isEmpty()) {
            passwordValidationLabel.setText("");
        } else if (!password.equals(confirmPassword)) {
            passwordValidationLabel.setText("Passwords do not match.");
            passwordValidationLabel.setForeground(THEME_ERROR_RED);
        } else {
            passwordValidationLabel.setText("");
        }
    }

    private JLabel createValidationLabel() {
        JLabel label = new JLabel("");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(THEME_ERROR_RED);
        label.setPreferredSize(new Dimension(200, 15));
        label.setMinimumSize(new Dimension(200, 15));
        label.setMaximumSize(new Dimension(200, 15));
        return label;
    }

    // REMOVED: private void toggleMaximize() - not needed with native controls
    // REMOVED: private JButton createControlButton(String text) - not needed with native controls

    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(250, 40));
        field.setMaximumSize(new Dimension(250, 40));
        field.setForeground(THEME_TEXT_ACCENT);
        field.setBackground(THEME_BACKGROUND_FIELD);
        field.setCaretColor(THEME_TEXT_ACCENT);
        field.putClientProperty("JTextField.placeholderText", placeholder);
        Border defaultBorder = new CompoundBorder(new LineBorder(THEME_BORDER_COLOR, 1), new EmptyBorder(10, 10, 10, 10));
        Border focusBorder = new CompoundBorder(new LineBorder(THEME_YELLOW, 2), new EmptyBorder(9, 9, 9, 9));
        field.setBorder(defaultBorder);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { field.setBorder(focusBorder); }
            @Override public void focusLost(FocusEvent e) { field.setBorder(defaultBorder); }
        });
        field.setOpaque(true);
        return field;
    }

    private JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(250, 40));
        field.setMaximumSize(new Dimension(250, 40));
        field.setForeground(THEME_TEXT_ACCENT);
        field.setBackground(THEME_BACKGROUND_FIELD);
        field.setCaretColor(THEME_TEXT_ACCENT); field.setEchoChar('•');
        field.putClientProperty("JTextField.placeholderText", placeholder);
        Border defaultBorder = new CompoundBorder(new LineBorder(THEME_BORDER_COLOR, 1), new EmptyBorder(10, 10, 10, 10));
        Border focusBorder = new CompoundBorder(new LineBorder(THEME_YELLOW, 2), new EmptyBorder(9, 9, 9, 9));
        field.setBorder(defaultBorder);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { field.setBorder(focusBorder); }
            @Override public void focusLost(FocusEvent e) { field.setBorder(defaultBorder); }
        });
        field.setOpaque(true);
        return field;
    }

    private JPanel createLabeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(THEME_TEXT_PRIMARY);
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        if (color.equals(THEME_YELLOW)) {
            button.setForeground(Color.BLACK);
        } else {
            button.setForeground(THEME_TEXT_PRIMARY);
        }
        button.setBackground(color); button.setOpaque(true); button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        button.setFocusPainted(false); button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        Color darkerColor = THEME_YELLOW_DARK;
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(darkerColor); }
            public void mouseExited(MouseEvent e) { button.setBackground(color); }
        });
        return button;
    }

    private JButton createTextButton(String text) {
        class HoverButton extends JButton {
            private boolean hovered = false;
            public HoverButton(String text) {
                super(text); setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setForeground(THEME_YELLOW); setContentAreaFilled(false); setBorderPainted(false);
                setFocusPainted(false); setCursor(new Cursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; setForeground(THEME_YELLOW_DARK); repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hovered = false; setForeground(THEME_YELLOW); repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (hovered) {
                    Graphics2D g2 = (Graphics2D) g.create(); g2.setColor(getForeground());
                    FontMetrics fm = g2.getFontMetrics(getFont()); int textWidth = fm.stringWidth(getText());
                    int textX = (getWidth() - textWidth) / 2; int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawLine(textX, textY + 2, textX + textWidth, textY + 2); g2.dispose();
                }
            }
        }
        return new HoverButton(text);
    }

    public static void main(String[] args) {
        if (LoginPage.users == null) {
            LoginPage.users = new ArrayList<>();
            LoginPage.users.add(new User("existinguser", "password123", "User", "Existing User", "01234567890", "existing@example.com"));
        }
        LoginPage.loadUsers();

        SwingUtilities.invokeLater(() -> new RegistrationPage());
    }
}