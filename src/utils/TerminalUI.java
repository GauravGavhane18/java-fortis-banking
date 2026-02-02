package com.fortis.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Premium Terminal UI - Enterprise Grade Rendering
 */
public class TerminalUI {
    
    private static final int WIDTH = 80;
    
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    
    public static void printBanner(String role) {
        String color = role.equalsIgnoreCase("ADMIN") ? ANSIColors.BOLD_RED : ANSIColors.BOLD_CYAN;
        String title = role.equalsIgnoreCase("ADMIN") ? "ADMINISTRATOR CONSOLE" : "SECURE BANKING TERMINAL";
        
        System.out.println(color);
        System.out.println("╔══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                                              ║");
        System.out.println("║                 ███████╗ ██████╗ ██████╗ ████████╗██╗███████╗                ║");
        System.out.println("║                 ██╔════╝██╔═══██╗██╔══██╗╚══██╔══╝██║██╔════╝                ║");
        System.out.println("║                 █████╗  ██║   ██║██████╔╝   ██║   ██║███████╗                ║");
        System.out.println("║                 ██╔══╝  ██║   ██║██╔══██╗   ██║   ██║╚════██║                ║");
        System.out.println("║                 ██║     ╚██████╔╝██║  ██║   ██║   ██║███████║                ║");
        System.out.println("║                 ╚═╝      ╚═════╝ ╚═╝  ╚═╝   ╚═╝   ╚═╝╚══════╝                ║");
        System.out.println("║                                                                              ║");
        System.out.println("║" + centerText("PREMIUM BANKING SYSTEM", 78) + "║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════╝");
        System.out.println(ANSIColors.RESET);
    }
    
    // Fallback for older calls (SearchFilterService, etc)
    public static void printBanner() {
        printBanner("SYSTEM");
    }
    
    // New Header method for SearchFilterService etc.
    public static void printHeader(String title) {
        printBox(title, "", ANSIColors.BOLD_CYAN);
    }
    
    public static void printBox(String title, String content, String color) {
        System.out.println(color + "╔" + "═".repeat(WIDTH - 2) + "╗");
        System.out.println("║ " + centerText(title, WIDTH - 4) + " ║");
        System.out.println("╠" + "═".repeat(WIDTH - 2) + "╣");
        
        if (!content.isEmpty()) {
            for (String line : content.split("\n")) {
                 System.out.println("║ " + padRight(line, WIDTH - 4) + " ║");
            }
        }
        System.out.println("╚" + "═".repeat(WIDTH - 2) + "╝" + ANSIColors.RESET);
    }
    
    // Deprecated alias helper
    public static void printBox(String title, String content) {
        printBox(title, content, ANSIColors.WHITE);
    }
    
    public static void showLoader(String message, int ms) {
        System.out.print(ANSIColors.YELLOW + message + " ");
        String[] animation = { "⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏" };
        try {
            for (int i = 0; i < 20; i++) {
                System.out.print("\r" + message + " " + animation[i % animation.length] + "   ");
                Thread.sleep(ms / 20);
            }
            System.out.print("\r" + message + " " + ANSIColors.GREEN + "✔ DONE    \n" + ANSIColors.RESET);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // Legacy support
    public static void showLoadingBar(String message, int ms) {
        showLoader(message, ms);
    }
    
    public static void printFooter(String user, String role) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println(ANSIColors.BOLD_BLACK + "════════════════════════════════════════════════════════════════════════════════");
        System.out.printf(" USER: %-20s | ROLE: %-15s | TIME: %s%n", user.toUpperCase(), role, time);
        System.out.println("════════════════════════════════════════════════════════════════════════════════" + ANSIColors.RESET);
    }
    
    // Legacy support for Arrays
    public static void printTable(String[] headers, String[][] data) {
        if (data == null) return;
        printTable(headers, java.util.Arrays.asList(data));
    }
    
    public static void printTable(String[] headers, List<String[]> data) {
        if (data == null || data.isEmpty()) {
            System.out.println("No data available.");
            return;
        }
        
        // Calculate widths
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) widths[i] = headers[i].length();
        
        for (String[] row : data) {
            for (int i = 0; i < row.length; i++) {
                if (row[i] != null && row[i].length() > widths[i]) {
                    widths[i] = row[i].length();
                }
            }
        }
        
        // Print Header
        StringBuilder sep = new StringBuilder("╠");
        StringBuilder top = new StringBuilder("╔");
        StringBuilder bottom = new StringBuilder("╚");
        
        for (int w : widths) {
            top.append("═".repeat(w + 2)).append("╦");
            sep.append("═".repeat(w + 2)).append("╬");
            bottom.append("═".repeat(w + 2)).append("╩");
        }
        // Fix last chars
        top.setCharAt(top.length() - 1, '╗');
        sep.setCharAt(sep.length() - 1, '╣');
        bottom.setCharAt(bottom.length() - 1, '╝');
        
        System.out.println(top);
        System.out.print("║");
        for (int i = 0; i < headers.length; i++) {
             System.out.print(" " + padRight(headers[i], widths[i]) + " ║");
        }
        System.out.println();
        System.out.println(sep);
        
        // Print Data
        for (String[] row : data) {
            System.out.print("║");
            for (int i = 0; i < row.length; i++) {
                System.out.print(" " + padRight(row[i] != null ? row[i] : "", widths[i]) + " ║");
            }
            System.out.println();
        }
        System.out.println(bottom);
    }
    
    private static String centerText(String text, int width) {
        if (text.length() >= width) return text.substring(0, width);
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text + " ".repeat(width - text.length() - padding);
    }
    
    private static String padRight(String text, int width) {
        if (text == null) text = "";
        if (text.length() >= width) return text.substring(0, width);
        return text + " ".repeat(width - text.length());
    }
}
