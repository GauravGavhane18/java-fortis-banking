package com.fortis.managers;

import com.fortis.core.Transaction;
import com.fortis.core.TransactionState;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * RollbackManager - Handles transaction rollback scenarios
 * Ensures atomicity by reverting all changes on failure
 */
public class RollbackManager {
    
    /**
     * Rollback a transaction
     * Ensures atomicity - all or nothing
     */
    public void rollbackTransaction(Transaction transaction, Connection conn, String reason) {
        try {
            // Rollback database changes
            if (conn != null && !conn.getAutoCommit()) {
                conn.rollback();
                System.out.println("✗ Database rolled back for: " + transaction.getTransactionUuid());
            }
            
            // Update transaction state
            transaction.transitionTo(TransactionState.ROLLED_BACK);
            transaction.setErrorMessage(reason);
            
            System.out.println("✗ Transaction rolled back: " + reason);
            System.out.println("  UUID: " + transaction.getTransactionUuid());
            System.out.println("  Amount: " + transaction.getAmount());
            
        } catch (SQLException e) {
            System.err.println("Error during rollback: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Check if transaction should be rolled back based on risk
     */
    public boolean shouldRollbackDueToRisk(int riskScore, int threshold) {
        return riskScore > threshold;
    }
    
    /**
     * Get rollback reason based on risk score
     */
    public String getRollbackReason(int riskScore) {
        if (riskScore > 90) {
            return "Critical risk level detected - automatic rollback";
        } else if (riskScore > 70) {
            return "High risk score exceeded threshold - automatic rollback";
        }
        return "Risk assessment failed";
    }
}
