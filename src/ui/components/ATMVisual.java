package com.fortis.ui.components;

import com.fortis.utils.ANSIColors;

/**
 * ATM Visual Component - ASCII Art ATM Machine with Animations
 * Creates a realistic ATM interface with card slot, keypad, and cash dispenser
 */
public class ATMVisual {
    
    private static final int ATM_WIDTH = 30;
    private static final int ATM_HEIGHT = 25;
    
    /**
     * Display complete ATM machine
     */
    public static void displayATM(String status, double balance) {
        System.out.println(ANSIColors.ATM_BORDER + "┌" + "─".repeat(ATM_WIDTH) + "┐" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_BORDER + "│" + ANSIColors.RESET + 
                          ANSIColors.centerText("FORTIS ATM", ATM_WIDTH) + 
                          ANSIColors.ATM_BORDER + "│" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_BORDER + "├" + "─".repeat(ATM_WIDTH) + "┤" + ANSIColors.RESET);
        
        // Card slot
        displayCardSlot(false);
        
        // Screen
        displayScreen(status, balance);
        
        // Keypad
        displayKeypad();
        
        // Cash dispenser
        displayCashDispenser(false);
        
        System.out.println(ANSIColors.ATM_BORDER + "└" + "─".repeat(ATM_WIDTH) + "┘" + ANSIColors.RESET);
    }
    
    /**
     * Display card slot
     */
    private static void displayCardSlot(boolean cardInserted) {
        String slot = cardInserted ? 
                     ANSIColors.BRIGHT_YELLOW + "▓▓▓▓▓▓▓▓" + ANSIColors.RESET :
                     ANSIColors.BRIGHT_BLACK + "░░░░░░░░" + ANSIColors.RESET;
        
        System.out.println(ANSIColors.ATM_BORDER + "│" + ANSIColors.RESET + 
                          "  " + ANSIColors.MUTED + "Card Slot:" + ANSIColors.RESET + 
                          "  [" + slot + "]  " +
                          ANSIColors.ATM_BORDER + "│" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_BORDER + "│" + " ".repeat(ATM_WIDTH) + "│" + ANSIColors.RESET);
    }
    
    /**
     * Display screen
     */
    private static void displayScreen(String status, double balance) {
        System.out.println(ANSIColors.ATM_BORDER + "│" + ANSIColors.RESET + 
                          "  " + ANSIColors.BG_BRIGHT_BLUE + ANSIColors.BLACK + 
                          " ".repeat(26) + ANSIColors.RESET + "  " +
                          ANSIColors.ATM_BORDER + "│" + ANSIColors.RESET);
        
        System.out.println(ANSIColors.ATM_BORDER + "│" + ANSIColors.RESET + 
                          "  " + ANSIColors.BG_BRIGHT_BLUE + ANSIColors.BLACK + 
                          ANSIColors.centerText(status, 26) + ANSIColors.RESET + "  " +
                          ANSIColors.ATM_BORDER + "│" + ANSIColors.RESET);
        
        if (balance >= 0) {
            System.out.println(ANSIColors.ATM_BORDER + "│" + ANSIColors.RESET + 
                              "  " + ANSIColors.BG_BRIGHT_BLUE + ANSIColors.BRIGHT_GREEN + 
                              ANSIColors.centerText(String.format("₹%.2f", balance), 26) + 
                              ANSIColors.RESET + "  " +
                              ANSIColors.ATM_BORDER + "│" + ANSIColors.RESET);
        }
        
        System.out.println(ANSIColors.ATM_BORDER + "│" + ANSIColors.RESET + 
                          "  " + ANSIColors.BG_BRIGHT_BLUE + ANSIColors.BLACK + 
                          " ".repeat(26) + ANSIColors.RESET + "  " +
                          ANSIColors.ATM_BORDER + "│" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_BORDER + "│" + " ".repeat(ATM_WIDTH) + "│" + ANSIColors.RESET);
    }
    
    /**
     * Display keypad
     */
    private static void displayKeypad() {
        System.out.println(ANSIColors.ATM_BORDER + "│" + ANSIColors.RESET + 
                          "  " + ANSIColors.MUTED + "Keypad:" + ANSIColors.RESET + 
                          " ".repeat(20) +
                          ANSIColors.ATM_BORDER + "│" + ANSIColors.RESET);
        
        String[][] keys = {
            {"1", "2", "3"},
            {"4", "5", "6"},
            {"7", "8", "9"},
            {"*", "0", "#"}
        };
        
        for (String[] row : keys) {
            StringBuilder line = new StringBuilder("  ");
            for (String key : row) {
                line.append(ANSIColors.BG_BRIGHT_BLACK + ANSIColors.WHITE + 
                           " " + key + " " + ANSIColors.RESET + " ");
            }
            line.append(" ".repeat(ATM_WIDTH - 17));
            System.out.println(ANSIColors.ATM_BORDER + "│" + ANSIColors.RESET + 
                              line.toString() +
                              ANSIColors.ATM_BORDER + "│" + ANSIColors.RESET);
        }
        System.out.println(ANSIColors.ATM_BORDER + "│" + " ".repeat(ATM_WIDTH) + "│" + ANSIColors.RESET);
    }
    
