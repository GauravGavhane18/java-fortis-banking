package com.fortis.managers;

import com.fortis.core.Account;
import com.fortis.core.Account.AccountStatus;
import com.fortis.core.Account.AccountType;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AccountManager - Handles all account-related operations
 */
public class AccountManager {
    
    /**
     * Get account by ID with row-level locking
     */
    public Account getAccount(long accountId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_id = ? FOR UPDATE";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, accountId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractAccount(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Get account by account number
     */
    public Account getAccountByNumber(String accountNumber, Connection conn) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountNumber);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractAccount(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Get all accounts
     */
    public List<Account> getAllAccounts(Connection conn) throws SQLException {
        String sql = "SELECT * FROM accounts ORDER BY account_id";
        List<Account> accounts = new ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                accounts.add(extractAccount(rs));
            }
        }
        return accounts;
    }
    
    /**
     * Update account balance
     */
    public void updateBalance(Account account, Connection conn) throws SQLException {
        String sql = "UPDATE accounts SET balance = ?, last_transaction_at = ? WHERE account_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, account.getBalance());
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(3, account.getAccountId());
            stmt.executeUpdate();
        }
    }
    
    /**
     * Update account status
     */
    public void updateStatus(long accountId, AccountStatus status, Connection conn) throws SQLException {
        String sql = "UPDATE accounts SET status = ? WHERE account_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setLong(2, accountId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Get today's transfer total for an account
     */
    public BigDecimal getTodayTransferTotal(long accountId, Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total " +
                    "FROM transactions " +
                    "WHERE from_account_id = ? " +
                    "AND DATE(initiated_at) = CURDATE() " +
                    "AND state = 'COMMITTED'";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, accountId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("total");
                }
            }
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Create new account
     */
    public long createAccount(String accountHolder, BigDecimal initialBalance, 
                             AccountType accountType, Connection conn) throws SQLException {
        String sql = "INSERT INTO accounts (account_number, account_holder, balance, account_type, status, daily_limit) " +
                    "VALUES (?, ?, ?, ?, 'ACTIVE', 100000.00)";
        
        String accountNumber = generateAccountNumber();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, accountNumber);
            stmt.setString(2, accountHolder);
            stmt.setBigDecimal(3, initialBalance);
            stmt.setString(4, accountType.name());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to create account");
    }
    
    /**
     * Generate unique account number
     */
    private String generateAccountNumber() {
        long timestamp = System.currentTimeMillis();
        long random = (long) (Math.random() * 10000);
        return String.format("ACC%d%04d", timestamp % 1000000000, random);
    }
    
    /**
     * Extract Account from ResultSet
     */
    private Account extractAccount(ResultSet rs) throws SQLException {
        return new Account(
            rs.getLong("account_id"),
            rs.getString("account_number"),
            rs.getString("account_holder"),
            rs.getBigDecimal("balance"),
            AccountType.valueOf(rs.getString("account_type")),
            AccountStatus.valueOf(rs.getString("status")),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getBigDecimal("daily_limit")
        );
    }
}
