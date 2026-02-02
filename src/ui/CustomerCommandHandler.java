package com.fortis.ui;

import com.fortis.model.*;
import com.fortis.service.*;
import com.fortis.utils.*;
import java.math.BigDecimal;
import java.util.*;
import java.io.File;
import java.util.stream.Collectors;

/**
 * Customer Command Handler
 * Handles all customer-specific banking operations
 * Extracted from EnhancedCLI for better maintainability
 */
public class CustomerCommandHandler {
    
    private final Scanner scanner;
    private final AuthenticationService authService;
    private final BankingService bankingService;
    private final LoanService loanService;
    private final NotificationService notifService;
    
    public CustomerCommandHandler(Scanner scanner) {
        this.scanner = scanner;
        this.authService = AuthenticationService.getInstance();
        this.bankingService = BankingService.getInstance();
        this.loanService = (LoanService) LoanService.getInstance();
        this.notifService = (NotificationService) NotificationService.getInstance();
    }
    
    /**
     * Process customer command
     */
    public void processCommand(String cmd, String[] parts) {
        switch (cmd) {
            case "withdraw": case "1": handleWithdraw(parts); break;
            case "deposit": case "2": handleDeposit(parts); break;
            case "transfer": case "3": handleTransfer(parts); break;
            case "check-balance": case "4": listAccounts(); break;
            case "account-details": case "5": accountDetails(); break;
            case "transaction-history": case "6": transactionHistory(); break;
            case "mini-statement": case "7": miniStatement(); break;
            case "download-statement": case "8": handleDownloadStatement(); break;
            case "view-limits": case "9": 
                simulateAction("Fetching Account Limits...", "Daily Withdrawal Limit: $50,000"); 
                break;
            case "request-loan": case "10": requestLoan(); break;
            case "loan-status": case "11": checkLoanStatus(); break;
            case "report-issue": case "12": reportIssue(); break;
            case "notifications": case "13": checkNotifications(); break;
            case "update-profile": case "14": 
                simulateAction("Accessing Profile...", "Profile update feature is read-only in this demo."); 
                break;
            case "change-password": case "15": changePassword(); break;
            default: System.out.println(ANSIColors.error("Unknown command."));
        }
    }
    
    // ==================== TRANSACTION HANDLERS ====================
    
    private void handleWithdraw(String[] parts) {
        try {
            double amount = getAmountFromPartsOrPrompt(parts);
            User user = authService.getCurrentUser();
            long id = resolveTargetAccountId(user);
            
            TransactionRecord txn = bankingService.withdraw(id, new BigDecimal(amount), "Withdrawal", user);
            BankAccount acc = bankingService.getAccount(id, user);
            
            // ANIMATION LAUNCH
            createAndRunBatch("withdraw", amount, "0C");
            
            TerminalUI.showLoader("Dispensing Cash...", 2000);
            TransactionReceiptGenerator.printReceipt(txn, acc, null);
            
        } catch (SecurityException se) {
            System.out.println(ANSIColors.error(se.getMessage()));
        } catch (Exception e) {
            System.out.println(ANSIColors.error("Transaction Failed: " + e.getMessage()));
        }
    }
    
    private void handleDeposit(String[] parts) {
        try {
            double amount = getAmountFromPartsOrPrompt(parts);
            User user = authService.getCurrentUser();
            long id = resolveTargetAccountId(user);
            
            TransactionRecord txn = bankingService.deposit(id, new BigDecimal(amount), "Deposit", user);
            
            BankAccount acc;
            try {
                acc = bankingService.getAccount(id, user);
            } catch (SecurityException e) {
                acc = bankingService.getAccountInternal(id);
            }
            
            // ANIMATION LAUNCH
            createAndRunBatch("deposit", amount, "0A");
            
            TerminalUI.showLoader("Counting Cash...", 2000);
            TransactionReceiptGenerator.printReceipt(txn, null, acc);
            
        } catch (SecurityException se) {
            System.out.println(ANSIColors.error(se.getMessage()));
        } catch (Exception e) {
            System.out.println(ANSIColors.error("Transaction Failed: " + e.getMessage()));
        }
    }
    
