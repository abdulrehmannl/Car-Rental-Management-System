// File: src/com/carrental/CarDetailPage.java

package com.carrental;

import com.carrental.model.User;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Timer;
import java.util.Objects;

// Import the actual classes from the com.carrental package
import com.carrental.CarListPage;
import com.carrental.PreviewBackgroundPage; // Assuming this class exists
import com.carrental.CheckoutPage; // Assuming this class exists
import com.carrental.LoginPage; // Explicitly import LoginPage to access User/loggedInUser
import com.carrental.model.Car; // IMPORTANT: Changed to use the standard Car model

public class CarDetailPage {

    private JFrame frame;
    private Car selectedCar;

    // For maximizing/restoring window state
    private boolean isMaximized = false;
    private int oldX, oldY, oldWidth, oldHeight;
    private JButton maximizeButton;

    // Constants for window control button characters
    private static final String MINIMIZE_CHAR = "\u2014"; // Em Dash
    private static final String MAXIMIZE_CHAR = "\u25A1"; // White Square (for maximize)
    private static final String RESTORE_CHAR = "\u25A3"; // White Square with Black Square (common restore icon)
    private static final String CLOSE_CHAR = "X";

    // THEME COLORS (Consistent with other pages)
    private static final Color THEME_YELLOW = new Color(255, 193, 7); // Primary Yellow
    private static final Color THEME_YELLOW_DARK = new Color(255, 179, 0); // Darker Yellow for hover
    private static final Color THEME_DARK_OVERLAY = new Color(0, 0, 0, 180); // Semi-transparent black for panels
    private static final Color THEME_BACKGROUND_STATIC_OVERLAY = new Color(0, 0, 0, 120); // Fixed overlay for background
    private static final Color THEME_TEXT_PRIMARY = Color.WHITE; // Text on dark backgrounds
    private static final Color THEME_TEXT_ACCENT = Color.BLACK; // Text on yellow backgrounds
    private static final Color THEME_LIGHT_GRAY_TEXT = new Color(200, 200, 200); // For labels
    private static final Color THEME_RED_BUTTON = new Color(217, 14, 14); // For the new Preview Button
    private static final Color THEME_RED_BUTTON_DARK = new Color(180, 12, 12); // Darker red for hover
    private static final Color THEME_BLUE_BUTTON = new Color(66, 133, 244); // Back button
    private static final Color THEME_BLUE_BUTTON_DARK = new Color(50, 100, 200); // Darker blue

    // Animation related fields
    private Timer pulseTimer;
    private JButton bookNowButton;
    private float pulseAlpha = 0.0f;
    private boolean pulseIncreasing = true;

    // Declare originalImage as an instance variable here
    private BufferedImage originalImage;

