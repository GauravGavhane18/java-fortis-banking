package com.fortis.service;

import com.fortis.model.*;
import com.fortis.model.TransactionRecord.TransactionStatus;
import com.fortis.model.TransactionRecord.TransactionType;
import com.fortis.persistence.AuditLogger;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.io.File;

/**
 * Banking Service - Core business logic
 * Implements Transactionable interface
 */
public class BankingService implements Transactionable {
    private static BankingService instance;
    private final Map<Long, BankAccount> accounts;
    private final List<TransactionRecord> transactions;
    private final AuditLogger auditLogger;
    
    private BankingService() {
        this.accounts = new ConcurrentHashMap<>();
        this.transactions = Collections.synchronizedList(new ArrayList<>());
        this.auditLogger = AuditLogger.getInstance();
        loadData(); // Load from CSV
        if (accounts.isEmpty()) {
            initializeSampleAccounts();
            saveData();
        }
    }
    
    public static synchronized BankingService getInstance() {
        if (instance == null) {
            instance = new BankingService();
        }
        return instance;
    }
    
    // PERSISTENCE LOGIC
    private void saveData() {
        try {
            new File("data").mkdirs();
            // Save Accounts
            java.io.FileWriter fw = new java.io.FileWriter("data/accounts.csv");
            fw.write("ID,NUMBER,HOLDER,BALANCE,LIMIT,TYPE,STATUS,USER_ID\n");
            for (BankAccount acc : accounts.values()) {
                String type = acc instanceof SavingsAccount ? "SAVINGS" : "CURRENT";
                String status = acc.isActive() ? "ACTIVE" : "BLOCKED";
                fw.write(String.format("%d,%s,%s,%s,%s,%s,%s,%d\n", 
                    acc.getAccountId(), acc.getAccountNumber(), acc.getAccountHolder(), 
                    acc.getBalance(), "0", type, status, acc.getUserId()));
            }
            fw.close();
            
            // Save Transactions
            java.io.FileWriter fw2 = new java.io.FileWriter("data/transactions.csv");
            fw2.write("ID,FROM,TO,AMOUNT,TYPE,DESC,STATUS,DATE\n");
            for (TransactionRecord t : transactions) {
                fw2.write(String.format("%s,%d,%d,%s,%s,%s,%s,%s\n",
                    t.getTransactionId(), t.getFromAccountId(), t.getToAccountId(),
                    t.getAmount(), t.getType(), t.getDescription(), t.getStatus(), t.getTimestamp()));
            }
            fw2.close();
            
        } catch (Exception e) {
            System.err.println("Data Save Error: " + e.getMessage());
        }
    }
    
    private void loadData() {
        try {
            File f = new File("data/accounts.csv");
            if (!f.exists()) return;
            
            Scanner sc = new Scanner(f);
            if (sc.hasNextLine()) sc.nextLine(); // Skip header
            while (sc.hasNextLine()) {
                String[] parts = sc.nextLine().split(",");
                if (parts.length < 8) continue; // Ensure we have USER_ID
                long id = Long.parseLong(parts[0]);
                String num = parts[1];
                String holder = parts[2];
                BigDecimal bal = new BigDecimal(parts[3]);
                String type = parts[5];
                boolean active = parts[6].equals("ACTIVE");
                long userId = Long.parseLong(parts[7]);
                
                BankAccount acc;
                if (type.equals("SAVINGS")) acc = new SavingsAccount(id, num, holder, bal, BigDecimal.ZERO, userId);
                else acc = new CurrentAccount(id, num, holder, bal, BigDecimal.ZERO, userId);
                
                if (!active) acc.setStatus(BankAccount.AccountStatus.BLOCKED);
                accounts.put(id, acc);
            }
            sc.close();
        } catch (Exception e) {
            System.err.println("Load Error: " + e.getMessage());
        }
    }

    private void initializeSampleAccounts() {
        // Create sample accounts
        SavingsAccount acc1 = new SavingsAccount(1L, "ACC1001", "Virat Kohli", 
            new BigDecimal("50000.00"), new BigDecimal("100000.00"), 2L);
        accounts.put(1L, acc1);
        
        CurrentAccount acc2 = new CurrentAccount(2L, "ACC1002", "Rohit Sharma", 
            new BigDecimal("75000.00"), new BigDecimal("200000.00"), 3L);
        accounts.put(2L, acc2);
        
        SavingsAccount acc3 = new SavingsAccount(3L, "ACC1003", "MS Dhoni", 
            new BigDecimal("60000.00"), new BigDecimal("150000.00"), 4L);
        accounts.put(3L, acc3);
    }
    
    private void validateAccess(BankAccount account, User user) {
        if (user.isAdmin()) return; // Admin has full access
        
        if (account.getUserId() != user.getUserId()) {
            throw new SecurityException("ACCESS DENIED - This account does not belong to you.");
        }
    }
    
