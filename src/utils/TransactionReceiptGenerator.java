package com.fortis.utils;

import com.fortis.model.BankAccount;
import com.fortis.model.TransactionRecord;
import java.time.format.DateTimeFormatter;

/**
 * Transaction Receipt Generator - Creates formatted receipts
 */
public class TransactionReceiptGenerator {
    
    public static void printReceipt(TransactionRecord transaction, BankAccount fromAccount, BankAccount toAccount) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        System.out.println(ANSIColors.BOLD_GREEN + "\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║              ✔ TRANSACTION RECEIPT                         ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                            ║");
        System.out.println("║  " + ANSIColors.WHITE + "Transaction ID: " + ANSIColors.BOLD_YELLOW + 
            String.format("%-38s", transaction.getTransactionId()) + ANSIColors.BOLD_GREEN + "║");
        System.out.println("║  " + ANSIColors.WHITE + "Date & Time: " + ANSIColors.BOLD_CYAN + 
            String.format("%-41s", transaction.getTimestamp().format(formatter)) + ANSIColors.BOLD_GREEN + "║");
        System.out.println("║  " + ANSIColors.WHITE + "Type: " + ANSIColors.BOLD_CYAN + 
            String.format("%-48s", transaction.getType().getDisplayName()) + ANSIColors.BOLD_GREEN + "║");
        System.out.println("║                                                            ║");
        System.out.println("║  " + ANSIColors.BOLD_WHITE + "FROM ACCOUNT" + ANSIColors.BOLD_GREEN + "                                          ║");
        
        if (fromAccount != null) {
            System.out.println("║    " + ANSIColors.WHITE + "Account: " + ANSIColors.CYAN + 
                String.format("%-43s", fromAccount.getAccountNumber()) + ANSIColors.BOLD_GREEN + "║");
            System.out.println("║    " + ANSIColors.WHITE + "Holder: " + ANSIColors.CYAN + 
                String.format("%-44s", fromAccount.getAccountHolder()) + ANSIColors.BOLD_GREEN + "║");
        }
        
        System.out.println("║                                                            ║");
        System.out.println("║  " + ANSIColors.BOLD_WHITE + "TO ACCOUNT" + ANSIColors.BOLD_GREEN + "                                            ║");
        
        if (toAccount != null) {
            System.out.println("║    " + ANSIColors.WHITE + "Account: " + ANSIColors.CYAN + 
                String.format("%-43s", toAccount.getAccountNumber()) + ANSIColors.BOLD_GREEN + "║");
            System.out.println("║    " + ANSIColors.WHITE + "Holder: " + ANSIColors.CYAN + 
                String.format("%-44s", toAccount.getAccountHolder()) + ANSIColors.BOLD_GREEN + "║");
        }
        
        System.out.println("║                                                            ║");
        System.out.println("║  " + ANSIColors.BOLD_WHITE + "AMOUNT: " + ANSIColors.BOLD_YELLOW + "₹" + 
            String.format("%-46.2f", transaction.getAmount()) + ANSIColors.BOLD_GREEN + "║");
        System.out.println("║  " + ANSIColors.WHITE + "Status: " + 
            (transaction.getStatus().toString().equals("COMPLETED") ? 
                ANSIColors.BOLD_GREEN + "✔ SUCCESS" : ANSIColors.BOLD_RED + "✖ FAILED") + 
            ANSIColors.BOLD_GREEN + "                                         ║");
        System.out.println("║                                                            ║");
        System.out.println("║  " + ANSIColors.WHITE + "Description: " + ANSIColors.CYAN + 
            String.format("%-41s", transaction.getDescription()) + ANSIColors.BOLD_GREEN + "║");
        System.out.println("║                                                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝" + ANSIColors.RESET);
    }
    
    public static void printSimpleReceipt(TransactionRecord transaction) {
        System.out.println(ANSIColors.BOLD_GREEN + "\n╔═══════ TRANSACTION RECEIPT ═══════╗");
        System.out.println("║ TXN ID: " + ANSIColors.YELLOW + transaction.getTransactionId().substring(0, 15) + ANSIColors.BOLD_GREEN + " ║");
        System.out.println("║ Amount: " + ANSIColors.YELLOW + "₹" + String.format("%.2f", transaction.getAmount()) + 
            ANSIColors.BOLD_GREEN + "                  ║");
        System.out.println("║ Status: " + ANSIColors.BOLD_GREEN + "✔ SUCCESS" + ANSIColors.BOLD_GREEN + "                ║");
        System.out.println("╚═══════════════════════════════════╝" + ANSIColors.RESET);
    }
}
