package bank;

import bank.ui.MainFrame;
import bank.util.DBConnection;

import javax.swing.*;

/**
 * Main – application entry point.
 *
 * Run this class to start JavaBank Management System.
 */
public class Main {

    public static void main(String[] args) {
        // Test DB connection before showing UI
        try {
            DBConnection.getConnection();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "❌ Cannot connect to MySQL!\n\n" +
                    "Please check:\n" +
                    "  1. MySQL is running\n" +
                    "  2. DB credentials in DBConnection.java\n" +
                    "  3. You ran sql/schema.sql\n" +
                    "  4. mysql-connector-java.jar is in /lib\n\n" +
                    "Error: " + e.getMessage(),
                    "Connection Failed", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Launch Swing UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Use system look-and-feel for a native feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
