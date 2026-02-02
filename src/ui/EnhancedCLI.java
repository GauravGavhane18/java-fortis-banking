package com.fortis.ui;

import com.fortis.model.*;
import com.fortis.service.*;
import com.fortis.persistence.AuditLogger;
import com.fortis.utils.*;
import java.util.Scanner;

/**
 * Enhanced CLI - Enterprise Command Banking Terminal
 * Premium UI/UX with Role-Based Access Control
 * REFACTORED: Delegates logic to CommandHandlers
 */
public class EnhancedCLI {
    
    private final Scanner scanner;
    private final AuthenticationService authService;
    private final AuditLogger auditLogger;
    private boolean running;
    
    private final CustomerCommandHandler customerHandler;
    private final AdminCommandHandler adminHandler;
    
    public EnhancedCLI() {
        this.scanner = new Scanner(System.in);
        this.authService = AuthenticationService.getInstance();
        this.auditLogger = AuditLogger.getInstance();
        this.running = true;
        
        // Initialize handlers
        this.customerHandler = new CustomerCommandHandler(scanner);
        this.adminHandler = new AdminCommandHandler(scanner);
    }
    
    public static void main(String[] args) {
        EnhancedCLI cli = new EnhancedCLI();
        cli.start();
    }
    
    public void start() {
        this.running = true;
        TerminalUI.clearScreen();
        TerminalUI.printBanner("SYSTEM");
        TerminalUI.showLoader("Initializing Secure Core Modules...", 1500);
        
        boolean loggedIn = false;
        int maxAttempts = 3;
        
        for (int i = 0; i < maxAttempts; i++) {
            if (performLogin()) {
                loggedIn = true;
                break;
            }
            
            int remaining = (maxAttempts - 1) - i;
            if (remaining > 0) {
                System.out.println(ANSIColors.warning("\n⚠ Login Authentication Failed. " + remaining + " attempts remaining."));
                try { Thread.sleep(1000); } catch (Exception e) {}
            } else {
                System.out.println(ANSIColors.error("\n❌ Maximum login attempts exceeded. Security Lockout Initiated."));
                return;
            }
        }
        
        if (loggedIn) {
            runMainLoop();
        }
    }
    
    private boolean performLogin() {
        System.out.println("\n");
        TerminalUI.printBox("SECURE LOGIN GATEWAY", 
            "Enter your credentials to access the \nFortis Banking Infrastructure.", ANSIColors.BOLD_BLUE);
            
        System.out.print(ANSIColors.BOLD_WHITE + "Username: " + ANSIColors.RESET);
        String username = scanner.nextLine().trim();
        System.out.print(ANSIColors.BOLD_WHITE + "Password: " + ANSIColors.RESET);
        String password = scanner.nextLine().trim();
        
        TerminalUI.showLoader("Verifying Encrypted Credentials...", 800);
        
        User user = authService.authenticate(username, password);
        
        if (user != null) {
            System.out.println(ANSIColors.success("\n✔ Authentication Successful. Welcome, " + user.getUsername().toUpperCase()));
            auditLogger.logSystemEvent("LOGIN_SUCCESS", "User logged in: " + username);
            try { Thread.sleep(800); } catch (Exception e) {}
            return true;
        }
        
        auditLogger.logSystemEvent("LOGIN_FAILED", "Failed login attempt: " + username);
        return false;
    }
    
    private void runMainLoop() {
        while (running) {
            User user = authService.getCurrentUser();
            if (user == null) {
                running = false;
                break;
            }

            TerminalUI.clearScreen();
            TerminalUI.printBanner(user.getRole().toString());
            TerminalUI.printFooter(user.getUsername(), user.getRole().toString());
            
            if (user.getRole() == User.UserRole.ADMIN) {
                showAdminMenu();
            } else {
                showCustomerMenu();
            }
            
            String command = readCommand();
            
            if (command.equals("logout") || command.equals("0")) {
                authService.logout();
                running = false;
                System.out.println(ANSIColors.warning("Logged out successfully."));
                start(); // Restart
                return;
            }
            
            processCommand(command, user);
            
            if (running) {
                System.out.println(ANSIColors.BRIGHT_BLACK + "\nPress ENTER to continue..." + ANSIColors.RESET);
                scanner.nextLine();
            }
        }
    }
    
    private void showCustomerMenu() {
        String menu = 
            " 1. withdraw <amt>      |  9. view-limits\n" +
            " 2. deposit <amt>       | 10. request-loan\n" +
            " 3. transfer <details>  | 11. loan-status\n" +
            " 4. check-balance       | 12. report-issue\n" +
            " 5. account-details     | 13. notifications\n" +
            " 6. transaction-history | 14. update-profile\n" +
            " 7. mini-statement      | 15. change-password\n" +
            " 8. download-statement  | 0.  logout";
            
        TerminalUI.printBox("CUSTOMER COMMAND CENTER", menu, ANSIColors.CYAN);
    }
    
    private void showAdminMenu() {
        String menu = 
            " 1. list-accounts       |  9. system-health\n" +
            " 2. create-account      | 10. audit-logs\n" +
            " 3. block-account       | 11. view-transactions\n" +
            " 4. unblock-account     | 12. suspicious-activity\n" +
            " 5. delete-account      | 13. db-backup\n" +
            " 6. reset-user-password | 14. db-restore\n" +
            " 7. loan-approvals      | 15. admin-reports\n" +
            " 8. change-rates        | 16. open-sql\n" +
            " 0. logout";
            
        TerminalUI.printBox("ADMINISTRATOR CONTROL PANEL", menu, ANSIColors.RED);
    }
    
    private String readCommand() {
        System.out.print(ANSIColors.BOLD_GREEN + "fortis-terminal" + ANSIColors.RESET + "> ");
        return scanner.nextLine().trim().toLowerCase();
    }
    
    private void processCommand(String input, User user) {
        if (input.isEmpty()) return;
        String[] parts = input.split("\\s+");
        String cmd = parts[0];
        
        try {
            if (user.getRole() == User.UserRole.ADMIN) {
                adminHandler.processCommand(cmd, parts);
            } else {
                customerHandler.processCommand(cmd, parts);
            }
        } catch (Exception e) {
            System.out.println(ANSIColors.error("Error executing command: " + e.getMessage()));
        }
    }
}
