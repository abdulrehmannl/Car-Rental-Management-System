package com.carrental;

import com.carrental.model.Car;
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
import java.awt.geom.Area;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CheckoutPage {

    private JFrame frame;
    private Car selectedCar;

    // Customer Details Fields
    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField phoneField;

    // Date Fields
    private JTextField pickupDateField;
    private JTextField returnDateField;

    // Validation Labels
    private JLabel fullNameValidationLabel;
    private JLabel emailValidationLabel;
    private JLabel phoneValidationLabel;
    private JLabel pickupDateValidationLabel;
    private JLabel returnDateValidationLabel;


    // UI Components for the frame
    private boolean isMaximized = false;
    // Store the *restored* (non-maximized) bounds.
    // Initialize with sensible defaults for the first restore operation.
    private int oldX;
    private int oldY;
    private int oldWidth;
    private int oldHeight;
    private JButton maximizeButton;

    // THEME COLORS (Consistent with RegistrationPage)
    private static final Color THEME_YELLOW = new Color(255, 193, 7);
    private static final Color THEME_YELLOW_DARK = new Color(255, 179, 0);
    private static final Color THEME_DARK_OVERLAY = new Color(0, 0, 0, 180);
    private static final Color THEME_BACKGROUND_FIELD = new Color(30, 30, 30); // Dark background for text fields
    private static final Color THEME_TEXT_PRIMARY = Color.WHITE;
    private static final Color THEME_TEXT_ACCENT = Color.BLACK; // Text on dark fields (used for field text)
    private static final Color THEME_BORDER_COLOR = new Color(70, 70, 70); // Darker border for dark fields
    private static final Color THEME_ERROR_RED = new Color(255, 90, 90); // For validation messages
    private static final Color THEME_SUCCESS_GREEN = new Color(144, 238, 144); // Light Green for success/price

    // Control button characters
    private static final String MINIMIZE_CHAR = "\u2014";
    private static final String MAXIMIZE_CHAR = "\u25A1"; // Square icon
    private static final String RESTORE_CHAR = "\u25A3";  // Overlapping squares icon
    private static final String CLOSE_CHAR = "X";

    // Date Format
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public CheckoutPage(Car car) {
        this.selectedCar = car;

        frame = new JFrame("Checkout - " + selectedCar.getName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);

        // Define a sensible default "restored" size and position.
        // This is crucial. If the frame starts maximized, these are the dimensions
        // it will revert to when the user clicks 'restore down'.
        // We set these *before* potentially maximizing the frame.
        oldWidth = 1050;
        oldHeight = 750;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        oldX = (screenSize.width - oldWidth) / 2;
        oldY = (screenSize.height - oldHeight) / 2;
        frame.setBounds(oldX, oldY, oldWidth, oldHeight); // Set initial bounds before showing

        JPanel rootPanel = new JPanel(new BorderLayout()) {
            private BufferedImage backgroundImage;
            {
                try {
                    backgroundImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/Images/123.jpg")));
                } catch (IOException e) {
                    System.err.println("Background image not found for CheckoutPage: " + e.getMessage());
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
        rootPanel.setOpaque(false);
        frame.setContentPane(rootPanel);

        // --- Header Panel for Window Controls and Dragging ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setPreferredSize(new Dimension(frame.getWidth(), 40)); // Width is irrelevant here, height is important

        // Only allow dragging if not maximized
        MouseAdapter ma = new MouseAdapter() {
            private Point initialClick;
            public void mousePressed(MouseEvent e) {
                if (!isMaximized) { initialClick = e.getPoint(); }
                // For double-click to maximize/restore:
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    toggleMaximize();
                }
            }
            public void mouseDragged(MouseEvent e) {
                if (!isMaximized && initialClick != null) {
                    // Get screen coordinates of the current frame
                    int thisX = frame.getLocation().x;
                    int thisY = frame.getLocation().y;

                    // Calculate movement relative to the initial click on the header
                    int xMoved = e.getXOnScreen() - (headerPanel.getLocationOnScreen().x + initialClick.x);
                    int yMoved = e.getYOnScreen() - (headerPanel.getLocationOnScreen().y + initialClick.y);

                    frame.setLocation(thisX + xMoved, thisY + yMoved);
                }
            }
        };
        headerPanel.addMouseListener(ma);
        headerPanel.addMouseMotionListener(ma);

        // --- Logout Button (Top-Left) ---
        JButton logoutButton = createTextButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutButton.setForeground(THEME_YELLOW);
        logoutButton.addActionListener(e -> {
            LoginPage.loggedInUser = null;
            LoginPage.loggedInUserRole = null;
            frame.dispose();
            new LoginPage();
        });
        JPanel leftHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftHeaderPanel.setOpaque(false);
        leftHeaderPanel.add(logoutButton);
        headerPanel.add(leftHeaderPanel, BorderLayout.WEST);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
        buttonPanel.setOpaque(false);

        JButton minimizeButton = createControlButton(MINIMIZE_CHAR);
        this.maximizeButton = createControlButton(MAXIMIZE_CHAR); // Initial text is maximize icon
        JButton closeButton = createControlButton(CLOSE_CHAR);

        minimizeButton.addActionListener(e -> frame.setState(Frame.ICONIFIED));
        this.maximizeButton.addActionListener(e -> toggleMaximize());
        closeButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(minimizeButton);
        buttonPanel.add(this.maximizeButton);
        buttonPanel.add(closeButton);

        headerPanel.add(buttonPanel, BorderLayout.EAST);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // --- Main Content Panel (using GridBagLayout for flexible arrangement) ---
        JPanel mainContentPanel = new JPanel(new GridBagLayout());
        mainContentPanel.setOpaque(false);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        // ------------------ Car Details Panel (Left Side) ------------------
        JPanel carDetailsPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(THEME_DARK_OVERLAY);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));
                g2.dispose();
            }
        };
        carDetailsPanel.setOpaque(false);
        carDetailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Car Image (with rounded corners)
        JLabel carImageLabel = new JLabel() {
            private BufferedImage img;
            {
                try {
                    String imagePath = selectedCar.getImagePath();
                    // Normalize path for resource loading
                    if (!imagePath.startsWith("/")) {
                        if (imagePath.startsWith("src/")) {
                            imagePath = imagePath.substring(3);
                        }
                        imagePath = "/" + imagePath;
                    }
                    img = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
                } catch (IOException e) {
                    System.err.println("Car image not found for rendering: " + selectedCar.getImagePath() + " - " + e.getMessage());
                    // Fallback to a placeholder image or empty if image not found
                } catch (NullPointerException e) {
                    System.err.println("Resource not found for image path: " + selectedCar.getImagePath() + " - " + e.getMessage());
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                if (img == null) {
                    super.paintComponent(g); // Draw default if image not loaded
                    return;
                }

                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                int width = getWidth();
                int height = getHeight();
                int arc = 25;

                // Create a rounded rectangle clip
                Area clip = new Area(new RoundRectangle2D.Double(0, 0, width, height, arc, arc));
                g2d.setClip(clip);

                // Draw the scaled image
                g2d.drawImage(img, 0, 0, width, height, this);

                g2d.dispose();
            }
        };
        carImageLabel.setPreferredSize(new Dimension(350, 250));
        carImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        carDetailsPanel.add(carImageLabel, BorderLayout.CENTER);


        // Car Info Panel
        JPanel carInfoPanel = new JPanel(new GridBagLayout());
        carInfoPanel.setOpaque(false);
        GridBagConstraints carInfoGbc = new GridBagConstraints();
        carInfoGbc.gridwidth = GridBagConstraints.REMAINDER; // Span full width
        carInfoGbc.fill = GridBagConstraints.HORIZONTAL;
        carInfoGbc.insets = new Insets(5, 0, 5, 0);

        JLabel carNameLabel = new JLabel(selectedCar.getName(), SwingConstants.CENTER);
        carNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        carNameLabel.setForeground(THEME_YELLOW);
        carInfoPanel.add(carNameLabel, carInfoGbc);

        // Reset GBC for two-column details
        carInfoGbc.gridwidth = 1; // Back to single column for sub-panels
        carInfoGbc.weightx = 0; // Not distributing extra horizontal space for titles
        carInfoGbc.anchor = GridBagConstraints.WEST;
        carInfoGbc.insets = new Insets(2, 0, 2, 0);

        // Use a wrapper panel for labels and values to ensure alignment
        JPanel detailsGridPanel = new JPanel(new GridBagLayout());
        detailsGridPanel.setOpaque(false);
        GridBagConstraints dgpGbc = new GridBagConstraints();
        dgpGbc.fill = GridBagConstraints.HORIZONTAL;
        dgpGbc.insets = new Insets(3, 5, 3, 5);

        int detailRow = 0;
        dgpGbc.gridx = 0; dgpGbc.gridy = detailRow; dgpGbc.anchor = GridBagConstraints.WEST; dgpGbc.weightx = 0.3;
        detailsGridPanel.add(createDetailLabel("Type:"), dgpGbc);
        dgpGbc.gridx = 1; dgpGbc.gridy = detailRow; dgpGbc.anchor = GridBagConstraints.WEST; dgpGbc.weightx = 0.7;
        detailsGridPanel.add(createDetailValue(selectedCar.getType()), dgpGbc);
        detailRow++;

        dgpGbc.gridx = 0; dgpGbc.gridy = detailRow; dgpGbc.anchor = GridBagConstraints.WEST; dgpGbc.weightx = 0.3;
        detailsGridPanel.add(createDetailLabel("Mileage:"), dgpGbc);
        dgpGbc.gridx = 1; dgpGbc.gridy = detailRow; dgpGbc.anchor = GridBagConstraints.WEST; dgpGbc.weightx = 0.7;
        detailsGridPanel.add(createDetailValue(selectedCar.getMileage()), dgpGbc);
        detailRow++;

        dgpGbc.gridx = 0; dgpGbc.gridy = detailRow; dgpGbc.anchor = GridBagConstraints.WEST; dgpGbc.weightx = 0.3;
        detailsGridPanel.add(createDetailLabel("Max Speed:"), dgpGbc);
        dgpGbc.gridx = 1; dgpGbc.gridy = detailRow; dgpGbc.anchor = GridBagConstraints.WEST; dgpGbc.weightx = 0.7;
        detailsGridPanel.add(createDetailValue(selectedCar.getMaxSpeed()), dgpGbc);
        detailRow++;

        dgpGbc.gridx = 0; dgpGbc.gridy = detailRow; dgpGbc.anchor = GridBagConstraints.WEST; dgpGbc.weightx = 0.3;
        detailsGridPanel.add(createDetailLabel("Seats:"), dgpGbc);
        dgpGbc.gridx = 1; dgpGbc.gridy = detailRow; dgpGbc.anchor = GridBagConstraints.WEST; dgpGbc.weightx = 0.7;
        detailsGridPanel.add(createDetailValue(selectedCar.getSeats()), dgpGbc);
        detailRow++;

        dgpGbc.gridx = 0; dgpGbc.gridy = detailRow; dgpGbc.anchor = GridBagConstraints.WEST; dgpGbc.weightx = 0.3;
        detailsGridPanel.add(createDetailLabel("Transmission:"), dgpGbc);
        dgpGbc.gridx = 1; dgpGbc.gridy = detailRow; dgpGbc.anchor = GridBagConstraints.WEST; dgpGbc.weightx = 0.7;
        detailsGridPanel.add(createDetailValue(selectedCar.getTransmission()), dgpGbc);
        detailRow++;

        dgpGbc.gridx = 0; dgpGbc.gridy = detailRow; dgpGbc.anchor = GridBagConstraints.WEST; dgpGbc.weightx = 0.3;
        detailsGridPanel.add(createDetailLabel("Vehicle Class:"), dgpGbc);
        dgpGbc.gridx = 1; dgpGbc.gridy = detailRow; dgpGbc.anchor = GridBagConstraints.WEST; dgpGbc.weightx = 0.7;
        detailsGridPanel.add(createDetailValue(selectedCar.getVehicleClass()), dgpGbc);
        detailRow++;

        // Add the detailsGridPanel to the carInfoPanel
        carInfoGbc.gridwidth = GridBagConstraints.REMAINDER; // Span full width again
        carInfoGbc.fill = GridBagConstraints.BOTH;
        carInfoGbc.insets = new Insets(10, 0, 5, 0);
        carInfoPanel.add(detailsGridPanel, carInfoGbc);


        // Car Price (Green Color)
        JLabel priceLabel = new JLabel(String.format("Daily Price: $%.2f", selectedCar.getPrice()), SwingConstants.CENTER);
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        priceLabel.setForeground(THEME_SUCCESS_GREEN);
        carInfoGbc.insets = new Insets(15, 0, 0, 0);
        carInfoPanel.add(priceLabel, carInfoGbc);

        carDetailsPanel.add(carInfoPanel, BorderLayout.SOUTH);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        mainContentPanel.add(carDetailsPanel, gbc);

        // ------------------ Customer & Rental Details Panel (Right Side) ------------------
        JPanel checkoutFormPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(THEME_DARK_OVERLAY);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));
                g2.dispose();
            }
        };
        checkoutFormPanel.setOpaque(false);
        checkoutFormPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.gridwidth = GridBagConstraints.REMAINDER; // Span full width
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        formGbc.insets = new Insets(0, 0, 0, 0);

        JLabel formTitleLabel = new JLabel("Rental Details", SwingConstants.CENTER);
        formTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        formTitleLabel.setForeground(THEME_YELLOW);
        checkoutFormPanel.add(formTitleLabel, formGbc);

        checkoutFormPanel.add(Box.createRigidArea(new Dimension(0, 20)), formGbc); // Spacer


        // --- NEW: Panel for Two-Column Fields ---
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);

        GridBagConstraints fieldsGbc = new GridBagConstraints();
        fieldsGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldsGbc.weightx = 0.5; // Distribute horizontal space evenly
        fieldsGbc.insets = new Insets(8, 10, 0, 10);

        int row = 0;

        // Customer Details
        JLabel customerDetailsTitle = new JLabel("Customer Information", SwingConstants.LEFT);
        customerDetailsTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        customerDetailsTitle.setForeground(THEME_TEXT_PRIMARY);
        // Use formGbc for this title as it's outside the fieldsPanel, spanning full width
        formGbc.insets = new Insets(20, 0, 5, 0);
        checkoutFormPanel.add(customerDetailsTitle, formGbc);


        // Add customer fields to the fieldsPanel (two columns)
        fieldsGbc.gridx = 0; fieldsGbc.gridy = row;
        fullNameField = createTextField("Full Name");
        fieldsPanel.add(createLabeledField("Full Name:", fullNameField), fieldsGbc);

        fieldsGbc.gridx = 1; fieldsGbc.gridy = row;
        emailField = createTextField("Email Address");
        fieldsPanel.add(createLabeledField("Email:", emailField), fieldsGbc);
        row++;

        // Validation labels for customer fields (below their respective fields)
        fieldsGbc.gridx = 0; fieldsGbc.gridy = row; fieldsGbc.insets = new Insets(0, 10, 5, 10);
        fullNameValidationLabel = createValidationLabel();
        fieldsPanel.add(fullNameValidationLabel, fieldsGbc);

        fieldsGbc.gridx = 1; fieldsGbc.gridy = row;
        emailValidationLabel = createValidationLabel();
        fieldsPanel.add(emailValidationLabel, fieldsGbc);
        row++;

        fieldsGbc.insets = new Insets(8, 10, 0, 10); // Reset insets for next fields

        // Phone field (spanning both columns)
        fieldsGbc.gridx = 0; fieldsGbc.gridy = row; fieldsGbc.gridwidth = 2; // Span 2 columns
        phoneField = createTextField("Phone Number (11 digits)");
        fieldsPanel.add(createLabeledField("Phone Number:", phoneField), fieldsGbc);
        row++;

        fieldsGbc.gridx = 0; fieldsGbc.gridy = row; fieldsGbc.gridwidth = 2; fieldsGbc.insets = new Insets(0, 10, 5, 10);
        phoneValidationLabel = createValidationLabel();
        fieldsPanel.add(phoneValidationLabel, fieldsGbc);
        row++;

        // Add the fieldsPanel (containing customer details) to the main checkoutFormPanel
        formGbc.gridwidth = GridBagConstraints.REMAINDER; // Span full width
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        formGbc.insets = new Insets(0, 0, 0, 0); // Reset insets
        checkoutFormPanel.add(fieldsPanel, formGbc);


        // Rental Dates Title (outside fieldsPanel, full width)
        JLabel rentalDatesTitle = new JLabel("Rental Dates", SwingConstants.LEFT);
        rentalDatesTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        rentalDatesTitle.setForeground(THEME_TEXT_PRIMARY);
        formGbc.insets = new Insets(20, 0, 5, 0);
        checkoutFormPanel.add(rentalDatesTitle, formGbc);

        // Add rental date fields to a new fieldsPanel for dates (or re-use the same one if desired)
        JPanel dateFieldsPanel = new JPanel(new GridBagLayout());
        dateFieldsPanel.setOpaque(false);
        GridBagConstraints dateFieldsGbc = new GridBagConstraints();
        dateFieldsGbc.fill = GridBagConstraints.HORIZONTAL;
        dateFieldsGbc.weightx = 0.5;
        dateFieldsGbc.insets = new Insets(8, 10, 0, 10);

        int dateRow = 0;
        dateFieldsGbc.gridx = 0; dateFieldsGbc.gridy = dateRow;
        pickupDateField = createTextField("YYYY-MM-DD");
        dateFieldsPanel.add(createLabeledFieldWithCalendar("Pickup Date:", pickupDateField), dateFieldsGbc);

        dateFieldsGbc.gridx = 1; dateFieldsGbc.gridy = dateRow;
        returnDateField = createTextField("YYYY-MM-DD");
        dateFieldsPanel.add(createLabeledFieldWithCalendar("Return Date:", returnDateField), dateFieldsGbc);
        dateRow++;

        dateFieldsGbc.gridx = 0; dateFieldsGbc.gridy = dateRow; dateFieldsGbc.insets = new Insets(0, 10, 5, 10);
        pickupDateValidationLabel = createValidationLabel();
        dateFieldsPanel.add(pickupDateValidationLabel, dateFieldsGbc);

        dateFieldsGbc.gridx = 1; dateFieldsGbc.gridy = dateRow;
        returnDateValidationLabel = createValidationLabel();
        dateFieldsPanel.add(returnDateValidationLabel, dateFieldsGbc);
        dateRow++;

        // Add the dateFieldsPanel to the main checkoutFormPanel
        formGbc.insets = new Insets(0, 0, 0, 0); // Reset insets
        checkoutFormPanel.add(dateFieldsPanel, formGbc);


        // Confirm Button
        JButton confirmButton = createStyledButton("Confirm Booking", THEME_YELLOW);
        formGbc.insets = new Insets(30, 0, 10, 0);
        formGbc.fill = GridBagConstraints.NONE; // Don't fill horizontally
        formGbc.anchor = GridBagConstraints.CENTER; // Center the button
        checkoutFormPanel.add(confirmButton, formGbc);

        confirmButton.addActionListener(e -> handleBookingConfirmation());

        // Back Button
        JButton backButton = createTextButton("Back to Car List");
        formGbc.insets = new Insets(10, 0, 0, 0);
        checkoutFormPanel.add(backButton, formGbc);
        backButton.addActionListener(e -> {
            frame.dispose();
            new CarListPage();
        });


        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        mainContentPanel.add(checkoutFormPanel, gbc);

        rootPanel.add(mainContentPanel, BorderLayout.CENTER);


        // ************ IMPORTANT CHANGE ************
        // Removed frame.addComponentListener for setShape.
        // setShape will now be directly managed by toggleMaximize().
        // ******************************************

        frame.setVisible(true); // Make frame visible first

        // Call toggleMaximize to ensure it starts in a maximized state.
        // The first call will set isMaximized to true and apply maxBounds.
        // Subsequent calls will toggle.
        toggleMaximize();


        // Add focus listeners for real-time validation feedback
        fullNameField.addFocusListener(new FocusAdapter() { @Override public void focusLost(FocusEvent e) { validateFullName(); } });
        emailField.addFocusListener(new FocusAdapter() { @Override public void focusLost(FocusEvent e) { validateEmail(); } });
        phoneField.addFocusListener(new FocusAdapter() { @Override public void focusLost(FocusEvent e) { validatePhone(); } });
        pickupDateField.addFocusListener(new FocusAdapter() { @Override public void focusLost(FocusEvent e) { validatePickupDate(); } });
        returnDateField.addFocusListener(new FocusAdapter() { @Override public void focusLost(FocusEvent e) { validateReturnDate(); } });

        // Pre-fill user details if logged in
        if (LoginPage.getLoggedInUser() != null) {
            User user = LoginPage.getLoggedInUser();
            fullNameField.setText(user.getFullName());
            emailField.setText(user.getEmail());
            phoneField.setText(user.getPhoneNumber());
        }
    }

    // New helper methods for car details alignment
    private JLabel createDetailLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(THEME_YELLOW);
        return label;
    }

    private JLabel createDetailValue(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(THEME_TEXT_PRIMARY);
        return label;
    }

    private void handleBookingConfirmation() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String pickupDateStr = pickupDateField.getText().trim();
        String returnDateStr = returnDateField.getText().trim();

        boolean isValid = true;

        // Reset all validation labels
        fullNameValidationLabel.setText("");
        emailValidationLabel.setText("");
        phoneValidationLabel.setText("");
        pickupDateValidationLabel.setText("");
        returnDateValidationLabel.setText("");

        // Perform all validations
        // Chaining `&&` ensures all validation methods are called, even if one fails initially.
        // The order is important if one validation depends on another (e.g., return date depends on pickup).
        isValid = validateFullName() && isValid;
        isValid = validateEmail() && isValid;
        isValid = validatePhone() && isValid;
        isValid = validatePickupDate() && isValid; // This also triggers validateReturnDate() internally
        // Removed separate validateReturnDate() call here as it's handled by validatePickupDate()
        // If you need to call it independently, ensure it handles cases where pickupDate is not yet valid.
        // For current logic: `isValid = validateReturnDate() && isValid;` would be needed if you always want it checked.

        if (!isValid) {
            JOptionPane.showMessageDialog(frame, "Please correct the errors in the form.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date pickupDate = null;
        Date returnDate = null;
        try {
            pickupDate = dateFormat.parse(pickupDateStr);
            returnDate = dateFormat.parse(returnDateStr);
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(frame, "Internal date parsing error. Please check date formats.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Calculate rental days
        long diffInMillies = Math.abs(returnDate.getTime() - pickupDate.getTime());
        // Add 1 day if pickup and return are on the same day for a 1-day rental.
        // Otherwise, add 1 to include the return day in the count.
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        diff = (diff == 0 && pickupDate.equals(returnDate)) ? 1 : diff + 1; // Correctly handle single-day rentals

        double totalCost = selectedCar.getPrice() * diff;

        // Confirmation Message
        String confirmationMessage = String.format(
                "Confirm your booking details:\n\n" +
                        "Car: %s\n" +
                        "Pickup Date: %s\n" +
                        "Return Date: %s\n" +
                        "Rental Days: %d\n" +
                        "Total Cost: $%.2f\n\n" +
                        "Customer Name: %s\n" +
                        "Email: %s\n" +
                        "Phone: %s\n\n" +
                        "Proceed with booking?",
                selectedCar.getName(), pickupDateStr, returnDateStr, diff, totalCost, fullName, email, phone);

        int confirm = JOptionPane.showConfirmDialog(frame, confirmationMessage, "Confirm Booking", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(frame, "Booking confirmed for " + selectedCar.getName() + "!\nTotal Cost: $" + String.format("%.2f", totalCost), "Booking Successful", JOptionPane.INFORMATION_MESSAGE);

            frame.dispose();
            new FeedbackPage(); // Assuming FeedbackPage exists
        }
    }

    // --- Validation Methods ---
    private boolean validateFullName() {
        String fullName = fullNameField.getText().trim();
        if (fullName.isEmpty()) {
            fullNameValidationLabel.setText("Full name is required.");
            fullNameValidationLabel.setForeground(THEME_ERROR_RED);
            return false;
        }
        fullNameValidationLabel.setText("");
        return true;
    }

    private boolean validateEmail() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            emailValidationLabel.setText("Email is required.");
            emailValidationLabel.setForeground(THEME_ERROR_RED);
            return false;
        } else if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            emailValidationLabel.setText("Invalid email format.");
            emailValidationLabel.setForeground(THEME_ERROR_RED);
            return false;
        }
        emailValidationLabel.setText("");
        return true;
    }

    private boolean validatePhone() {
        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) {
            phoneValidationLabel.setText("Phone number is required.");
            phoneValidationLabel.setForeground(THEME_ERROR_RED);
            return false;
        } else if (!phone.matches("^\\d{11}$")) { // Ensure it's exactly 11 digits
            phoneValidationLabel.setText("Phone must be 11 digits.");
            phoneValidationLabel.setForeground(THEME_ERROR_RED);
            return false;
        }
        phoneValidationLabel.setText("");
        return true;
    }

    private boolean validatePickupDate() {
        String pickupDateStr = pickupDateField.getText().trim();
        if (pickupDateStr.isEmpty()) {
            pickupDateValidationLabel.setText("Pickup date is required.");
            pickupDateValidationLabel.setForeground(THEME_ERROR_RED);
            return false;
        }
        try {
            Date pickup = dateFormat.parse(pickupDateStr);
            Calendar todayCal = Calendar.getInstance();
            todayCal.set(Calendar.HOUR_OF_DAY, 0);
            todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);
            todayCal.set(Calendar.MILLISECOND, 0);

            if (pickup.before(todayCal.getTime())) {
                pickupDateValidationLabel.setText("Pickup date cannot be in the past.");
                pickupDateValidationLabel.setForeground(THEME_ERROR_RED);
                return false;
            }
        } catch (ParseException e) {
            pickupDateValidationLabel.setText("Invalid date format (YYYY-MM-DD).");
            pickupDateValidationLabel.setForeground(THEME_ERROR_RED);
            return false;
        }
        pickupDateValidationLabel.setText("");
        // After validating pickup date, re-validate return date in case pickup date changed
        validateReturnDate();
        return true;
    }

    private boolean validateReturnDate() {
        String pickupDateStr = pickupDateField.getText().trim();
        String returnDateStr = returnDateField.getText().trim();

        if (returnDateStr.isEmpty()) {
            returnDateValidationLabel.setText("Return date is required.");
            returnDateValidationLabel.setForeground(THEME_ERROR_RED);
            return false;
        }
        try {
            // Ensure pickup date is valid before comparing
            if (pickupDateStr.isEmpty() || !dateFormat.parse(pickupDateStr).after(new Date(0))) { // Simple check if parsed. More robust would be to call validatePickupDate()
                // If pickup date is invalid or empty, return date cannot be properly validated against it.
                // We assume validatePickupDate() was called or will be called.
                return false; // Prevent comparison with invalid pickup date
            }

            Date pickup = dateFormat.parse(pickupDateStr);
            Date ret = dateFormat.parse(returnDateStr);

            if (ret.before(pickup)) {
                returnDateValidationLabel.setText("Return date before pickup date.");
                returnDateValidationLabel.setForeground(THEME_ERROR_RED);
                return false;
            }
        } catch (ParseException e) {
            returnDateValidationLabel.setText("Invalid date format (YYYY-MM-DD).");
            returnDateValidationLabel.setForeground(THEME_ERROR_RED);
            return false;
        }
        returnDateValidationLabel.setText("");
        return true;
    }

    private JLabel createValidationLabel() {
        JLabel label = new JLabel("");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(THEME_ERROR_RED);
        label.setPreferredSize(new Dimension(200, 15)); // Give it a fixed size
        label.setMinimumSize(new Dimension(200, 15));
        label.setMaximumSize(new Dimension(200, 15));
        return label;
    }

    // --- Custom Calendar Panel Class (Inner Class) ---
    private class CalendarPanel extends JPanel {
        private JTextField targetField;
        private JDialog parentDialog;
        private Calendar currentCalendar;
        private JLabel monthYearLabel;
        private JPanel daysPanel;
        private SimpleDateFormat dateFormat;

        public CalendarPanel(JTextField targetField, JDialog parentDialog, SimpleDateFormat dateFormat) {
            this.targetField = targetField;
            this.parentDialog = parentDialog;
            this.dateFormat = dateFormat;
            this.currentCalendar = Calendar.getInstance();
            try {
                if (!targetField.getText().isEmpty()) {
                    currentCalendar.setTime(dateFormat.parse(targetField.getText()));
                }
            } catch (ParseException e) {
                // Ignore parse errors for initial calendar display, default to current date
            }


            setLayout(new BorderLayout());
            setBackground(new Color(40, 40, 40));
            setBorder(BorderFactory.createLineBorder(THEME_YELLOW, 2));

            // Header for month/year navigation
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(new Color(60, 60, 60));
            headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JButton prevMonth = createCalendarNavButton("<");
            prevMonth.addActionListener(e -> changeMonth(-1));
            JButton nextMonth = createCalendarNavButton(">");
            nextMonth.addActionListener(e -> changeMonth(1));

            monthYearLabel = new JLabel("", SwingConstants.CENTER);
            monthYearLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            monthYearLabel.setForeground(THEME_YELLOW);

            headerPanel.add(prevMonth, BorderLayout.WEST);
            headerPanel.add(monthYearLabel, BorderLayout.CENTER);
            headerPanel.add(nextMonth, BorderLayout.EAST);
            add(headerPanel, BorderLayout.NORTH);

            // Day of week labels
            JPanel weekDaysPanel = new JPanel(new GridLayout(1, 7));
            weekDaysPanel.setBackground(new Color(70, 70, 70));
            String[] weekDays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            for (String day : weekDays) {
                JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
                dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                dayLabel.setForeground(THEME_TEXT_PRIMARY);
                weekDaysPanel.add(dayLabel);
            }
            add(weekDaysPanel, BorderLayout.NORTH); // Changed to NORTH to stack above daysPanel

            // Days panel
            daysPanel = new JPanel(new GridLayout(0, 7, 1, 1)); // 0 rows means dynamic based on content
            daysPanel.setBackground(new Color(50, 50, 50));
            add(daysPanel, BorderLayout.CENTER); // Changed to CENTER to fill available space

            updateCalendar();
        }

        private JButton createCalendarNavButton(String text) {
            JButton button = new JButton(text);
            button.setFont(new Font("Segoe UI", Font.BOLD, 16));
            button.setForeground(THEME_YELLOW);
            button.setBackground(new Color(60, 60, 60));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setContentAreaFilled(true);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return button;
        }

        private void changeMonth(int amount) {
            currentCalendar.add(Calendar.MONTH, amount);
            updateCalendar();
        }

        private void updateCalendar() {
            daysPanel.removeAll();
            // Corrected format string for full month name and 4-digit year
            monthYearLabel.setText(new SimpleDateFormat("MMMM yyyy").format(currentCalendar.getTime()));

            Calendar displayCalendar = (Calendar) currentCalendar.clone();
            displayCalendar.set(Calendar.DAY_OF_MONTH, 1);

            int firstDayOfWeek = displayCalendar.get(Calendar.DAY_OF_WEEK); // 1 = Sunday, 7 = Saturday
            int daysInMonth = displayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            // Add empty labels for days before the 1st of the month
            for (int i = 1; i < firstDayOfWeek; i++) {
                daysPanel.add(new JLabel(""));
            }

            for (int i = 1; i <= daysInMonth; i++) {
                JButton dayButton = new JButton(String.valueOf(i));
                dayButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                dayButton.setFocusPainted(false);
                dayButton.setBorderPainted(false);
                dayButton.setContentAreaFilled(true);
                dayButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

                Calendar dayCal = (Calendar) currentCalendar.clone();
                dayCal.set(Calendar.DAY_OF_MONTH, i);

                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);

                boolean isToday = dayCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        dayCal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                        dayCal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);

                Date selectedDate = null;
                try {
                    if (!targetField.getText().isEmpty()) {
                        selectedDate = dateFormat.parse(targetField.getText());
                    }
                } catch (ParseException ex) { /* ignore */ }

                boolean isSelectedDay = (selectedDate != null &&
                        dayCal.get(Calendar.YEAR) == (selectedDate.getYear() + 1900) && // Year is 1900-based
                        dayCal.get(Calendar.MONTH) == selectedDate.getMonth() &&
                        dayCal.get(Calendar.DAY_OF_MONTH) == selectedDate.getDate());


                if (isToday) {
                    dayButton.setBackground(THEME_YELLOW_DARK);
                    dayButton.setForeground(THEME_TEXT_ACCENT);
                    dayButton.setBorder(BorderFactory.createLineBorder(THEME_YELLOW, 1));
                } else if (isSelectedDay) {
                    dayButton.setBackground(new Color(100, 100, 100));
                    dayButton.setForeground(THEME_YELLOW);
                    dayButton.setBorder(BorderFactory.createLineBorder(THEME_YELLOW_DARK, 1)); // Highlight selected
                }
                else {
                    dayButton.setBackground(new Color(90, 90, 90));
                    dayButton.setForeground(THEME_TEXT_PRIMARY);
                    dayButton.setBorder(null); // No border for regular days
                }

                int day = i;
                dayButton.addActionListener(e -> {
                    currentCalendar.set(Calendar.DAY_OF_MONTH, day);
                    targetField.setText(dateFormat.format(currentCalendar.getTime()));
                    parentDialog.dispose(); // Close calendar after selection
                });
                daysPanel.add(dayButton);
            }
            daysPanel.revalidate();
            daysPanel.repaint();
        }
    }

    // --- Maximize/Restore Toggle Logic ---
    private void toggleMaximize() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle maxBounds = env.getMaximumWindowBounds(); // Screen bounds excluding taskbar

        if (isMaximized) {
            // Currently maximized, so restore
            frame.setBounds(oldX, oldY, oldWidth, oldHeight);
            frame.setShape(new RoundRectangle2D.Double(0, 0, oldWidth, oldHeight, 20, 20));
            if (this.maximizeButton != null) {
                this.maximizeButton.setText(MAXIMIZE_CHAR); // Show maximize icon
            }
            isMaximized = false;
        } else {
            // Currently not maximized, so maximize
            // SAVE CURRENT BOUNDS *BEFORE* MAXIMIZING. These are the "old" bounds for restoration.
            oldX = frame.getX();
            oldY = frame.getY();
            oldWidth = frame.getWidth();
            oldHeight = frame.getHeight();

            frame.setShape(null); // Remove rounded corners for maximized state
            frame.setBounds(maxBounds); // Maximize to screen bounds
            if (this.maximizeButton != null) {
                this.maximizeButton.setText(RESTORE_CHAR); // Show restore icon
            }
            isMaximized = true;
        }
        // Force repaint of content pane to ensure components redraw correctly
        frame.getContentPane().revalidate();
        frame.getContentPane().repaint();
    }

    // --- Custom Control Button Creation ---
    private JButton createControlButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false); // Make it transparent
        button.setOpaque(false);
        button.setForeground(THEME_TEXT_PRIMARY);
        button.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        button.setMargin(new Insets(0, 8, 0, 8));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setForeground(new Color(220, 220, 220)); }
            public void mouseExited(MouseEvent e) { button.setForeground(THEME_TEXT_PRIMARY); }
        });
        return button;
    }

    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(200, 40));
        field.setMaximumSize(new Dimension(200, 40));
        field.setForeground(THEME_TEXT_PRIMARY);
        field.setBackground(THEME_BACKGROUND_FIELD);
        field.setCaretColor(THEME_TEXT_PRIMARY);
        // Using client properties for placeholder text (requires modern Swing L&F or custom UI)
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.putClientProperty("JTextField.showPlaceholder", true);

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

    private JPanel createLabeledField(String labelText, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(THEME_TEXT_PRIMARY);
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLabeledFieldWithCalendar(String labelText, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(THEME_TEXT_PRIMARY);
        panel.add(lbl, BorderLayout.NORTH);

        JPanel fieldWithButtonPanel = new JPanel(new BorderLayout());
        fieldWithButtonPanel.setOpaque(false);
        fieldWithButtonPanel.add(field, BorderLayout.CENTER);

        JButton calendarButton = new JButton("🗓️"); // Calendar icon
        calendarButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        calendarButton.setFocusPainted(false);
        calendarButton.setBorderPainted(false);
        calendarButton.setContentAreaFilled(true);
        calendarButton.setOpaque(true);
        calendarButton.setBackground(THEME_YELLOW);
        calendarButton.setForeground(Color.BLACK);
        calendarButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calendarButton.setPreferredSize(new Dimension(40, 40)); // Fixed size for the button

        calendarButton.setBorder(new CompoundBorder(
                new LineBorder(THEME_BORDER_COLOR, 1), // Use a border for the button to match theme
                new EmptyBorder(0, 0, 0, 0)
        ));
        calendarButton.addActionListener(e -> {
            JDialog calendarDialog = new JDialog(frame, "Select Date", true); // Modal dialog
            calendarDialog.setUndecorated(true); // Custom look for the dialog
            CalendarPanel calendarPanel = new CalendarPanel(field, calendarDialog, dateFormat);
            calendarDialog.getContentPane().add(calendarPanel);
            calendarDialog.pack(); // Size the dialog to fit its contents

            // Position the calendar dialog relative to the text field
            Point fieldLocation = field.getLocationOnScreen();
            Dimension calendarSize = calendarDialog.getSize();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            int x = fieldLocation.x;
            int y = fieldLocation.y + field.getHeight(); // Below the text field

            // Adjust position if it goes off-screen
            if (y + calendarSize.height > screenSize.height - 20) { // -20 for some margin from bottom
                y = fieldLocation.y - calendarSize.height - 5; // Try to put it above the field
                if (y < 0) { // If still off-screen, put it at top
                    y = 0;
                }
            }

            if (x + calendarSize.width > screenSize.width) {
                x = screenSize.width - calendarSize.width; // Adjust to fit on right
            }
            if (x < 0) {
                x = 0; // Adjust to fit on left
            }

            calendarDialog.setLocation(x, y);
            calendarDialog.setVisible(true);
        });
        fieldWithButtonPanel.add(calendarButton, BorderLayout.EAST);
        panel.add(fieldWithButtonPanel, BorderLayout.CENTER);

        return panel;
    }


    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setForeground(Color.BLACK); // Text color for the yellow button
        button.setBackground(color);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Color darkerColor = THEME_YELLOW_DARK; // Use the predefined darker yellow
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
                super(text);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setForeground(THEME_YELLOW);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(new Cursor(Cursor.HAND_CURSOR));

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
                    g2.drawLine(textX, textY + 2, textX + textWidth, textY + 2); // Underline effect
                    g2.dispose();
                }
            }
        }
        return new HoverButton(text);
    }

    public static void main(String[] args) {
        // Dummy data for testing purposes
        Car dummyCar = new Car(
                "Luxurious Sedan", true, "Sedan", 4.5, "/Images/hav.jpg",
                "15 km/l", "180 km/h", "5", "Automatic", "Compact", 30.00, "2023-01-01"
        );

        // Ensure LoginPage.users and LoginPage.loggedInUser are initialized for testing
        // In a real application, these would come from a login process
        if (LoginPage.users == null || LoginPage.users.isEmpty()) {
            LoginPage.users = new java.util.ArrayList<>();
            LoginPage.users.add(new User("dummyUser", "dummyPass", "User", "Dummy Full Name", "03451234567", "dummy@example.com"));
            LoginPage.users.add(new User("testuser", "password123", "User", "Jane Smith", "03119876543", "jane.smith@example.com"));
        }
        // Simulate a logged-in user for pre-filling fields
        LoginPage.loggedInUser = LoginPage.getUser("dummyUser");

        SwingUtilities.invokeLater(() -> new CheckoutPage(dummyCar));
    }
}