// File: src/com/carrental/FeedbackPage.java
package com.carrental;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL; // Added for URL loading
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.plaf.basic.BasicScrollBarUI; // Import for custom scrollbar UI

public class FeedbackPage {

    private boolean isMaximized = false;
    private int oldX, oldY, oldWidth, oldHeight;
    private int selectedRating = 0; // To store the selected star rating
    private JFrame frame; // Make frame a class member so it can be disposed
    private BackgroundPanel rootPanel; // <--- CHANGED TYPE HERE to our custom panel

    // Define theme colors (assuming they are consistent across your application)
    private static final Color THEME_YELLOW = new Color(255, 193, 7);       // Primary Yellow
    private static final Color THEME_YELLOW_DARK = new Color(255, 179, 0);  // Darker Yellow for hover
    private static final Color THEME_TEXT_ACCENT = Color.BLACK;             // Text on yellow backgrounds
    private static final Color THEME_PRIMARY_DARK = new Color(30, 30, 30); // Dark background for fallback
    private static final Color THEME_SCROLLBAR_TRACK = new Color(50, 50, 50, 150); // Semi-transparent dark grey for scrollbar track


    // --- Custom JPanel for background image ---
    private class BackgroundPanel extends JPanel {
        private BufferedImage backgroundImage;
        private boolean isLoading = true; // Flag to show loading state

