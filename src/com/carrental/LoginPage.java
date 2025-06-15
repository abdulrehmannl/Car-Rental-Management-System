// File: src/com/carrental/LoginPage.java
package com.carrental;

import com.carrental.model.User;
import com.carrental.model.Car;
import java.util.List;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;

public class LoginPage extends JFrame {

    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JToggleButton userToggleButton;
    private JToggleButton adminToggleButton;
    private JButton forgotPasswordButton;
    private JButton minimizeButton;
    private JButton maximizeButton; // Made class member for access in toggleMaximize
    private JButton closeButton;

    private boolean isMaximized = false;
    private int oldX, oldY, oldWidth, oldHeight; // Store previous bounds for restore

    private Point initialClick;

    private static final String MINIMIZE_CHAR = "\u2014";
    private static final String MAXIMIZE_CHAR_ICON = "\u25A1"; // White Square (for maximize)
    private static final String RESTORE_CHAR_ICON = "\u25A3";  // White Square with Black Square (for restore)
    private static final String CLOSE_CHAR = "X";

    public static List<User> users = new ArrayList<>();
    public static List<Car> allCars = new ArrayList<>();

    public static User loggedInUser = null;
    public static String loggedInUserRole = null;

    private static final String USER_DATA_FILE = "users.dat";

    private static final Color THEME_YELLOW = new Color(255, 193, 7);
    private static final Color THEME_YELLOW_DARK = new Color(255, 179, 0);
    private static final Color THEME_DARK_OVERLAY = new Color(0, 0, 0, 180);
    private static final Color THEME_BACKGROUND_STATIC_OVERLAY = new Color(0, 0, 0, 120);
    private static final Color THEME_TEXT_PRIMARY = Color.WHITE;
    private static final Color THEME_TEXT_ACCENT = Color.BLACK;
    private static final Color THEME_LIGHT_GRAY_BG = new Color(250, 250, 250);
    private static final Color THEME_BORDER_COLOR = new Color(200, 200, 200);
    private static final Color THEME_RED_BUTTON = new Color(220, 53, 69);
    private static final Color THEME_BLUE_BUTTON = new Color(66, 133, 244);
    private static final Color THEME_BLUE_BUTTON_DARK = new Color(50, 100, 200);

    private BufferedImage backgroundImage;
    private ImageIcon appIcon;

    static {
        users = loadUsers();
        if (users.isEmpty()) {
            users.add(new User("user", "1", "User", "John Doe", "03001234567", "john.doe@example.com"));
            users.add(new User("testuser", "password123", "User", "Jane Smith", "03119876543", "jane.smith@example.com"));
            users.add(new User("admin", "1", "Admin", "Admin User", "03221122334", "admin@example.com"));
            saveUsers(users);
        }
    }

