package com.fortis.model;

import java.math.BigDecimal;

/**
 * Savings Account - Demonstrates Inheritance and Polymorphism
 * Overrides abstract methods with specific implementation
 */
public class SavingsAccount extends BankAccount {
    private static final BigDecimal INTEREST_RATE = new BigDecimal("0.04"); // 4% annual
    private static final BigDecimal MINIMUM_BALANCE = new BigDecimal("1000.00");
    private static final int MAX_WITHDRAWALS_PER_MONTH = 5;
    
    private int withdrawalsThisMonth;
    
    public SavingsAccount(long accountId, String accountNumber, String accountHolder,
                         BigDecimal balance, BigDecimal dailyLimit, long userId) {
        super(accountId, accountNumber, accountHolder, balance, dailyLimit, userId);
        this.withdrawalsThisMonth = 0;
    }
    
    @Override
    public String getAccountType() {
        return "SAVINGS";
    }
    
    @Override
    public BigDecimal calculateInterest() {
        // Monthly interest = (balance * annual_rate) / 12
        return balance.multiply(INTEREST_RATE).divide(new BigDecimal("12"), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    @Override
    public boolean canWithdraw(BigDecimal amount) {
        if (!isActive()) return false;
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return false;
        if (withdrawalsThisMonth >= MAX_WITHDRAWALS_PER_MONTH) return false;
        
        BigDecimal balanceAfterWithdrawal = balance.subtract(amount);
        return balanceAfterWithdrawal.compareTo(MINIMUM_BALANCE) >= 0;
    }
    
    @Override
    public BigDecimal getMinimumBalance() {
        return MINIMUM_BALANCE;
    }
    
    @Override
    public synchronized void withdraw(BigDecimal amount) {
        super.withdraw(amount);
        withdrawalsThisMonth++;
    }
    
    public void resetMonthlyWithdrawals() {
        this.withdrawalsThisMonth = 0;
    }
    
    public int getWithdrawalsThisMonth() {
        return withdrawalsThisMonth;
    }
}