    private void handleTransfer(String[] parts) {
        try {
            User user = authService.getCurrentUser();
            long from = resolveTargetAccountId(user);
            
            System.out.print("To Account ID: ");
            long to = Long.parseLong(scanner.nextLine());
            
            System.out.print("Amount: ");
            double amt = Double.parseDouble(scanner.nextLine());
            System.out.print("Description: ");
            String desc = scanner.nextLine();
            if (desc.isEmpty()) desc = "Transfer";
            
            TransactionRecord txn = bankingService.transfer(from, to, new BigDecimal(amt), desc, user);
            BankAccount fromAcc = bankingService.getAccount(from, user);
            BankAccount toAcc = bankingService.getAccountInternal(to);
            
            TerminalUI.showLoader("Processing Transfer...", 1000);
            
            // Notify recipient
            notifService.addNotification(toAcc.getUserId(), 
                String.format("Received $%.2f from %s (Acc: ...%s)", amt, fromAcc.getAccountHolder(), 
                fromAcc.getAccountNumber().substring(3)));
            
            TransactionReceiptGenerator.printReceipt(txn, fromAcc, toAcc);
            
        } catch (SecurityException se) {
            System.out.println(ANSIColors.error(se.getMessage()));
        } catch (Exception e) {
            System.out.println(ANSIColors.error("Transfer Failed: " + e.getMessage()));
        }
    }
    
    // ==================== ACCOUNT MANAGEMENT ====================
    
    private void listAccounts() {
        User user = authService.getCurrentUser();
        List<BankAccount> accounts = bankingService.getAccountsByUser(user.getUserId());
        
        String[] headers = {"ID", "Account Number", "Holder", "Balance", "Status"};
        List<String[]> data = new ArrayList<>();
        for (BankAccount a : accounts) {
            data.add(new String[]{
                String.valueOf(a.getAccountId()),
                a.getAccountNumber(),
                a.getAccountHolder(),
                ANSIColors.GREEN + "$" + a.getBalance().toString() + ANSIColors.RESET,
                a.isActive() ? "ACTIVE" : "BLOCKED"
            });
        }
        
        TerminalUI.printTable(headers, data);
    }
    
    private void accountDetails() {
        try {
            User user = authService.getCurrentUser();
            long id = resolveTargetAccountId(user);
            
            BankAccount acc = bankingService.getAccount(id, user);
            if (acc != null) {
                String content = 
                    "Number:  " + acc.getAccountNumber() + "\n" +
                    "Holder:  " + acc.getAccountHolder() + "\n" +
                    "Balance: $" + acc.getBalance() + "\n" +
                    "Status:  " + (acc.isActive() ? "ACTIVE" : "BLOCKED") + "\n" +
                    "Type:    " + acc.getClass().getSimpleName();
                TerminalUI.printBox("ACCOUNT DETAILS", content, ANSIColors.CYAN);
            }
        } catch (Exception e) {
            System.out.println(ANSIColors.error(e.getMessage()));
        }
    }
    
    private void transactionHistory() {
        try {
            User user = authService.getCurrentUser();
            long id = resolveTargetAccountId(user);
            
            List<TransactionRecord> txns = bankingService.getTransactionHistory(id, user);
            
            String[] headers = {"ID", "Type", "Amount", "Status", "Date"};
            List<String[]> data = new ArrayList<>();
            for (TransactionRecord t : txns) {
                data.add(new String[]{
                    t.getTransactionId().substring(0, 12),
                    t.getType().toString(),
                    "$" + t.getAmount().toString(),
                    t.getStatus().toString(),
                    t.getTimestamp().toString().substring(0, 16)
                });
            }
            
            TerminalUI.printTable(headers, data);
        } catch (Exception e) {
            System.out.println(ANSIColors.error(e.getMessage()));
        }
    }
    
    private void miniStatement() {
        try {
            User user = authService.getCurrentUser();
            long id = resolveTargetAccountId(user);
            
            List<TransactionRecord> txns = bankingService.getTransactionHistory(id, user);
            String[] headers = {"ID", "Type", "Amount"};
            List<String[]> data = new ArrayList<>();
            
            int count = 0;
            for (TransactionRecord t : txns) {
                if (count >= 5) break;
                data.add(new String[]{
                    t.getTransactionId().substring(0, 12),
                    t.getType().toString(),
                    "$" + t.getAmount().toString()
                });
                count++;
            }
            
            TerminalUI.printTable(headers, data);
        } catch (Exception e) {
            System.out.println(ANSIColors.error(e.getMessage()));
        }
    }
    
