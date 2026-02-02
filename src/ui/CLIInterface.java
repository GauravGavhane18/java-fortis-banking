package com.fortis.ui;

import com.fortis.core.Account;
import com.fortis.core.Transaction;
import com.fortis.managers.*;
import com.fortis.persistence.DatabaseManager;
import com.fortis.recovery.RecoveryManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Scanner;

/**
 * CLI Interface for JAVA-FORTIS Banking System
 * Provides command-line access to all features
 */
public class CLIInterface {
    
    private final TransactionManager transactionManager;
    private final AccountManager accountManager;
    private final ACIDController acidController;
    private final DatabaseManager dbManager;
    private final RecoveryManager recoveryManager;
    private final Scanner scanner;
    
    public CLIInterface() {
        this.transactionManager = new TransactionManager();
        this.accountManager = new AccountManager();
        this.acidController = new ACIDController();
        this.dbManager = DatabaseManager.getInstance();
        this.recoveryManager = new RecoveryManager();
        this.scanner = new Scanner(System.in);
    }
    
    public static void main(String[] args) {
        CLIInterface cli = new CLIInterface();
        cli.start();
    }
    
    public void start() {
        printBanner();
        
        // Test database connection
        if (!dbManager.testConnection()) {
            System.err.println("✗ Cannot connect to database. Please check configuration.");
            return;
        }
        
        // Run crash recovery
        recoveryManager.recoverFromCrash();
        recoveryManager.verifyConsistency();
        
        // Main menu loop
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    viewAllAccounts();
                    break;
                case "2":
                    viewAccountDetails();
                    break;
                case "3":
                    executeTransfer();
                    break;
                case "4":
                    viewTransactionHistory();
                    break;
                case "5":
                    viewStatistics();
                    break;
                case "6":
                    createAccount();
                    break;
                case "7":
                    testConcurrentTransactions();
                    break;
                case "8":
                    createCheckpoint();
                    break;
                case "9":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
            
            if (running) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
        
        System.out.println("\n✓ Thank you for using JAVA-FORTIS!");
        scanner.close();
    }
    
