package com.fortis.managers;

import com.fortis.core.*;
import com.fortis.persistence.DatabaseManager;
import com.fortis.persistence.WriteAheadLog;
import com.fortis.persistence.AuditLogger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TransactionManager - Orchestrates ACID-compliant transactions
 * Implements the transaction state machine and coordinates all managers
 */
public class TransactionManager {
    private final DatabaseManager dbManager;
    private final AccountManager accountManager;
    private final RiskEngine riskEngine;
    private final ACIDController acidController;
    private final RollbackManager rollbackManager;
    private final WriteAheadLog wal;
    private final AuditLogger auditLogger;
    
    // Account-level locks for concurrency control
    private final ConcurrentHashMap<Long, ReentrantLock> accountLocks;
    
    public TransactionManager() {
        this.dbManager = DatabaseManager.getInstance();
        this.accountManager = new AccountManager();
        this.riskEngine = new RiskEngine();
        this.acidController = new ACIDController();
        this.rollbackManager = new RollbackManager();
        this.wal = WriteAheadLog.getInstance();
        this.auditLogger = AuditLogger.getInstance();
        this.accountLocks = new ConcurrentHashMap<>();
    }
    
    /**
     * Execute a fund transfer with full ACID compliance
     * State machine: INIT → VALIDATED → RISK_CHECK → COMMITTED/ROLLED_BACK
     */
    public Transaction executeTransfer(long fromAccountId, long toAccountId, 
                                      BigDecimal amount, String description) {
        Transaction transaction = new Transaction(fromAccountId, toAccountId, amount, description);
        Connection conn = null;
        
        // Acquire locks in consistent order to prevent deadlock
        ReentrantLock lock1 = getLockForAccount(Math.min(fromAccountId, toAccountId));
        ReentrantLock lock2 = getLockForAccount(Math.max(fromAccountId, toAccountId));
        
        lock1.lock();
        try {
            if (fromAccountId != toAccountId) {
                lock2.lock();
            }
            
            try {
                // Log transaction initiation
                auditLogger.logEvent(transaction.getTransactionUuid(), "TRANSACTION_INITIATED",
                        fromAccountId, String.format("Transfer %.2f to account %d", amount, toAccountId));
                
                // Get database connection and start transaction
                conn = dbManager.getConnection();
                conn.setAutoCommit(false);
                conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                
                // Write to WAL: BEGIN
                wal.logBegin(transaction.getTransactionUuid());
                
                // STATE 1: INIT → VALIDATED
                if (!validateTransaction(transaction, conn)) {
                    rollback(transaction, conn, "Validation failed");
                    return transaction;
                }
                transaction.transitionTo(TransactionState.VALIDATED);
                auditLogger.logEvent(transaction.getTransactionUuid(), "VALIDATION_PASSED",
                        fromAccountId, "Business rules validated");
                
                // STATE 2: VALIDATED → RISK_CHECK
                transaction.transitionTo(TransactionState.RISK_CHECK);
                RiskScore riskScore = riskEngine.evaluateTransaction(transaction, conn);
                transaction.setRiskScore(riskScore.getTotalScore());
                transaction.setRiskFactors(riskScore.getDetailedBreakdown());
                
                auditLogger.logEvent(transaction.getTransactionUuid(), "RISK_EVALUATED",
                        fromAccountId, String.format("Risk score: %d", riskScore.getTotalScore()));
                
                // Check if risk is too high
                if (riskScore.shouldRollback()) {
                    rollback(transaction, conn, "High risk score: " + riskScore.getTotalScore());
                    return transaction;
                }
                
                // STATE 3: RISK_CHECK → COMMITTED
                // Execute the actual transfer
                Account fromAccount = accountManager.getAccount(fromAccountId, conn);
                Account toAccount = accountManager.getAccount(toAccountId, conn);
                
                // Debit from source
                BigDecimal oldFromBalance = fromAccount.getBalance();
                fromAccount.debit(amount);
                accountManager.updateBalance(fromAccount, conn);
                wal.logDebit(transaction.getTransactionUuid(), fromAccountId, amount, 
                           oldFromBalance, fromAccount.getBalance());
                
                // Credit to destination
                BigDecimal oldToBalance = toAccount.getBalance();
                toAccount.credit(amount);
                accountManager.updateBalance(toAccount, conn);
                wal.logCredit(transaction.getTransactionUuid(), toAccountId, amount,
                            oldToBalance, toAccount.getBalance());
                
                // Save transaction to database
                acidController.saveTransaction(transaction, conn);
                
                // Commit database transaction
                conn.commit();
                wal.logCommit(transaction.getTransactionUuid());
                
                // Update state to COMMITTED
                transaction.transitionTo(TransactionState.COMMITTED);
                
                auditLogger.logEvent(transaction.getTransactionUuid(), "TRANSACTION_COMMITTED",
                        fromAccountId, String.format("Successfully transferred %.2f", amount));
                
                System.out.println("✓ Transaction committed: " + transaction.getTransactionUuid());
                
            } catch (Exception e) {
                rollback(transaction, conn, "Exception: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            
        } finally {
            if (fromAccountId != toAccountId) {
                lock2.unlock();
            }
            lock1.unlock();
        }
        
        return transaction;
    }
    
    /**
     * Validate transaction business rules
     */
    private boolean validateTransaction(Transaction transaction, Connection conn) {
        try {
            Account fromAccount = accountManager.getAccount(transaction.getFromAccountId(), conn);
            Account toAccount = accountManager.getAccount(transaction.getToAccountId(), conn);
            
            // Check if accounts exist
            if (fromAccount == null || toAccount == null) {
                transaction.setErrorMessage("One or both accounts not found");
                return false;
            }
            
            // Check if accounts are active
            if (!fromAccount.isActive()) {
                transaction.setErrorMessage("Source account is not active");
                return false;
            }
            if (!toAccount.isActive()) {
                transaction.setErrorMessage("Destination account is not active");
                return false;
            }
            
            // Check if same account
            if (transaction.getFromAccountId() == transaction.getToAccountId()) {
                transaction.setErrorMessage("Cannot transfer to same account");
                return false;
            }
            
            // Check amount validity
            if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                transaction.setErrorMessage("Amount must be positive");
                return false;
            }
            
            // Check sufficient balance
            if (!fromAccount.hasSufficientBalance(transaction.getAmount())) {
                transaction.setErrorMessage("Insufficient balance");
                return false;
            }
            
            // Check daily limit
            BigDecimal todayTotal = accountManager.getTodayTransferTotal(
                    transaction.getFromAccountId(), conn);
            if (todayTotal.add(transaction.getAmount()).compareTo(fromAccount.getDailyLimit()) > 0) {
                transaction.setErrorMessage("Daily transfer limit exceeded");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            transaction.setErrorMessage("Validation error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Rollback transaction
     */
    private void rollback(Transaction transaction, Connection conn, String reason) {
        try {
            if (conn != null) {
                conn.rollback();
            }
            wal.logRollback(transaction.getTransactionUuid());
            transaction.transitionTo(TransactionState.ROLLED_BACK);
            transaction.setErrorMessage(reason);
            
            // Save rolled back transaction for audit
            if (conn != null) {
                acidController.saveTransaction(transaction, conn);
                conn.commit();
            }
            
            auditLogger.logEvent(transaction.getTransactionUuid(), "TRANSACTION_ROLLED_BACK",
                    transaction.getFromAccountId(), reason);
            
            System.out.println("✗ Transaction rolled back: " + reason);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Get or create lock for account
     */
    private ReentrantLock getLockForAccount(long accountId) {
        return accountLocks.computeIfAbsent(accountId, k -> new ReentrantLock(true));
    }
    
    /**
     * Get transaction by UUID
     */
    public Transaction getTransaction(String uuid) {
        try (Connection conn = dbManager.getConnection()) {
            return acidController.getTransaction(uuid, conn);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
