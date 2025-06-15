// File: src/com/carrental/AdminDashboard.java
package com.carrental;

import com.carrental.model.Car;
import com.carrental.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.plaf.basic.BasicComboBoxUI; // For custom JComboBox UI
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

public class AdminDashboard {

    private JFrame frame;
    private JPanel contentPanel; // Panel to hold different views (User/Car Management)
    private CardLayout cardLayout; // Layout for switching between views

    private List<User> allUsers;
    private List<Car> allCars;

    // --- Theme Colors (consistent with LoginPage and CarListPage) ---
    private static final Color THEME_PRIMARY = new Color(30, 30, 30); // Dark background
    private static final Color THEME_SECONDARY = new Color(45, 45, 45); // Slightly lighter for panels
    private static final Color THEME_ACCENT = new Color(0, 120, 215); // Accent blue (for tables, buttons)
    private static final Color THEME_YELLOW = new Color(255, 204, 0); // Yellow accent (for highlights)
    private static final Color THEME_YELLOW_DARK = new Color(204, 163, 0); // Darker yellow for hover
    private static final Color THEME_TEXT_PRIMARY = new Color(220, 220, 220); // Light grey text
    private static final Color THEME_TEXT_ACCENT = new Color(30, 30, 30); // Dark text on light background
    private static final Color THEME_BORDER_COLOR = new Color(100, 100, 100); // Grey border
    private static final Color THEME_RED_BUTTON = new Color(200, 50, 50); // Red for delete action


    // --- Custom Panel for Background Image ---
    private class BackgroundPanel extends JPanel {
        private BufferedImage backgroundImage;
        private final Color overlayColor;

        public BackgroundPanel(LayoutManager layout, String imagePath, Color overlay) {
            super(layout);
            this.overlayColor = overlay;
            // Load background image asynchronously for smoother UI
            new SwingWorker<BufferedImage, Void>() {
                @Override
                protected BufferedImage doInBackground() throws Exception {
                    try {
                        URL bgUrl = getClass().getResource(imagePath);
                        if (bgUrl != null) {
                            return ImageIO.read(bgUrl);
                        } else {
                            System.err.println("AdminDashboard: Background image '" + imagePath + "' not found on classpath.");
                            return null;
                        }
                    } catch (IOException e) {
                        System.err.println("AdminDashboard: Exception loading background image from URL: " + e.getMessage());
                        return null;
                    }
                }

                @Override
                protected void done() {
                    try {
                        backgroundImage = get();
                        repaint(); // Repaint after image is loaded
                    } catch (Exception e) {
                        System.err.println("AdminDashboard: Error setting background image: " + e.getMessage());
                    }
                }
            }.execute();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                g2d.setColor(THEME_PRIMARY); // Fallback color if image not loaded
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
            g2d.setColor(overlayColor); // Apply the overlay
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.dispose();
        }
    }


