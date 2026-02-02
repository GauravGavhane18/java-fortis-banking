package com.fortis.utils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Session Manager - Tracks user session statistics
 */
public class SessionManager {
    private static SessionManager instance;
    private LocalDateTime sessionStart;
    private int transactionCount;
    private int errorCount;
    private int successCount;
    
    private SessionManager() {
        this.sessionStart = LocalDateTime.now();
        this.transactionCount = 0;
        this.errorCount = 0;
        this.successCount = 0;
    }
    
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public void reset() {
        this.sessionStart = LocalDateTime.now();
        this.transactionCount = 0;
        this.errorCount = 0;
        this.successCount = 0;
    }
    
    public void recordTransaction(boolean success) {
        transactionCount++;
        if (success) {
            successCount++;
        } else {
            errorCount++;
        }
    }
    
    public void recordError() {
        errorCount++;
    }
    
    public long getSessionDurationMinutes() {
        return ChronoUnit.MINUTES.between(sessionStart, LocalDateTime.now());
    }
    
    public int getTransactionCount() { return transactionCount; }
    public int getErrorCount() { return errorCount; }
    public int getSuccessCount() { return successCount; }
    public LocalDateTime getSessionStart() { return sessionStart; }
    
    public void printSessionSummary() {
        System.out.println(ANSIColors.BOLD_CYAN + "\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                    SESSION SUMMARY                         ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║  " + ANSIColors.WHITE + "✔ Transactions: " + ANSIColors.BOLD_GREEN + 
            String.format("%-42d", transactionCount) + ANSIColors.BOLD_CYAN + "║");
        System.out.println("║  " + ANSIColors.WHITE + "✔ Successful: " + ANSIColors.BOLD_GREEN + 
            String.format("%-44d", successCount) + ANSIColors.BOLD_CYAN + "║");
        System.out.println("║  " + ANSIColors.WHITE + "✖ Errors: " + ANSIColors.BOLD_RED + 
            String.format("%-49d", errorCount) + ANSIColors.BOLD_CYAN + "║");
        System.out.println("║  " + ANSIColors.WHITE + "⏱ Duration: " + ANSIColors.BOLD_YELLOW + 
            String.format("%-44s", getSessionDurationMinutes() + " mins") + ANSIColors.BOLD_CYAN + "║");
        System.out.println("╚════════════════════════════════════════════════════════════╝" + ANSIColors.RESET);
    }
}