    private void printBanner() {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║                                                           ║");
        System.out.println("║              JAVA-FORTIS (ATOMICASH)                      ║");
        System.out.println("║        ACID-Compliant Banking System v1.0                 ║");
        System.out.println("║                                                           ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();
    }
    
    private void printMenu() {
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║                      MAIN MENU                            ║");
        System.out.println("╠═══════════════════════════════════════════════════════════╣");
        System.out.println("║  1. View All Accounts                                     ║");
        System.out.println("║  2. View Account Details                                  ║");
        System.out.println("║  3. Execute Fund Transfer                                 ║");
        System.out.println("║  4. View Transaction History                              ║");
        System.out.println("║  5. View Statistics                                       ║");
        System.out.println("║  6. Create New Account                                    ║");
        System.out.println("║  7. Test Concurrent Transactions                          ║");
        System.out.println("║  8. Create Checkpoint                                     ║");
        System.out.println("║  9. Exit                                                  ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.print("\nEnter your choice: ");
    }
    
    private void viewAllAccounts() {
        System.out.println("\n=== ALL ACCOUNTS ===");
        
        try (Connection conn = dbManager.getConnection()) {
            List<Account> accounts = accountManager.getAllAccounts(conn);
            
            System.out.println(String.format("%-15s %-20s %-15s %-10s %-10s",
                    "Account ID", "Account Number", "Holder", "Balance", "Status"));
            System.out.println("─".repeat(75));
            
            for (Account account : accounts) {
                System.out.println(String.format("%-15d %-20s %-15s ₹%-10.2f %-10s",
                        account.getAccountId(),
                        account.getAccountNumber(),
                        account.getAccountHolder(),
                        account.getBalance(),
                        account.getStatus()));
            }
            
            System.out.println("\nTotal Accounts: " + accounts.size());
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
        }
    }
    
    private void viewAccountDetails() {
        System.out.print("\nEnter Account ID: ");
        long accountId = Long.parseLong(scanner.nextLine().trim());
        
        try (Connection conn = dbManager.getConnection()) {
            Account account = accountManager.getAccount(accountId, conn);
            
            if (account == null) {
                System.out.println("✗ Account not found");
                return;
            }
            
            System.out.println("\n=== ACCOUNT DETAILS ===");
            System.out.println("Account ID:       " + account.getAccountId());
            System.out.println("Account Number:   " + account.getAccountNumber());
            System.out.println("Account Holder:   " + account.getAccountHolder());
            System.out.println("Balance:          ₹" + account.getBalance());
            System.out.println("Account Type:     " + account.getAccountType());
            System.out.println("Status:           " + account.getStatus());
            System.out.println("Risk Level:       " + account.getRiskLevel());
            System.out.println("Daily Limit:      ₹" + account.getDailyLimit());
            System.out.println("Account Age:      " + account.getAccountAgeInDays() + " days");
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
        }
    }
    
    private void executeTransfer() {
        System.out.println("\n=== FUND TRANSFER ===");
        
        try {
            System.out.print("From Account ID: ");
            long fromAccountId = Long.parseLong(scanner.nextLine().trim());
            
            System.out.print("To Account ID: ");
            long toAccountId = Long.parseLong(scanner.nextLine().trim());
            
            System.out.print("Amount: ₹");
            BigDecimal amount = new BigDecimal(scanner.nextLine().trim());
            
            System.out.print("Description: ");
            String description = scanner.nextLine().trim();
            
            System.out.println("\nProcessing transfer...");
            Transaction transaction = transactionManager.executeTransfer(
                    fromAccountId, toAccountId, amount, description);
            
            System.out.println("\n=== TRANSACTION RESULT ===");
            System.out.println("Transaction UUID: " + transaction.getTransactionUuid());
            System.out.println("State:            " + transaction.getState());
            System.out.println("Risk Score:       " + transaction.getRiskScore() + "/100");
            System.out.println("Duration:         " + transaction.getDurationMillis() + "ms");
            
            if (transaction.isSuccessful()) {
                System.out.println("\n✓ Transfer completed successfully!");
            } else {
                System.out.println("\n✗ Transfer failed: " + transaction.getErrorMessage());
            }
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
        }
    }
    
    private void viewTransactionHistory() {
        System.out.print("\nEnter Account ID: ");
        long accountId = Long.parseLong(scanner.nextLine().trim());
        
        try (Connection conn = dbManager.getConnection()) {
            List<Transaction> transactions = acidController.getAccountTransactions(accountId, conn);
            
            System.out.println("\n=== TRANSACTION HISTORY ===");
            System.out.println(String.format("%-10s %-15s %-15s %-10s %-12s %-10s",
                    "From", "To", "Amount", "Risk", "State", "Time"));
            System.out.println("─".repeat(80));
            
            for (Transaction txn : transactions) {
                System.out.println(String.format("%-10d %-15d ₹%-15.2f %-10d %-12s %s",
                        txn.getFromAccountId(),
                        txn.getToAccountId(),
                        txn.getAmount(),
                        txn.getRiskScore(),
                        txn.getState(),
                        txn.getInitiatedAt().toString().substring(0, 19)));
            }
            
            System.out.println("\nTotal Transactions: " + transactions.size());
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
        }
    }
    
    private void viewStatistics() {
        System.out.println("\n=== TODAY'S STATISTICS ===");
        
        try (Connection conn = dbManager.getConnection()) {
            ACIDController.TransactionStats stats = acidController.getStatistics(conn);
            
            System.out.println("Total Transactions:    " + stats.total);
            System.out.println("Committed:             " + stats.committed);
            System.out.println("Rolled Back:           " + stats.rolledBack);
            System.out.println("Success Rate:          " + String.format("%.1f%%", stats.getSuccessRate()));
            System.out.println("Average Risk Score:    " + String.format("%.1f", stats.avgRisk));
            System.out.println("Total Amount:          ₹" + stats.totalAmount);
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
        }
    }
    
    private void createAccount() {
        System.out.println("\n=== CREATE NEW ACCOUNT ===");
        
        try {
            System.out.print("Account Holder Name: ");
            String holder = scanner.nextLine().trim();
            
            System.out.print("Initial Balance: ₹");
            BigDecimal balance = new BigDecimal(scanner.nextLine().trim());
            
            System.out.print("Account Type (SAVINGS/CURRENT/FIXED): ");
            String typeStr = scanner.nextLine().trim().toUpperCase();
            Account.AccountType type = Account.AccountType.valueOf(typeStr);
            
            try (Connection conn = dbManager.getConnection()) {
                conn.setAutoCommit(false);
                long accountId = accountManager.createAccount(holder, balance, type, conn);
                conn.commit();
                
                System.out.println("\n✓ Account created successfully!");
                System.out.println("Account ID: " + accountId);
            }
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
        }
    }
    
    private void testConcurrentTransactions() {
        System.out.println("\n=== TESTING CONCURRENT TRANSACTIONS ===");
        System.out.println("This will execute 5 concurrent transfers...\n");
        
        Thread[] threads = new Thread[5];
        
        for (int i = 0; i < 5; i++) {
            final int threadNum = i + 1;
            threads[i] = new Thread(() -> {
                Transaction txn = transactionManager.executeTransfer(
                        1, 2, new BigDecimal("100.00"), "Concurrent test " + threadNum);
                System.out.println("Thread " + threadNum + ": " + txn.getState());
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("\n✓ Concurrent test completed");
    }
    
    private void createCheckpoint() {
        System.out.println("\n=== CREATING CHECKPOINT ===");
        recoveryManager.createCheckpoint();
        System.out.println("✓ Checkpoint created successfully");
    }
}
