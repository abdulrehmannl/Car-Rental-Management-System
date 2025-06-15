package com.carrental;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

public class RoundedTextField extends JTextField {
    private boolean hasFocus = false;

    public RoundedTextField() {
        super();
        setOpaque(false);
        // --- MODIFIED: Adjusted padding to make inner size consistent ---
        setBorder(new EmptyBorder(8, 15, 8, 15)); // Reduced top/bottom padding
        // --- END MODIFIED ---

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                hasFocus = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                hasFocus = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int width = getWidth();
        int height = getHeight();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw the background of the text field (rounded rectangle)
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, width - 1, height - 1, 25, 25); // Rounded corners (25)

        // Draw the focus border
        if (hasFocus) {
            g2.setColor(new Color(66, 133, 244, 100)); // Google Blue with transparency
            g2.setStroke(new BasicStroke(2)); // Thicker border
            g2.drawRoundRect(1, 1, width - 3, height - 3, 25, 25); // Rounded corners (25)
        }
        g2.dispose();
        super.paintComponent(g); // IMPORTANT: This calls the default JTextField painting for text/caret
    }

    private Shape shape;
    @Override
    public boolean contains(int x, int y) {
        if (shape == null || !shape.getBounds().equals(getBounds())) {
            shape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 25, 25);
        }
        return shape.contains(x, y);
    }
}