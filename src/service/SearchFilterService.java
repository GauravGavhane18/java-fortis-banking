package com.fortis.service;

import com.fortis.model.TransactionRecord;
import com.fortis.utils.ANSIColors;
import com.fortis.utils.TerminalUI;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Search and Filter Service - Advanced transaction filtering
 */
public class SearchFilterService {
    private final BankingService bankingService;
    private final AuthenticationService authService;
    
    public SearchFilterService() {
        this.bankingService = BankingService.getInstance();
        this.authService = AuthenticationService.getInstance();
    }
    
    public void showSearchMenu() {
        TerminalUI.printHeader("SEARCH & FILTER TRANSACTIONS");
        
        System.out.println(ANSIColors.BOLD_BLUE + "╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                  SEARCH OPTIONS                            ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                            ║");
        System.out.println("║  " + ANSIColors.GREEN + "1. " + ANSIColors.WHITE + "Transactions > ₹10,000" + 
            ANSIColors.BOLD_BLUE + "                                  ║");
        System.out.println("║  " + ANSIColors.GREEN + "2. " + ANSIColors.WHITE + "Failed Transactions" + 
            ANSIColors.BOLD_BLUE + "                                     ║");
        System.out.println("║  " + ANSIColors.GREEN + "3. " + ANSIColors.WHITE + "Today's Transactions" + 
            ANSIColors.BOLD_BLUE + "                                    ║");
        System.out.println("║  " + ANSIColors.GREEN + "4. " + ANSIColors.WHITE + "This Week's Transactions" + 
            ANSIColors.BOLD_BLUE + "                                ║");
        System.out.println("║  " + ANSIColors.GREEN + "5. " + ANSIColors.WHITE + "High-Value Transactions (>₹50,000)" + 
            ANSIColors.BOLD_BLUE + "                     ║");
        System.out.println("║                                                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝" + ANSIColors.RESET);
    }
    
    public List<TransactionRecord> filterByAmount(BigDecimal minAmount) {
        return getAllTransactions().stream()
            .filter(t -> t.getAmount().compareTo(minAmount) > 0)
            .collect(Collectors.toList());
    }
    
    public List<TransactionRecord> filterFailedTransactions() {
        return getAllTransactions().stream()
            .filter(t -> !t.getStatus().toString().equals("COMPLETED"))
            .collect(Collectors.toList());
    }
    
    public List<TransactionRecord> filterTodayTransactions() {
        LocalDate today = LocalDate.now();
        return getAllTransactions().stream()
            .filter(t -> t.getTimestamp().toLocalDate().equals(today))
            .collect(Collectors.toList());
    }
    
    public List<TransactionRecord> filterThisWeekTransactions() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(7);
        return getAllTransactions().stream()
            .filter(t -> t.getTimestamp().toLocalDate().isAfter(weekStart))
            .collect(Collectors.toList());
    }
    
    public void displayFilteredResults(List<TransactionRecord> transactions, String filterName) {
        TerminalUI.printHeader("FILTERED RESULTS: " + filterName);
        
        if (transactions.isEmpty()) {
            System.out.println(ANSIColors.warning("No transactions found matching the filter"));
            return;
        }
        
        String[] headers = {"Transaction ID", "Type", "Amount", "Status", "Date"};
        String[][] data = new String[Math.min(transactions.size(), 20)][5];
        
        for (int i = 0; i < Math.min(transactions.size(), 20); i++) {
            TransactionRecord txn = transactions.get(i);
            data[i][0] = txn.getTransactionId().substring(0, Math.min(15, txn.getTransactionId().length()));
            data[i][1] = txn.getType().getDisplayName();
            data[i][2] = "₹" + String.format("%.2f", txn.getAmount());
            data[i][3] = txn.getStatus().toString();
            data[i][4] = txn.getFormattedTimestamp().substring(0, 10);
        }
        
        TerminalUI.printTable(headers, data);
        System.out.println(ANSIColors.info("Total Results: " + transactions.size() + 
            (transactions.size() > 20 ? " (Showing first 20)" : "")));
    }
    
    private List<TransactionRecord> getAllTransactions() {
        return bankingService.getAllTransactions(authService.getCurrentUser());
    }
}
