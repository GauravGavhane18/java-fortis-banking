package com.fortis.core;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Account entity representing a bank account
 * Immutable for thread safety
 */
public class Account {
    private final long accountId;
    private final String accountNumber;
    private final String accountHolder;
    private volatile BigDecimal balance; // volatile for visibility across threads
    private final AccountType accountType;
    private volatile AccountStatus status;
    private final LocalDateTime createdAt;
    private volatile LocalDateTime lastTransactionAt;
    private volatile RiskLevel riskLevel;
    private final BigDecimal dailyLimit;
    
    public enum AccountType {
        SAVINGS, CURRENT, FIXED
    }
    
    public enum AccountStatus {
        ACTIVE, FROZEN, CLOSED
    }
    
    public enum RiskLevel {
        LOW, MEDIUM, HIGH
    }
    
    // Constructor
    public Account(long accountId, String accountNumber, String accountHolder, 
                   BigDecimal balance, AccountType accountType, AccountStatus status,
                   LocalDateTime createdAt, BigDecimal dailyLimit) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.balance = balance;
        this.accountType = accountType;
        this.status = status;
        this.createdAt = createdAt;
        this.dailyLimit = dailyLimit;
        this.riskLevel = RiskLevel.LOW;
    }
    
    // Getters
    public long getAccountId() {
        return accountId;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public String getAccountHolder() {
        return accountHolder;
    }
    
    public synchronized BigDecimal getBalance() {
        return balance;
    }
    
    public AccountType getAccountType() {
        return accountType;
    }
    
    public synchronized AccountStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public synchronized LocalDateTime getLastTransactionAt() {
        return lastTransactionAt;
    }
    
    public synchronized RiskLevel getRiskLevel() {
        return riskLevel;
    }
    
    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }
    
    // Synchronized setters for thread safety
    public synchronized void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    public synchronized void setStatus(AccountStatus status) {
        this.status = status;
    }
    
    public synchronized void setLastTransactionAt(LocalDateTime lastTransactionAt) {
        this.lastTransactionAt = lastTransactionAt;
    }
    
    public synchronized void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }
    
    // Business methods
    public synchronized boolean hasSufficientBalance(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }
    
    public synchronized boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }
    
    public synchronized void debit(BigDecimal amount) {
        if (!hasSufficientBalance(amount)) {
            throw new IllegalStateException("Insufficient balance");
        }
        this.balance = this.balance.subtract(amount);
        this.lastTransactionAt = LocalDateTime.now();
    }
    
    public synchronized void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.lastTransactionAt = LocalDateTime.now();
    }
    
    public int getAccountAgeInDays() {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
    }
    
    @Override
    public String toString() {
        return String.format("Account[%s, %s, Balance: %.2f, Status: %s]",
                accountNumber, accountHolder, balance, status);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Account)) return false;
        Account other = (Account) obj;
        return accountId == other.accountId;
    }
    
    @Override//@override means we are overriding the method of the parent class
    public int hashCode() {
        return Long.hashCode(accountId);
    }
}