    public LoginPage() {
        frame = new JFrame("Car Rental System - Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // frame.setSize(900, 600); // Removed fixed size for initial setting
        frame.setUndecorated(true);

        // --- Initialize old bounds to a sensible default restored size/position ---
        // These values will be used when restoring from the initial maximized state.
        oldWidth = 900;
        oldHeight = 600;
        frame.setSize(oldWidth, oldHeight); // Set to default size temporarily to get location
        frame.setLocationRelativeTo(null);  // Center the frame on screen
        oldX = frame.getX(); // Capture the X position of the centered frame
        oldY = frame.getY(); // Capture the Y position of the centered frame

        // --- Set frame to maximize like Chrome (showing taskbar) ---
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle maxBounds = env.getMaximumWindowBounds(); // This gives bounds excluding taskbar
        frame.setBounds(maxBounds); // Set frame to these bounds
        isMaximized = true; // Set flag to true as it starts in a maximized state


        try {
            URL bgUrl = getClass().getResource("/Images/R.jpg");
            if (bgUrl != null) {
                backgroundImage = ImageIO.read(bgUrl);
            } else {
                System.err.println("LoginPage: Error: Background image '/Images/R.jpg' not found on classpath.");
            }
        } catch (IOException e) {
            System.err.println("LoginPage: Exception loading background image from URL: " + e.getMessage());
        }

        try {
            URL iconUrl = getClass().getResource("/Images/car_icon.png");
            if (iconUrl != null) {
                appIcon = new ImageIcon(iconUrl);
                frame.setIconImage(appIcon.getImage());
            } else {
                System.err.println("LoginPage: Error: Icon image '/Images/car_icon.png' not found on classpath.");
            }
        } catch (Exception e) {
            System.err.println("LoginPage: Exception loading app icon: " + e.getMessage());
        }

        JPanel rootPanel = new JPanel(new BorderLayout()) {
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
                g2d.setColor(THEME_BACKGROUND_STATIC_OVERLAY);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        rootPanel.setOpaque(false);
        frame.setContentPane(rootPanel);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setPreferredSize(new Dimension(frame.getWidth(), 40));

        MouseAdapter ma = new MouseAdapter() {
            private Point initialClick;
            public void mousePressed(MouseEvent e) {
                if (!isMaximized) { // Only allow dragging if not maximized
                    initialClick = e.getPoint();
                }
            }
            public void mouseDragged(MouseEvent e) {
                if (!isMaximized && initialClick != null) { // Only drag if not maximized
                    int thisX = frame.getLocation().x;
                    int thisY = frame.getLocation().y;
                    int xMoved = e.getXOnScreen() - (headerPanel.getLocationOnScreen().x + initialClick.x);
                    int yMoved = e.getYOnScreen() - (headerPanel.getLocationOnScreen().y + initialClick.y);
                    frame.setLocation(thisX + xMoved, thisY + yMoved);
                }
            }
        };
        headerPanel.addMouseListener(ma);
        headerPanel.addMouseMotionListener(ma);

        JPanel controlButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
        controlButtonPanel.setOpaque(false);

        minimizeButton = createControlButton(MINIMIZE_CHAR);
        this.maximizeButton = createControlButton(RESTORE_CHAR_ICON); // Initial icon is restore
        closeButton = createControlButton(CLOSE_CHAR);

        minimizeButton.addActionListener(e -> frame.setState(JFrame.ICONIFIED));
        this.maximizeButton.addActionListener(e -> toggleMaximize());
        closeButton.addActionListener(e -> System.exit(0));

        controlButtonPanel.add(minimizeButton);
        controlButtonPanel.add(this.maximizeButton);
        controlButtonPanel.add(closeButton);

        headerPanel.add(controlButtonPanel, BorderLayout.EAST);

        JLabel titleTextLabel = new JLabel("  Car Rental System - Login");
        titleTextLabel.setForeground(THEME_TEXT_PRIMARY);
        titleTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        headerPanel.add(titleTextLabel, BorderLayout.WEST);

        rootPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel loginFormPanel = new JPanel(new GridBagLayout()) {
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
        loginFormPanel.setOpaque(false);
        loginFormPanel.setPreferredSize(new Dimension(450, 480));

        loginFormPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 50, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 5, 0);

        JLabel titleLabel = new JLabel("BOOK A RIDE", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(THEME_TEXT_PRIMARY);
        loginFormPanel.add(titleLabel, gbc);

        JPanel userTypePanel = new JPanel(new GridLayout(1, 2, 0, 0));
        userTypePanel.setOpaque(false);
        userTypePanel.setBorder(BorderFactory.createLineBorder(new Color(218, 220, 224, 100), 1));

        userToggleButton = new JToggleButton("User", true);
        adminToggleButton = new JToggleButton("Admin", false);

        ButtonGroup group = new ButtonGroup();
        group.add(userToggleButton);
        group.add(adminToggleButton);
        userTypePanel.add(userToggleButton);
        userTypePanel.add(adminToggleButton);

        styleToggleButton(userToggleButton, true);
        styleToggleButton(adminToggleButton, false);

        userToggleButton.addActionListener(e -> {
            styleToggleButton(userToggleButton, true);
            styleToggleButton(adminToggleButton, false);
        });
        adminToggleButton.addActionListener(e -> {
            styleToggleButton(adminToggleButton, true);
            styleToggleButton(userToggleButton, false);
        });
        loginFormPanel.add(userTypePanel, gbc);

        usernameField = createTextField("Username");
        passwordField = createPasswordField("Password");
        loginFormPanel.add(createLabeledField("Username", usernameField), gbc);
        loginFormPanel.add(createLabeledField("Password", passwordField), gbc);

        JCheckBox showPassword = new JCheckBox("Show password");
        showPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        showPassword.setForeground(THEME_TEXT_PRIMARY);
        showPassword.setOpaque(false);
        showPassword.addActionListener(e -> passwordField.setEchoChar(showPassword.isSelected() ? '\0' : '•'));
        gbc.insets = new Insets(5, 0, 20, 0);
        loginFormPanel.add(showPassword, gbc);

        loginButton = createStyledButton("SIGN IN", THEME_YELLOW);
        gbc.insets = new Insets(20, 0, 10, 0);
        loginFormPanel.add(loginButton, gbc);

        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        optionsPanel.setOpaque(false);
        forgotPasswordButton = createTextButton("Forgot password?");
        forgotPasswordButton.addActionListener(e -> showForgotPasswordDialog(frame));
        JButton registerButton = createTextButton("Create account");
        registerButton.addActionListener(e -> {
            frame.dispose();
            new RegistrationPage();
        });
        optionsPanel.add(forgotPasswordButton);
        optionsPanel.add(registerButton);
        loginFormPanel.add(optionsPanel, gbc);

        JPanel centerWrapperPanel = new JPanel(new GridBagLayout());
        centerWrapperPanel.setOpaque(false);
        GridBagConstraints gbcCenter = new GridBagConstraints();
        gbcCenter.gridx = 0;
        gbcCenter.gridy = 0;
        gbcCenter.weightx = 0.0;
        gbcCenter.weighty = 0.0;
        gbcCenter.fill = GridBagConstraints.NONE;
        gbcCenter.anchor = GridBagConstraints.CENTER;
        centerWrapperPanel.add(loginFormPanel, gbcCenter);

        rootPanel.add(centerWrapperPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        footerPanel.setOpaque(false);
        JLabel footerLabel = new JLabel("© " + java.time.Year.now().getValue() + " The Rental Car");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(new Color(200, 200, 200));
        footerPanel.add(footerLabel);
        rootPanel.add(footerPanel, BorderLayout.SOUTH);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            boolean isAdminSelected = adminToggleButton.isSelected();

            User authenticatedUser = getUser(username);

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Username and password cannot be empty!", "Login Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (authenticatedUser != null && authenticatedUser.getPassword().equals(password)) {
                loggedInUser = authenticatedUser;
                loggedInUserRole = isAdminSelected ? "Admin" : "User";

                frame.dispose(); // Close login frame immediately

                // Data is already loaded at app startup, proceed directly
                if (isAdminSelected) {
                    if ("Admin".equals(authenticatedUser.getRole())) {
                        new AdminDashboard(users, allCars);
                    } else {
                        JOptionPane.showMessageDialog(null, "Access Denied: You are not an administrator.", "Login Error", JOptionPane.ERROR_MESSAGE);
                        new LoginPage().setVisible(true); // Reopen login if role mismatch
                    }
                } else {
                    if ("User".equals(authenticatedUser.getRole())) {
                        new CarListPage();
                    } else {
                        JOptionPane.showMessageDialog(null, "Access Denied: Please select 'Admin' to log in as administrator.", "Login Error", JOptionPane.ERROR_MESSAGE);
                        new LoginPage().setVisible(true); // Reopen login if role mismatch
                    }
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid username or password!", "Error", JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
            }
        });

        // Add component listener for rounded corners on frame resize
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Apply rounded corners only if not maximized.
                // When maximized, setShape(null) removes rounded corners.
                if (!isMaximized) {
                    frame.setShape(new RoundRectangle2D.Double(0, 0, frame.getWidth(), frame.getHeight(), 20, 20));
                } else {
                    frame.setShape(null);
                }
            }
        });

        // Set initial shape based on current maximized state (should be null as it starts maximized)
        if (!isMaximized) {
            frame.setShape(new RoundRectangle2D.Double(0, 0, frame.getWidth(), frame.getHeight(), 20, 20));
        } else {
            frame.setShape(null); // No rounded corners when maximized
        }

        frame.setVisible(true); // Make login frame visible after construction
    }

