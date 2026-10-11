package com.crowdcheck.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton database connection manager for CrowdCheck.
 * Supports configuration from:
 * 1. Environment variables (DB_URL, DB_USER, DB_PASSWORD, DB_DRIVER)
 * 2. External db.properties file in working directory
 * 3. Classpath db.properties resource
 */
public class DatabaseConnection {

    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/crowdcheck?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static String url;
    private static String user;
    private static String password;
    private static String driver;
    private static boolean initialized = false;

    private DatabaseConnection() {}

    /**
     * Initializes configuration properties prioritizing environment variables,
     * then external file, then classpath resource.
     */
    private static synchronized void initialize() {
        if (initialized) {
            return;
        }

        Properties props = new Properties();

        // 1. Try external db.properties in current directory
        File externalProps = new File("db.properties");
        if (externalProps.exists()) {
            try (InputStream is = new FileInputStream(externalProps)) {
                props.load(is);
                System.out.println("[DatabaseConnection] Loaded external db.properties");
            } catch (Exception e) {
                System.err.println("[DatabaseConnection] Warning: Could not read external db.properties: " + e.getMessage());
            }
        } else {
            // 2. Try classpath db.properties
            try (InputStream is = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (is != null) {
                    props.load(is);
                }
            } catch (Exception e) {
                System.err.println("[DatabaseConnection] Warning: Could not read classpath db.properties: " + e.getMessage());
            }
        }

        // Apply properties or defaults
        driver = props.getProperty("db.driver", DEFAULT_DRIVER);
        url = props.getProperty("db.url", DEFAULT_URL);
        user = props.getProperty("db.user", DEFAULT_USER);
        password = props.getProperty("db.password", DEFAULT_PASSWORD);

        // 3. Environment variables override properties
        String envDriver = System.getenv("DB_DRIVER");
        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASSWORD");

        if (envDriver != null && !envDriver.isBlank()) driver = envDriver;
        if (envUrl != null && !envUrl.isBlank()) url = envUrl;
        if (envUser != null && !envUser.isBlank()) user = envUser;
        if (envPass != null && !envPass.isBlank()) password = envPass;

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] Error loading JDBC Driver: " + driver + " -> " + e.getMessage());
        }

        initialized = true;
    }

    /**
     * Obtains a new JDBC connection.
     * Callers should manage the lifecycle using try-with-resources.
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            initialize();
        }
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Checks if MySQL server is reachable with the current credentials.
     */
    public static boolean testConnection() {
        if (!initialized) {
            initialize();
        }
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Explicitly update connection parameters at runtime (useful for testing or CLI config).
     */
    public static synchronized void configure(String customUrl, String customUser, String customPassword) {
        url = customUrl;
        user = customUser;
        password = customPassword;
        initialized = true;
    }

    public static String getUrl() {
        if (!initialized) initialize();
        return url;
    }

    public static String getUser() {
        if (!initialized) initialize();
        return user;
    }
}
