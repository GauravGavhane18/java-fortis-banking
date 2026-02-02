package com.fortis.ui;

import com.fortis.ui.components.ATMVisual;
import com.fortis.utils.ANSIColors;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

/**
 * ATM Terminal - Interactive ATM Interface with Auto-timeout
 * Automatically shows animations during transactions
 */
public class ATMTerminal {
    
    private Scanner scanner;
    private Timer timeoutTimer;
    private static final int TIMEOUT_SECONDS = 15;
    private boolean sessionActive = true;
    
    public ATMTerminal() {
        this.scanner = new Scanner(System.in);
    }
    
    public void start() {
        ANSIColors.clearScreen();
        ATMVisual.displayWelcomeScreen();
        
        // Simulate card insertion
        ATMVisual.animateCardInsertion();
        
        // Login
        if (!performLogin()) {
            System.out.println(ANSIColors.ERROR + "Login failed. Exiting..." + ANSIColors.RESET);
            return;
        }
        
        // Main menu loop
        while (sessionActive) {
            resetTimeout();
            showMainMenu();
            
            String choice = readInput("Select option: ");
            if (choice == null) break; // Timeout
            
            processChoice(choice);
        }
        
        // Session ended
        System.out.println("\n" + ANSIColors.INFO + "Session ended. Thank you for using FORTIS ATM!" + ANSIColors.RESET);
        ejectCard();
    }
    
    private boolean performLogin() {
        System.out.println("\n" + ANSIColors.ATM_PRIMARY + "═══ LOGIN ═══" + ANSIColors.RESET);
        
        System.out.print("Account Number: ");
        String accountNumber = scanner.nextLine();
        
        System.out.print("PIN: ");
        String pin = scanner.nextLine();
        
        // Simulate authentication
        ANSIColors.showSpinner("Authenticating", 1500);
        
        if (pin.equals("1234") || pin.equals("5678") || pin.equals("9012")) {
            System.out.println(ANSIColors.SUCCESS + "Login successful!" + ANSIColors.RESET);
            return true;
        }
        
        return false;
    }
    
    private void showMainMenu() {
        System.out.println("\n" + ANSIColors.ATM_PRIMARY + "╔" + "═".repeat(50) + "╗" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_PRIMARY + "║" + ANSIColors.RESET + 
                          ANSIColors.centerText(ANSIColors.BOLD + "FORTIS ATM - MAIN MENU" + ANSIColors.RESET, 50) + 
                          ANSIColors.ATM_PRIMARY + "║" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_PRIMARY + "╠" + "═".repeat(50) + "╣" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_PRIMARY + "║" + ANSIColors.RESET + "  " + ANSIColors.BRIGHT_GREEN + "[1]" + ANSIColors.RESET + " Withdraw Money" + " ".repeat(31) + ANSIColors.ATM_PRIMARY + "║" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_PRIMARY + "║" + ANSIColors.RESET + "  " + ANSIColors.BRIGHT_GREEN + "[2]" + ANSIColors.RESET + " Deposit Money" + " ".repeat(32) + ANSIColors.ATM_PRIMARY + "║" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_PRIMARY + "║" + ANSIColors.RESET + "  " + ANSIColors.BRIGHT_GREEN + "[3]" + ANSIColors.RESET + " Check Balance" + " ".repeat(32) + ANSIColors.ATM_PRIMARY + "║" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_PRIMARY + "║" + ANSIColors.RESET + "  " + ANSIColors.BRIGHT_GREEN + "[4]" + ANSIColors.RESET + " Transfer Funds" + " ".repeat(31) + ANSIColors.ATM_PRIMARY + "║" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_PRIMARY + "║" + ANSIColors.RESET + "  " + ANSIColors.BRIGHT_GREEN + "[5]" + ANSIColors.RESET + " Mini Statement" + " ".repeat(31) + ANSIColors.ATM_PRIMARY + "║" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_PRIMARY + "║" + ANSIColors.RESET + "  " + ANSIColors.BRIGHT_RED + "[0]" + ANSIColors.RESET + " Exit" + " ".repeat(41) + ANSIColors.ATM_PRIMARY + "║" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_PRIMARY + "╚" + "═".repeat(50) + "╝" + ANSIColors.RESET);
        System.out.println();
    }
    
    private void processChoice(String choice) {
        cancelTimeout();
        
        switch (choice) {
            case "1":
                withdrawMoney();
                break;
            case "2":
                depositMoney();
                break;
            case "3":
                checkBalance();
                break;
            case "4":
                transferFunds();
                break;
            case "5":
                miniStatement();
                break;
            case "0":
                sessionActive = false;
                break;
            default:
                System.out.println(ANSIColors.ERROR + "Invalid option!" + ANSIColors.RESET);
        }
    }
    
