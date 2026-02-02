package com.fortis.service;

import com.fortis.model.TransactionRecord;
import com.fortis.utils.ANSIColors;
import com.fortis.utils.TerminalUI;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Statistics Service - Generates transaction statistics and reports
 */
public class StatisticsService {
    private final BankingService bankingService;
    private final AuthenticationService authService;
    
    public StatisticsService() {
        this.bankingService = BankingService.getInstance();
        this.authService = AuthenticationService.getInstance();
    }
    
    public void displayDailyStatistics() {
        TerminalUI.printHeader("DAILY STATISTICS");
        
        LocalDate today = LocalDate.now();
        List<TransactionRecord> allTransactions = getAllTransactions();
        
        List<TransactionRecord> todayTransactions = allTransactions.stream()
            .filter(t -> t.getTimestamp().toLocalDate().equals(today))
            .collect(Collectors.toList());
        
        long totalCount = todayTransactions.size();
        long successCount = todayTransactions.stream()
            .filter(t -> t.getStatus().toString().equals("COMPLETED"))
            .count();
        long failedCount = totalCount - successCount;
        
        BigDecimal totalAmount = todayTransactions.stream()
            .filter(t -> t.getStatus().toString().equals("COMPLETED"))
            .map(TransactionRecord::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal highestTransfer = todayTransactions.stream()
            .filter(t -> t.getStatus().toString().equals("COMPLETED"))
            .map(TransactionRecord::getAmount)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        
        System.out.println(ANSIColors.BOLD_CYAN + "╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                  TODAY'S STATISTICS                        ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                            ║");
        System.out.println("║  " + ANSIColors.WHITE + "Total Transactions Today: " + ANSIColors.BOLD_YELLOW + 
            String.format("%-28d", totalCount) + ANSIColors.BOLD_CYAN + "║");
        System.out.println("║  " + ANSIColors.WHITE + "Successful: " + ANSIColors.BOLD_GREEN + 
            String.format("%-42d", successCount) + ANSIColors.BOLD_CYAN + "║");
        System.out.println("║  " + ANSIColors.WHITE + "Failed: " + ANSIColors.BOLD_RED + 
            String.format("%-46d", failedCount) + ANSIColors.BOLD_CYAN + "║");
        System.out.println("║                                                            ║");
        System.out.println("║  " + ANSIColors.WHITE + "Total Amount Moved: " + ANSIColors.BOLD_GREEN + "₹" + 
            String.format("%-32.2f", totalAmount) + ANSIColors.BOLD_CYAN + "║");
        System.out.println("║  " + ANSIColors.WHITE + "Highest Transfer: " + ANSIColors.BOLD_YELLOW + "₹" + 
            String.format("%-34.2f", highestTransfer) + ANSIColors.BOLD_CYAN + "║");
        System.out.println("║                                                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝" + ANSIColors.RESET);
    }
    
    public void displayMonthlyStatistics() {
        TerminalUI.printHeader("MONTHLY STATISTICS");
        
        LocalDate today = LocalDate.now();
        List<TransactionRecord> allTransactions = getAllTransactions();
        
        List<TransactionRecord> monthTransactions = allTransactions.stream()
            .filter(t -> t.getTimestamp().getMonth().equals(today.getMonth()) &&
                        t.getTimestamp().getYear() == today.getYear())
            .collect(Collectors.toList());
        
        long totalCount = monthTransactions.size();
        long successCount = monthTransactions.stream()
            .filter(t -> t.getStatus().toString().equals("COMPLETED"))
            .count();
        
        BigDecimal totalAmount = monthTransactions.stream()
            .filter(t -> t.getStatus().toString().equals("COMPLETED"))
            .map(TransactionRecord::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal avgTransaction = totalCount > 0 ? 
            totalAmount.divide(BigDecimal.valueOf(totalCount), 2, BigDecimal.ROUND_HALF_UP) : 
            BigDecimal.ZERO;
        
        System.out.println(ANSIColors.BOLD_PURPLE + "╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                  MONTHLY STATISTICS                        ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                            ║");
        System.out.println("║  " + ANSIColors.WHITE + "Month: " + ANSIColors.BOLD_YELLOW + 
            String.format("%-47s", today.getMonth() + " " + today.getYear()) + ANSIColors.BOLD_PURPLE + "║");
        System.out.println("║  " + ANSIColors.WHITE + "Total Transactions: " + ANSIColors.BOLD_YELLOW + 
            String.format("%-34d", totalCount) + ANSIColors.BOLD_PURPLE + "║");
        System.out.println("║  " + ANSIColors.WHITE + "Successful: " + ANSIColors.BOLD_GREEN + 
            String.format("%-42d", successCount) + ANSIColors.BOLD_PURPLE + "║");
        System.out.println("║                                                            ║");
        System.out.println("║  " + ANSIColors.WHITE + "Total Amount: " + ANSIColors.BOLD_GREEN + "₹" + 
            String.format("%-38.2f", totalAmount) + ANSIColors.BOLD_PURPLE + "║");
        System.out.println("║  " + ANSIColors.WHITE + "Average Transaction: " + ANSIColors.BOLD_CYAN + "₹" + 
            String.format("%-29.2f", avgTransaction) + ANSIColors.BOLD_PURPLE + "║");
        System.out.println("║                                                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝" + ANSIColors.RESET);
    }
    
    private List<TransactionRecord> getAllTransactions() {
        return bankingService.getAllTransactions(authService.getCurrentUser());
    }
}
