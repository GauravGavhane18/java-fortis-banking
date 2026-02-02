package com.fortis.core;

/**
 * Transaction state machine states
 * INIT → VALIDATED → RISK_CHECK → COMMITTED / ROLLED_BACK
 */
public enum TransactionState {
    /**
     * Initial state when transaction is created
     */
    INIT("Transaction initiated"),
    
    /**
     * Business rules and constraints validated
     */
    VALIDATED("Validation passed"),
    
    /**
     * Risk assessment in progress
     */
    RISK_CHECK("Risk evaluation in progress"),
    
    /**
     * Transaction successfully committed
     */
    COMMITTED("Transaction committed successfully"),
    
    /**
     * Transaction rolled back due to failure or high risk
     */
    ROLLED_BACK("Transaction rolled back");
    
    private final String description;
    
    TransactionState(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if state transition is valid
     */
    public boolean canTransitionTo(TransactionState newState) {
        switch (this) {
            case INIT:
                return newState == VALIDATED || newState == ROLLED_BACK;
            case VALIDATED:
                return newState == RISK_CHECK || newState == ROLLED_BACK;
            case RISK_CHECK:
                return newState == COMMITTED || newState == ROLLED_BACK;
            case COMMITTED:
            case ROLLED_BACK:
                return false; // Terminal states
            default:
                return false;
        }
    }
    
    public boolean isTerminal() {
        return this == COMMITTED || this == ROLLED_BACK;
    }
    
    public boolean isSuccessful() {
        return this == COMMITTED;
    }
}
