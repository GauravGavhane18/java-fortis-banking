package com.fortis.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Abstract base class for all account types
 * Demonstrates Abstraction and Inheritance
 */
public abstract class BankAccount {
    protected final long accountId;
    protected final String accountNumber;
    protected final String accountHolder;
    protected BigDecimal balance;
    protected AccountStatus status;
    protected final LocalDateTime createdAt;
    protected LocalDateTime lastTransactionAt;
    protected final BigDecimal dailyLimit;
    protected long userId; // Link to User
    
    public enum AccountStatus {
        ACTIVE("Active", "Account is operational"),
        BLOCKED("Blocked", "Account is temporarily blocked"),
        FROZEN("Frozen", "Account is frozen by admin"),
        CLOSED("Closed", "Account is permanently closed");
        
        private final String displayName;
        private final String description;
        
        AccountStatus(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public BankAccount(long accountId, String accountNumber, String accountHolder,
                      BigDecimal balance, BigDecimal dailyLimit, long userId) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.balance = balance;
        this.dailyLimit = dailyLimit;
        this.userId = userId;
        this.status = AccountStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }
    
    // Abstract methods - must be implemented by subclasses
    public abstract String getAccountType();
    public abstract BigDecimal calculateInterest();
    public abstract boolean canWithdraw(BigDecimal amount);
    public abstract BigDecimal getMinimumBalance();
    
    // Common methods
    public synchronized void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        this.balance = this.balance.add(amount);
        this.lastTransactionAt = LocalDateTime.now();
    }
    
    public synchronized void withdraw(BigDecimal amount) {
        if (!canWithdraw(amount)) {
            throw new IllegalStateException("Withdrawal not allowed");
        }
        this.balance = this.balance.subtract(amount);
        this.lastTransactionAt = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }
    
    // Getters
    public long getAccountId() { return accountId; }
    public String getAccountNumber() { return accountNumber; }
    public String getAccountHolder() { return accountHolder; }
    public synchronized BigDecimal getBalance() { return balance; }
    public synchronized AccountStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastTransactionAt() { return lastTransactionAt; }
    public BigDecimal getDailyLimit() { return dailyLimit; }
    public long getUserId() { return userId; }
    
    // Setters
    public synchronized void setBalance(BigDecimal balance) { this.balance = balance; }
    public synchronized void setStatus(AccountStatus status) { this.status = status; }
    public void setLastTransactionAt(LocalDateTime time) { this.lastTransactionAt = time; }
}
