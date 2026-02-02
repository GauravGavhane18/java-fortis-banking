package com.fortis.service;

import java.io.*;
import java.util.*;
import java.math.BigDecimal;

/**
 * Loan Service - Manages loan applications and status
 */
public class LoanService {
    private static LoanService instance;
    private final File loanFile = new File("data/loans.csv");

    private LoanService() {
        if (!loanFile.exists()) {
            try {
                File dir = loanFile.getParentFile();
                if (dir != null) dir.mkdirs();
                loanFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Could not create loan file: " + e.getMessage());
            }
        }
    }

    public static synchronized LoanService getInstance() {
        if (instance == null) {
            instance = new LoanService();
        }
        return instance;
    }

    public void requestLoan(long userId, double amount, int duration, String purpose) {
        try (FileWriter fw = new FileWriter(loanFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            String loanId = "LN-" + (System.currentTimeMillis() % 100000);
            // Format: ID,UserId,Amount,Duration,Purpose,Status,Date
            out.println(String.format("%s,%d,%.2f,%d,%s,%s,%s", 
                loanId, userId, amount, duration, purpose, "PENDING", new java.util.Date()));
        } catch (IOException e) {
            System.err.println("Error saving loan request: " + e.getMessage());
        }
    }

    public List<String> getLoanStatus(long userId) {
        List<String> loans = new ArrayList<>();
        if (!loanFile.exists()) return loans;

        try (BufferedReader br = new BufferedReader(new FileReader(loanFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    try {
                        long uid = Long.parseLong(parts[1]);
                        if (uid == userId) {
                             loans.add(String.format("Loan #%s: $%.2f - %s (%s)", parts[0], Double.parseDouble(parts[2]), parts[5], parts[6]));
                        }
                    } catch (NumberFormatException e) {
                        // skip header or bad line
                    }
                }
            }
        } catch (IOException e) {
             System.err.println("Error reading loan status: " + e.getMessage());
        }
        return loans;
    }
    
    public List<String> getAllPendingLoans() {
        List<String> loans = new ArrayList<>();
        if (!loanFile.exists()) return loans;

        try (BufferedReader br = new BufferedReader(new FileReader(loanFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    if ("PENDING".equalsIgnoreCase(parts[5])) {
                         loans.add(String.format("Loan %s: User %s applied for $%.2f (%s)", parts[0], parts[1], Double.parseDouble(parts[2]), parts[4]));
                    }
                }
            }
        } catch (IOException e) {
             System.err.println("Error reading loans: " + e.getMessage());
        }
        return loans;
    }

    public boolean approveLoan(String loanId, boolean approved) {
        if (!loanFile.exists()) return false;
        
        List<String> lines = new ArrayList<>();
        boolean found = false;
        
        try (BufferedReader br = new BufferedReader(new FileReader(loanFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                // parts[0] is ID
                if (parts.length > 0 && parts[0].equals(loanId.trim())) {
                    found = true;
                    // Update Status part[5]
                    if (parts.length >= 6) {
                        parts[5] = approved ? "APPROVED" : "REJECTED";
                        line = String.join(",", parts);
                    }
                }
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading loan file: " + e.getMessage());
            return false;
        }
        
        if (found) {
            try (PrintWriter out = new PrintWriter(new FileWriter(loanFile))) {
                for (String l : lines) {
                    out.println(l);
                }
                return true;
            } catch (IOException e) {
                System.err.println("Error writing loan file: " + e.getMessage());
            }
        }
        return false;
    }
    public double[] getLoanDetails(String loanId) {
        if (!loanFile.exists()) return null;
        try (BufferedReader br = new BufferedReader(new FileReader(loanFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 2 && parts[0].equals(loanId)) {
                    // Returns [UserId, Amount]
                    return new double[]{ Double.parseDouble(parts[1]), Double.parseDouble(parts[2]) };
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading loan details: " + e.getMessage());
        }
        return null;
    }
}