    private void withdrawMoney() {
        System.out.println("\n" + ANSIColors.ATM_ACCENT + "═══ WITHDRAW MONEY ═══" + ANSIColors.RESET);
        
        String amountStr = readInput("Enter amount: ₹");
        if (amountStr == null) return;
        
        try {
            double amount = Double.parseDouble(amountStr);
            
            // Show processing
            ANSIColors.showSpinner("Processing withdrawal", 1000);
            
            // OPEN NEW ANIMATION WINDOW
            createAndRunBatch("withdraw", amount, "0C");
            
            // Show receipt
            ATMVisual.displayReceipt("TXN" + System.currentTimeMillis(), "WITHDRAWAL", amount, 50000 - amount, "SUCCESS");
            
            System.out.println("\n" + ANSIColors.SUCCESS + "Please collect your cash!" + ANSIColors.RESET);
            
        } catch (NumberFormatException e) {
            System.out.println(ANSIColors.ERROR + "Invalid amount!" + ANSIColors.RESET);
        }
    }
    
    private void depositMoney() {
        System.out.println("\n" + ANSIColors.ATM_ACCENT + "═══ DEPOSIT MONEY ═══" + ANSIColors.RESET);
        
        String amountStr = readInput("Enter amount: ₹");
        if (amountStr == null) return;
        
        try {
            double amount = Double.parseDouble(amountStr);
            
            System.out.println("\n" + ANSIColors.INFO + "Please insert cash..." + ANSIColors.RESET);
            
            // OPEN NEW ANIMATION WINDOW
            createAndRunBatch("deposit", amount, "0A");
            
            // Show receipt
            ATMVisual.displayReceipt("TXN" + System.currentTimeMillis(), "DEPOSIT", amount, 50000 + amount, "SUCCESS");
            
            System.out.println("\n" + ANSIColors.SUCCESS + "Deposit successful!" + ANSIColors.RESET);
            
        } catch (NumberFormatException e) {
            System.out.println(ANSIColors.ERROR + "Invalid amount!" + ANSIColors.RESET);
        }
    }

    /**
     * Creates a temporary batch file and runs it to guarantee a new window opens
     */
    private void createAndRunBatch(String mode, double amount, String color) {
        try {
            String projectDir = System.getProperty("user.dir");
            
            // Auto-detect Java Path
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + java.io.File.separator + "bin" + java.io.File.separator + "java";
            
            // Absolute Classpath
            String binPath = projectDir + java.io.File.separator + "bin";
            String libPath = projectDir + java.io.File.separator + "lib" + java.io.File.separator + "*";
            String classpath = "\"" + binPath + ";" + libPath + "\"";
            
            String batchContent = "@echo off\r\n" +
                "title FORTIS ATM Animation\r\n" +
                "color " + color + "\r\n" +
                "cd /d \"" + projectDir + "\"\r\n" +  // <--- CRITICAL FIX: Switch to project dir
                "echo Starting Animation...\r\n" +
                "echo Mode: " + mode + "\r\n" +
                "echo Amount: " + amount + "\r\n" +
                "\"" + javaBin + "\" -cp " + classpath + " -Dmode=" + mode + " -Damount=" + amount + " com.fortis.ui.components.ATMVisual\r\n" +
                "if %errorlevel% neq 0 (\r\n" +
                "    echo.\r\n" +
                "    echo ERROR: Animation crashed or class not found.\r\n" +
                "    echo Classpath: " + classpath + "\r\n" +
                ")\r\n" +
                "echo.\r\n" +
                "pause\r\n" +
                "exit\r\n";
            
            // Write to a temporary file
            java.io.File tempBatch = new java.io.File(projectDir, "temp_anim_" + System.currentTimeMillis() + ".bat");
            java.io.FileWriter writer = new java.io.FileWriter(tempBatch);
            writer.write(batchContent);
            writer.close();
            
            // Execute it
            Runtime.getRuntime().exec("cmd /c start \"\" \"" + tempBatch.getAbsolutePath() + "\"");
            
            System.out.println(ANSIColors.INFO + " → Opening Animation Window..." + ANSIColors.RESET);
            
            Thread.sleep(500);
        } catch (Exception e) {
            System.out.println(ANSIColors.WARNING + "Animation launch error: " + e.getMessage() + ANSIColors.RESET);
        }
    }
    
