package com.carrental;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.*;

public class MaterialTextField extends JPanel {
    private JTextField textField;
    private JLabel label;
    private boolean isPasswordField = false;

    public MaterialTextField(String placeholder) {
        // Set layout for the panel to overlay the label on the text field
        setLayout(new BorderLayout());

        // Create the text field
        textField = new JTextField();
        textField.setText(placeholder);
        textField.setForeground(Color.GRAY);
        textField.setFont(new Font("Arial", Font.PLAIN, 16));

        // Set the label for the placeholder
        label = new JLabel(placeholder);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        label.setForeground(Color.GRAY);
        label.setBounds(5, 5, 200, 30);

        // Add a focus listener to handle the label movement
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                    textField.setForeground(Color.GRAY);
                }
            }
        });

        add(textField, BorderLayout.CENTER);
        setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Set the label position
        label.setBounds(5, 5, 200, 30);
        add(label, BorderLayout.NORTH);
    }

    public String getText() {
        return textField.getText();
    }

    public void setPasswordField(boolean isPasswordField) {
        this.isPasswordField = isPasswordField;
        if (isPasswordField) {
            textField = new JPasswordField();
            add(textField, BorderLayout.CENTER);
        }
    }

    public boolean isPasswordField() {
        return isPasswordField;
    }
}
