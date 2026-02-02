package com.fortis.managers;

import com.fortis.core.Transaction;
import com.fortis.core.TransactionState;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ACIDController - Ensures ACID properties for all transactions
 * Handles transaction persistence and retrieval
 */
public class ACIDController {
    
    /**
     * Save transaction to database
     * Part of Durability guarantee
     */
    public void saveTransaction(Transaction transaction, Connection conn) throws SQLException {
        String sql = "INSERT INTO transactions " +
                    "(transaction_uuid, from_account_id, to_account_id, amount, state, " +
                    "risk_score, risk_factors, description, initiated_at, completed_at, error_message) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "state = VALUES(state), " +
                    "risk_score = VALUES(risk_score), " +
                    "risk_factors = VALUES(risk_factors), " +
                    "completed_at = VALUES(completed_at), " +
                    "error_message = VALUES(error_message)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, transaction.getTransactionUuid());
            stmt.setLong(2, transaction.getFromAccountId());
            stmt.setLong(3, transaction.getToAccountId());
            stmt.setBigDecimal(4, transaction.getAmount());
            stmt.setString(5, transaction.getState().name());
            stmt.setInt(6, transaction.getRiskScore());
            stmt.setString(7, transaction.getRiskFactors());
            stmt.setString(8, transaction.getDescription());
            stmt.setTimestamp(9, Timestamp.valueOf(transaction.getInitiatedAt()));
            
            if (transaction.getCompletedAt() != null) {
                stmt.setTimestamp(10, Timestamp.valueOf(transaction.getCompletedAt()));
            } else {
                stmt.setNull(10, Types.TIMESTAMP);
            }
            
            stmt.setString(11, transaction.getErrorMessage());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Get transaction by UUID
     */
    public Transaction getTransaction(String uuid, Connection conn) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE transaction_uuid = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractTransaction(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Get all transactions for an account
     */
    public List<Transaction> getAccountTransactions(long accountId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM transactions " +
                    "WHERE from_account_id = ? OR to_account_id = ? " +
                    "ORDER BY initiated_at DESC LIMIT 100";
        
        List<Transaction> transactions = new ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, accountId);
            stmt.setLong(2, accountId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(extractTransaction(rs));
                }
            }
        }
        return transactions;
    }
    
    /**
     * Get transactions by state
     */
    public List<Transaction> getTransactionsByState(TransactionState state, Connection conn) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE state = ? ORDER BY initiated_at DESC LIMIT 100";
        
        List<Transaction> transactions = new ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, state.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(extractTransaction(rs));
                }
            }
        }
        return transactions;
    }
    
    /**
     * Get transaction statistics
     */
    public TransactionStats getStatistics(Connection conn) throws SQLException {
        String sql = "SELECT " +
                    "COUNT(*) as total, " +
                    "SUM(CASE WHEN state = 'COMMITTED' THEN 1 ELSE 0 END) as committed, " +
                    "SUM(CASE WHEN state = 'ROLLED_BACK' THEN 1 ELSE 0 END) as rolled_back, " +
                    "AVG(risk_score) as avg_risk, " +
                    "SUM(CASE WHEN state = 'COMMITTED' THEN amount ELSE 0 END) as total_amount " +
                    "FROM transactions " +
                    "WHERE DATE(initiated_at) = CURDATE()";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return new TransactionStats(
                    rs.getInt("total"),
                    rs.getInt("committed"),
                    rs.getInt("rolled_back"),
                    rs.getDouble("avg_risk"),
                    rs.getBigDecimal("total_amount")
                );
            }
        }
        return new TransactionStats(0, 0, 0, 0.0, java.math.BigDecimal.ZERO);
    }
    
    /**
     * Extract Transaction from ResultSet
     */
    private Transaction extractTransaction(ResultSet rs) throws SQLException {
        Timestamp completedTs = rs.getTimestamp("completed_at");
        
        return new Transaction(
            rs.getLong("transaction_id"),
            rs.getString("transaction_uuid"),
            rs.getLong("from_account_id"),
            rs.getLong("to_account_id"),
            rs.getBigDecimal("amount"),
            TransactionState.valueOf(rs.getString("state")),
            rs.getInt("risk_score"),
            rs.getString("risk_factors"),
            rs.getString("description"),
            rs.getTimestamp("initiated_at").toLocalDateTime(),
            completedTs != null ? completedTs.toLocalDateTime() : null,
            rs.getString("error_message")
        );
    }
    
    /**
     * Inner class for transaction statistics
     */
    public static class TransactionStats {
        public final int total;
        public final int committed;
        public final int rolledBack;
        public final double avgRisk;
        public final java.math.BigDecimal totalAmount;
        
        public TransactionStats(int total, int committed, int rolledBack, 
                              double avgRisk, java.math.BigDecimal totalAmount) {
            this.total = total;
            this.committed = committed;
            this.rolledBack = rolledBack;
            this.avgRisk = avgRisk;
            this.totalAmount = totalAmount;
        }
        
        public double getSuccessRate() {
            return total > 0 ? (committed * 100.0 / total) : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format("Stats[Total: %d, Committed: %d, Rolled Back: %d, Success Rate: %.1f%%, Avg Risk: %.1f]",
                    total, committed, rolledBack, getSuccessRate(), avgRisk);
        }
    }
}
