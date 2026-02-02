package com.fortis.persistence;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * WriteAheadLog (WAL) - Ensures durability and enables crash recovery
 * All operations are logged before being committed to database
 */
public class WriteAheadLog {
    private static WriteAheadLog instance;
    private static final String WAL_DIR = "logs/wal/";
    private static final String WAL_FILE = WAL_DIR + "transactions.wal";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    private BufferedWriter writer;
    
    private WriteAheadLog() {
        initializeWAL();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized WriteAheadLog getInstance() {
        if (instance == null) {
            instance = new WriteAheadLog();
        }
        return instance;
    }
    
    /**
     * Initialize WAL file
     */
    private void initializeWAL() {
        try {
            File dir = new File(WAL_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            writer = new BufferedWriter(new FileWriter(WAL_FILE, true));
            System.out.println("✓ Write-Ahead Log initialized: " + WAL_FILE);
            
        } catch (IOException e) {
            System.err.println("✗ Failed to initialize WAL: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Log transaction begin
     */
    public synchronized void logBegin(String transactionUuid) {
        writeLog(String.format("BEGIN|%s|%s", transactionUuid, getCurrentTimestamp()));
    }
    
    /**
     * Log debit operation
     */
    public synchronized void logDebit(String transactionUuid, long accountId, 
                                     BigDecimal amount, BigDecimal oldBalance, BigDecimal newBalance) {
        writeLog(String.format("DEBIT|%s|%d|%.2f|%.2f|%.2f|%s",
                transactionUuid, accountId, amount, oldBalance, newBalance, getCurrentTimestamp()));
    }
    
    /**
     * Log credit operation
     */
    public synchronized void logCredit(String transactionUuid, long accountId,
                                      BigDecimal amount, BigDecimal oldBalance, BigDecimal newBalance) {
        writeLog(String.format("CREDIT|%s|%d|%.2f|%.2f|%.2f|%s",
                transactionUuid, accountId, amount, oldBalance, newBalance, getCurrentTimestamp()));
    }
    
    /**
     * Log transaction commit
     */
    public synchronized void logCommit(String transactionUuid) {
        writeLog(String.format("COMMIT|%s|%s", transactionUuid, getCurrentTimestamp()));
    }
    
    /**
     * Log transaction rollback
     */
    public synchronized void logRollback(String transactionUuid) {
        writeLog(String.format("ROLLBACK|%s|%s", transactionUuid, getCurrentTimestamp()));
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
            System.err.println("✗ Failed to write to WAL: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Read all WAL entries
     */
    public List<WALEntry> readWAL() {
        List<WALEntry> entries = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(WAL_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                entries.add(parseWALEntry(line));
            }
        } catch (IOException e) {
            System.err.println("Error reading WAL: " + e.getMessage());
        }
        
        return entries;
    }
    
    /**
     * Parse WAL entry from string
     */
    private WALEntry parseWALEntry(String line) {
        String[] parts = line.split("\\|");
        return new WALEntry(parts[0], parts);
    }
    
    /**
     * Get uncommitted transactions from WAL
     */
    public List<String> getUncommittedTransactions() {
        List<String> uncommitted = new ArrayList<>();
        List<WALEntry> entries = readWAL();
        
        for (WALEntry entry : entries) {
            if (entry.operation.equals("BEGIN")) {
                uncommitted.add(entry.transactionUuid);
            } else if (entry.operation.equals("COMMIT") || entry.operation.equals("ROLLBACK")) {
                uncommitted.remove(entry.transactionUuid);
            }
        }
        
        return uncommitted;
    }
    
    /**
     * Checkpoint - mark all transactions as committed in database
     */
    public synchronized void checkpoint() {
        try {
            writer.write("CHECKPOINT|" + getCurrentTimestamp());
            writer.newLine();
            writer.flush();
            System.out.println("✓ WAL checkpoint created");
        } catch (IOException e) {
            System.err.println("✗ Checkpoint failed: " + e.getMessage());
        }
    }
    
    /**
     * Archive old WAL file and start new one
     */
    public synchronized void archive() {
        try {
            writer.close();
            
            String archiveName = WAL_DIR + "transactions_" + 
                               LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".wal";
            File currentFile = new File(WAL_FILE);
            File archiveFile = new File(archiveName);
            
            if (currentFile.renameTo(archiveFile)) {
                System.out.println("✓ WAL archived: " + archiveName);
            }
            
            // Start new WAL
            initializeWAL();
            
        } catch (IOException e) {
            System.err.println("✗ WAL archive failed: " + e.getMessage());
        }
    }
    
    /**
     * Get current timestamp
     */
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(formatter);
    }
    
    /**
     * Close WAL
     */
    public synchronized void close() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing WAL: " + e.getMessage());
        }
    }
    
    /**
     * WAL Entry class
     */
    public static class WALEntry {
        public final String operation;
        public final String transactionUuid;
        public final String[] data;
        
        public WALEntry(String operation, String[] data) {
            this.operation = operation;
            this.transactionUuid = data.length > 1 ? data[1] : "";
            this.data = data;
        }
        
        @Override
        public String toString() {
            return String.format("WALEntry[%s, %s]", operation, transactionUuid);
        }
    }
}