        public BackgroundPanel(LayoutManager layout) {
            super(layout);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                // Draw the loaded image scaled to fill the panel
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                // Fallback background color if image isn't found or still loading
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(THEME_PRIMARY_DARK); // Dark background fallback
                g2.fillRect(0, 0, getWidth(), getHeight());

                if (isLoading) {
                    // Draw a prominent loading message
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 28)); // Larger font
                    String loadingText = "Loading background...";
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(loadingText)) / 2;
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(loadingText, x, y);
                }
                g2.dispose();
            }
        }

        // Public setter for the background image
        public void setBackgroundImage(BufferedImage image) {
            this.backgroundImage = image;
            this.isLoading = false; // Loading is complete
            repaint(); // Repaint the panel to show the image
        }
    }


    public FeedbackPage() {

        frame = new JFrame("Feedback - The Rental Car");
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // --- Initialize old bounds to a sensible default restored size/position ---
        // These values will be used when restoring from the initial maximized state.
        oldWidth = 1000;
        oldHeight = 600;
        frame.setSize(oldWidth, oldHeight);
        frame.setLocationRelativeTo(null); // Center the frame for its initial restored size
        oldX = frame.getX(); // Capture the X position of the centered frame
        oldY = frame.getY(); // Capture the Y position of the centered frame

        // --- Set frame to maximize like Chrome (showing taskbar) ---
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle maxBounds = env.getMaximumWindowBounds(); // This gives bounds excluding taskbar
        frame.setBounds(maxBounds); // Set frame to these bounds
        isMaximized = true; // Set flag to true as it starts in a maximized state


        // --- Instantiate our custom BackgroundPanel ---
        rootPanel = new BackgroundPanel(new BorderLayout()); // <--- INSTANTIATED HERE
        rootPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- Start loading background image in a background thread ---
        loadBackgroundImageAsync();


        // --- Window Control Buttons (Minimize, Maximize, Close) ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        controlPanel.setOpaque(false);
        JButton closeButton = createWindowButton("X");
        JButton minimizeButton = createWindowButton("—");
        JButton maximizeButton = createWindowButton("❐"); // Initial state is "restore" icon as it's maximized
        controlPanel.add(minimizeButton);
        controlPanel.add(maximizeButton);
        controlPanel.add(closeButton);
        closeButton.addActionListener(e -> System.exit(0));
        minimizeButton.addActionListener(e -> frame.setState(JFrame.ICONIFIED));
        maximizeButton.addActionListener(e -> {
            // Updated logic for toggleMaximize() to handle Chrome-like behavior
            if (isMaximized) { // Currently maximized, so restore
                frame.setBounds(oldX, oldY, oldWidth, oldHeight);
                maximizeButton.setText("⬜"); // Change to maximize icon
                isMaximized = false;
            } else { // Currently restored, so maximize
                // Save current (restored) bounds BEFORE maximizing
                oldX = frame.getX();
                oldY = frame.getY();
                oldWidth = frame.getWidth();
                oldHeight = frame.getHeight();

                frame.setBounds(maxBounds); // Maximize to screen bounds minus taskbar
                maximizeButton.setText("❐"); // Change to restore icon
                isMaximized = true;
            }
        });
        rootPanel.add(controlPanel, BorderLayout.NORTH);

        // --- Central Content Panel with iOS-like Blur Effect (Feedback Form) ---
        JPanel feedbackFormPanel = new JPanel(new BorderLayout(20, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 180)); // Dark overlay with transparency
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 30, 30));
                g2.dispose();
            }
        };
        feedbackFormPanel.setOpaque(false);
        feedbackFormPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        feedbackFormPanel.setPreferredSize(new Dimension(600, 450)); // Keep preferred size for centering

        // --- Title ---
        JLabel titleLabel = new JLabel("WE APPRECIATE YOUR FEEDBACK", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        feedbackFormPanel.add(titleLabel, BorderLayout.NORTH);

        // --- Feedback Input Panel (Center) ---
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setOpaque(false);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Rating Section
        JLabel ratingLabel = new JLabel("Rate your experience", SwingConstants.CENTER);
        ratingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        ratingLabel.setForeground(Color.LIGHT_GRAY);
        ratingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel starPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        starPanel.setOpaque(false);
        List<JLabel> stars = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            JLabel star = new JLabel(createStarIcon(false));
            star.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            final int starIndex = i + 1;
            star.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedRating = starIndex;
                    updateStars(stars, selectedRating);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    updateStars(stars, starIndex);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    updateStars(stars, selectedRating);
                }
            });
            stars.add(star);
            starPanel.add(star);
        }

        inputPanel.add(ratingLabel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        inputPanel.add(starPanel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Feedback Text Area Section
        JLabel instructionLabel = new JLabel("Please share your detailed feedback below:", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        instructionLabel.setForeground(Color.LIGHT_GRAY);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputPanel.add(instructionLabel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JTextArea feedbackArea = new JTextArea(6, 40);
        feedbackArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setForeground(Color.WHITE);
        feedbackArea.setBackground(new Color(40, 40, 40));
        feedbackArea.setCaretColor(Color.WHITE);
        feedbackArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 50), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        JScrollPane scrollPane = new JScrollPane(feedbackArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputPanel.add(scrollPane);

        // --- Apply custom scrollbar UI ---
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = THEME_YELLOW;
                this.trackColor = THEME_SCROLLBAR_TRACK;
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


        feedbackFormPanel.add(inputPanel, BorderLayout.CENTER);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        buttonPanel.setOpaque(false);

        JButton submitButton = createStyledButton("Submit Feedback", THEME_YELLOW, THEME_YELLOW_DARK);
        submitButton.addActionListener(e -> {
            String feedbackText = feedbackArea.getText().trim();

            if (selectedRating == 0) {
                JOptionPane.showMessageDialog(frame, "Please select a star rating (1-5 stars)!", "Missing Rating", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (feedbackText.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter your feedback before submitting.", "Empty Feedback", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Here you would typically save the feedback and rating to a database
            System.out.println("Feedback Received:");
            System.out.println("Rating: " + selectedRating + " stars");
            System.out.println("Feedback Text: " + feedbackText);

            JOptionPane.showMessageDialog(frame, "Thank you for your feedback!", "Feedback Submitted", JOptionPane.INFORMATION_MESSAGE);

            // ******* CRITICAL CHANGE HERE: Navigate to CarListPage *******
            frame.dispose(); // Close the current FeedbackPage frame
            new CarListPage(); // Open the CarListPage
            // System.exit(0); // Remove this line as we are navigating
            // ************************************************************
        });
        buttonPanel.add(submitButton);
        feedbackFormPanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- Add feedbackFormPanel to a GridBagLayout for centering within rootPanel ---
        JPanel centerWrapperPanel = new JPanel(new GridBagLayout());
        centerWrapperPanel.setOpaque(false);
        centerWrapperPanel.add(feedbackFormPanel);
        rootPanel.add(centerWrapperPanel, BorderLayout.CENTER);

        frame.setContentPane(rootPanel);
        frame.setVisible(true);
    }

    // --- New method to load background image asynchronously ---
    private void loadBackgroundImageAsync() {
        new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                long startTime = System.currentTimeMillis();
                System.out.println("FeedbackPage: Background image loading started at " + startTime + " ms.");
                BufferedImage loadedImage = null;
                try {
                    // CORRECTED LINE: Load as a classpath resource!
                    URL imageUrl = getClass().getResource("/Images/1112.jpg");
                    if (imageUrl != null) {
                        loadedImage = ImageIO.read(imageUrl);
                    } else {
                        System.err.println("FeedbackPage: Background image '/Images/1112.jpg' not found on classpath.");
                    }
                } catch (IOException e) {
                    System.err.println("FeedbackPage: IOException loading background image: " + e.getMessage());
                    e.printStackTrace();
                } catch (IllegalArgumentException e) { // Catches issues if URL is null (resource not found)
                    System.err.println("FeedbackPage: Error: Resource path for FeedbackPage background image is invalid or image not found: " + e.getMessage());
                }
                long endTime = System.currentTimeMillis();
                System.out.println("FeedbackPage: Background image loading finished at " + endTime + " ms. Duration: " + (endTime - startTime) + " ms.");
                return loadedImage; // Return null if loading fails
            }

            @Override
            protected void done() {
                // This code runs on the EDT
                try {
                    BufferedImage image = get(); // Get the result from doInBackground
                    if (image != null) {
                        rootPanel.setBackgroundImage(image);
                    } else {
                        System.err.println("FeedbackPage: Failed to set background image. Displaying fallback color.");
                    }
                } catch (Exception e) {
                    System.err.println("FeedbackPage: Error setting background image on EDT: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.execute(); // Start the SwingWorker
    }


    // --- Helper Methods ---

    // Method to create a star icon (filled or empty)
    private Icon createStarIcon(boolean filled) {
        int size = 30; // Size of the star icon
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int[] xPoints = new int[10];
        int[] yPoints = new int[10];
        double outerRadius = size / 2.0;
        double innerRadius = outerRadius * 0.4;
        double angleStep = Math.PI / 5;

        for (int i = 0; i < 10; i++) {
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            double angle = i * angleStep - Math.PI / 2;
            xPoints[i] = (int) (size / 2 + radius * Math.cos(angle));
            yPoints[i] = (int) (size / 2 + radius * Math.sin(angle));
        }

        if (filled) {
            g2.setColor(THEME_YELLOW);
            g2.fillPolygon(xPoints, yPoints, 10);
        } else {
            g2.setColor(new Color(100, 100, 100)); // Dark gray for empty star outline
            g2.drawPolygon(xPoints, yPoints, 10);
            g2.setColor(new Color(50, 50, 50)); // Lighter gray fill for empty star
            g2.fillPolygon(xPoints, yPoints, 10);
        }

        g2.dispose();
        return new ImageIcon(image);
    }

    // Method to update the appearance of stars based on the selected rating
    private void updateStars(List<JLabel> stars, int rating) {
        for (int i = 0; i < stars.size(); i++) {
            if (i < rating) {
                stars.get(i).setIcon(createStarIcon(true));
            } else {
                stars.get(i).setIcon(createStarIcon(false));
            }
        }
    }

    // Helper to create window control buttons (X, —, ⬜)
    private JButton createWindowButton(String label) {
        JButton button = new JButton(label);
        button.setFont(new Font("Dialog", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(null);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    // Modified createStyledButton to accept a hover color
    private JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g2); // Paint the text
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                // Do not paint default border
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        if (bgColor.equals(THEME_YELLOW)) {
            button.setForeground(THEME_TEXT_ACCENT);
        } else {
            button.setForeground(Color.WHITE);
        }

        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(200, 50));
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

    // Optional main method for standalone testing of FeedbackPage
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FeedbackPage());
    }
}
