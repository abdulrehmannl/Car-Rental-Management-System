// File: src/com/carrental/CarListPage.java
package com.carrental;

import com.carrental.model.Car;
import com.carrental.model.User; // Ensure User is imported if used
// import com.carrental.service.CarService; // Not explicitly imported, but its methods are called via LoginPage

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.NumberFormat;
import java.util.Locale;

import java.awt.event.FocusAdapter;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.UIManager;
import javax.swing.DefaultListCellRenderer;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class CarListPage {

    private JFrame frame;
    private List<Car> allCars; // Now populated by SwingWorker
    private JPanel carDisplayPanel; // Panel to hold car cards
    private JScrollPane scrollPane; // Scroll pane to hold carDisplayPanel
    private JTextField searchField;
    private JComboBox<String> typeFilterComboBox;
    private JComboBox<String> availabilityFilterComboBox;

    private JDialog filterDialog;
    private JPanel headerPanel;
    private JPanel rootPanel; // Made class member to access for revalidation

    private boolean isMaximized = false;
    private int oldX, oldY, oldWidth, oldHeight;
    private JButton maximizeButton;

    // Constants for window control button characters
    private static final String MINIMIZE_CHAR = "\u2014"; // Em Dash
    private static final String MAXIMIZE_CHAR = "\u25A1"; // White Square (for maximize)
    private static final String RESTORE_CHAR = "\u25A3";  // White Square with Black Square (for restore)
    private static final String CLOSE_CHAR = "X";

    // THEME COLORS (Consistent with other pages)
    private static final Color THEME_YELLOW = new Color(255, 193, 7);
    private static final Color THEME_YELLOW_DARK = new Color(255, 179, 0);
    private static final Color THEME_DARK_OVERLAY = new Color(0, 0, 0, 180); // Semi-transparent black for content areas
    private static final Color THEME_BACKGROUND_STATIC_OVERLAY = new Color(0, 0, 0, 120); // Fixed overlay for background
    private static final Color THEME_TEXT_PRIMARY = Color.WHITE;
    private static final Color THEME_TEXT_ACCENT = Color.BLACK;
    private static final Color THEME_LIGHT_GRAY_TEXT = new Color(200, 200, 200);
    private static final Color THEME_BLUE_BUTTON = new Color(66, 133, 244);
    private static final Color THEME_BLUE_BUTTON_DARK = new Color(50, 100, 200);
    private static final Color THEME_RED_BUTTON = new Color(220, 53, 69); // For logout maybe

    // --- New: Loading indicator panel ---
    private JPanel loadingIndicatorPanel;

    // Image assets loaded once (for background/card border)
    private BufferedImage backgroundImage;


    public CarListPage() {
        long constructorStartTime = System.currentTimeMillis();
        System.out.println("CarListPage: Constructor started at " + constructorStartTime + " ms.");

        // allCars will be populated asynchronously
        this.allCars = new ArrayList<>();

        frame = new JFrame("Car List - The Rental Car");
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // --- Initialize old bounds to a sensible default restored size/position ---
        // These values will be used when restoring from the initial maximized state.
        oldWidth = 1200; // Default width
        oldHeight = 700; // Default height
        frame.setSize(oldWidth, oldHeight);
        frame.setLocationRelativeTo(null); // Center the frame for its initial restored size
        oldX = frame.getX(); // Capture the X position of the centered frame
        oldY = frame.getY(); // Capture the Y position of the centered frame

        // --- Set frame to maximize like Chrome (showing taskbar) ---
        // This ensures the window maximizes to the available screen space,
        // respecting the taskbar/dock.
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle maxBounds = env.getMaximumWindowBounds(); // This gives bounds excluding taskbar
        frame.setBounds(maxBounds); // Set frame to these bounds
        isMaximized = true; // Set flag to true as it starts in a maximized state


        // --- Instantiate our custom BackgroundPanel ---
        rootPanel = new JPanel(new BorderLayout()) { // Changed to class member
            {
                try {
                    // Load background image as a resource using getClass().getResource()
                    java.net.URL bgUrl = getClass().getResource("/Images/1112.jpg");
                    if (bgUrl != null) {
                        backgroundImage = ImageIO.read(bgUrl);
                    } else {
                        System.err.println("CarListPage: Background image '/Images/1112.jpg' not found on classpath.");
                    }
                } catch (IOException e) {
                    System.err.println("CarListPage: Exception loading background image: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.err.println("CarListPage: Invalid URL for background image. Path: /Images/1112.jpg - " + e.getMessage());
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                if (backgroundImage != null) {
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g2d.setColor(new Color(30, 30, 30)); // Fallback color if image not found
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
                g2d.setColor(THEME_BACKGROUND_STATIC_OVERLAY);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        rootPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // --- Header Panel for Window Controls and Dragging ---
        headerPanel = new JPanel(new BorderLayout());
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

        // Left side of header (Filter/Menu Button)
        JPanel leftHeaderControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        leftHeaderControls.setOpaque(false);

        JButton filterMenuButton = createMenuButton("☰");
        filterMenuButton.setToolTipText("Open Filters and Options");
        filterMenuButton.addActionListener(e -> showFilterDialog());
        leftHeaderControls.add(filterMenuButton);

        headerPanel.add(leftHeaderControls, BorderLayout.WEST);

        // Right side of header (Window controls)
        JPanel windowControlButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
        windowControlButtonPanel.setOpaque(false);

        JButton minimizeButton = createWindowButton(MINIMIZE_CHAR);
        this.maximizeButton = createWindowButton(RESTORE_CHAR); // Initial state is "restore" icon as it's maximized
        JButton closeButton = createWindowButton(CLOSE_CHAR);

        minimizeButton.addActionListener(e -> frame.setState(JFrame.ICONIFIED));
        this.maximizeButton.addActionListener(e -> toggleMaximize());
        closeButton.addActionListener(e -> System.exit(0));

        windowControlButtonPanel.add(minimizeButton);
        windowControlButtonPanel.add(this.maximizeButton);
        windowControlButtonPanel.add(closeButton);

        headerPanel.add(windowControlButtonPanel, BorderLayout.EAST);
        rootPanel.add(headerPanel, BorderLayout.NORTH);


        // --- Central Content Panel (Will hold loading indicator or car display) ---
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(0, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 0;
        gbc.weighty = 0;
        gbc.insets = new Insets(5, 0, 10, 0);
        JLabel titleLabel = new JLabel("AVAILABLE CARS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(THEME_TEXT_PRIMARY);
        contentPanel.add(titleLabel, gbc);

        // --- NEW: Loading Indicator Setup ---
        // This panel is displayed while the car data is being loaded asynchronously.
        loadingIndicatorPanel = new JPanel(new GridBagLayout());
        loadingIndicatorPanel.setOpaque(false);
        JLabel loadingLabel = new JLabel("Loading cars, please wait...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        loadingLabel.setForeground(THEME_YELLOW);
        loadingIndicatorPanel.add(loadingLabel);

        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        contentPanel.add(loadingIndicatorPanel, gbc); // Initially add loading indicator

        rootPanel.add(contentPanel, BorderLayout.CENTER);

        frame.setContentPane(rootPanel); // Set the root panel as content pane.

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
        if (!isMaximized) {
            frame.setShape(new RoundRectangle2D.Double(0, 0, frame.getWidth(), frame.getHeight(), 20, 20));
        } else {
            frame.setShape(null);
        }

        frame.setVisible(true); // Make the frame visible immediately with loading screen

        // Initialize and setup the filter dialog (it needs allCars for initial population)
        initializeFilterDialog(); // Setup dialog structure, but don't populate combo boxes fully yet

        // --- NEW: Start asynchronous data loading ---
        // The actual car data fetching is performed in the background to keep the UI responsive.
        loadCarDataAsync(contentPanel); // Pass the contentPanel to swap components

        System.out.println("CarListPage: Constructor finished. Time taken: " + (System.currentTimeMillis() - constructorStartTime) + " ms.");
    }

    // --- NEW: Method to load car data asynchronously ---
    private void loadCarDataAsync(JPanel contentPanel) {
        new SwingWorker<List<Car>, Void>() {
            long backgroundTaskStartTime;

            @Override
            protected List<Car> doInBackground() throws Exception {
                backgroundTaskStartTime = System.currentTimeMillis();
                System.out.println("CarListPage: SwingWorker: doInBackground started (calling LoginPage.getCars()) at " + backgroundTaskStartTime + " ms.");
                // This runs on a background thread.
                // Call LoginPage.getCars() which is now expected to be pre-populated or will trigger CarService.getAllCars()
                return LoginPage.getCars();
            }

            @Override
            protected void done() {
                long doneMethodStartTime = System.currentTimeMillis();
                System.out.println("CarListPage: SwingWorker: done() started (updating UI) at " + doneMethodStartTime + " ms.");
                try {
                    allCars = get(); // Get the result from the background task
                    if (allCars == null) {
                        allCars = new ArrayList<>(); // Ensure it's not null
                        System.err.println("CarListPage: SwingWorker: allCars is null after background fetch. Initializing empty list.");
                    }
                    System.out.println("CarListPage: SwingWorker: Car data retrieved. Backend/Data fetch time: " + (doneMethodStartTime - backgroundTaskStartTime) + " ms. Total cars: " + allCars.size());

                    // Remove the loading indicator
                    contentPanel.remove(loadingIndicatorPanel);

                    // Create the carDisplayPanel and JScrollPane here, after data is available
                    carDisplayPanel = new JPanel();
                    carDisplayPanel.setOpaque(false);
                    carDisplayPanel.setLayout(new WrapLayout(FlowLayout.CENTER, 15, 15));

                    scrollPane = new JScrollPane(carDisplayPanel); // Make it a class member for accessibility
                    scrollPane.setOpaque(false);
                    scrollPane.getViewport().setOpaque(false);
                    scrollPane.setBorder(BorderFactory.createEmptyBorder());
                    scrollPane.getVerticalScrollBar().setUnitIncrement(16);

                    // Customize the scrollbar UI (as before)
                    scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
                        @Override
                        protected void configureScrollBarColors() {
                            this.thumbColor = THEME_YELLOW;
                            this.trackColor = new Color(50, 50, 50, 150);
                        }
                        @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
                        @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
                        private JButton createZeroButton() {
                            JButton button = new JButton();
                            button.setPreferredSize(new Dimension(0, 0));
                            button.setMinimumSize(new Dimension(0, 0));
                            button.setMaximumSize(new Dimension(0, 0));
                            return button;
                        }
                        @Override
                        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(trackColor);
                            g2.fillRoundRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height, 8, 8);
                            g2.dispose();
                        }
                        @Override
                        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) { return; }
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(thumbColor);
                            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 8, 8);
                            g2.dispose();
                        }
                    });

                    // Add the scroll pane to the contentPanel
                    GridBagConstraints gbcForScrollPane = new GridBagConstraints();
                    gbcForScrollPane.gridx = 0;
                    gbcForScrollPane.gridy = 1; // Position below title
                    gbcForScrollPane.weightx = 1.0;
                    gbcForScrollPane.weighty = 1.0;
                    gbcForScrollPane.fill = GridBagConstraints.BOTH;
                    contentPanel.add(scrollPane, gbcForScrollPane);

                    // Populate filter combo boxes *after* allCars is loaded
                    String[] uniqueTypes = getUniqueCarTypes(allCars);
                    typeFilterComboBox.setModel(new DefaultComboBoxModel<>(
                            prependToArray(uniqueTypes, "All Types")));

                    long displayCarsStartTime = System.currentTimeMillis();
                    displayCars(allCars); // Display all cars initially
                    System.out.println("CarListPage: SwingWorker: displayCars() execution time (UI rendering): " + (System.currentTimeMillis() - displayCarsStartTime) + " ms for " + allCars.size() + " cars.");


                    // Revalidate and repaint the entire hierarchy
                    rootPanel.revalidate();
                    rootPanel.repaint();
                    System.out.println("CarListPage: SwingWorker: done() method finished. Total UI update time: " + (System.currentTimeMillis() - doneMethodStartTime) + " ms.");


                } catch (Exception e) {
                    System.err.println("CarListPage: SwingWorker: Error during background task or UI update: " + e.getMessage());
                    JOptionPane.showMessageDialog(frame, "Failed to load car data. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                    // Handle error: e.g., display empty list, show error message, or revert to login
                    allCars = new ArrayList<>(); // Ensure it's not null even on error
                    contentPanel.remove(loadingIndicatorPanel);
                    // Add a message like "Failed to load cars" here
                    JLabel errorLabel = new JLabel("Failed to load cars. Please restart.", SwingConstants.CENTER);
                    errorLabel.setForeground(Color.RED);
                    errorLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
                    contentPanel.add(errorLabel, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
                    contentPanel.revalidate();
                    contentPanel.repaint();
                }
            }
        }.execute(); // Execute the SwingWorker
    }


    private void initializeFilterDialog() {
        filterDialog = new JDialog(frame, "Filters and Options", true);
        filterDialog.setUndecorated(true);
        filterDialog.setBackground(new Color(0, 0, 0, 0));

        JPanel dialogContent = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(THEME_DARK_OVERLAY);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 25, 25));
                g2.dispose();
            }
        };
        dialogContent.setOpaque(false);
        dialogContent.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        // Add a close button for the dialog itself
        JButton closeDialogButton = createWindowButton("X");
        closeDialogButton.addActionListener(e -> filterDialog.dispose());
        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        closePanel.setOpaque(false);
        closePanel.add(closeDialogButton);
        gbc.gridwidth = 2; // Span across two columns
        dialogContent.add(closePanel, gbc);
        gbc.gridwidth = 1; // Reset to 1 for other components

        // Search Field
        gbc.gridy++;
        dialogContent.add(new JLabel("Search:", SwingConstants.RIGHT) {{ setForeground(THEME_LIGHT_GRAY_TEXT); setFont(new Font("Segoe UI", Font.BOLD, 12)); }}, gbc);
        gbc.gridx = 1;
        searchField = createStyledTextField("Search by Car Name");
        searchField.setPreferredSize(new Dimension(180, 30)); // Smaller for dialog
        searchField.addActionListener(e -> { applyFilters(); filterDialog.dispose(); }); // Apply and close on Enter
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyFilters(); // Apply on key release
            }
        });
        dialogContent.add(searchField, gbc);

        // Type Filter - Model will be set after async data load
        gbc.gridy++;
        gbc.gridx = 0;
        dialogContent.add(new JLabel("Type:", SwingConstants.RIGHT) {{ setForeground(THEME_LIGHT_GRAY_TEXT); setFont(new Font("Segoe UI", Font.BOLD, 12)); }}, gbc);
        gbc.gridx = 1;
        // Provide an initial empty model. It will be updated in SwingWorker.done()
        typeFilterComboBox = createStyledComboBox(new String[]{"Loading..."}); // Initial text
        typeFilterComboBox.setPreferredSize(new Dimension(180, 30));
        typeFilterComboBox.addActionListener(e -> applyFilters());
        dialogContent.add(typeFilterComboBox, gbc);

        // Availability Filter
        gbc.gridy++;
        gbc.gridx = 0;
        dialogContent.add(new JLabel("Availability:", SwingConstants.RIGHT) {{ setForeground(THEME_LIGHT_GRAY_TEXT); setFont(new Font("Segoe UI", Font.BOLD, 12)); }}, gbc);
        gbc.gridx = 1;
        availabilityFilterComboBox = createStyledComboBox(new String[]{"All", "Available", "Unavailable"});
        availabilityFilterComboBox.setPreferredSize(new Dimension(180, 30));
        availabilityFilterComboBox.addActionListener(e -> applyFilters());
        dialogContent.add(availabilityFilterComboBox, gbc);

        // Spacer
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0; // Push buttons to bottom
        dialogContent.add(Box.createVerticalGlue(), gbc);
        gbc.weighty = 0; // Reset

        // Action Buttons (Logout, Admin Dashboard)
        gbc.gridy++;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setOpaque(false);

        JButton logoutButton = createCuteLogoutButton("Logout");
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(filterDialog, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                filterDialog.dispose();
                frame.dispose();
                new LoginPage();
            }
        });
        buttonPanel.add(logoutButton);

        if ("Admin".equals(LoginPage.getLoggedInUserRole())) {
            JButton adminDashboardButton = createHeaderButton("Admin Dashboard", THEME_BLUE_BUTTON, THEME_BLUE_BUTTON_DARK);
            adminDashboardButton.setPreferredSize(new Dimension(130, 30)); // Smaller button
            adminDashboardButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            adminDashboardButton.addActionListener(e -> {
                filterDialog.dispose();
                frame.dispose();
                new AdminDashboard(LoginPage.getUsers(), LoginPage.getCars());
            });
            buttonPanel.add(adminDashboardButton);
        }

        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        dialogContent.add(buttonPanel, gbc);

        filterDialog.setContentPane(dialogContent);
        filterDialog.pack();
        filterDialog.setResizable(false);
    }

    private void showFilterDialog() {
        Point p = frame.getLocationOnScreen();
        // Position the dialog relative to the header button, or top-left of the frame
        // You might need to adjust (p.x + 20) and (p.y + headerPanel.getHeight() + 10)
        // depending on your desired dialog placement relative to the filter button itself.
        filterDialog.setLocation(p.x + 20, p.y + headerPanel.getHeight() + 10);
        filterDialog.setVisible(true);
    }

    private void displayCars(List<Car> carsToDisplay) {
        long displayCarsMethodStartTime = System.currentTimeMillis();
        carDisplayPanel.removeAll(); // Clear existing cards

        if (carsToDisplay.isEmpty()) {
            JLabel noCarsLabel = new JLabel("No cars match your criteria.", SwingConstants.CENTER);
            noCarsLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            noCarsLabel.setForeground(THEME_TEXT_PRIMARY);
            carDisplayPanel.add(noCarsLabel);
        } else {
            for (Car car : carsToDisplay) {
                carDisplayPanel.add(createCarCard(car));
            }
        }
        carDisplayPanel.revalidate();
        carDisplayPanel.repaint();
        System.out.println("CarListPage: displayCars() total UI rendering time: " + (System.currentTimeMillis() - displayCarsMethodStartTime) + " ms for " + carsToDisplay.size() + " cars.");
    }

    private JPanel createCarCard(Car car) {
        JPanel card = new JPanel(new BorderLayout(5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Card background: Darker, more opaque for distinct GTA V look
                g2.setColor(new Color(25, 25, 25, 200)); // Darker, more solid background for each card
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 15, 15)); // Slightly smaller roundness
                g2.dispose();
            }
        };
        card.setOpaque(false); // Crucial to see the custom paintComponent
        card.setBorder(new EmptyBorder(8, 8, 8, 8)); // Adjusted padding for smaller card
        card.setPreferredSize(new Dimension(280, 350)); // Made card smaller (originally 350, 400)
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Top Panel for Image and Basic Info (Name, Price)
        JPanel topInfoPanel = new JPanel(new BorderLayout(5, 5));
        topInfoPanel.setOpaque(false);

        // Car Image
        JLabel carImageLabel = new JLabel();
        carImageLabel.setPreferredSize(new Dimension(260, 150)); // Adjusted image size for smaller card
        carImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        carImageLabel.setVerticalAlignment(SwingConstants.CENTER);

        // --- Asynchronous Image Loading for each Car Card (SwingWorker) ---
        final String imagePath = car.getImagePath();
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                String adjustedPath = imagePath;
                if (adjustedPath != null && !adjustedPath.startsWith("/")) {
                    if (adjustedPath.startsWith("src/")) {
                        adjustedPath = adjustedPath.substring(3);
                    }
                    adjustedPath = "/" + adjustedPath;
                }

                try {
                    java.net.URL imageUrl = getClass().getResource(adjustedPath);
                    if (imageUrl != null) {
                        BufferedImage img = ImageIO.read(imageUrl);
                        if (img != null) {
                            Image scaledImage = img.getScaledInstance(
                                    carImageLabel.getPreferredSize().width,
                                    carImageLabel.getPreferredSize().height,
                                    Image.SCALE_SMOOTH);
                            return new ImageIcon(scaledImage);
                        }
                    }
                } catch (IOException | IllegalArgumentException e) {
                    System.err.println("CarListPage: Error loading image for car " + car.getName() + " from " + imagePath + ": " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        carImageLabel.setIcon(icon);
                    } else {
                        carImageLabel.setText("<html><center>Image N/A:<br>" + imagePath + "</center></html>");
                        carImageLabel.setForeground(Color.RED);
                        carImageLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    }
                } catch (Exception e) {
                    System.err.println("CarListPage: Error setting car image for " + car.getName() + ": " + e.getMessage());
                    carImageLabel.setText("<html><center>Error setting image:<br>" + imagePath + "</center></html>");
                    carImageLabel.setForeground(Color.RED);
                    carImageLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
                }
            }
        }.execute();

        topInfoPanel.add(carImageLabel, BorderLayout.CENTER);

        // Name and Price Panel (at the bottom of topInfoPanel)
        JPanel namePricePanel = new JPanel(new BorderLayout());
        namePricePanel.setOpaque(false);
        namePricePanel.setBorder(new EmptyBorder(5, 0, 0, 0)); // Padding above name/price

        JLabel carNameLabel = new JLabel(car.getName(), SwingConstants.LEFT);
        carNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Slightly smaller font
        carNameLabel.setForeground(THEME_YELLOW); // Accent color for name
        namePricePanel.add(carNameLabel, BorderLayout.WEST);

        // Format price to currency
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US); // Or your preferred locale
        JLabel carPriceLabel = new JLabel(currencyFormat.format(car.getPrice()), SwingConstants.RIGHT);
        carPriceLabel.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Slightly smaller font
        carPriceLabel.setForeground(THEME_TEXT_PRIMARY); // White for price
        namePricePanel.add(carPriceLabel, BorderLayout.EAST);

        topInfoPanel.add(namePricePanel, BorderLayout.SOUTH);
        card.add(topInfoPanel, BorderLayout.NORTH);


        // Car Details (center of the card)
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setOpaque(false);
        detailsPanel.setBorder(new EmptyBorder(5, 0, 5, 0)); // Padding around details block
        GridBagConstraints gbcDetails = new GridBagConstraints(); // Use a different GBC for details panel
        gbcDetails.insets = new Insets(1, 5, 1, 5); // Reduced insets between details
        gbcDetails.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        // Display Vehicle Class, and other specs prominently
        addDetailToCard(detailsPanel, gbcDetails, "Class:", car.getVehicleClass(), row++);
        addDetailToCard(detailsPanel, gbcDetails, "Max Speed:", car.getMaxSpeed(), row++);
        addDetailToCard(detailsPanel, gbcDetails, "Transmission:", car.getTransmission(), row++);
        addDetailToCard(detailsPanel, gbcDetails, "Seats:", car.getSeats(), row++);
        addDetailToCard(detailsPanel, gbcDetails, "Mileage:", car.getMileage(), row++);

        card.add(detailsPanel, BorderLayout.CENTER);

        // "View Details" Button
        JButton viewDetailsButton = createStyledButton("View Details", THEME_YELLOW, THEME_YELLOW_DARK);
        viewDetailsButton.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Smaller font for button
        viewDetailsButton.setPreferredSize(new Dimension(card.getWidth(), 35)); // Smaller button height
        viewDetailsButton.addActionListener(e -> {
            frame.dispose();
            new CarDetailPage(car); // Pass the com.carrental.model.Car object
        });
        card.add(viewDetailsButton, BorderLayout.SOUTH);

        // Modified hover effect: now only changes padding, no visible line border
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(new EmptyBorder(7, 7, 7, 7)); // Slightly less padding to hint at selection
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(new EmptyBorder(8, 8, 8, 8)); // Restore original padding
            }
        });

        return card;
    }

    private void addDetailToCard(JPanel panel, GridBagConstraints gbc, String labelText, String valueText, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.4; // Label takes slightly more space
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Smaller font for details
        label.setForeground(THEME_LIGHT_GRAY_TEXT);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0.6; // Value takes remaining space
        JLabel value = new JLabel(valueText);
        value.setFont(new Font("Segoe UI", Font.PLAIN, 11)); // Smaller font for details
        value.setForeground(THEME_TEXT_PRIMARY);
        panel.add(value, gbc);
    }

    private String[] getUniqueCarTypes(List<Car> cars) {
        if (cars == null) return new String[]{};
        return cars.stream()
                .map(Car::getType)
                .distinct()
                .sorted()
                .toArray(String[]::new);
    }

    private String[] prependToArray(String[] array, String itemToPrepend) {
        String[] newArray = new String[array.length + 1];
        newArray[0] = itemToPrepend;
        System.arraycopy(array, 0, newArray, 1, array.length);
        return newArray;
    }

    private void applyFilters() {
        if (allCars == null) {
            System.err.println("CarListPage: allCars is null during filter application. Data might not be loaded yet.");
            return;
        }

        String searchText = searchField.getText().toLowerCase();
        String selectedType = (String) typeFilterComboBox.getSelectedItem();
        String selectedAvailability = (String) availabilityFilterComboBox.getSelectedItem();

        List<Car> filteredCars = allCars.stream()
                .filter(car -> car.getName().toLowerCase().contains(searchText) ||
                        car.getType().toLowerCase().contains(searchText))
                .filter(car -> "All Types".equals(selectedType) || car.getType().equals(selectedType))
                .filter(car -> {
                    if ("All".equals(selectedAvailability)) return true;
                    if ("Available".equals(selectedAvailability)) return car.isAvailable();
                    return !car.isAvailable(); // "Unavailable"
                })
                .collect(Collectors.toList());

        displayCars(filteredCars);
    }

    // --- Helper Methods for UI Components and Window Management ---

    private void toggleMaximize() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle maxBounds = env.getMaximumWindowBounds(); // Get maximized bounds (excludes taskbar/dock)

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
        // Revalidate and repaint to ensure layout changes are applied
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

    // New method for the "Menu" or "Filter" button
    private JButton createMenuButton(String label) {
        JButton button = new JButton(label);
        button.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 24)); // Larger font for icon
        button.setForeground(THEME_TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(null);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(0, 5, 0, 5)); // Smaller margins
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setForeground(THEME_YELLOW); // Yellow on hover
            }
            public void mouseExited(MouseEvent e) {
                button.setForeground(THEME_TEXT_PRIMARY);
            }
        });
        return button;
    }

    private JButton createHeaderButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(THEME_TEXT_PRIMARY);
        button.setBackground(bgColor);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Smaller padding
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.putClientProperty("JButton.segmentPosition", "only"); // For Nimbus L&F styling


        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }

    // New method for the "cute" logout button
    private JButton createCuteLogoutButton(String text) {
        final Color hoverColor = new Color(180, 40, 50); // Darker red on hover

        JButton button = new JButton(text) {
            private Color currentBg = THEME_RED_BUTTON;

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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

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

        button.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Smaller font
        button.setForeground(THEME_TEXT_PRIMARY);
        button.setBackground(THEME_RED_BUTTON);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(80, 30)); // Smaller dimensions
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(hoverColor);
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(THEME_RED_BUTTON);
            }
        });

        return button;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 13)); // Slightly smaller font
        textField.setPreferredSize(new Dimension(180, 30)); // Adjusted height
        textField.setForeground(THEME_TEXT_PRIMARY);
        textField.setBackground(new Color(50, 50, 50, 180));
        textField.setCaretColor(THEME_TEXT_PRIMARY);
        textField.putClientProperty("JTextField.placeholderText", placeholder);
        textField.putClientProperty("JTextField.showPlaceholder", true);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 50), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(THEME_YELLOW, 2),
                        BorderFactory.createEmptyBorder(4, 9, 4, 9)
                ));
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 255, 255, 50), 1),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
        });
        return textField;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13)); // Slightly smaller font
        comboBox.setForeground(THEME_TEXT_PRIMARY);
        comboBox.setBackground(new Color(50, 50, 50, 180));
        comboBox.setOpaque(true);
        comboBox.setFocusable(false);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 50), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8) // Slightly less internal padding
        ));

        comboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton();
                button.setOpaque(false);
                button.setContentAreaFilled(false);
                button.setBorder(BorderFactory.createEmptyBorder());
                // UIManager's arrow icon might not be easily colorable. Create our own.
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

            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                c.setOpaque(true);
            }
            @Override
            protected void installDefaults() {
                super.installDefaults();
                UIManager.put("ComboBox.selectionBackground", THEME_YELLOW_DARK);
                UIManager.put("ComboBox.selectionForeground", THEME_TEXT_ACCENT);
            }
        });

        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    setBackground(THEME_YELLOW_DARK);
                    setForeground(THEME_TEXT_ACCENT);
                } else {
                    setBackground(new Color(50, 50, 50, 200));
                    setForeground(THEME_TEXT_PRIMARY);
                }
                setBorder(new EmptyBorder(4, 8, 4, 8)); // Padding for items
                return this;
            }
        });

        return comboBox;
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
        if (bgColor == THEME_YELLOW) {
            button.setForeground(THEME_TEXT_ACCENT);
        } else {
            button.setForeground(THEME_TEXT_PRIMARY);
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

    // Helper for a wrapping FlowLayout
    class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            return layoutSize(target, false);
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int horizInsets = insets.left + insets.right;
                int vertInsets = insets.top + insets.bottom;
                int width = target.getWidth();

                if (width == 0) {
                    width = Integer.MAX_VALUE;
                }

                int x = 0, y = insets.top;
                int rowHeight = 0;
                int maxRowWidth = 0;

                for (int i = 0; i < target.getComponentCount(); i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize() ;
                        if ((x + d.width + hgap) > (width - horizInsets) && x > 0) {
                            x = 0;
                            y += vgap + rowHeight;
                            rowHeight = 0;
                        }
                        x += d.width + hgap;
                        rowHeight = Math.max(rowHeight, d.height);
                        maxRowWidth = Math.max(maxRowWidth, x - hgap);
                    }
                }
                return new Dimension(maxRowWidth + horizInsets, y + rowHeight + vertInsets);
            }
        }
    }
}
