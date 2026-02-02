package com.fortis.service;

import com.fortis.model.TransactionRecord;
import java.math.BigDecimal;
import java.util.List;

/**
 * Interface for transaction operations
 * Demonstrates Interface usage in OOP
 */
public interface Transactionable {
    TransactionRecord deposit(long accountId, BigDecimal amount, String description, com.fortis.model.User user);
    TransactionRecord withdraw(long accountId, BigDecimal amount, String description, com.fortis.model.User user);
    TransactionRecord transfer(long fromAccountId, long toAccountId, BigDecimal amount, String description, com.fortis.model.User user);
    List<TransactionRecord> getTransactionHistory(long accountId, com.fortis.model.User user);
    TransactionRecord getTransactionById(String transactionId, com.fortis.model.User user);
}
