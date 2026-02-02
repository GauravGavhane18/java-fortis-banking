package com.fortis.managers;

import com.fortis.core.Account;
import com.fortis.core.RiskScore;
import com.fortis.core.Transaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * RiskEngine - Evaluates transaction risk based on multiple factors
 * Risk Score: 0-100 (0 = lowest risk, 100 = highest risk)
 */
public class RiskEngine {
    
    private static final int RISK_THRESHOLD = 70;
    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("50000");
    private static final int VELOCITY_WINDOW_MINUTES = 60;
    private static final int MAX_TRANSACTIONS_PER_HOUR = 10;
    
    private final AccountManager accountManager;
    
    public RiskEngine() {
        this.accountManager = new AccountManager();
    }
    
    /**
     * Evaluate transaction risk
     * Returns RiskScore with breakdown of all factors
     */
    public RiskScore evaluateTransaction(Transaction transaction, Connection conn) {
        Map<String, Integer> factorScores = new HashMap<>();
        
        try {
            Account fromAccount = accountManager.getAccount(transaction.getFromAccountId(), conn);
            Account toAccount = accountManager.getAccount(transaction.getToAccountId(), conn);
            
            // Factor 1: Transaction Amount (0-30 points)
            int amountScore = calculateAmountRisk(transaction.getAmount(), fromAccount.getBalance());
            factorScores.put("amount", amountScore);
            
            // Factor 2: Transaction Frequency (0-25 points)
            int frequencyScore = calculateFrequencyRisk(transaction.getFromAccountId(), conn);
            factorScores.put("frequency", frequencyScore);
            
            // Factor 3: Account Age (0-15 points)
            int accountAgeScore = calculateAccountAgeRisk(fromAccount);
            factorScores.put("account_age", accountAgeScore);
            
            // Factor 4: Velocity (Rapid Transfers) (0-20 points)
            int velocityScore = calculateVelocityRisk(transaction.getFromAccountId(), conn);
            factorScores.put("velocity", velocityScore);
            
            // Factor 5: Account Status (0-10 points)
            int statusScore = calculateStatusRisk(fromAccount, toAccount);
            factorScores.put("status", statusScore);
            
            // Calculate total score
            int totalScore = factorScores.values().stream().mapToInt(Integer::intValue).sum();
            
            return new RiskScore(totalScore, factorScores);
            
        } catch (Exception e) {
            e.printStackTrace();
            // On error, return high risk score
            factorScores.put("error", 100);
            return new RiskScore(100, factorScores);
        }
    }
    
    /**
     * Calculate risk based on transaction amount
     * Higher amounts = higher risk
     */
    private int calculateAmountRisk(BigDecimal amount, BigDecimal balance) {
        // Risk increases with amount
        if (amount.compareTo(HIGH_AMOUNT_THRESHOLD) > 0) {
            return 30; // Very high amount
        }
        
        // Calculate percentage of balance
        BigDecimal percentage = amount.divide(balance, 2, BigDecimal.ROUND_HALF_UP)
                                     .multiply(new BigDecimal("100"));
        
        if (percentage.compareTo(new BigDecimal("80")) > 0) {
            return 25; // Transferring >80% of balance
        } else if (percentage.compareTo(new BigDecimal("50")) > 0) {
            return 15; // Transferring >50% of balance
        } else if (percentage.compareTo(new BigDecimal("25")) > 0) {
            return 8; // Transferring >25% of balance
        }
        
        return 0; // Low amount
    }
    
    /**
     * Calculate risk based on transaction frequency
     * Too many transactions = higher risk
     */
    private int calculateFrequencyRisk(long accountId, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM transactions " +
                    "WHERE from_account_id = ? " +
                    "AND DATE(initiated_at) = CURDATE() " +
                    "AND state = 'COMMITTED'";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, accountId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("count");
                    
                    if (count > 20) return 25; // Very high frequency
                    if (count > 10) return 15; // High frequency
                    if (count > 5) return 8;   // Moderate frequency
                    return 0; // Normal frequency
                }
            }
        }
        return 0;
    }
    
    /**
     * Calculate risk based on account age
     * Newer accounts = higher risk
     */
    private int calculateAccountAgeRisk(Account account) {
        int ageInDays = account.getAccountAgeInDays();
        
        if (ageInDays < 7) return 15;   // Less than 1 week
        if (ageInDays < 30) return 10;  // Less than 1 month
        if (ageInDays < 90) return 5;   // Less than 3 months
        return 0; // Established account
    }
    
    /**
     * Calculate risk based on velocity (rapid successive transfers)
     * Multiple transfers in short time = higher risk
     */
    private int calculateVelocityRisk(long accountId, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM transactions " +
                    "WHERE from_account_id = ? " +
                    "AND initiated_at >= DATE_SUB(NOW(), INTERVAL ? MINUTE) " +
                    "AND state = 'COMMITTED'";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, accountId);
            stmt.setInt(2, VELOCITY_WINDOW_MINUTES);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("count");
                    
                    if (count > MAX_TRANSACTIONS_PER_HOUR) return 20; // Rapid transfers
                    if (count > 5) return 10; // Moderate velocity
                    return 0; // Normal velocity
                }
            }
        }
        return 0;
    }
    
    /**
     * Calculate risk based on account status
     */
    private int calculateStatusRisk(Account fromAccount, Account toAccount) {
        int risk = 0;
        
        // Check risk levels
        if (fromAccount.getRiskLevel() == Account.RiskLevel.HIGH) {
            risk += 5;
        }
        if (toAccount.getRiskLevel() == Account.RiskLevel.HIGH) {
            risk += 5;
        }
        
        return risk;
    }
    
    /**
     * Update account risk level based on transaction history
     */
    public void updateAccountRiskLevel(long accountId, Connection conn) throws SQLException {
        String sql = "SELECT AVG(risk_score) as avg_risk FROM transactions " +
                    "WHERE (from_account_id = ? OR to_account_id = ?) " +
                    "AND initiated_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                    "AND state = 'COMMITTED'";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, accountId);
            stmt.setLong(2, accountId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double avgRisk = rs.getDouble("avg_risk");
                    
                    Account.RiskLevel newLevel;
                    if (avgRisk > 70) {
                        newLevel = Account.RiskLevel.HIGH;
                    } else if (avgRisk > 30) {
                        newLevel = Account.RiskLevel.MEDIUM;
                    } else {
                        newLevel = Account.RiskLevel.LOW;
                    }
                    
                    // Update account risk level
                    String updateSql = "UPDATE accounts SET risk_level = ? WHERE account_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, newLevel.name());
                        updateStmt.setLong(2, accountId);
                        updateStmt.executeUpdate();
                    }
                }
            }
        }
    }
}
