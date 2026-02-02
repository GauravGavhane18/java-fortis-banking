package com.fortis.persistence;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AuditLogger - Immutable audit trail for compliance
 * All events are logged and never deleted
 */
public class AuditLogger {
    private static AuditLogger instance;
    private static final String AUDIT_DIR = "logs/audit/";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    private BufferedWriter writer;
    private String currentLogFile;
    
    private AuditLogger() {
        initializeAuditLog();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized AuditLogger getInstance() {
        if (instance == null) {
            instance = new AuditLogger();
        }
        return instance;
    }
    
    /**
     * Initialize audit log
     */
    private void initializeAuditLog() {
        try {
            File dir = new File(AUDIT_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // Create daily log file
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            currentLogFile = AUDIT_DIR + "audit_" + date + ".log";
            
            writer = new BufferedWriter(new FileWriter(currentLogFile, true));
            System.out.println("✓ Audit Logger initialized: " + currentLogFile);
            
        } catch (IOException e) {
            System.err.println("✗ Failed to initialize Audit Logger: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Log an event
     */
    public synchronized void logEvent(String transactionUuid, String eventType, 
                                     long accountId, String details) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[%s] TXN:%s | EVENT:%s | ACCOUNT:%d | %s",
                timestamp, transactionUuid, eventType, accountId, details);
        
        writeLog(logEntry);
    }
    
    /**
     * Log system event
     */
    public synchronized void logSystemEvent(String eventType, String details) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[%s] SYSTEM | EVENT:%s | %s",
                timestamp, eventType, details);
        
        writeLog(logEntry);
    }
    
    /**
     * Log security event
     */
    public synchronized void logSecurityEvent(String eventType, long accountId, String details) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[%s] SECURITY | EVENT:%s | ACCOUNT:%d | %s",
                timestamp, eventType, accountId, details);
        
        writeLog(logEntry);
        System.out.println("⚠ Security Event: " + eventType);
    }
    
    /**
     * Write log entry
     */
    private void writeLog(String entry) {
        try {
            writer.write(entry);
            writer.newLine();
            writer.flush(); // Ensure durability
        } catch (IOException e) {
            System.err.println("✗ Failed to write to audit log: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Close audit logger
     */
    public synchronized void close() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing audit log: " + e.getMessage());
        }
    }
    
    /**
     * Rotate log file (create new file for new day)
     */
    public synchronized void rotateLog() {
        try {
            writer.close();
            initializeAuditLog();
            logSystemEvent("LOG_ROTATED", "New audit log file created");
        } catch (IOException e) {
            System.err.println("Error rotating audit log: " + e.getMessage());
        }
    }
}