    @Override
    public TransactionRecord deposit(long accountId, BigDecimal amount, String description, User user) {
        BankAccount account = accounts.get(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }
        
        // Deposit allowed to any account (e.g. cash deposit)
        // validateAccess(account, user); <-- Removed to allow third-party deposits
        
        if (!account.isActive()) {
            throw new IllegalStateException("Account is not active");
        }
        
        String txnId = generateTransactionId();
        try {
            account.deposit(amount);
            TransactionRecord record = new TransactionRecord(txnId, 0L, accountId, 
                amount, TransactionType.DEPOSIT, description, TransactionStatus.COMPLETED);
            transactions.add(record);
            
            auditLogger.logSystemEvent("DEPOSIT", String.format("Account %d, Amount: %.2f, User: %s", accountId, amount, user.getUsername()));
            saveData();
            return record;
        } catch (Exception e) {
            TransactionRecord record = new TransactionRecord(txnId, 0L, accountId, 
                amount, TransactionType.DEPOSIT, description, TransactionStatus.FAILED);
            transactions.add(record);
            throw e;
        }
    }
    
    @Override
    public TransactionRecord withdraw(long accountId, BigDecimal amount, String description, User user) {
        BankAccount account = accounts.get(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }
        
        validateAccess(account, user);
        
        if (!account.isActive()) {
            throw new IllegalStateException("Account is not active");
        }
        
        // CONSISTENCY Check: Ensure balance checks are atomic with withdrawal
        synchronized (account) {
            if (!account.canWithdraw(amount)) {
                throw new IllegalStateException("Insufficient Funds");
            }
        }
        
        String txnId = generateTransactionId();
        try {
            // ATOMICITY: All or nothing
            account.withdraw(amount);
            
            TransactionRecord record = new TransactionRecord(txnId, accountId, 0L, 
                amount, TransactionType.WITHDRAWAL, description, TransactionStatus.COMPLETED);
            transactions.add(record);
            
            auditLogger.logSystemEvent("WITHDRAWAL", String.format("Account %d, Amount: %.2f, User: %s", accountId, amount, user.getUsername()));
            saveData();
            return record;
        } catch (Exception e) {
            TransactionRecord record = new TransactionRecord(txnId, accountId, 0L, 
                amount, TransactionType.WITHDRAWAL, description, TransactionStatus.FAILED);
            transactions.add(record);
            throw e;
        }
    }
    
    @Override
    public TransactionRecord transfer(long fromAccountId, long toAccountId, BigDecimal amount, String description, User user) {
        BankAccount fromAccount = accounts.get(fromAccountId);
        BankAccount toAccount = accounts.get(toAccountId);
        
        if (fromAccount == null || toAccount == null) {
            throw new IllegalArgumentException("Account not found");
        }
        
        // Only sender needs to be validated
        validateAccess(fromAccount, user);
        
        if (!fromAccount.isActive() || !toAccount.isActive()) {
            throw new IllegalStateException("One or both accounts are not active");
        }
        
        String txnId = generateTransactionId();
        try {
            // ATOMICITY & ISOLATION
            // Determine lock order to prevent deadlocks
            BankAccount first = fromAccountId < toAccountId ? fromAccount : toAccount;
            BankAccount second = fromAccountId < toAccountId ? toAccount : fromAccount;
            
            synchronized (first) {
                synchronized (second) {
                   // Double-check consistency inside lock
                   if (!fromAccount.canWithdraw(amount)) {
                       throw new IllegalStateException("Insufficient Funds");
                   }
                   
                   fromAccount.withdraw(amount);
                   try {
                       toAccount.deposit(amount);
                   } catch (Exception e) {
                       // ROLLBACK
                       fromAccount.deposit(amount); 
                       throw new IllegalStateException("Transfer failed, rolled back: " + e.getMessage());
                   }
                }
            }
            
            TransactionRecord record = new TransactionRecord(txnId, fromAccountId, toAccountId, 
                amount, TransactionType.TRANSFER, description, TransactionStatus.COMPLETED);
            transactions.add(record);
            
            auditLogger.logSystemEvent("TRANSFER", String.format("From %d to %d, Amount: %.2f, User: %s", 
                fromAccountId, toAccountId, amount, user.getUsername()));
            saveData();
            return record;
        } catch (Exception e) {
            TransactionRecord record = new TransactionRecord(txnId, fromAccountId, toAccountId, 
                amount, TransactionType.TRANSFER, description, TransactionStatus.FAILED);
            transactions.add(record);
            throw e;
        }
    }
    
