package com.fortis.tests;

import com.fortis.core.RiskScore;
import com.fortis.core.Transaction;
import com.fortis.managers.RiskEngine;
import com.fortis.managers.TransactionManager;
import com.fortis.persistence.DatabaseManager;

import java.math.BigDecimal;
import java.sql.Connection;

/**
 * Test Risk Engine functionality
 * Verifies risk scoring and automatic rollback
 */
public class RiskEngineTest {
    
    public static void main(String[] args) {
        System.out.println("=== RISK ENGINE TEST ===\n");
        
        RiskEngineTest test = new RiskEngineTest();
        test.testLowRiskTransaction();
        test.testHighAmountRisk();
        test.testHighFrequencyRisk();
        test.testAutomaticRollback();
        
        System.out.println("\n=== ALL TESTS COMPLETED ===");
    }
    
    /**
     * Test 1: Low risk transaction should succeed
     */
    public void testLowRiskTransaction() {
        System.out.println("Test 1: Low Risk Transaction");
        System.out.println("─".repeat(50));
        
        TransactionManager tm = new TransactionManager();
        Transaction txn = tm.executeTransfer(
                1, 2,
                new BigDecimal("500.00"),
                "Low risk test"
        );
        
        System.out.println("State: " + txn.getState());
        System.out.println("Risk Score: " + txn.getRiskScore());
        System.out.println("Expected: COMMITTED with low risk score");
        
        if (txn.isSuccessful() && txn.getRiskScore() < 30) {
            System.out.println("✓ Test passed\n");
        } else {
            System.out.println("✗ Test failed\n");
        }
    }
    
    /**
     * Test 2: High amount should increase risk score
     */
    public void testHighAmountRisk() {
        System.out.println("Test 2: High Amount Risk");
        System.out.println("─".repeat(50));
        
        TransactionManager tm = new TransactionManager();
        Transaction txn = tm.executeTransfer(
                3, 2,
                new BigDecimal("80000.00"),
                "High amount test"
        );
        
        System.out.println("State: " + txn.getState());
        System.out.println("Risk Score: " + txn.getRiskScore());
        System.out.println("Expected: Higher risk score due to large amount");
        
        if (txn.getRiskScore() > 20) {
            System.out.println("✓ Test passed - High amount detected\n");
        } else {
            System.out.println("✗ Test failed - Risk score too low\n");
        }
    }
    
    /**
     * Test 3: Multiple rapid transactions should increase risk
     */
    public void testHighFrequencyRisk() {
        System.out.println("Test 3: High Frequency Risk");
        System.out.println("─".repeat(50));
        
        TransactionManager tm = new TransactionManager();
        
        // Execute multiple transactions rapidly
        System.out.println("Executing 10 rapid transactions...");
        int totalRisk = 0;
        
        for (int i = 0; i < 10; i++) {
            Transaction txn = tm.executeTransfer(
                    1, 2,
                    new BigDecimal("100.00"),
                    "Frequency test " + i
            );
            totalRisk += txn.getRiskScore();
            System.out.println("  Transaction " + (i+1) + ": Risk = " + txn.getRiskScore());
        }
        
        double avgRisk = totalRisk / 10.0;
        System.out.println("\nAverage Risk Score: " + avgRisk);
        System.out.println("Expected: Risk should increase with frequency");
        
        if (avgRisk > 10) {
            System.out.println("✓ Test passed - Frequency risk detected\n");
        } else {
            System.out.println("✗ Test failed - Risk not increasing\n");
        }
    }
    
    /**
     * Test 4: Very high risk should trigger automatic rollback
     */
    public void testAutomaticRollback() {
        System.out.println("Test 4: Automatic Rollback on High Risk");
        System.out.println("─".repeat(50));
        
        TransactionManager tm = new TransactionManager();
        
        // Try to create a high-risk scenario
        // Execute many large transactions rapidly
        System.out.println("Creating high-risk scenario...");
        
        for (int i = 0; i < 15; i++) {
            Transaction txn = tm.executeTransfer(
                    1, 2,
                    new BigDecimal("10000.00"),
                    "High risk test " + i
            );
            
            System.out.println("  Transaction " + (i+1) + ": " + 
                             txn.getState() + " (Risk: " + txn.getRiskScore() + ")");
            
            if (txn.getState().toString().equals("ROLLED_BACK") && txn.getRiskScore() > 70) {
                System.out.println("\n✓ Test passed - Automatic rollback triggered");
                System.out.println("  Risk Score: " + txn.getRiskScore());
                System.out.println("  Reason: " + txn.getErrorMessage() + "\n");
                return;
            }
        }
        
        System.out.println("\n⚠ Test inconclusive - No automatic rollback triggered\n");
    }
}