    public AdminDashboard(List<User> users, List<Car> cars) {
        this.allUsers = users;
        this.allCars = cars;

        frame = new JFrame("Car Rental System - Admin Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 700);
        frame.setLocationRelativeTo(null);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Start maximized

        // Set a custom icon
        try {
            ImageIcon icon = new ImageIcon(
                    ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/Images/car_icon.png"))));
            frame.setIconImage(icon.getImage());
        } catch (IOException e) {
            System.err.println("Icon image not found (IOException): " + e.getMessage());
        } catch (NullPointerException e) {
            System.err.println("Icon image resource not found (NullPointerException). Path: /Images/car_icon.png");
        }


        // Main content panel now uses BackgroundPanel
        BackgroundPanel mainContentPanel = new BackgroundPanel(new BorderLayout(), "/Images/5.jpg", new Color(0, 0, 0, 180)); // Example background image
        frame.setContentPane(mainContentPanel);

        // --- Sidebar for Navigation ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(25, 25, 25, 200)); // Darker, semi-transparent for sidebar
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));
        sidebar.setPreferredSize(new Dimension(200, frame.getHeight())); // Fixed width sidebar

        JLabel sidebarTitle = new JLabel("MENU");
        sidebarTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        sidebarTitle.setForeground(THEME_YELLOW);
        sidebarTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(sidebarTitle);
        sidebar.add(Box.createRigidArea(new Dimension(0, 30))); // Spacer

        // Navigation Buttons
        JButton userManagementBtn = createSidebarButton("User Management");
        JButton carManagementBtn = createSidebarButton("Car Management");

        userManagementBtn.addActionListener(e -> cardLayout.show(contentPanel, "UserPanel"));
        carManagementBtn.addActionListener(e -> cardLayout.show(contentPanel, "CarPanel"));

        sidebar.add(userManagementBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebar.add(carManagementBtn);
        sidebar.add(Box.createVerticalGlue()); // Pushes buttons to top

        // Add Logout button to the sidebar
        JButton logoutButton = createSidebarButton("Logout");
        logoutButton.setBackground(THEME_RED_BUTTON); // Make logout button red
        logoutButton.addMouseListener(new MouseAdapter() { // Custom hover for logout button
            public void mouseEntered(MouseEvent e) { logoutButton.setBackground(THEME_RED_BUTTON.darker()); }
            public void mouseExited(MouseEvent e) { logoutButton.setBackground(THEME_RED_BUTTON); }
        });
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showOptionDialog(frame, "Are you sure you want to log out?", "Confirm Logout",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"Yes", "No"}, "No");
            if (confirm == JOptionPane.YES_OPTION) {
                LoginPage.loggedInUser = null;
                LoginPage.loggedInUserRole = null;
                frame.dispose();
                new LoginPage();
            }
        });
        sidebar.add(logoutButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10))); // Small space at bottom

        mainContentPanel.add(sidebar, BorderLayout.WEST);

        // --- Main Content Area (uses CardLayout) ---
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false); // To let the background panel show through
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Padding around content

        // Add the management panels to the CardLayout
        contentPanel.add(new UserManagementPanel(), "UserPanel");
        contentPanel.add(new CarManagementPanel(), "CarPanel");

        // Initially show the User Management Panel
        cardLayout.show(contentPanel, "UserPanel");


        mainContentPanel.add(contentPanel, BorderLayout.CENTER);


        // Add component listener for rounded corners on frame resize
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Apply rounded corners only when not maximized
                if (frame.getExtendedState() == JFrame.NORMAL) {
                    frame.setShape(new RoundRectangle2D.Double(0, 0, frame.getWidth(), frame.getHeight(), 20, 20));
                } else {
                    frame.setShape(null); // Remove custom shape when maximized
                }
            }
        });
        // Initial set for rounded corners for when it's restored from maximized
        // The frame.setExtendedState(JFrame.MAXIMIZED_BOTH); will make it maximized initially, so no initial shape needed.

        frame.setVisible(true);
    }

    // --- Inner Class for User Management Panel ---
    private class UserManagementPanel extends JPanel {
        private DefaultTableModel userTableModel;
        private JTable userTable;

        public UserManagementPanel() {
            setLayout(new BorderLayout(15, 15)); // Added gaps
            setBackground(THEME_PRIMARY); // Consistent background (will be covered by transparency)
            setOpaque(false); // Make it transparent to show main background

            JLabel panelTitle = new JLabel("User Management", SwingConstants.CENTER);
            panelTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
            panelTitle.setForeground(THEME_YELLOW);
            panelTitle.setBorder(new EmptyBorder(10, 0, 20, 0)); // Padding
            add(panelTitle, BorderLayout.NORTH);

            String[] columnNames = {"Username", "Full Name", "Role", "Phone Number", "Email"};
            userTableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            userTable = new JTable(userTableModel);
            userTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            userTable.setForeground(THEME_TEXT_PRIMARY);
            userTable.setBackground(THEME_SECONDARY.darker());
            userTable.setSelectionBackground(THEME_ACCENT.brighter().brighter());
            userTable.setSelectionForeground(Color.WHITE);
            userTable.setRowHeight(28); // Increased row height
            userTable.setGridColor(new Color(70, 70, 70));

            userTable.getTableHeader().setBackground(THEME_YELLOW); // Changed from THEME_ACCENT to THEME_YELLOW
            userTable.getTableHeader().setForeground(THEME_TEXT_ACCENT); // Changed from Color.WHITE to THEME_TEXT_ACCENT
            userTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14)); // Bolder header
            userTable.getTableHeader().setReorderingAllowed(false);

            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
            centerRenderer.setBorder(new EmptyBorder(8, 5, 8, 5)); // Add padding
            userTable.setDefaultRenderer(Object.class, centerRenderer);

            JScrollPane scrollPane = new JScrollPane(userTable);
            scrollPane.getViewport().setBackground(THEME_SECONDARY.darker());
            scrollPane.setBorder(BorderFactory.createLineBorder(THEME_BORDER_COLOR, 1));
            add(scrollPane, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
            buttonPanel.setBackground(THEME_PRIMARY); // Match panel background
            buttonPanel.setOpaque(false); // Transparent to show main background

            // NEW: Add User button
            JButton addUserButton = createStyledButton("Add User", THEME_ACCENT, THEME_ACCENT.darker());
            addUserButton.addActionListener(e -> showAddUserDialog());
            buttonPanel.add(addUserButton);

            JButton editUserButton = createStyledButton("Edit User", THEME_ACCENT, THEME_ACCENT.darker());
            editUserButton.addActionListener(e -> showUserDialog(userTable));
            buttonPanel.add(editUserButton);

            JButton deleteUserButton = createStyledButton("Delete User", THEME_RED_BUTTON, THEME_RED_BUTTON.darker());
            deleteUserButton.addActionListener(e -> deleteSelectedUser(userTable));
            buttonPanel.add(deleteUserButton);

            add(buttonPanel, BorderLayout.SOUTH);

            populateUserTable(); // Populate table on panel creation
        }

        private void populateUserTable() {
            userTableModel.setRowCount(0); // Clear existing data
            for (User user : allUsers) {
                userTableModel.addRow(new Object[]{
                        user.getUsername(),
                        user.getFullName(),
                        user.getRole(),
                        user.getPhoneNumber(),
                        user.getEmail()
                });
            }
        }

        private void showAddUserDialog() {
            JTextField usernameField = createStyledTextField("");
            JPasswordField passwordField = createStyledPasswordField("");
            JTextField fullNameField = createStyledTextField("");
            JTextField phoneNumberField = createStyledTextField("");
            JTextField emailField = createStyledTextField("");
            String[] roles = {"User", "Admin"};
            JComboBox<String> roleComboBox = createStyledComboBox(roles);
            roleComboBox.setSelectedItem("User"); // Default to 'User'

            JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));
            panel.setBackground(THEME_SECONDARY);

            panel.add(createDialogLabel("Username:")); panel.add(usernameField);
            panel.add(createDialogLabel("Password:")); panel.add(passwordField);
            panel.add(createDialogLabel("Full Name:")); panel.add(fullNameField);
            panel.add(createDialogLabel("Phone Number:")); panel.add(phoneNumberField);
            panel.add(createDialogLabel("Email:")); panel.add(emailField);
            panel.add(createDialogLabel("Role:")); panel.add(roleComboBox);

            // Temporarily set UIManager properties for this dialog
            applyDialogUIManagerSettings();

            int result = JOptionPane.showConfirmDialog(frame, panel, "Add New User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            // Reset UIManager properties
            resetDialogUIManagerSettings();

            if (result == JOptionPane.OK_OPTION) {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();
                String fullName = fullNameField.getText().trim();
                String phoneNumber = phoneNumberField.getText().trim();
                String email = emailField.getText().trim();
                String role = (String) roleComboBox.getSelectedItem();

                // Basic validation
                if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || phoneNumber.isEmpty() || email.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "All fields must be filled.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (username.length() < 3) {
                    JOptionPane.showMessageDialog(frame, "Username must be at least 3 characters long.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (password.length() < 4) {
                    JOptionPane.showMessageDialog(frame, "Password must be at least 4 characters long.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!email.contains("@") || !email.contains(".")) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid email address.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check for duplicate username
                if (allUsers.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username))) {
                    JOptionPane.showMessageDialog(frame, "Username already exists. Please choose a different one.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                User newUser = new User(username, password, role, fullName, phoneNumber, email);
                allUsers.add(newUser);
                LoginPage.saveUsers(allUsers);
                populateUserTable(); // Refresh table
                JOptionPane.showMessageDialog(frame, "User added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }


        private void showUserDialog(JTable userTable) {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a user to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String originalUsername = (String) userTableModel.getValueAt(selectedRow, 0);
            User userToEdit = allUsers.stream().filter(u -> u.getUsername().equals(originalUsername)).findFirst().orElse(null);

            if (userToEdit == null) {
                JOptionPane.showMessageDialog(frame, "User not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JTextField usernameField = createStyledTextField(userToEdit.getUsername());
            usernameField.setEditable(false);
            JPasswordField passwordField = createStyledPasswordField(userToEdit.getPassword());
            JTextField fullNameField = createStyledTextField(userToEdit.getFullName());
            JTextField phoneNumberField = createStyledTextField(userToEdit.getPhoneNumber());
            JTextField emailField = createStyledTextField(userToEdit.getEmail());
            String[] roles = {"User", "Admin"};
            JComboBox<String> roleComboBox = createStyledComboBox(roles);
            roleComboBox.setSelectedItem(userToEdit.getRole());


            JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));
            panel.setBackground(THEME_SECONDARY);

            panel.add(createDialogLabel("Username:")); panel.add(usernameField);
            panel.add(createDialogLabel("Password:")); panel.add(passwordField);
            panel.add(createDialogLabel("Full Name:")); panel.add(fullNameField);
            panel.add(createDialogLabel("Phone Number:")); panel.add(phoneNumberField);
            panel.add(createDialogLabel("Email:")); panel.add(emailField);
            panel.add(createDialogLabel("Role:")); panel.add(roleComboBox);

            // Temporarily set UIManager properties for this dialog
            applyDialogUIManagerSettings();

            int result = JOptionPane.showConfirmDialog(frame, panel, "Edit User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            // Reset UIManager properties
            resetDialogUIManagerSettings();


            if (result == JOptionPane.OK_OPTION) {
                // Basic validation
                String newPassword = new String(passwordField.getPassword()).trim();
                String newFullName = fullNameField.getText().trim();
                String newPhoneNumber = phoneNumberField.getText().trim();
                String newEmail = emailField.getText().trim();

                if (newPassword.isEmpty() || newFullName.isEmpty() || newPhoneNumber.isEmpty() || newEmail.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "All fields must be filled.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (newPassword.length() < 4) {
                    JOptionPane.showMessageDialog(frame, "Password must be at least 4 characters long.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!newEmail.contains("@") || !newEmail.contains(".")) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid email address.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                userToEdit.setPassword(newPassword);
                userToEdit.setFullName(newFullName);
                userToEdit.setPhoneNumber(newPhoneNumber);
                userToEdit.setEmail(newEmail);
                userToEdit.setRole((String) roleComboBox.getSelectedItem());

                LoginPage.saveUsers(allUsers);
                populateUserTable();
                JOptionPane.showMessageDialog(frame, "User updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        private void deleteSelectedUser(JTable userTable) {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a user to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String usernameToDelete = (String) userTableModel.getValueAt(selectedRow, 0);
            if (LoginPage.loggedInUser != null && usernameToDelete.equals(LoginPage.loggedInUser.getUsername())) {
                JOptionPane.showMessageDialog(frame, "You cannot delete your own account.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Temporarily set UIManager properties for this dialog
            applyDialogUIManagerSettings();
            int confirm = JOptionPane.showOptionDialog(frame, "Are you sure you want to delete user '" + usernameToDelete + "'?", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"Yes", "No"}, "No");
            // Reset UIManager properties
            resetDialogUIManagerSettings();

            if (confirm == JOptionPane.YES_OPTION) {
                allUsers.removeIf(user -> user.getUsername().equals(usernameToDelete));
                LoginPage.saveUsers(allUsers);
                populateUserTable();
                JOptionPane.showMessageDialog(frame, "User deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    } // End of UserManagementPanel

    // --- Inner Class for Car Management Panel ---
    private class CarManagementPanel extends JPanel {
        private DefaultTableModel carTableModel;
        private JTable carTable;

        public CarManagementPanel() {
            setLayout(new BorderLayout(15, 15));
            setBackground(THEME_PRIMARY); // Will be transparent
            setOpaque(false);

            JLabel panelTitle = new JLabel("Car Management", SwingConstants.CENTER);
            panelTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
            panelTitle.setForeground(THEME_YELLOW);
            panelTitle.setBorder(new EmptyBorder(10, 0, 20, 0));
            add(panelTitle, BorderLayout.NORTH);

            String[] columnNames = {
                    "Name", "Available", "Type", "Rating", "Image Path",
                    "Mileage", "Max Speed", "Seats", "Transmission", "Vehicle Class",
                    "Price", "Release Date"
            };
            carTableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            carTable = new JTable(carTableModel);
            carTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            carTable.setForeground(THEME_TEXT_PRIMARY);
            carTable.setBackground(THEME_SECONDARY.darker());
            carTable.setSelectionBackground(THEME_ACCENT.brighter().brighter());
            carTable.setSelectionForeground(Color.WHITE);
            carTable.setRowHeight(28); // Increased row height
            carTable.setGridColor(new Color(70, 70, 70));

            carTable.getTableHeader().setBackground(THEME_YELLOW); // Changed from THEME_ACCENT to THEME_YELLOW
            carTable.getTableHeader().setForeground(THEME_TEXT_ACCENT); // Changed from Color.WHITE to THEME_TEXT_ACCENT
            carTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
            carTable.getTableHeader().setReorderingAllowed(false);

            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
            centerRenderer.setBorder(new EmptyBorder(8, 5, 8, 5));
            carTable.setDefaultRenderer(Object.class, centerRenderer);

            JScrollPane scrollPane = new JScrollPane(carTable);
            scrollPane.getViewport().setBackground(THEME_SECONDARY.darker());
            scrollPane.setBorder(BorderFactory.createLineBorder(THEME_BORDER_COLOR, 1));
            add(scrollPane, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
            buttonPanel.setBackground(THEME_PRIMARY);
            buttonPanel.setOpaque(false);

            JButton addCarButton = createStyledButton("Add New Car", THEME_ACCENT, THEME_ACCENT.darker());
            addCarButton.addActionListener(e -> showCarDialog(null));
            buttonPanel.add(addCarButton);

            JButton editCarButton = createStyledButton("Edit Car", THEME_ACCENT, THEME_ACCENT.darker());
            editCarButton.addActionListener(e -> {
                int selectedRow = carTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(frame, "Please select a car to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String carName = (String) carTableModel.getValueAt(selectedRow, 0);
                Car carToEdit = LoginPage.allCars.stream()
                        .filter(c -> c.getName().equals(carName))
                        .findFirst()
                        .orElse(null);
                if (carToEdit != null) {
                    showCarDialog(carToEdit);
                } else {
                    JOptionPane.showMessageDialog(frame, "Car not found in data. Please refresh.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            buttonPanel.add(editCarButton);

            JButton deleteCarButton = createStyledButton("Delete Car", THEME_RED_BUTTON, THEME_RED_BUTTON.darker());
            deleteCarButton.addActionListener(e -> deleteSelectedCar(carTable));
            buttonPanel.add(deleteCarButton);

            add(buttonPanel, BorderLayout.SOUTH);

            populateCarTable(); // Call to populateCarTable()
        }

        private void populateCarTable() {
            carTableModel.setRowCount(0); // Clear existing data
            for (Car car : LoginPage.allCars) {
                carTableModel.addRow(new Object[]{
                        car.getName(),
                        car.isAvailable() ? "Yes" : "No",
                        car.getType(),
                        car.getRating(),
                        car.getImagePath(),
                        car.getMileage(),
                        car.getMaxSpeed(),
                        car.getSeats(),
                        car.getTransmission(),
                        car.getVehicleClass(),
                        String.format("%.2f", car.getPrice()),
                        car.getReleaseDate()
                });
            }
        }

        private void showCarDialog(Car carToEdit) {
            JTextField nameField = createStyledTextField("");
            JCheckBox availableBox = new JCheckBox("Available");
            availableBox.setBackground(THEME_SECONDARY);
            availableBox.setForeground(THEME_TEXT_PRIMARY);
            JTextField typeField = createStyledTextField("");
            JTextField ratingField = createStyledTextField("");
            JTextField imagePathField = createStyledTextField("");
            JTextField mileageField = createStyledTextField("");
            JTextField maxSpeedField = createStyledTextField("");
            JTextField seatsField = createStyledTextField("");
            JTextField transmissionField = createStyledTextField("");
            JTextField vehicleClassField = createStyledTextField("");
            JTextField priceField = createStyledTextField("");
            JTextField releaseDateField = createStyledTextField("");

            if (carToEdit != null) {
                nameField.setText(carToEdit.getName());
                nameField.setEditable(false);
                availableBox.setSelected(carToEdit.isAvailable());
                typeField.setText(carToEdit.getType());
                ratingField.setText(String.valueOf(carToEdit.getRating()));
                imagePathField.setText(carToEdit.getImagePath());
                mileageField.setText(carToEdit.getMileage());
                maxSpeedField.setText(carToEdit.getMaxSpeed());
                seatsField.setText(carToEdit.getSeats());
                transmissionField.setText(carToEdit.getTransmission());
                vehicleClassField.setText(carToEdit.getVehicleClass());
                priceField.setText(String.valueOf(carToEdit.getPrice()));
                releaseDateField.setText(carToEdit.getReleaseDate());
            }

            JPanel panel = new JPanel(new GridLayout(0, 2, 10, 5));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));
            panel.setBackground(THEME_SECONDARY);

            panel.add(createDialogLabel("Name:")); panel.add(nameField);
            panel.add(createDialogLabel("Available:")); panel.add(availableBox);
            panel.add(createDialogLabel("Type:")); panel.add(typeField);
            panel.add(createDialogLabel("Rating:")); panel.add(ratingField);
            panel.add(createDialogLabel("Image Path:")); panel.add(imagePathField);
            panel.add(createDialogLabel("Mileage:")); panel.add(mileageField);
            panel.add(createDialogLabel("Max Speed:")); panel.add(maxSpeedField);
            panel.add(createDialogLabel("Seats:")); panel.add(seatsField);
            panel.add(createDialogLabel("Transmission:")); panel.add(transmissionField);
            panel.add(createDialogLabel("Vehicle Class:")); panel.add(vehicleClassField);
            panel.add(createDialogLabel("Price:")); panel.add(priceField);
            panel.add(createDialogLabel("Release Date (YYYY-MM-DD):")); panel.add(releaseDateField);


            // Temporarily set UIManager properties for this dialog
            applyDialogUIManagerSettings();

            int result = JOptionPane.showConfirmDialog(frame, panel,
                    (carToEdit == null ? "Add New Car" : "Edit Car Details"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            // Reset UIManager properties
            resetDialogUIManagerSettings();


            if (result == JOptionPane.OK_OPTION) {
                try {
                    String name = nameField.getText().trim();
                    boolean available = availableBox.isSelected();
                    String type = typeField.getText().trim();
                    double rating = Double.parseDouble(ratingField.getText().trim());
                    String imagePath = imagePathField.getText().trim();
                    String mileage = mileageField.getText().trim();
                    String maxSpeed = maxSpeedField.getText().trim();
                    String seats = seatsField.getText().trim();
                    String transmission = transmissionField.getText().trim();
                    String vehicleClass = vehicleClassField.getText().trim();
                    double price = Double.parseDouble(priceField.getText().trim());
                    String releaseDate = releaseDateField.getText().trim();

                    if (name.isEmpty() || type.isEmpty() || imagePath.isEmpty() || mileage.isEmpty() ||
                            maxSpeed.isEmpty() || seats.isEmpty() || transmission.isEmpty() ||
                            vehicleClass.isEmpty() || releaseDate.isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "All fields must be filled.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (rating < 0 || rating > 5) {
                        JOptionPane.showMessageDialog(frame, "Rating must be between 0 and 5.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (price < 0) {
                        JOptionPane.showMessageDialog(frame, "Price cannot be negative.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    try {
                        LocalDate.parse(releaseDate);
                    } catch (DateTimeParseException e) {
                        JOptionPane.showMessageDialog(frame, "Invalid Release Date format. Use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String pathForCpp = imagePath;
                    if (imagePath.startsWith("/Images/")) {
                        pathForCpp = "src" + imagePath;
                    }

                    if (carToEdit == null) {
                        Car newCar = new Car(name, available, type, rating, pathForCpp, mileage, maxSpeed, seats, transmission, vehicleClass, price, releaseDate);
                        if (LoginPage.allCars.stream().anyMatch(c -> c.getName().equals(name))) {
                            JOptionPane.showMessageDialog(frame, "A car with this name already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        if (CarService.addCar(newCar)) {
                            LoginPage.allCars.add(newCar);
                            JOptionPane.showMessageDialog(frame, "Car added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(frame, "Failed to add car to service. Check console for details.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        carToEdit.setAvailable(available);
                        carToEdit.setType(type);
                        carToEdit.setRating(rating);
                        carToEdit.setImagePath(pathForCpp);
                        carToEdit.setMileage(mileage);
                        carToEdit.setMaxSpeed(maxSpeed);
                        carToEdit.setSeats(seats);
                        carToEdit.setTransmission(transmission);
                        carToEdit.setVehicleClass(vehicleClass);
                        carToEdit.setPrice(price);
                        carToEdit.setReleaseDate(releaseDate);

                        if (CarService.updateCar(carToEdit)) {
                            JOptionPane.showMessageDialog(frame, "Car updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(frame, "Failed to update car. Check console for details.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    populateCarTable(); // Call to populateCarTable()
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter valid numbers for Rating and Price.", "Input Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }

        private void deleteSelectedCar(JTable carTable) {
            int selectedRow = carTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a car to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String carNameToDelete = (String) carTableModel.getValueAt(selectedRow, 0);

            // Temporarily set UIManager properties for this dialog
            applyDialogUIManagerSettings();
            int confirm = JOptionPane.showOptionDialog(frame, "Are you sure you want to delete '" + carNameToDelete + "'?", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"Yes", "No"}, "No");
            // Reset UIManager properties
            resetDialogUIManagerSettings();

            if (confirm == JOptionPane.YES_OPTION) {
                if (CarService.deleteCar(carNameToDelete)) {
                    LoginPage.allCars.removeIf(car -> car.getName().equals(carNameToDelete));
                    JOptionPane.showMessageDialog(frame, "Car deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to delete car (it might not have existed or service failed).", "Error", JOptionPane.ERROR_MESSAGE);
                }
                populateCarTable(); // Call to populateCarTable()
            }
        }
    } // End of CarManagementPanel


    // --- Helper Methods for consistent styling ---

    // New methods to apply/reset UIManager settings for dialogs
    private void applyDialogUIManagerSettings() {
        UIManager.put("OptionPane.background", THEME_SECONDARY);
        UIManager.put("Panel.background", THEME_SECONDARY);
        UIManager.put("Label.foreground", THEME_TEXT_PRIMARY);
        UIManager.put("Button.background", THEME_ACCENT);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("ComboBox.background", THEME_SECONDARY.brighter());
        UIManager.put("ComboBox.foreground", THEME_TEXT_PRIMARY);
        UIManager.put("TextField.background", THEME_PRIMARY);
        UIManager.put("TextField.foreground", THEME_TEXT_PRIMARY);
        UIManager.put("PasswordField.background", THEME_PRIMARY);
        UIManager.put("PasswordField.foreground", THEME_TEXT_PRIMARY);
        UIManager.put("CheckBox.background", THEME_SECONDARY); // For JCheckBox in dialogs
        UIManager.put("CheckBox.foreground", THEME_TEXT_PRIMARY);
    }

    private void resetDialogUIManagerSettings() {
        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("Label.foreground", null);
        UIManager.put("Button.background", null);
        UIManager.put("Button.foreground", null);
        UIManager.put("ComboBox.background", null);
        UIManager.put("ComboBox.foreground", null);
        UIManager.put("TextField.background", null);
        UIManager.put("TextField.foreground", null);
        UIManager.put("PasswordField.background", null);
        UIManager.put("PasswordField.foreground", null);
        UIManager.put("CheckBox.background", null);
        UIManager.put("CheckBox.foreground", null);
    }


    private JLabel createDialogLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(THEME_TEXT_PRIMARY);
        return label;
    }

    private JTextField createStyledTextField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBackground(THEME_PRIMARY);
        field.setForeground(THEME_TEXT_PRIMARY);
        field.setCaretColor(THEME_YELLOW);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(THEME_BORDER_COLOR, 1),
                new EmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private JPasswordField createStyledPasswordField(String text) {
        JPasswordField field = new JPasswordField(text);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBackground(THEME_PRIMARY);
        field.setForeground(THEME_TEXT_PRIMARY);
        field.setCaretColor(THEME_YELLOW);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(THEME_BORDER_COLOR, 1),
                new EmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setBackground(THEME_PRIMARY);
        comboBox.setForeground(THEME_TEXT_PRIMARY);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(THEME_BORDER_COLOR, 1),
                new EmptyBorder(5, 8, 5, 8)
        ));
        // Custom renderer for combobox items to match theme
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    setBackground(THEME_ACCENT.darker());
                    setForeground(Color.WHITE);
                } else {
                    setBackground(THEME_SECONDARY);
                    setForeground(THEME_TEXT_PRIMARY);
                }
                setBorder(new EmptyBorder(5, 8, 5, 8));
                return this;
            }
        });

        // Custom UI for the arrow button
        comboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton();
                button.setOpaque(false);
                button.setContentAreaFilled(false);
                button.setBorder(BorderFactory.createEmptyBorder());
                // Create a custom arrow icon (triangle pointing down)
                button.setIcon(new ImageIcon(createArrowImage(10, 5, THEME_TEXT_PRIMARY)));
                button.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        button.setIcon(new ImageIcon(createArrowImage(10, 5, THEME_YELLOW)));
                    }
                    public void mouseExited(MouseEvent e) {
                        button.setIcon(new ImageIcon(createArrowImage(10, 5, THEME_TEXT_PRIMARY)));
                    }
                });
                return button;
            }
            private Image createArrowImage(int width, int height, Color color) {
                BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = img.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                int[] xPoints = {0, width, width / 2};
                int[] yPoints = {0, 0, height};
                g2d.fillPolygon(xPoints, yPoints, 3);
                g2d.dispose();
                return img;
            }
        });
        return comboBox;
    }


    private JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(hoverColor); }
            public void mouseExited(MouseEvent e) { button.setBackground(bgColor); }
        });
        return button;
    }

    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setForeground(THEME_TEXT_PRIMARY);
        button.setBackground(new Color(60, 60, 60, 150)); // Semi-transparent dark grey
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the button
        button.setMaximumSize(new Dimension(180, 45)); // Fixed width for consistent look
        button.setPreferredSize(new Dimension(180, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(THEME_ACCENT); // Accent color on hover
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(60, 60, 60, 150));
            }
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
            // Apply default UIManager properties for consistent look across dialogs
            UIManager.put("OptionPane.background", new Color(45, 45, 45));
            UIManager.put("Panel.background", new Color(45, 45, 45));
            UIManager.put("Label.foreground", new Color(220, 220, 220));
            UIManager.put("TextField.background", new Color(30, 30, 30));
            UIManager.put("TextField.foreground", new Color(220, 220, 220));
            UIManager.put("PasswordField.background", new Color(30, 30, 30));
            UIManager.put("PasswordField.foreground", new Color(220, 220, 220));
            UIManager.put("ComboBox.background", new Color(30, 30, 30));
            UIManager.put("ComboBox.foreground", new Color(220, 220, 220));
            UIManager.put("ComboBox.selectionBackground", new Color(0, 120, 215));
            UIManager.put("ComboBox.selectionForeground", Color.WHITE);
            UIManager.put("Button.background", new Color(0, 120, 215));
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("CheckBox.background", new Color(45, 45, 45));
            UIManager.put("CheckBox.foreground", new Color(220, 220, 220));


        } catch (Exception e) {
            System.err.println("Nimbus Look and Feel not available. Using default. " + e.getMessage());
        }
        // Ensure CarService and user data are initialized if AdminDashboard is run directly for testing
        CarService.initialize();
        LoginPage.loadUsers();

        SwingUtilities.invokeLater(() -> new AdminDashboard(LoginPage.users, LoginPage.allCars));
    }
}