    /**
     * Display cash dispenser
     */
    private static void displayCashDispenser(boolean dispensing) {
        String cash = dispensing ?
                     ANSIColors.BRIGHT_GREEN + "₹₹₹₹₹₹₹₹" + ANSIColors.RESET :
                     ANSIColors.BRIGHT_BLACK + "────────" + ANSIColors.RESET;
        
        System.out.println(ANSIColors.ATM_BORDER + "│" + ANSIColors.RESET + 
                          "  " + ANSIColors.MUTED + "Cash Out:" + ANSIColors.RESET + 
                          "  [" + cash + "]  " +
                          ANSIColors.ATM_BORDER + "│" + ANSIColors.RESET);
    }
    
    /**
     * Animate card insertion
     */
    public static void animateCardInsertion() {
        ANSIColors.hideCursor();
        System.out.println("\n" + ANSIColors.INFO + "Insert your card..." + ANSIColors.RESET);
        
        String[] frames = {
            "░░░░░░░░",
            "▓░░░░░░░",
            "▓▓░░░░░░",
            "▓▓▓░░░░░",
            "▓▓▓▓░░░░",
            "▓▓▓▓▓░░░",
            "▓▓▓▓▓▓░░",
            "▓▓▓▓▓▓▓░",
            "▓▓▓▓▓▓▓▓"
        };
        
        for (String frame : frames) {
            System.out.print("\r  Card Slot: [" + ANSIColors.BRIGHT_YELLOW + frame + ANSIColors.RESET + "]");
            sleep(100);
        }
        
        System.out.println("\n" + ANSIColors.SUCCESS + "Card inserted successfully!" + ANSIColors.RESET);
        ANSIColors.showCursor();
        sleep(500);
    }
    
    /**
     * Animate cash dispensing
     */
    public static void animateCashDispense(double amount) {
        ANSIColors.hideCursor();
        System.out.println("\n" + ANSIColors.INFO + "Dispensing cash..." + ANSIColors.RESET);
        
        // Counting animation
        for (int i = 0; i <= 100; i += 10) {
            System.out.print("\r  Processing: [" + 
                           ANSIColors.BRIGHT_GREEN + "█".repeat(i/10) + 
                           ANSIColors.BRIGHT_BLACK + "░".repeat(10 - i/10) + 
                           ANSIColors.RESET + "] " + i + "%");
            sleep(100);
        }
        System.out.println();
        
        // Cash dispensing animation
        String[] frames = {
            "────────",
            "₹───────",
            "₹₹──────",
            "₹₹₹─────",
            "₹₹₹₹────",
            "₹₹₹₹₹───",
            "₹₹₹₹₹₹──",
            "₹₹₹₹₹₹₹─",
            "₹₹₹₹₹₹₹₹"
        };
        
        for (String frame : frames) {
            System.out.print("\r  Cash Out: [" + ANSIColors.BRIGHT_GREEN + frame + ANSIColors.RESET + "]");
            sleep(150);
        }
        
        System.out.println("\n\n" + ANSIColors.SUCCESS + 
                          "Please collect your cash: " + ANSIColors.money(amount) + ANSIColors.RESET);
        ANSIColors.showCursor();
        sleep(1000);
    }
    
    /**
     * Animate deposit counting
     */
    public static void animateDepositCounting(double amount) {
        ANSIColors.hideCursor();
        System.out.println("\n" + ANSIColors.INFO + "Counting cash..." + ANSIColors.RESET);
        
        double counted = 0;
        while (counted < amount) {
            counted += Math.min(100, amount - counted);
            int percent = (int) ((counted / amount) * 100);
            
            System.out.print("\r  Counted: " + ANSIColors.money(counted) + 
                           " [" + ANSIColors.BRIGHT_CYAN + "█".repeat(percent/5) + 
                           ANSIColors.BRIGHT_BLACK + "░".repeat(20 - percent/5) + 
                           ANSIColors.RESET + "] " + percent + "%");
            sleep(50);
        }
        
        System.out.println("\n" + ANSIColors.SUCCESS + "Cash counted: " + 
                          ANSIColors.money(amount) + ANSIColors.RESET);
        ANSIColors.showCursor();
        sleep(500);
    }
    
