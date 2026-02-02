package com.fortis.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Transaction Record - Immutable transaction data
 */
public class TransactionRecord {
    private final String transactionId;
    private final long fromAccountId;
    private final long toAccountId;
    private final BigDecimal amount;
    private final TransactionType type;
    private final LocalDateTime timestamp;
    private final String description;
    private final TransactionStatus status;
    
    public enum TransactionType {
        DEPOSIT("Deposit", "Credit to account"),
        WITHDRAWAL("Withdrawal", "Debit from account"),
        TRANSFER("Transfer", "Fund transfer"),
        INTEREST("Interest", "Interest credit"),
        FEE("Fee", "Service charge");
        
        private final String displayName;
        private final String description;
        
        TransactionType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public enum TransactionStatus {
        PENDING, COMPLETED, FAILED, REVERSED
    }
    
    public TransactionRecord(String transactionId, long fromAccountId, long toAccountId,
                           BigDecimal amount, TransactionType type, String description,
                           TransactionStatus status) {
        this.transactionId = transactionId;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.description = description;
        this.status = status;
    }
    
    // Getters
    public String getTransactionId() { return transactionId; }
    public long getFromAccountId() { return fromAccountId; }
    public long getToAccountId() { return toAccountId; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDescription() { return description; }
    public TransactionStatus getStatus() { return status; }
    
    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s: ₹%.2f - %s", 
            getFormattedTimestamp(), type.getDisplayName(), amount, description);
    }
}