    private void handleDownloadStatement() {
        try {
            User user = authService.getCurrentUser();
            long id = resolveTargetAccountId(user);
            
            BankAccount acc = bankingService.getAccount(id, user);
            if (acc == null) {
                System.out.println(ANSIColors.error("Account not found."));
                return;
            }
            List<TransactionRecord> history = bankingService.getTransactionHistory(id, user);
            
            String userHome = System.getProperty("user.home");
            File file = new File(userHome + "/Downloads/Fortis_Statement_" + id + ".txt");
            if (!file.getParentFile().exists()) {
                file = new File("Fortis_Statement_" + id + ".txt");
            }
            
            java.io.FileWriter fw = new java.io.FileWriter(file);
            fw.write("FORTIS BANKING SYSTEM\n");
            fw.write("ACCOUNT STATEMENT\n");
            fw.write("Generated on: " + new java.util.Date() + "\n\n");
            fw.write("Account: " + acc.getAccountNumber() + " (" + acc.getAccountHolder() + ")\n");
            fw.write("Current Balance: $" + acc.getBalance() + "\n");
            fw.write("------------------------------------------------------------\n");
            fw.write(String.format("%-20s %-10s %-15s %-30s\n", "DATE", "TYPE", "AMOUNT", "DESCRIPTION"));
            fw.write("------------------------------------------------------------\n");
            
            for (TransactionRecord t : history) {
                fw.write(String.format("%-20s %-10s %-15s %-30s\n", 
                    t.getTimestamp().toString().substring(0, 16),
                    t.getType(),
                    "$" + t.getAmount(),
                    t.getDescription()));
            }
            fw.close();
            
            TerminalUI.showLoader("Generating PDF (Simulated)...", 1000);
            System.out.println(ANSIColors.success("✔ Statement downloaded to: " + file.getAbsolutePath()));
            
        } catch (Exception e) {
            System.out.println(ANSIColors.error("Download failed: " + e.getMessage()));
        }
    }
    
    // ==================== LOAN & SERVICES ====================
    
    private void requestLoan() {
        System.out.println(ANSIColors.BOLD_CYAN + "\nLOAN APPLICATION" + ANSIColors.RESET);
        
        try {
            System.out.print("Loan Amount Required: ");
            double amount = Double.parseDouble(scanner.nextLine());
            System.out.print("Duration (Months): ");
            int months = Integer.parseInt(scanner.nextLine());
            System.out.print("Purpose of Loan: ");
            String purpose = scanner.nextLine();
            
            // Calculate estimated EMI
            double rate = 12.0; // 12% PA
            double interest = (amount * rate * (months/12.0)) / 100;
            double total = amount + interest;
            double emi = total / months;
            
            TerminalUI.showLoader("Analyzing Eligibility...", 1500);
            
            String details = 
                "Principal:   $" + String.format("%.2f", amount) + "\n" +
                "Interest:    " + rate + "% PA\n" +
                "Total Pay:   $" + String.format("%.2f", total) + "\n" +
                "Est. EMI:    $" + String.format("%.2f", emi) + "/mo\n" +
                "Status:      SUBMITTED FOR REVIEW";
            
            TerminalUI.printBox("LOAN ESTIMATE", details, ANSIColors.YELLOW);
            
            loanService.requestLoan(authService.getCurrentUser().getUserId(), amount, months, purpose);
            
            System.out.println(ANSIColors.success("Application Submitted Successfully."));
        } catch (Exception e) {
            System.out.println(ANSIColors.error("Invalid Input: " + e.getMessage()));
        }
    }
    
    private void checkLoanStatus() {
        System.out.println(ANSIColors.BOLD_CYAN + "\nYOUR LOAN APPLICATIONS" + ANSIColors.RESET);
        List<String> loans = loanService.getLoanStatus(authService.getCurrentUser().getUserId());
        if (loans.isEmpty()) {
            System.out.println(ANSIColors.warning("No active loan applications found."));
        } else {
            loans.forEach(System.out::println);
        }
    }
    
    private void checkNotifications() {
        System.out.println(ANSIColors.BOLD_CYAN + "\nNOTIFICATIONS" + ANSIColors.RESET);
        List<String> notifs = notifService.getNotifications(authService.getCurrentUser().getUserId());
        if (notifs.isEmpty()) {
            System.out.println(ANSIColors.success("No new notifications."));
        } else {
            notifs.forEach(System.out::println);
        }
    }
    
    private void reportIssue() {
        System.out.println(ANSIColors.BOLD_CYAN + "\nREPORT TECHNICAL ISSUE" + ANSIColors.RESET);
        System.out.print("Subject: ");
        String subject = scanner.nextLine();
        System.out.print("Description: ");
        String desc = scanner.nextLine();
        
        TerminalUI.showLoader("Submitting Ticket...", 1000);
        
        try {
            File issueFile = new File("data/issues.log");
            java.io.FileWriter fw = new java.io.FileWriter(issueFile, true);
            fw.write(String.format("[%s] User: %s | Subject: %s | %s\n", 
                new java.util.Date(), authService.getCurrentUser().getUsername(), subject, desc));
            fw.close();
            System.out.println(ANSIColors.success("✔ Ticket Created. Support will contact you shortly."));
        } catch (Exception e) {
            System.out.println(ANSIColors.error("Failed to save report: " + e.getMessage()));
        }
    }
    
