// File: src/com/carrental/PreviewBackgroundPage.java
package com.carrental;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
// import java.io.File; // No longer needed for resource loading
import java.io.IOException;
import java.net.URL; // Needed for getClass().getResource()
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class PreviewBackgroundPage extends JDialog { // Still extends JDialog

    private String currentImagePath; // Store the image path for potential repaints

    public PreviewBackgroundPage(String imagePath) {
        super((JFrame)null, "Car Preview", true); // Set to modal (blocks parent until closed)
        setUndecorated(true); // For a borderless window
        setSize(800, 500); // Adjust size for a good preview
        setLocationRelativeTo(null); // Center on screen
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Close only this dialog

        this.currentImagePath = imagePath; // Store the provided image path

        // --- Root Panel with the Car Image as Background ---
        JPanel rootPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                BufferedImage originalImage = null;

                // Ensure the path starts with '/' for classpath lookup
                String resourcePath = currentImagePath;
                if (resourcePath != null && !resourcePath.startsWith("/")) {
                    resourcePath = "/" + resourcePath;
                }

                try {
                    // Correct way to load image from classpath resources
                    URL imageUrl = getClass().getResource(resourcePath);
                    if (imageUrl != null) {
                        originalImage = ImageIO.read(imageUrl);
                    } else {
                        System.err.println("Error: Image resource not found for preview: " + resourcePath);
                    }
                } catch (IOException ex) {
                    System.err.println("Exception loading image for preview: " + resourcePath + " - " + ex.getMessage());
                    ex.printStackTrace();
                }

                if (originalImage != null) {
                    // Scale image to fill the panel while maintaining aspect ratio
                    Image scaledImage = originalImage.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
                    g.drawImage(scaledImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Draw a "Image not found" message as fallback
                    g.setColor(Color.DARK_GRAY); // Fallback background
                    g.fillRect(0, 0, getWidth(), getHeight());

                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Segoe UI", Font.BOLD, 24));
                    String message = "Image not found for car"; // Simpler message
                    String pathMessage = "Path: " + (resourcePath != null ? resourcePath : "null");
                    FontMetrics fm = g.getFontMetrics();
                    int textHeight = fm.getHeight();

                    int x1 = (getWidth() - fm.stringWidth(message)) / 2;
                    int y1 = (getHeight() - textHeight) / 2 + fm.getAscent() - (textHeight / 2); // Center primary message

                    int x2 = (getWidth() - fm.stringWidth(pathMessage)) / 2;
                    int y2 = y1 + textHeight; // Below primary message

                    g.drawString(message, x1, y1);
                    g.setFont(new Font("Segoe UI", Font.PLAIN, 16)); // Smaller font for path
                    g.drawString(pathMessage, x2, y2);
                }
            }
        };
        rootPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Slight padding

        // --- Window Control Buttons (Only Close Button) ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        controlPanel.setOpaque(false);
        JButton closeButton = createWindowButton("X");
        controlPanel.add(closeButton);

        closeButton.addActionListener(e -> dispose()); // Close only this dialog

        rootPanel.add(controlPanel, BorderLayout.NORTH);


        // --- Go Back Button (centered at bottom) ---
        JPanel buttonWrapperPanel = new JPanel(new GridBagLayout());
        buttonWrapperPanel.setOpaque(false); // Transparent to show background
        // Use a consistent theme color for the button if possible, or define new one.
        // For now, using a similar blue.
        JButton goBackButton = createStyledButton("Go Back", new Color(66, 133, 244), new Color(50, 100, 200)); // Added hover color
        goBackButton.addActionListener(e -> dispose()); // Close only this dialog

        buttonWrapperPanel.add(goBackButton); // GridBagLayout centers it
        rootPanel.add(buttonWrapperPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel); // Set the rootPanel as the content pane
        // setVisible(true); // Call this outside the constructor or in main for actual display
    }

    // --- Helper Methods (Copied from other pages for consistency) ---

    // Helper to create window control buttons (X)
    private JButton createWindowButton(String label) {
        JButton button = new JButton(label);
        button.setFont(new Font("Dialog", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(null);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Added hover effect for consistency
        Color buttonHoverColor = new Color(220, 53, 69); // Red for close
        if (!"X".equals(label)) { // For other buttons (if added later)
            buttonHoverColor = new Color(60,60,60,150);
        }

        final Color finalButtonHoverColor = buttonHoverColor; // Need final for lambda
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(finalButtonHoverColor);
                button.setOpaque(true);
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(0,0,0,0)); // Transparent
                button.setOpaque(false);
            }
        });
        return button;
    }

    // Helper to create styled buttons with rounded corners and hover effect
    // Added a hoverColor parameter for more control
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
                g2.setColor(currentBg); // Use currentBg for dynamic color
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                // To paint text and icon over custom background, call super.paintComponent
                // after setting the color, but before disposing g2.
                // Or, draw text manually as shown in LoginPage
                FontMetrics fm = g2.getFontMetrics(getFont());
                Rectangle stringBounds = fm.getStringBounds(this.getText(), g2).getBounds();
                int textX = (getWidth() - stringBounds.width) / 2;
                int textY = (getHeight() - stringBounds.height) / 2 + fm.getAscent();
                g2.setColor(getForeground());
                g2.setFont(getFont());
                g2.drawString(getText(), textX, textY);
                g2.dispose(); // Dispose the graphics object
            }

            @Override
            protected void paintBorder(Graphics g) {
                // Do not paint default border
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setForeground(Color.WHITE); // Assuming white text on colored buttons
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(150, 45)); // Slightly larger button
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(hoverColor); // Use provided hoverColor
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(bgColor); // Revert to base color
            }
        });
        return button;
    }

    // You can remove the main method if this class is only instantiated by others.
    // It's useful for testing this specific page in isolation.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Example usage: Make sure you have a test image at this path
            // This path must be a classpath resource, e.g., in src/Images/
            new PreviewBackgroundPage("/Images/tc.jpg").setVisible(true); // Changed to classpath resource path and made visible
        });
    }
}