    /**
     * Display transaction receipt
     */
    public static void displayReceipt(String txnId, String type, double amount, 
                                     double balanceAfter, String status) {
        int width = 40;
        
        System.out.println("\n" + ANSIColors.ATM_BORDER + "╔" + "═".repeat(width) + "╗" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_BORDER + "║" + ANSIColors.RESET + 
                          ANSIColors.centerText(ANSIColors.BOLD + "TRANSACTION RECEIPT" + ANSIColors.RESET, width) + 
                          ANSIColors.ATM_BORDER + "║" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_BORDER + "╠" + "═".repeat(width) + "╣" + ANSIColors.RESET);
        
        printReceiptLine("Transaction ID", txnId, width);
        printReceiptLine("Type", type, width);
        printReceiptLine("Amount", String.format("₹%.2f", amount), width);
        printReceiptLine("Balance", String.format("₹%.2f", balanceAfter), width);
        printReceiptLine("Status", status.equals("SUCCESS") ? "✔ SUCCESS" : "✖ FAILED", width);
        printReceiptLine("Date/Time", java.time.LocalDateTime.now().toString(), width);
        
        System.out.println(ANSIColors.ATM_BORDER + "╚" + "═".repeat(width) + "╝" + ANSIColors.RESET);
    }
    
    /**
     * Print receipt line
     */
    private static void printReceiptLine(String label, String value, int width) {
        String line = String.format("  %-15s: %s", label, value);
        int padding = width - stripANSI(line).length();
        System.out.println(ANSIColors.ATM_BORDER + "║" + ANSIColors.RESET + 
                          line + " ".repeat(Math.max(0, padding)) +
                          ANSIColors.ATM_BORDER + "║" + ANSIColors.RESET);
    }
    
    /**
     * Display welcome screen
     */
    public static void displayWelcomeScreen() {
        ANSIColors.clearScreen();
        ANSIColors.printBanner();
        
        System.out.println(ANSIColors.ATM_PRIMARY + "╔" + "═".repeat(70) + "╗" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_PRIMARY + "║" + ANSIColors.RESET + 
                          ANSIColors.centerText(ANSIColors.BOLD + "Welcome to FORTIS ATM" + ANSIColors.RESET, 70) + 
                          ANSIColors.ATM_PRIMARY + "║" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_PRIMARY + "║" + ANSIColors.RESET + 
                          ANSIColors.centerText("24/7 Banking at Your Fingertips", 70) + 
                          ANSIColors.ATM_PRIMARY + "║" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_PRIMARY + "╚" + "═".repeat(70) + "╝" + ANSIColors.RESET);
        System.out.println();
    }
    
    /**
     * Display loading screen
     */
    public static void displayLoadingScreen(String message) {
        ANSIColors.showSpinner(message, 2000);
    }
    
    /**
     * Strip ANSI codes
     */
    private static String stripANSI(String text) {
        return text.replaceAll("\u001B\\[[;\\d]*m", "");
    }
    
    /**
     * Sleep helper
     */
    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Test method
     */
    public static void main(String[] args) {
        String mode = System.getProperty("mode", "demo");
        String amountStr = System.getProperty("amount", "5000");
        double amount = Double.parseDouble(amountStr);
        
        if (mode.equals("deposit")) {
            // Deposit mode
            displayWelcomeScreen();
            sleep(1000);
            
            animateDepositCounting(amount);
            sleep(1000);
            
            displayReceipt("TXN" + System.currentTimeMillis(), "DEPOSIT", amount, 50000 + amount, "SUCCESS");
            
        } else if (mode.equals("withdraw")) {
            // Withdraw mode
            displayWelcomeScreen();
            sleep(1000);
            
            animateCardInsertion();
            sleep(1000);
            
            animateCashDispense(amount);
            sleep(1000);
            
            displayReceipt("TXN" + System.currentTimeMillis(), "WITHDRAWAL", amount, 50000 - amount, "SUCCESS");
            
        } else {
            // Demo mode
            displayWelcomeScreen();
            sleep(2000);
            
            animateCardInsertion();
            sleep(1000);
            
            displayATM("Ready", 50000.00);
            sleep(2000);
            
            animateCashDispense(5000.00);
            sleep(1000);
            
            displayReceipt("TXN123456789", "WITHDRAWAL", 5000.00, 45000.00, "SUCCESS");
        }
        
        // Keep display visible
        System.out.println("\n" + ANSIColors.BRIGHT_CYAN + "Press Enter to exit..." + ANSIColors.RESET);
        try {
            System.in.read();
        } catch (Exception e) {
            // Ignore
        }
    }
}