    // Removed showLoadingDialogAndFetchCars method

    public static List<Car> getCars() {
        return allCars;
    }

    public static String getLoggedInUserRole() {
        return loggedInUserRole;
    }

    public static User getLoggedInUser() {
        return loggedInUser;
    }

    public static List<User> getUsers() {
        return users;
    }

    public static User getUser(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public static void saveUsers(List<User> usersList) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_DATA_FILE))) {
            oos.writeObject(new ArrayList<>(usersList));
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static List<User> loadUsers() {
        File file = new File(USER_DATA_FILE);
        if (file.exists() && file.length() > 0) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USER_DATA_FILE))) {
                return (List<User>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading users: " + e.getMessage());
            }
        }
        return new ArrayList<>();
    }

    private void toggleMaximize() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle maxBounds = env.getMaximumWindowBounds();

        if (isMaximized) { // Currently maximized, so restore
            frame.setBounds(oldX, oldY, oldWidth, oldHeight);
            frame.setShape(new RoundRectangle2D.Double(0, 0, oldWidth, oldHeight, 20, 20)); // Re-apply rounded corners
            if (this.maximizeButton != null) {
                this.maximizeButton.setText(MAXIMIZE_CHAR_ICON); // Change to maximize icon
            }
            isMaximized = false;
        } else { // Currently restored, so maximize
            // Save current (restored) bounds BEFORE maximizing
            oldX = frame.getX();
            oldY = frame.getY();
            oldWidth = frame.getWidth();
            oldHeight = frame.getHeight();

            frame.setShape(null); // Remove rounded corners when maximizing
            frame.setBounds(maxBounds); // Maximize to screen bounds minus taskbar
            if (this.maximizeButton != null) {
                this.maximizeButton.setText(RESTORE_CHAR_ICON); // Change to restore icon
            }
            isMaximized = true;
        }
        frame.getContentPane().revalidate();
        frame.getContentPane().repaint();
    }

    private void styleToggleButton(JToggleButton button, boolean selected) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        if (selected) {
            button.setBackground(THEME_YELLOW);
            button.setForeground(THEME_TEXT_ACCENT);
        } else {
            button.setBackground(THEME_LIGHT_GRAY_BG);
            button.setForeground(new Color(95, 99, 104));
        }
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(218, 220, 224, 100), 1),
                new EmptyBorder(8, 15, 8, 15)
        ));
        button.revalidate();
        button.repaint();
    }

    private JButton createTextButton(String text) {
        class HoverButton extends JButton {
            private boolean hovered = false;

            public HoverButton(String text) {
                super(text);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setForeground(THEME_YELLOW);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        setForeground(THEME_YELLOW_DARK);
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        setForeground(THEME_YELLOW);
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (hovered) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(getForeground());
                    FontMetrics fm = g2.getFontMetrics(getFont());
                    int textWidth = fm.stringWidth(getText());
                    int textX = (getWidth() - textWidth) / 2;
                    int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawLine(textX, textY + 2, textX + textWidth, textY + 2);
                    g2.dispose();
                }
            }
        }
        return new HoverButton(text);
    }

    private void showForgotPasswordDialog(JFrame parent) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbcDialog = new GridBagConstraints();
        gbcDialog.gridwidth = GridBagConstraints.REMAINDER;
        gbcDialog.fill = GridBagConstraints.HORIZONTAL;
        gbcDialog.insets = new Insets(5, 0, 5, 0);
        JLabel label = new JLabel("Enter your email to reset password:");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(label, gbcDialog);
        JTextField emailField = new JTextField(25);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailField.setForeground(THEME_TEXT_ACCENT);
        emailField.setBackground(THEME_LIGHT_GRAY_BG);
        emailField.setCaretColor(THEME_TEXT_ACCENT);
        emailField.setBorder(new CompoundBorder(
                new LineBorder(THEME_BORDER_COLOR, 1),
                new EmptyBorder(5, 5, 5, 5)
        ));
        panel.add(emailField, gbcDialog);

        Object originalOptionPaneBg = UIManager.get("OptionPane.background");
        Object originalPanelBg = UIManager.get("Panel.background");
        Object originalLabelFg = UIManager.get("Label.foreground");
        Object originalButtonBg = UIManager.get("Button.background");
        Object originalButtonFg = UIManager.get("Button.foreground");

        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("Label.foreground", Color.BLACK);
        UIManager.put("Button.background", THEME_YELLOW);
        UIManager.put("Button.foreground", THEME_TEXT_ACCENT);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Forgot Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        UIManager.put("OptionPane.background", originalOptionPaneBg);
        UIManager.put("Panel.background", originalPanelBg);
        UIManager.put("Label.foreground", originalLabelFg);
        UIManager.put("Button.background", originalButtonBg);
        UIManager.put("Button.foreground", originalButtonFg);

        if (result == JOptionPane.OK_OPTION) {
            String email = emailField.getText().trim();
            if (email != null && !email.isEmpty() && email.contains("@") && email.length() > 5) {
                User userToReset = users.stream()
                        .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(email))
                        .findFirst()
                        .orElse(null);
                if (userToReset != null) {
                    JOptionPane.showMessageDialog(parent, "Password reset link has been sent to: " + email, "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(parent, "No account found with that email address.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(parent, "Please enter a valid email address.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JButton createControlButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setForeground(THEME_TEXT_PRIMARY);
        button.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        button.setMargin(new Insets(0, 8, 0, 8));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (CLOSE_CHAR.equals(text)) {
                    button.setBackground(THEME_RED_BUTTON);
                    button.setOpaque(true);
                } else {
                    button.setBackground(new Color(60, 60, 60, 150));
                    button.setOpaque(true);
                }
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(0, 0, 0, 0));
                button.setOpaque(false);
            }
        });
        return button;
    }

    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(280, 40));
        field.setMaximumSize(new Dimension(280, 40));
        field.setForeground(THEME_TEXT_ACCENT);
        field.setBackground(THEME_LIGHT_GRAY_BG);
        field.setCaretColor(THEME_TEXT_ACCENT);

        field.putClientProperty("JTextField.placeholderText", placeholder);

        field.setBorder(new CompoundBorder(
                new LineBorder(THEME_BORDER_COLOR, 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(new CompoundBorder(
                        new LineBorder(THEME_YELLOW, 2),
                        new EmptyBorder(9, 9, 9, 9)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(new CompoundBorder(
                        new LineBorder(THEME_BORDER_COLOR, 1),
                        new EmptyBorder(10, 10, 10, 10)
                ));
            }
        });
        return field;
    }

    private JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(280, 40));
        field.setMaximumSize(new Dimension(280, 40));
        field.setForeground(THEME_TEXT_ACCENT);
        field.setBackground(THEME_LIGHT_GRAY_BG);
        field.setCaretColor(THEME_TEXT_ACCENT);
        field.setEchoChar('•');

        field.putClientProperty("JTextField.placeholderText", placeholder);

        field.setBorder(new CompoundBorder(
                new LineBorder(THEME_BORDER_COLOR, 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(new CompoundBorder(
                        new LineBorder(THEME_YELLOW, 2),
                        new EmptyBorder(9, 9, 9, 9)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(new CompoundBorder(
                        new LineBorder(THEME_BORDER_COLOR, 1),
                        new EmptyBorder(10, 10, 10, 10)
                ));
            }
        });
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
        button.setForeground(THEME_TEXT_ACCENT);
        button.setBackground(color);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Color darkerColor = THEME_YELLOW_DARK;
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(darkerColor); }
            public void mouseExited(MouseEvent e) { button.setBackground(color); }
        });
        return button;
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            UIManager.put("Button.background", THEME_YELLOW);
            UIManager.put("Button.foreground", THEME_TEXT_ACCENT);
            UIManager.put("TextField.background", THEME_LIGHT_GRAY_BG);
            UIManager.put("TextField.foreground", THEME_TEXT_ACCENT);
            UIManager.put("PasswordField.background", THEME_LIGHT_GRAY_BG);
            UIManager.put("PasswordField.foreground", THEME_TEXT_ACCENT);
            UIManager.put("CheckBox.foreground", THEME_TEXT_PRIMARY);
            UIManager.put("ComboBox.background", new Color(50, 50, 50, 180));
            UIManager.put("ComboBox.foreground", THEME_TEXT_PRIMARY);
            UIManager.put("ComboBox.selectionBackground", THEME_YELLOW_DARK);
            UIManager.put("ComboBox.selectionForeground", THEME_TEXT_ACCENT);

        } catch (Exception e) {
            System.err.println("LoginPage: Nimbus Look and Feel not available. Using default. " + e.getMessage());
        }

        CarService.initialize();
        LoginPage.allCars = CarService.getAllCars();
        Runtime.getRuntime().addShutdownHook(new Thread(CarService::shutdown));

        SwingUtilities.invokeLater(LoginPage::new);
    }
}