    private void changePassword() {
        User user = authService.getCurrentUser();
        System.out.println(ANSIColors.BOLD_CYAN + "\nCHANGE PASSWORD" + ANSIColors.RESET);
        
        System.out.print("Enter Current Password: ");
        String current = scanner.nextLine();
        
        if (!authService.validatePin(current)) {
            System.out.println(ANSIColors.error("✖ Incorrect password."));
            return;
        }
        
        System.out.print("Enter New Password: ");
        String newPass = scanner.nextLine();
        System.out.print("Confirm New Password: ");
        String confirmPass = scanner.nextLine();
        
        if (!newPass.equals(confirmPass)) {
            System.out.println(ANSIColors.error("✖ Passwords do not match."));
            return;
        }
        
        user.setPin(com.fortis.utils.SecurityUtils.hashPin(newPass));
        TerminalUI.showLoader("Updating Security Credentials...", 1000);
        System.out.println(ANSIColors.success("✔ Password Changed Successfully."));
    }
    
    // ==================== HELPER METHODS ====================
    
    private long resolveTargetAccountId(User user) {
        List<BankAccount> myAccounts = bankingService.getAccountsByUser(user.getUserId());
        if (myAccounts.isEmpty()) {
            throw new IllegalStateException("No accounts found linked to your profile.");
        }
        if (myAccounts.size() == 1) {
            return myAccounts.get(0).getAccountId();
        }
        
        System.out.println(ANSIColors.CYAN + "\nSelect Account:" + ANSIColors.RESET);
        for (BankAccount acc : myAccounts) {
            System.out.println(String.format(" %d. %s ($%s)", 
                acc.getAccountId(), acc.getAccountNumber(), acc.getBalance()));
        }
        System.out.print("Enter Account ID: ");
        return Long.parseLong(scanner.nextLine());
    }
    
    private double getAmountFromPartsOrPrompt(String[] parts) {
        if (parts.length > 1) {
            return Double.parseDouble(parts[1]);
        }
        System.out.print("Enter amount: ");
        return Double.parseDouble(scanner.nextLine());
    }
    
    private void simulateAction(String loadingMsg, String successMsg) {
        TerminalUI.showLoader(loadingMsg, 1200);
        System.out.println(ANSIColors.success("✓ " + successMsg));
    }

    private void createAndRunBatch(String type, double amount, String color) {
        try {
            String filename = "temp_anim_" + System.currentTimeMillis() + ".bat";
            java.io.File file = new java.io.File(filename);
            java.io.FileWriter fw = new java.io.FileWriter(file);
            
            fw.write("@echo off\n");
            fw.write("title Fortis Transaction Processing\n");
            fw.write("color " + color + "\n");
            fw.write("mode 60,20\n");
            fw.write("cls\n");
            
            fw.write("echo.\n");
            fw.write("echo    ==============================================\n");
            fw.write("echo           FORTIS BANKING SECURE GATEWAY          \n");
            fw.write("echo    ==============================================\n");
            fw.write("echo.\n");
            fw.write("echo    Processing: " + type.toUpperCase() + "\n");
            fw.write("echo    Amount:     $" + amount + "\n");
            fw.write("echo.\n");
            fw.write("timeout /t 1 >nul\n");

            // Counting Animation
            int notes = 5;
            for(int i=1; i<=notes; i++) {
                fw.write("cls\n");
                fw.write("echo.\n");
                fw.write("echo    [ " + i + " / " + notes + " ] Counting Cash...\n");
                fw.write("echo.\n");
                fw.write("echo    $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
                fw.write("echo    $                                       $\n");
                fw.write("echo    $   $   $   $   $   $   $   $   $   $   $\n");
                fw.write("echo    $                                       $\n");
                fw.write("echo    $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
                fw.write("echo.\n");
                fw.write("echo    Verified: $" + String.format("%.2f", (amount/notes * i)) + "\n");
                fw.write("timeout /t 1 >nul\n");
            }
            
            fw.write("cls\n");
            fw.write("color 2F\n");
            fw.write("echo.\n");
            fw.write("echo    ==============================================\n");
            fw.write("echo               TRANSACTION SUCCESSFUL             \n");
            fw.write("echo    ==============================================\n");
            fw.write("echo.\n");
            fw.write("echo    Total " + type + ": $" + amount + "\n");
            fw.write("timeout /t 3 >nul\n");
            fw.write("exit\n");
            fw.close();
            
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", filename);
            pb.start();
            
            // Clean up later
            new Thread(() -> {
                 try { Thread.sleep(12000); file.delete(); } catch(Exception e){}
            }).start();
            
        } catch(Exception e) {
            // silent fail
        }
    }
}
