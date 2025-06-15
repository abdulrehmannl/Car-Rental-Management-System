package com.carrental;
import javax.swing.SwingUtilities;

public class  Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPage());
    }
}
