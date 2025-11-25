package bookwise.DataAccess;

/*
 * Database Connection Manager
 * Handles all database connections using MySQL
 */

import bookwise.Config;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection utility class
 * Provides methods to establish and manage database connections
 * 
 * @author wsr
 */
public class DB {
    
    private static Connection activeConnection = null;
    
    // Static initializer to load MySQL driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL JDBC Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL JDBC Driver not found!");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection connectDb() {
        try {
            // Close existing connection if any
            if (activeConnection != null && !activeConnection.isClosed()) {
                activeConnection.close();
            }
            
            String connectionString = Config.Database.getConnectionStringWithOptions();
            activeConnection = DriverManager.getConnection(
                connectionString,
                Config.Database.USER,
                Config.Database.PASSWORD
            );
            
            System.out.println("✅ Connected to MySQL successfully!");
            System.out.println("Database: " + Config.Database.NAME);
            System.out.println("Host: " + Config.Database.HOST);
            
            return activeConnection;
        } catch (SQLException e) {
            System.out.println("❌ Connection failed!");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Gets the current active connection
     * If no connection exists, attempts to create one
     * 
     * @return Connection object
     */
    public static Connection getConnection() {
        try {
            if (activeConnection == null || activeConnection.isClosed()) {
                return connectDb();
            }
            return activeConnection;
        } catch (SQLException e) {
            System.out.println("❌ Error checking connection status!");
            e.printStackTrace();
            return connectDb();
        }
    }
    
    /**
     * Closes the active database connection
     */
    public static void closeConnection() {
        try {
            if (activeConnection != null && !activeConnection.isClosed()) {
                activeConnection.close();
                System.out.println("✅ Database connection closed.");
                activeConnection = null;
            }
        } catch (SQLException e) {
            System.out.println("❌ Error closing connection!");
            e.printStackTrace();
        }
    }
    
    /**
     * Checks if the database connection is active
     * 
     * @return true if connection is active, false otherwise
     */
    public static boolean isConnected() {
        try {
            return activeConnection != null && !activeConnection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

}