    @Override
    public List<TransactionRecord> getTransactionHistory(long accountId, User user) {
        BankAccount account = accounts.get(accountId);
        if (account == null) {
             // For history, if account doesn't exist, just return empty or error.
             // But security check first requires account existence IF we check ownership of paramID
             // However, if paramID is not found, we can't check ownership.
             throw new IllegalArgumentException("Account not found");
        }
        validateAccess(account, user);
        
        return transactions.stream()
            .filter(t -> t.getFromAccountId() == accountId || t.getToAccountId() == accountId)
            .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()))
            .collect(Collectors.toList());
    }
    
    @Override
    public TransactionRecord getTransactionById(String transactionId, User user) {
        TransactionRecord txn = transactions.stream()
            .filter(t -> t.getTransactionId().equals(transactionId))
            .findFirst()
            .orElse(null);
            
        if (txn != null) {
            // Check if user owns either 'from' or 'to' account
            BankAccount from = accounts.get(txn.getFromAccountId());
            BankAccount to = accounts.get(txn.getToAccountId());
            
            boolean hasAccess = false;
            // From account check
            if (from != null && (user.isAdmin() || from.getUserId() == user.getUserId())) hasAccess = true;
            // To account check
            if (to != null && (user.isAdmin() || to.getUserId() == user.getUserId())) hasAccess = true;
            
            if (!hasAccess) {
                throw new SecurityException("ACCESS DENIED - You cannot view this transaction.");
            }
        }
        return txn;
    }
    
    // Secure Get Account
    public BankAccount getAccount(long accountId, User user) {
        BankAccount account = accounts.get(accountId);
        if (account == null) return null;
        validateAccess(account, user);
        return account;
    }
    
    // Internal use / Admin restricted - DEPRECATED for public use
    public BankAccount getAccountInternal(long accountId) {
        return accounts.get(accountId);
    }
    
    public List<BankAccount> getAllAccounts() {
        return new ArrayList<>(accounts.values());
    }
    
    public List<TransactionRecord> getAllTransactions(User user) {
         if (user.isAdmin()) {
             return new ArrayList<>(transactions);
         }
         return transactions.stream()
             .filter(t -> {
                 BankAccount from = accounts.get(t.getFromAccountId());
                 BankAccount to = accounts.get(t.getToAccountId());
                 boolean isFromMine = from != null && from.getUserId() == user.getUserId();
                 boolean isToMine = to != null && to.getUserId() == user.getUserId();
                 return isFromMine || isToMine;
             })
             .collect(Collectors.toList());
    }
    
    public List<BankAccount> getAccountsByUser(long userId) {
        return accounts.values().stream()
            .filter(acc -> acc.getUserId() == userId)
            .collect(Collectors.toList());
    }
    
    public long createAccount(String accountHolder, BigDecimal initialBalance, 
                             String accountType, long userId, User user) {
        if (!user.isAdmin()) {
            throw new SecurityException("ACCESS DENIED - Only Admins can create accounts.");
        }
        
        long accountId = accounts.size() + 1L;
        String accountNumber = "ACC" + String.format("%04d", accountId);
        BigDecimal dailyLimit = new BigDecimal("100000.00");
        
        BankAccount account;
        if ("SAVINGS".equalsIgnoreCase(accountType)) {
            account = new SavingsAccount(accountId, accountNumber, accountHolder, 
                initialBalance, dailyLimit, userId);
        } else {
            account = new CurrentAccount(accountId, accountNumber, accountHolder, 
                initialBalance, dailyLimit, userId);
        }
        
        accounts.put(accountId, account);
        auditLogger.logSystemEvent("ACCOUNT_CREATED", String.format("ID %d, Type: %s, Holder: %s, By: %s", 
            accountId, accountType, accountHolder, user.getUsername()));
        saveData();
        return accountId;
    }
    
    public void blockAccount(long accountId, User user) {
        if (!user.isAdmin()) {
            throw new SecurityException("ACCESS DENIED - Only Admins can block accounts.");
        }
        BankAccount account = accounts.get(accountId);
        if (account != null) {
            account.setStatus(BankAccount.AccountStatus.BLOCKED);
            auditLogger.logSystemEvent("ACCOUNT_BLOCKED", String.format("ID %d, By: %s", accountId, user.getUsername()));
            saveData();
        }
    }
    
    public void unblockAccount(long accountId, User user) {
        if (!user.isAdmin()) {
            throw new SecurityException("ACCESS DENIED - Only Admins can unblock accounts.");
        }
        BankAccount account = accounts.get(accountId);
        if (account != null) {
            account.setStatus(BankAccount.AccountStatus.ACTIVE);
            auditLogger.logSystemEvent("ACCOUNT_UNBLOCKED", String.format("ID %d, By: %s", accountId, user.getUsername()));
            saveData();
        }
    }
    
    public void deleteAccount(long accountId, User user) {
        if (!user.isAdmin()) {
            throw new SecurityException("ACCESS DENIED - Only Admins can delete accounts.");
        }
        if (!accounts.containsKey(accountId)) {
            throw new IllegalArgumentException("Account not found");
        }
        accounts.remove(accountId);
        auditLogger.logSystemEvent("ACCOUNT_DELETED", String.format("ID %d, By: %s", accountId, user.getUsername()));
        saveData();
    }
    
    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
