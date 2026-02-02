package com.fortis.persistence;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

/**
 * Database Manager - MySQL Connection and Query Management
 * Handles all database operations with connection pooling
 */
public class DatabaseManager {
    
    private static DatabaseManager instance;
    private Connection connection;
    private Properties dbProperties;
    
    // Database configuration
    private String dbHost;
    private String dbPort;
    private String dbName;
    private String dbUser;
    private String dbPassword;
    private String dbUrl;
    
    /**
     * Private constructor for singleton pattern
     */
    private DatabaseManager() {
        loadConfiguration();
        connect();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Load database configuration
     */
    private void loadConfiguration() {
        dbProperties = new Properties();
        
        try {
            // Try to load from properties file
            FileInputStream fis = new FileInputStream("database/db.properties");
            dbProperties.load(fis);
            fis.close();
            
            dbHost = dbProperties.getProperty("db.host", "localhost");
            dbPort = dbProperties.getProperty("db.port", "3306");
            dbName = dbProperties.getProperty("db.name", "fortis_atm");
            dbUser = dbProperties.getProperty("db.user", "root");
            dbPassword = dbProperties.getProperty("db.password", "");
            
        } catch (IOException e) {
            // Use default configuration
            System.out.println("[WARN] Could not load db.properties, using defaults");
            dbHost = "localhost";
            dbPort = "3306";
            dbName = "fortis_atm";
            dbUser = "root";
            dbPassword = "";
        }
        
        dbUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                dbHost, dbPort, dbName);
    }
    
    /**
     * Establish database connection
     */
    private void connect() {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Establish connection
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            
            System.out.println("[SUCCESS] Connected to database: " + dbName);
            
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] MySQL JDBC Driver not found!");
            System.err.println("Please add mysql-connector-java-8.0.33.jar to lib/ directory");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to connect to database!");
            System.err.println("URL: " + dbUrl);
            System.err.println("User: " + dbUser);
            e.printStackTrace();
        }
    }
    
    /**
     * Get active connection
     */
    public Connection getConnection() {
        try {
            // Check if connection is still valid
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Connection check failed");
            e.printStackTrace();
        }
        return connection;
    }
    
    /**
     * Execute SELECT query
     */
    public ResultSet executeQuery(String query) throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeQuery(query);
    }
    
    /**
     * Execute SELECT query with parameters (PreparedStatement)
     */
    public ResultSet executeQuery(String query, Object... params) throws SQLException {
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        
        return pstmt.executeQuery();
    }
    
    /**
     * Execute UPDATE/INSERT/DELETE query
     */
    public int executeUpdate(String query) throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeUpdate(query);
    }
    
    /**
     * Execute UPDATE/INSERT/DELETE with parameters
     */
    public int executeUpdate(String query, Object... params) throws SQLException {
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        
        return pstmt.executeUpdate();
    }
    
    /**
     * Call stored procedure with OUT parameters
     */
    public CallableStatement callProcedure(String procedureName, int inParams, int outParams) throws SQLException {
        StringBuilder sql = new StringBuilder("{CALL " + procedureName + "(");
        
        int totalParams = inParams + outParams;
        for (int i = 0; i < totalParams; i++) {
            sql.append("?");
            if (i < totalParams - 1) sql.append(",");
        }
        sql.append(")}");
        
        return getConnection().prepareCall(sql.toString());
    }
    
    /**
     * Test database connection
     */
    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                // Execute simple query
                ResultSet rs = executeQuery("SELECT 1");
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Connection test failed");
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get database statistics
     */
    public void printDatabaseStats() {
        try {
            System.out.println("\n═══ DATABASE STATISTICS ═══");
            
            ResultSet rs = executeQuery("SELECT COUNT(*) as count FROM users");
            if (rs.next()) {
                System.out.println("Total Users: " + rs.getInt("count"));
            }
            
            rs = executeQuery("SELECT COUNT(*) as count FROM accounts");
            if (rs.next()) {
                System.out.println("Total Accounts: " + rs.getInt("count"));
            }
            
            rs = executeQuery("SELECT COUNT(*) as count FROM transactions");
            if (rs.next()) {
                System.out.println("Total Transactions: " + rs.getInt("count"));
            }
            
            rs = executeQuery("SELECT SUM(denomination * quantity) as total FROM atm_cash");
            if (rs.next()) {
                System.out.println("ATM Cash Available: ₹" + rs.getDouble("total"));
            }
            
            System.out.println("═══════════════════════════\n");
            
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch database statistics");
            e.printStackTrace();
        }
    }
    
    /**
     * Close database connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[INFO] Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to close connection");
            e.printStackTrace();
        }
    }
    
    /**
     * Begin transaction
     */
    public void beginTransaction() throws SQLException {
        getConnection().setAutoCommit(false);
    }
    
    /**
     * Commit transaction
     */
    public void commit() throws SQLException {
        getConnection().commit();
        getConnection().setAutoCommit(true);
    }
    
    /**
     * Rollback transaction
     */
    public void rollback() throws SQLException {
        getConnection().rollback();
        getConnection().setAutoCommit(true);
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        System.out.println("Testing Database Connection...\n");
        
        DatabaseManager db = DatabaseManager.getInstance();
        
        if (db.testConnection()) {
            System.out.println("✔ Database connection successful!");
            db.printDatabaseStats();
        } else {
            System.out.println("✖ Database connection failed!");
        }
        
        db.close();
    }
}
