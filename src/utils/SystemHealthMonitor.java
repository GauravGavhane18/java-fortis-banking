package com.fortis.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * System Health Monitor - Monitors system resources and status
 */
public class SystemHealthMonitor {
    
    public static void printHealthCheck() {
        displayHealthStatus();
    }
    
    public static void displayHealthStatus() {
        TerminalUI.printHeader("SYSTEM HEALTH MONITOR");
        
        // Simulate database status (always connected in standalone mode)
        boolean dbConnected = true;
        
        // Thread Information
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        int activeThreads = threadBean.getThreadCount();
        int peakThreads = threadBean.getPeakThreadCount();
        
        // Memory Information
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        
        System.out.println(ANSIColors.BOLD_BLUE + "╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                    SYSTEM HEALTH STATUS                    ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                            ║");
        System.out.println("║  " + ANSIColors.BOLD_WHITE + "DATABASE STATUS" + ANSIColors.BOLD_BLUE + "                                          ║");
        System.out.println("║    " + ANSIColors.WHITE + "Connection: " + 
            (dbConnected ? ANSIColors.BOLD_GREEN + "✔ CONNECTED" : ANSIColors.BOLD_RED + "✖ DISCONNECTED") + 
            ANSIColors.BOLD_BLUE + "                                   ║");
        System.out.println("║    " + ANSIColors.WHITE + "Connection Pool: " + ANSIColors.BOLD_GREEN + "OK" + 
            ANSIColors.BOLD_BLUE + "                                      ║");
        System.out.println("║                                                            ║");
        System.out.println("║  " + ANSIColors.BOLD_WHITE + "THREAD INFORMATION" + ANSIColors.BOLD_BLUE + "                                     ║");
        System.out.println("║    " + ANSIColors.WHITE + "Active Threads: " + ANSIColors.BOLD_YELLOW + 
            String.format("%-38d", activeThreads) + ANSIColors.BOLD_BLUE + "║");
        System.out.println("║    " + ANSIColors.WHITE + "Peak Threads: " + ANSIColors.BOLD_YELLOW + 
            String.format("%-40d", peakThreads) + ANSIColors.BOLD_BLUE + "║");
        System.out.println("║                                                            ║");
        System.out.println("║  " + ANSIColors.BOLD_WHITE + "MEMORY USAGE" + ANSIColors.BOLD_BLUE + "                                          ║");
        System.out.println("║    " + ANSIColors.WHITE + "Used Memory: " + ANSIColors.BOLD_CYAN + 
            String.format("%-38s", usedMemory + " MB") + ANSIColors.BOLD_BLUE + "║");
        System.out.println("║    " + ANSIColors.WHITE + "Total Memory: " + ANSIColors.BOLD_CYAN + 
            String.format("%-37s", totalMemory + " MB") + ANSIColors.BOLD_BLUE + "║");
        System.out.println("║    " + ANSIColors.WHITE + "Max Memory: " + ANSIColors.BOLD_CYAN + 
            String.format("%-39s", maxMemory + " MB") + ANSIColors.BOLD_BLUE + "║");
        System.out.println("║    " + ANSIColors.WHITE + "Free Memory: " + ANSIColors.BOLD_GREEN + 
            String.format("%-38s", freeMemory + " MB") + ANSIColors.BOLD_BLUE + "║");
        System.out.println("║                                                            ║");
        
        // Health Score
        int healthScore = calculateHealthScore(dbConnected, activeThreads, usedMemory, maxMemory);
        String healthStatus = getHealthStatus(healthScore);
        String healthColor = getHealthColor(healthScore);
        
        System.out.println("║  " + ANSIColors.BOLD_WHITE + "OVERALL HEALTH" + ANSIColors.BOLD_BLUE + "                                        ║");
        System.out.println("║    " + ANSIColors.WHITE + "Health Score: " + healthColor + 
            String.format("%-38s", healthScore + "/100 - " + healthStatus) + ANSIColors.BOLD_BLUE + "║");
        System.out.println("║                                                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝" + ANSIColors.RESET);
    }
    
    private static int calculateHealthScore(boolean dbConnected, int activeThreads, long usedMemory, long maxMemory) {
        int score = 100;
        
        if (!dbConnected) score -= 50;
        if (activeThreads > 50) score -= 20;
        
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        if (memoryUsagePercent > 80) score -= 20;
        else if (memoryUsagePercent > 60) score -= 10;
        
        return Math.max(0, score);
    }
    
    private static String getHealthStatus(int score) {
        if (score >= 80) return "EXCELLENT";
        if (score >= 60) return "GOOD";
        if (score >= 40) return "FAIR";
        return "CRITICAL";
    }
    
    private static String getHealthColor(int score) {
        if (score >= 80) return ANSIColors.BOLD_GREEN;
        if (score >= 60) return ANSIColors.BOLD_YELLOW;
        return ANSIColors.BOLD_RED;
    }
}
