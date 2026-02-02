package com.fortis.model;

import java.math.BigDecimal;

/**
 * Current Account - Demonstrates Inheritance and Polymorphism
 * Different rules than Savings Account
 */
public class CurrentAccount extends BankAccount {
    private static final BigDecimal MINIMUM_BALANCE = new BigDecimal("5000.00");
    private static final BigDecimal OVERDRAFT_LIMIT = new BigDecimal("10000.00");
    private static final BigDecimal MAINTENANCE_FEE = new BigDecimal("500.00");
    
    private BigDecimal overdraftUsed;
    
    public CurrentAccount(long accountId, String accountNumber, String accountHolder,
                         BigDecimal balance, BigDecimal dailyLimit, long userId) {
        super(accountId, accountNumber, accountHolder, balance, dailyLimit, userId);
        this.overdraftUsed = BigDecimal.ZERO;
    }
    
    @Override
    public String getAccountType() {
        return "CURRENT";
    }
    
    @Override
    public BigDecimal calculateInterest() {
        // Current accounts typically don't earn interest
        return BigDecimal.ZERO;
    }
    
    @Override
    public boolean canWithdraw(BigDecimal amount) {
        if (!isActive()) return false;
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return false;
        
        // Can use overdraft facility
        BigDecimal availableBalance = balance.add(OVERDRAFT_LIMIT).subtract(overdraftUsed);
        return amount.compareTo(availableBalance) <= 0;
    }
    
    @Override
    public BigDecimal getMinimumBalance() {
        return MINIMUM_BALANCE;
    }
    
    @Override
    public synchronized void withdraw(BigDecimal amount) {
        if (amount.compareTo(balance) > 0) {
            // Using overdraft
            BigDecimal overdraftNeeded = amount.subtract(balance);
            overdraftUsed = overdraftUsed.add(overdraftNeeded);
        }
        super.withdraw(amount);
    }
    
    public void applyMaintenanceFee() {
        if (balance.compareTo(MINIMUM_BALANCE) < 0) {
            balance = balance.subtract(MAINTENANCE_FEE);
        }
    }
    
    public BigDecimal getOverdraftUsed() {
        return overdraftUsed;
    }
    
    public BigDecimal getAvailableOverdraft() {
        return OVERDRAFT_LIMIT.subtract(overdraftUsed);
    }
}
