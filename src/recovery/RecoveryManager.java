package com.fortis.recovery;

import com.fortis.persistence.WriteAheadLog;
import com.fortis.persistence.DatabaseManager;
import com.fortis.persistence.AuditLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * RecoveryManager - Handles crash recovery using Write-Ahead Log
 * Replays uncommitted transactions after system restart
 */
public class RecoveryManager {
    
    private final WriteAheadLog wal;
    private final DatabaseManager dbManager;
    private final AuditLogger auditLogger;
    
    public RecoveryManager() {
        this.wal = WriteAheadLog.getInstance();
        this.dbManager = DatabaseManager.getInstance();
        this.auditLogger = AuditLogger.getInstance();
    }
    
    /**
     * Recover from crash using WAL
     * Called on system startup
     */
    public void recoverFromCrash() {
        System.out.println("=== Starting Crash Recovery ===");
        auditLogger.logSystemEvent("RECOVERY_STARTED", "Checking for uncommitted transactions");
        
        try {
            List<String> uncommittedTxns = wal.getUncommittedTransactions();
            
            if (uncommittedTxns.isEmpty()) {
                System.out.println("✓ No uncommitted transactions found");
                auditLogger.logSystemEvent("RECOVERY_COMPLETE", "No recovery needed");
                return;
            }
            
            System.out.println("Found " + uncommittedTxns.size() + " uncommitted transactions");
            
            // Rollback all uncommitted transactions
            for (String txnUuid : uncommittedTxns) {
                rollbackUncommittedTransaction(txnUuid);
            }
            
            System.out.println("✓ Recovery completed successfully");
            auditLogger.logSystemEvent("RECOVERY_COMPLETE", 
                    "Rolled back " + uncommittedTxns.size() + " transactions");
            
        } catch (Exception e) {
            System.err.println("✗ Recovery failed: " + e.getMessage());
            auditLogger.logSystemEvent("RECOVERY_FAILED", e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== Recovery Complete ===\n");
    }
    
    /**
     * Rollback an uncommitted transaction
     */
    private void rollbackUncommittedTransaction(String txnUuid) {
        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            
            // Mark transaction as rolled back in database
            String sql = "UPDATE transactions SET state = 'ROLLED_BACK', " +
                        "error_message = 'Rolled back during crash recovery' " +
                        "WHERE transaction_uuid = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, txnUuid);
                int updated = stmt.executeUpdate();
                
                if (updated > 0) {
                    conn.commit();
                    System.out.println("  ✓ Rolled back transaction: " + txnUuid);
                    auditLogger.logEvent(txnUuid, "RECOVERY_ROLLBACK", 0, 
                            "Transaction rolled back during recovery");
                } else {
                    conn.rollback();
                }
            }
            
        } catch (SQLException e) {
            System.err.println("  ✗ Failed to rollback transaction " + txnUuid + ": " + e.getMessage());
        }
    }
    
    /**
     * Verify database consistency after recovery
     */
    public boolean verifyConsistency() {
        System.out.println("=== Verifying Database Consistency ===");
        
        try (Connection conn = dbManager.getConnection()) {
            // Check for any inconsistencies
            String sql = "CALL check_balance_consistency()";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                var rs = stmt.executeQuery();
                
                if (rs.next()) {
                    System.out.println("✗ Consistency check failed - inconsistencies found");
                    auditLogger.logSystemEvent("CONSISTENCY_CHECK_FAILED", 
                            "Database inconsistencies detected");
                    return false;
                }
            }
            
            System.out.println("✓ Database consistency verified");
            auditLogger.logSystemEvent("CONSISTENCY_CHECK_PASSED", "Database is consistent");
            return true;
            
        } catch (SQLException e) {
            System.err.println("✗ Consistency check error: " + e.getMessage());
            return false;
        } finally {
            System.out.println("=== Consistency Check Complete ===\n");
        }
    }
    
    /**
     * Create backup checkpoint
     */
    public void createCheckpoint() {
        System.out.println("Creating checkpoint...");
        wal.checkpoint();
        auditLogger.logSystemEvent("CHECKPOINT_CREATED", "System checkpoint created");
    }
    
    /**
     * Archive old logs
     */
    public void archiveLogs() {
        System.out.println("Archiving logs...");
        wal.archive();
        auditLogger.logSystemEvent("LOGS_ARCHIVED", "Old logs archived");
    }
}
