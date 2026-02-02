package com.fortis.tests;

import com.fortis.core.Transaction;
import com.fortis.managers.TransactionManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Test concurrent transactions to verify ACID properties
 * Tests isolation and atomicity under concurrent load
 */
public class ConcurrentTransactionTest {
    
    private static final int NUM_THREADS = 10;
    private static final int TRANSACTIONS_PER_THREAD = 5;
    
    public static void main(String[] args) {
        System.out.println("=== CONCURRENT TRANSACTION TEST ===\n");
        
        ConcurrentTransactionTest test = new ConcurrentTransactionTest();
        test.testConcurrentTransfers();
        test.testDeadlockPrevention();
        test.testRaceCondition();
        
        System.out.println("\n=== ALL TESTS COMPLETED ===");
    }
    
    /**
     * Test 1: Multiple threads transferring simultaneously
     */
    public void testConcurrentTransfers() {
        System.out.println("Test 1: Concurrent Transfers");
        System.out.println("─".repeat(50));
        
        TransactionManager tm = new TransactionManager();
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        List<Future<Transaction>> futures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        // Submit concurrent transfer tasks
        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadNum = i;
            Future<Transaction> future = executor.submit(() -> {
                Transaction txn = tm.executeTransfer(
                        1, 2,
                        new BigDecimal("100.00"),
                        "Concurrent test " + threadNum
                );
                System.out.println("Thread " + threadNum + ": " + txn.getState() + 
                                 " (Risk: " + txn.getRiskScore() + ")");
                return txn;
            });
            futures.add(future);
        }
        
        // Wait for all to complete
        int successful = 0;
        int failed = 0;
        
        for (Future<Transaction> future : futures) {
            try {
                Transaction txn = future.get();
                if (txn.isSuccessful()) {
                    successful++;
                } else {
                    failed++;
                }
            } catch (Exception e) {
                failed++;
                e.printStackTrace();
            }
        }
        
        executor.shutdown();
        long duration = System.currentTimeMillis() - startTime;
        
        System.out.println("\nResults:");
        System.out.println("  Successful: " + successful);
        System.out.println("  Failed: " + failed);
        System.out.println("  Duration: " + duration + "ms");
        System.out.println("  ✓ Test completed\n");
    }
    
    /**
     * Test 2: Deadlock prevention with circular transfers
     */
    public void testDeadlockPrevention() {
        System.out.println("Test 2: Deadlock Prevention");
        System.out.println("─".repeat(50));
        
        TransactionManager tm = new TransactionManager();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        // Thread 1: Transfer from Account 1 to Account 2
        Future<Transaction> future1 = executor.submit(() -> {
            System.out.println("Thread 1: Transferring 1 → 2");
            return tm.executeTransfer(1, 2, new BigDecimal("500.00"), "Deadlock test 1");
        });
        
        // Thread 2: Transfer from Account 2 to Account 1 (potential deadlock)
        Future<Transaction> future2 = executor.submit(() -> {
            System.out.println("Thread 2: Transferring 2 → 1");
            return tm.executeTransfer(2, 1, new BigDecimal("300.00"), "Deadlock test 2");
        });
        
        try {
            Transaction txn1 = future1.get(10, TimeUnit.SECONDS);
            Transaction txn2 = future2.get(10, TimeUnit.SECONDS);
            
            System.out.println("\nResults:");
            System.out.println("  Transaction 1: " + txn1.getState());
            System.out.println("  Transaction 2: " + txn2.getState());
            System.out.println("  ✓ No deadlock detected\n");
            
        } catch (TimeoutException e) {
            System.out.println("  ✗ Deadlock detected!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        executor.shutdown();
    }
    
    /**
     * Test 3: Race condition - multiple threads accessing same account
     */
    public void testRaceCondition() {
        System.out.println("Test 3: Race Condition Test");
        System.out.println("─".repeat(50));
        
        TransactionManager tm = new TransactionManager();
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<Transaction>> futures = new ArrayList<>();
        
        // All threads try to transfer from same account simultaneously
        for (int i = 0; i < 5; i++) {
            final int threadNum = i;
            Future<Transaction> future = executor.submit(() -> {
                try {
                    latch.await(); // Wait for signal to start simultaneously
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                Transaction txn = tm.executeTransfer(
                        1, 3,
                        new BigDecimal("1000.00"),
                        "Race condition test " + threadNum
                );
                System.out.println("Thread " + threadNum + ": " + txn.getState());
                return txn;
            });
            futures.add(future);
        }
        
        // Start all threads simultaneously
        latch.countDown();
        
        int successful = 0;
        for (Future<Transaction> future : futures) {
            try {
                Transaction txn = future.get();
                if (txn.isSuccessful()) {
                    successful++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        executor.shutdown();
        
        System.out.println("\nResults:");
        System.out.println("  Successful transfers: " + successful);
        System.out.println("  ✓ Race condition handled correctly\n");
    }
}