    public CarDetailPage(Car car) {
        this.selectedCar = car;

        frame = new JFrame("Car Details - " + selectedCar.getName());
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // --- Initialize old bounds to a sensible default restored size/position ---
        oldWidth = 1000; // Default width
        oldHeight = 600; // Default height
        frame.setSize(oldWidth, oldHeight);
        frame.setLocationRelativeTo(null); // Center the frame for its initial restored size
        oldX = frame.getX(); // Capture the X position of the centered frame
        oldY = frame.getY(); // Capture the Y position of the centered frame

        // --- Set frame to maximize like Chrome (showing taskbar) ---
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle maxBounds = env.getMaximumWindowBounds(); // This gives bounds excluding taskbar
        frame.setBounds(maxBounds); // Set frame to these bounds
        isMaximized = true; // Set flag to true as it starts in a maximized state


        // --- Root Panel with Static Background Image and Fixed Overlay ---
        JPanel rootPanel = new JPanel(new BorderLayout()) {
            private BufferedImage backgroundImage;
            { // Instance initializer to load image once
                try {
                    URL bgUrl = getClass().getResource("/Images/bgl.jpg");
                    if (bgUrl != null) {
                        backgroundImage = ImageIO.read(bgUrl);
                    } else {
                        System.err.println("Background image '/Images/bgl.jpg' not found on classpath for CarDetailPage.");
                    }
                } catch (IOException e) {
                    System.err.println("Exception loading background image for CarDetailPage: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid URL for background image in CarDetailPage. Path: /Images/bgl.jpg - " + e.getMessage());
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
                g2d.setColor(THEME_BACKGROUND_STATIC_OVERLAY);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        rootPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // --- Header Panel for Window Controls and Dragging ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setPreferredSize(new Dimension(frame.getWidth(), 40));

        MouseAdapter frameDragListener = new MouseAdapter() {
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
                    int xMoved = thisX + e.getX() - initialClick.x;
                    int yMoved = thisY + e.getY() - initialClick.y;
                    frame.setLocation(xMoved, yMoved);
                }
            }
        };
        headerPanel.addMouseListener(frameDragListener);
        headerPanel.addMouseMotionListener(frameDragListener);

        // --- Logout Button (Top-Left Corner) ---
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


        JPanel windowControlButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
        windowControlButtonPanel.setOpaque(false);

        JButton minimizeButton = createWindowButton(MINIMIZE_CHAR);
        this.maximizeButton = createWindowButton(RESTORE_CHAR); // Initial state is "restore" icon as it's maximized
        JButton closeButton = createWindowButton(CLOSE_CHAR);

        minimizeButton.addActionListener(e -> frame.setState(JFrame.ICONIFIED));
        this.maximizeButton.addActionListener(e -> toggleMaximize());
        closeButton.addActionListener(e -> frame.dispose());

        windowControlButtonPanel.add(minimizeButton);
        windowControlButtonPanel.add(this.maximizeButton);
        windowControlButtonPanel.add(closeButton);

        headerPanel.add(windowControlButtonPanel, BorderLayout.EAST);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // --- Central Content Panel with iOS-like Blur Effect ---
        JPanel contentPanel = new JPanel() {
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
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BorderLayout(20, 20));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentPanel.setPreferredSize(new Dimension(700, 500)); // Still a good preferred size for its content

        // --- Title ---
        JLabel titleLabel = new JLabel("CAR DETAILS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(THEME_TEXT_PRIMARY);
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        // --- Car Image & Details Panel (Horizontal Split) ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOpaque(false);
        splitPane.setDividerSize(0);
        splitPane.setResizeWeight(0.5);

        // Left Panel: Car Image
        JPanel imagePanel = new JPanel(new GridBagLayout());
        imagePanel.setOpaque(false);

        // Custom JPanel for the rounded image
        JPanel roundedImagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (originalImage != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                    int arcWidth = 20; // Adjust for desired roundness
                    int arcHeight = 20; // Adjust for desired roundness

                    // Create a rounded rectangle shape
                    RoundRectangle2D roundedRect = new RoundRectangle2D.Double(
                            0, 0, getWidth(), getHeight(), arcWidth, arcHeight
                    );

                    // Set the clip to the rounded rectangle
                    g2d.setClip(roundedRect);

                    // Draw the image scaled to fit the panel
                    g2d.drawImage(originalImage, 0, 0, getWidth(), getHeight(), this);
                    g2d.dispose();
                } else {
                    // Fallback if originalImage is null
                    g.setColor(Color.DARK_GRAY);
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    String msg = "No Image Available";
                    int x = (getWidth() - g.getFontMetrics().stringWidth(msg)) / 2;
                    int y = (getHeight() - g.getFontMetrics().getHeight()) / 2 + g.getFontMetrics().getAscent();
                    g.drawString(msg, x, y);
                }
            }
        };
        roundedImagePanel.setOpaque(false); // Make sure it's transparent
        roundedImagePanel.setPreferredSize(new Dimension(350, 200));

        // Load and scale image using selectedCar.getImagePath()
        try {
            String imagePath = selectedCar.getImagePath();
            // Ensure image path is a valid classpath resource path
            if (imagePath != null && !imagePath.startsWith("/")) {
                if (imagePath.startsWith("src/")) {
                    imagePath = imagePath.substring(3);
                }
                imagePath = "/" + imagePath;
            }

            URL imageUrl = getClass().getResource(imagePath);
            if (imageUrl != null) {
                originalImage = ImageIO.read(imageUrl);
                if (originalImage == null) {
                    JLabel errorLabel = new JLabel("Image data empty: " + selectedCar.getImagePath());
                    errorLabel.setForeground(Color.RED);
                    errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                    roundedImagePanel.add(errorLabel);
                    System.err.println("Image data was empty for " + selectedCar.getImagePath());
                }
            } else {
                JLabel errorLabel = new JLabel("Image not found: " + selectedCar.getImagePath());
                errorLabel.setForeground(Color.RED);
                errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                roundedImagePanel.add(errorLabel);
                System.err.println("Image URL is null for " + selectedCar.getImagePath());
            }
        } catch (IOException ex) {
            JLabel errorLabel = new JLabel("Error loading image: " + selectedCar.getImagePath());
            errorLabel.setForeground(Color.RED);
            errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            roundedImagePanel.add(errorLabel);
            System.err.println("IOException loading image for CarDetailPage: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IllegalArgumentException e) {
            JLabel errorLabel = new JLabel("Invalid image path: " + selectedCar.getImagePath());
            errorLabel.setForeground(Color.RED);
            errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            roundedImagePanel.add(errorLabel);
            System.err.println("IllegalArgumentException for image path in CarDetailPage: " + selectedCar.getImagePath() + " - " + e.getMessage());
        }


        imagePanel.add(roundedImagePanel);
        splitPane.setLeftComponent(imagePanel);


        // Right Panel: Details GridBagLayout
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setOpaque(false);
        detailsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbcDetails = new GridBagConstraints();
        gbcDetails.insets = new Insets(8, 5, 8, 5);
        gbcDetails.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 15);
        Font valueFont = new Font("Segoe UI", Font.PLAIN, 15);

        addDetailRow(detailsPanel, gbcDetails, "Car Name:", selectedCar.getName(), labelFont, valueFont, 0);
        addDetailRow(detailsPanel, gbcDetails, "Availability:", selectedCar.isAvailable() ? "Available" : "Not Available", labelFont, valueFont, 1);
        addDetailRow(detailsPanel, gbcDetails, "Car Type:", selectedCar.getType(), labelFont, valueFont, 2);
        addDetailRow(detailsPanel, gbcDetails, "Mileage:", selectedCar.getMileage(), labelFont, valueFont, 3);
        addDetailRow(detailsPanel, gbcDetails, "Max Speed:", selectedCar.getMaxSpeed(), labelFont, valueFont, 4);
        addDetailRow(detailsPanel, gbcDetails, "Rating:", String.valueOf(selectedCar.getRating()), labelFont, valueFont, 5);
        addDetailRow(detailsPanel, gbcDetails, "Seats:", selectedCar.getSeats(), labelFont, valueFont, 6);
        addDetailRow(detailsPanel, gbcDetails, "Transmission:", selectedCar.getTransmission(), labelFont, valueFont, 7);
        addDetailRow(detailsPanel, gbcDetails, "Vehicle Class:", selectedCar.getVehicleClass(), labelFont, valueFont, 8);
        addDetailRow(detailsPanel, gbcDetails, "Daily Price:", String.format("$%.2f", selectedCar.getPrice()), labelFont, valueFont, 9);

        splitPane.setRightComponent(detailsPanel);
        contentPanel.add(splitPane, BorderLayout.CENTER);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        buttonPanel.setOpaque(false);

        JButton goBackButton = createStyledButton("Go to Car List", THEME_BLUE_BUTTON, THEME_BLUE_BUTTON_DARK);
        JButton previewButton = createStyledButton("Preview Car", THEME_RED_BUTTON, THEME_RED_BUTTON_DARK);
        this.bookNowButton = createStyledButton("Book Now!", THEME_YELLOW, THEME_YELLOW_DARK);

        buttonPanel.add(goBackButton);
        buttonPanel.add(previewButton);
        buttonPanel.add(bookNowButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        JPanel centerWrapperPanel = new JPanel(new GridBagLayout());
        centerWrapperPanel.setOpaque(false);
        GridBagConstraints gbcCenter = new GridBagConstraints();
        gbcCenter.gridx = 0;
        gbcCenter.gridy = 0;
        gbcCenter.weightx = 0.0;
        gbcCenter.weighty = 0.0;
        gbcCenter.fill = GridBagConstraints.NONE;
        gbcCenter.anchor = GridBagConstraints.CENTER;
        centerWrapperPanel.add(contentPanel, gbcCenter);
        rootPanel.add(centerWrapperPanel, BorderLayout.CENTER);

        frame.setContentPane(rootPanel);

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

        // Set initial shape based on current maximized state
        // It starts maximized, so no rounded corners initially.
        frame.setShape(null);
        frame.setVisible(true);

        // --- Button Actions ---
        goBackButton.addActionListener(e -> {
            frame.dispose();
            new CarListPage();
        });

        previewButton.addActionListener(e -> {
            if (originalImage != null && classExists("com.carrental.PreviewBackgroundPage")) {
                String imagePathForPreview = selectedCar.getImagePath();
                if (imagePathForPreview != null && !imagePathForPreview.startsWith("/")) {
                    if (imagePathForPreview.startsWith("src/")) {
                        imagePathForPreview = imagePathForPreview.substring(3);
                    }
                    imagePathForPreview = "/" + imagePathForPreview;
                }
                new PreviewBackgroundPage(imagePathForPreview).setVisible(true);
            } else {
                if (originalImage == null) {
                    JOptionPane.showMessageDialog(frame, "Cannot preview: Car image failed to load. Check path: " + selectedCar.getImagePath(), "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "PreviewBackgroundPage class not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        bookNowButton.addActionListener(e -> {
            if (LoginPage.getLoggedInUser() == null) {
                JOptionPane.showMessageDialog(frame, "You must be logged in to book a car.", "Login Required", JOptionPane.INFORMATION_MESSAGE);
                frame.dispose();
                new LoginPage();
                return;
            }

            if (classExists("com.carrental.CheckoutPage")) {
                new CheckoutPage(selectedCar);
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(frame, "CheckoutPage class not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        startPulsingAnimation();
    }

    // --- Helper Methods ---

    private void toggleMaximize() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle maxBounds = env.getMaximumWindowBounds();

        if (isMaximized) {
            // Restore to old size and position
            frame.setBounds(oldX, oldY, oldWidth, oldHeight);
            frame.setShape(new RoundRectangle2D.Double(0, 0, oldWidth, oldHeight, 20, 20));
            if (this.maximizeButton != null) {
                this.maximizeButton.setText(MAXIMIZE_CHAR); // Change button to maximize icon
            }
            isMaximized = false;
        } else {
            // Save current bounds before maximizing
            oldX = frame.getX();
            oldY = frame.getY();
            oldWidth = frame.getWidth();
            oldHeight = frame.getHeight();

            frame.setShape(null); // Remove rounded corners when maximized
            frame.setBounds(maxBounds); // Set to maximized bounds
            if (this.maximizeButton != null) {
                this.maximizeButton.setText(RESTORE_CHAR); // Change button to restore icon
            }
            isMaximized = true;
        }
        frame.getContentPane().revalidate();
        frame.getContentPane().repaint();
    }

    private JButton createWindowButton(String label) {
        JButton button = new JButton(label);
        button.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        button.setForeground(THEME_TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(null);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(0, 8, 0, 8));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setForeground(new Color(220, 220, 220));
            }
            public void mouseExited(MouseEvent e) {
                button.setForeground(THEME_TEXT_PRIMARY);
            }
        });
        return button;
    }

    private JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text) {
            private Color currentBg = bgColor;

            @Override
            public void setBackground(Color bg) {
                super.setBackground(bg);
                this.currentBg = bg;
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(currentBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                if (this == bookNowButton && pulseAlpha > 0) {
                    g2.setColor(new Color(255, 255, 0, (int) (255 * pulseAlpha * 0.4)));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                }

                FontMetrics fm = g2.getFontMetrics(getFont());
                Rectangle stringBounds = fm.getStringBounds(this.getText(), g2).getBounds();
                int textX = (getWidth() - stringBounds.width) / 2;
                int textY = (getHeight() - stringBounds.height) / 2 + fm.getAscent();
                g2.setColor(getForeground());
                g2.setFont(getFont());
                g2.drawString(getText(), textX, textY);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) { /* Do not paint default border */ }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        if (bgColor == THEME_YELLOW) { // Special case for "Book Now!"
            button.setForeground(THEME_TEXT_ACCENT); // Black text on yellow
        } else {
            button.setForeground(THEME_TEXT_PRIMARY); // White text on other buttons
        }

        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(160, 45));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(hoverColor);
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(bgColor);
            }
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
                    g2.drawLine(textX, textY + 2, textX + textWidth, textY + 2);
                    g2.dispose();
                }
            }
        }
        return new HoverButton(text);
    }


    private void addDetailRow(JPanel panel, GridBagConstraints gbc, String labelText, String valueText, Font labelFont, Font valueFont, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.3;
        JLabel label = new JLabel(labelText);
        label.setFont(labelFont);
        label.setForeground(THEME_LIGHT_GRAY_TEXT);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0.7;
        JLabel value = new JLabel(valueText);
        value.setFont(valueFont);
        value.setForeground(THEME_TEXT_PRIMARY);
        panel.add(value, gbc);
    }

    private boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void startPulsingAnimation() {
        pulseTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pulseIncreasing) {
                    pulseAlpha += 0.05f;
                    if (pulseAlpha >= 0.8f) {
                        pulseAlpha = 0.8f;
                        pulseIncreasing = false;
                    }
                } else {
                    pulseAlpha -= 0.05f;
                    if (pulseAlpha <= 0.0f) {
                        pulseAlpha = 0.0f;
                        pulseIncreasing = true;
                    }
                }
                if (bookNowButton != null) {
                    bookNowButton.repaint();
                }
            }
        });
        pulseTimer.start();
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
            UIManager.put("Label.foreground", THEME_TEXT_PRIMARY);
        } catch (Exception e) {
            System.err.println("Look and Feel not available. Using default. " + e.getMessage());
        }

        // Create a dummy com.carrental.model.Car object for testing
        // Ensure you have an image at src/Images/image_56f507.jpg or change the path
        Car dummyCar = new Car(
                "Tesla Model 3", // Name
                true, // Is Available
                "Electric", // Type
                4.5, // Rating
                "src/Images/image_56f507.jpg", // Image Path
                "10,000 km", // Mileage
                "225 km/h", // Max Speed
                "5", // Seats
                "Automatic", // Transmission
                "Luxury Sedan", // Vehicle Class
                80.00, // Daily Price (example)
                "2023-01-01" // Last Serviced Date (example, though not displayed on this page)
        );

        if (LoginPage.users == null || LoginPage.users.isEmpty()) {
            LoginPage.users = new java.util.ArrayList<>();
            LoginPage.users.add(new User("testuser", "password123", "User", "Test User", "12345678901", "test@example.com"));        }
        LoginPage.loggedInUser = LoginPage.getUser("testuser");


        SwingUtilities.invokeLater(() -> new CarDetailPage(dummyCar));
    }
}
