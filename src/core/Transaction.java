package com.fortis.core;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transaction entity representing a money transfer
 * Thread-safe with synchronized state transitions
 */
public class Transaction {
    private final long transactionId;
    private final String transactionUuid;
    private final long fromAccountId;
    private final long toAccountId;
    private final BigDecimal amount;
    private volatile TransactionState state;
    private volatile int riskScore;
    private volatile String riskFactors;
    private final String description;
    private final LocalDateTime initiatedAt;
    private volatile LocalDateTime completedAt;
    private volatile String errorMessage;
    
    // Constructor for new transaction
    public Transaction(long fromAccountId, long toAccountId, BigDecimal amount, String description) {
        this.transactionId = 0; // Will be set by database
        this.transactionUuid = UUID.randomUUID().toString();
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.state = TransactionState.INIT;
        this.riskScore = 0;
        this.description = description;
        this.initiatedAt = LocalDateTime.now();
    }
    
    // Constructor for loading from database
    public Transaction(long transactionId, String transactionUuid, long fromAccountId, 
                      long toAccountId, BigDecimal amount, TransactionState state,
                      int riskScore, String riskFactors, String description,
                      LocalDateTime initiatedAt, LocalDateTime completedAt, String errorMessage) {
        this.transactionId = transactionId;
        this.transactionUuid = transactionUuid;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.state = state;
        this.riskScore = riskScore;
        this.riskFactors = riskFactors;
        this.description = description;
        this.initiatedAt = initiatedAt;
        this.completedAt = completedAt;
        this.errorMessage = errorMessage;
    }
    
    // Getters
    public long getTransactionId() {
        return transactionId;
    }
    
    public String getTransactionUuid() {
        return transactionUuid;
    }
    
    public long getFromAccountId() {
        return fromAccountId;
    }
    
    public long getToAccountId() {
        return toAccountId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public synchronized TransactionState getState() {
        return state;
    }
    
    public synchronized int getRiskScore() {
        return riskScore;
    }
    
    public synchronized String getRiskFactors() {
        return riskFactors;
    }
    
    public String getDescription() {
        return description;
    }
    
    public LocalDateTime getInitiatedAt() {
        return initiatedAt;
    }
    
    public synchronized LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public synchronized String getErrorMessage() {
        return errorMessage;
    }
    
    // State transition with validation
    public synchronized boolean transitionTo(TransactionState newState) {
        if (state.canTransitionTo(newState)) {
            this.state = newState;
            if (newState.isTerminal()) {
                this.completedAt = LocalDateTime.now();
            }
            return true;
        }
        return false;
    }
    
    // Setters
    public synchronized void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }
    
    public synchronized void setRiskFactors(String riskFactors) {
        this.riskFactors = riskFactors;
    }
    
    public synchronized void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    // Business methods
    public synchronized boolean isCompleted() {
        return state.isTerminal();
    }
    
    public synchronized boolean isSuccessful() {
        return state == TransactionState.COMMITTED;
    }
    
    public synchronized boolean isHighRisk() {
        return riskScore > 70;
    }
    
    public long getDurationMillis() {
        if (completedAt == null) {
            return java.time.Duration.between(initiatedAt, LocalDateTime.now()).toMillis();
        }
        return java.time.Duration.between(initiatedAt, completedAt).toMillis();
    }
    
    @Override
    public String toString() {
        return String.format("Transaction[UUID: %s, From: %d, To: %d, Amount: %.2f, State: %s, Risk: %d]",
                transactionUuid, fromAccountId, toAccountId, amount, state, riskScore);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Transaction)) return false;
        Transaction other = (Transaction) obj;
        return transactionUuid.equals(other.transactionUuid);
    }
    
    @Override
    public int hashCode() {
        return transactionUuid.hashCode();
    }
}
