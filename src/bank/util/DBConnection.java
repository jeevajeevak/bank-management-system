package bank.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection – singleton MySQL connection helper.
 *
 * ┌─────────────────────────────────────────────────────────────┐
 *  HOW TO CONFIGURE (two options):
 *
 *  Option A – Environment Variables (recommended for GitHub):
 *    Set these before running:
 *      DB_HOST      (default: localhost)
 *      DB_PORT      (default: 3306)
 *      DB_USER      (default: root)
 *      DB_PASSWORD  (required)
 *
 *  Option B – Edit the DEFAULT_PASSWORD value below directly.
 *    Only do this on your local machine. Never commit your password.
 * └─────────────────────────────────────────────────────────────┘
 */
public class DBConnection {

    // ── Fallback values (edit for local testing only) ─────────────
    private static final String DEFAULT_HOST     = "localhost";
    private static final String DEFAULT_PORT     = "3306";
    private static final String DEFAULT_USER     = "root";
    private static final String DEFAULT_PASSWORD = "";  // ← your password here
    // ──────────────────────────────────────────────────────────────

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String host     = getEnv("DB_HOST",     DEFAULT_HOST);
            String port     = getEnv("DB_PORT",     DEFAULT_PORT);
            String user     = getEnv("DB_USER",     DEFAULT_USER);
            String password = getEnv("DB_PASSWORD", DEFAULT_PASSWORD);

            String url = "jdbc:mysql://" + host + ":" + port
                    + "/bank_db?useSSL=false&serverTimezone=UTC";
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(url, user, password);
                System.out.println("[DB] Connected to MySQL at " + host + ":" + port);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found.", e);
            }
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static String getEnv(String key, String fallback) {
        String val = System.getenv(key);
        return (val != null && !val.isBlank()) ? val : fallback;
    }
}