    private void checkBalance() {
        System.out.println("\n" + ANSIColors.ATM_ACCENT + "═══ BALANCE INQUIRY ═══" + ANSIColors.RESET);
        
        ANSIColors.showSpinner("Fetching balance", 1000);
        
        ATMVisual.displayATM("Balance Inquiry", 50000.00);
        
        System.out.println("\n" + ANSIColors.SUCCESS + "Current Balance: " + ANSIColors.money(50000.00) + ANSIColors.RESET);
    }
    
    private void transferFunds() {
        System.out.println("\n" + ANSIColors.ATM_ACCENT + "═══ TRANSFER FUNDS ═══" + ANSIColors.RESET);
        
        String toAccount = readInput("To Account Number: ");
        if (toAccount == null) return;
        
        String amountStr = readInput("Enter amount: ₹");
        if (amountStr == null) return;
        
        try {
            double amount = Double.parseDouble(amountStr);
            
            ANSIColors.showSpinner("Processing transfer", 2000);
            
            ATMVisual.displayReceipt("TXN" + System.currentTimeMillis(), "TRANSFER", amount, 50000 - amount, "SUCCESS");
            
            System.out.println("\n" + ANSIColors.SUCCESS + "Transfer successful!" + ANSIColors.RESET);
            
        } catch (NumberFormatException e) {
            System.out.println(ANSIColors.ERROR + "Invalid amount!" + ANSIColors.RESET);
        }
    }
    
    private void miniStatement() {
        System.out.println("\n" + ANSIColors.ATM_ACCENT + "═══ MINI STATEMENT ═══" + ANSIColors.RESET);
        
        ANSIColors.showSpinner("Generating statement", 1500);
        
        System.out.println("\n" + ANSIColors.ATM_BORDER + "╔" + "═".repeat(60) + "╗" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_BORDER + "║" + ANSIColors.RESET + ANSIColors.centerText("LAST 5 TRANSACTIONS", 60) + ANSIColors.ATM_BORDER + "║" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_BORDER + "╠" + "═".repeat(60) + "╣" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_BORDER + "║" + ANSIColors.RESET + "  Withdrawal  -₹5,000   Balance: ₹45,000  [2024-01-06]  " + ANSIColors.ATM_BORDER + "║" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_BORDER + "║" + ANSIColors.RESET + "  Deposit     +₹10,000  Balance: ₹55,000  [2024-01-05]  " + ANSIColors.ATM_BORDER + "║" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_BORDER + "║" + ANSIColors.RESET + "  Transfer    -₹2,000   Balance: ₹53,000  [2024-01-04]  " + ANSIColors.ATM_BORDER + "║" + ANSIColors.RESET);
        System.out.println(ANSIColors.ATM_BORDER + "╚" + "═".repeat(60) + "╝" + ANSIColors.RESET);
    }
    
    private void ejectCard() {
        System.out.println("\n" + ANSIColors.INFO + "Ejecting card..." + ANSIColors.RESET);
        
        String[] frames = {
            "▓▓▓▓▓▓▓▓",
            "▓▓▓▓▓▓▓░",
            "▓▓▓▓▓▓░░",
            "▓▓▓▓▓░░░",
            "▓▓▓▓░░░░",
            "▓▓▓░░░░░",
            "▓▓░░░░░░",
            "▓░░░░░░░",
            "░░░░░░░░"
        };
        
        for (String frame : frames) {
            System.out.print("\r  Card Slot: [" + ANSIColors.BRIGHT_YELLOW + frame + ANSIColors.RESET + "]");
            sleep(100);
        }
        
        System.out.println("\n" + ANSIColors.SUCCESS + "Please take your card!" + ANSIColors.RESET);
    }
    
    private String readInput(String prompt) {
        System.out.print(prompt);
        
        // Start timeout
        final String[] input = {null};
        final boolean[] completed = {false};
        
        Thread inputThread = new Thread(() -> {
            input[0] = scanner.nextLine();
            completed[0] = true;
        });
        inputThread.start();
        
        // Wait for input or timeout
        try {
            inputThread.join(TIMEOUT_SECONDS * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        if (!completed[0]) {
            System.out.println("\n" + ANSIColors.WARNING + "Session timeout due to inactivity!" + ANSIColors.RESET);
            sessionActive = false;
            return null;
        }
        
        return input[0];
    }
    
    private void resetTimeout() {
        // Timeout is handled in readInput method
    }
    
    private void cancelTimeout() {
        // Timeout is handled in readInput method
    }
    
    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public static void main(String[] args) {
        ATMTerminal terminal = new ATMTerminal();
        terminal.start();
    }
}
