package com.fortis.utils;

/**
 * Enhanced ANSI Colors and Terminal Control
 * Industry-standard terminal styling with animations and effects
 */
public class ANSIColors {
    
    // ========== BASIC COLORS ==========
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    
    // ========== BOLD COLORS (Missing) ==========
    public static final String BOLD_BLACK = "\u001B[1;30m";
    public static final String BOLD_RED = "\u001B[1;31m";
    public static final String BOLD_GREEN = "\u001B[1;32m";
    public static final String BOLD_YELLOW = "\u001B[1;33m";
    public static final String BOLD_BLUE = "\u001B[1;34m";
    public static final String BOLD_PURPLE = "\u001B[1;35m";
    public static final String BOLD_CYAN = "\u001B[1;36m";
    public static final String BOLD_WHITE = "\u001B[1;37m";
    
    // ========== BRIGHT COLORS ==========
    public static final String BRIGHT_BLACK = "\u001B[90m";
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_MAGENTA = "\u001B[95m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_WHITE = "\u001B[97m";
    
    // ========== BACKGROUND COLORS ==========
    public static final String BG_BLACK = "\u001B[40m";
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_MAGENTA = "\u001B[45m";
    public static final String BG_CYAN = "\u001B[46m";
    public static final String BG_WHITE = "\u001B[47m";
    
    // ========== BRIGHT BACKGROUNDS ==========
    public static final String BG_BRIGHT_BLACK = "\u001B[100m";
    public static final String BG_BRIGHT_RED = "\u001B[101m";
    public static final String BG_BRIGHT_GREEN = "\u001B[102m";
    public static final String BG_BRIGHT_YELLOW = "\u001B[103m";
    public static final String BG_BRIGHT_BLUE = "\u001B[104m";
    public static final String BG_BRIGHT_MAGENTA = "\u001B[105m";
    public static final String BG_BRIGHT_CYAN = "\u001B[106m";
    public static final String BG_BRIGHT_WHITE = "\u001B[107m";
    
    // ========== TEXT STYLES ==========
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";
    public static final String ITALIC = "\u001B[3m";
    public static final String UNDERLINE = "\u001B[4m";
    public static final String BLINK = "\u001B[5m";
    public static final String REVERSE = "\u001B[7m";
    public static final String HIDDEN = "\u001B[8m";
    public static final String STRIKETHROUGH = "\u001B[9m";
    
    // ========== CURSOR CONTROL ==========
    public static final String CURSOR_UP = "\u001B[1A";
    public static final String CURSOR_DOWN = "\u001B[1B";
    public static final String CURSOR_RIGHT = "\u001B[1C";
    public static final String CURSOR_LEFT = "\u001B[1D";
    public static final String CLEAR_SCREEN = "\u001B[2J\u001B[H";
    public static final String CLEAR_LINE = "\u001B[2K";
    public static final String SAVE_CURSOR = "\u001B[s";
    public static final String RESTORE_CURSOR = "\u001B[u";
    public static final String HIDE_CURSOR = "\u001B[?25l";
    public static final String SHOW_CURSOR = "\u001B[?25h";
    
    // ========== SEMANTIC COLORS ==========
    public static final String SUCCESS = BRIGHT_GREEN;
    public static final String ERROR = BRIGHT_RED;
    public static final String WARNING = BRIGHT_YELLOW;
    public static final String INFO = BRIGHT_CYAN;
    public static final String HIGHLIGHT = BRIGHT_MAGENTA;
    public static final String MUTED = BRIGHT_BLACK;
    
    // ========== ATM THEME COLORS ==========
    public static final String ATM_PRIMARY = BRIGHT_CYAN;
    public static final String ATM_SECONDARY = BRIGHT_BLUE;
    public static final String ATM_ACCENT = BRIGHT_YELLOW;
    public static final String ATM_SUCCESS = BRIGHT_GREEN;
    public static final String ATM_ERROR = BRIGHT_RED;
    public static final String ATM_TEXT = WHITE;
    public static final String ATM_BORDER = CYAN;
    public static final String ATM_MONEY = BRIGHT_GREEN + BOLD;
    
    // ========== HELPER METHODS ==========
    
    /**
     * Color text with specified color
     */
    public static String color(String text, String color) {
        return color + text + RESET;
    }
    
    /**
     * Colorize text with specified color (alias for color)
     */
    public static String colorize(String text, String color) {
        return color + text + RESET;
    }
    
    /**
     * Bold text
     */
    public static String bold(String text) {
        return BOLD + text + RESET;
    }
    
    /**
     * Success message (green)
     */
    public static String success(String text) {
        return SUCCESS + "✔ " + text + RESET;
    }
    
    /**
     * Error message (red)
     */
    public static String error(String text) {
        return ERROR + "✖ " + text + RESET;
    }
    
    /**
     * Warning message (yellow)
     */
    public static String warning(String text) {
        return WARNING + "⚠ " + text + RESET;
    }
    
    /**
     * Info message (cyan)
     */
    public static String info(String text) {
        return INFO + "ℹ " + text + RESET;
    }
    
    /**
     * Money format (green with rupee symbol)
     */
    public static String money(double amount) {
        return ATM_MONEY + String.format("₹%.2f", amount) + RESET;
    }
    
    /**
     * Clear the entire screen
     */
    public static void clearScreen() {
        System.out.print(CLEAR_SCREEN);
        System.out.flush();
    }
    
    /**
     * Move cursor to position
     */
    public static void moveCursor(int row, int col) {
        System.out.print(String.format("\u001B[%d;%dH", row, col));
        System.out.flush();
    }
    
    /**
     * Hide cursor
     */
    public static void hideCursor() {
        System.out.print(HIDE_CURSOR);
        System.out.flush();
    }
    
    /**
     * Show cursor
     */
    public static void showCursor() {
        System.out.print(SHOW_CURSOR);
        System.out.flush();
    }
    
    /**
     * Print colored box
     */
    public static void printBox(String title, String content, String color) {
        int width = 70;
        String border = color + "═".repeat(width) + RESET;
        
        System.out.println(color + "╔" + border + "╗" + RESET);
        System.out.println(color + "║" + RESET + centerText(title, width) + color + "║" + RESET);
        System.out.println(color + "╠" + border + "╣" + RESET);
        
        for (String line : content.split("\n")) {
            System.out.println(color + "║" + RESET + padText(line, width) + color + "║" + RESET);
        }
        
        System.out.println(color + "╚" + border + "╝" + RESET);
    }
    
    /**
     * Print simple box
     */
    public static void printSimpleBox(String content) {
        printBox("", content, CYAN);
    }
    
    /**
     * Center text
     */
    public static String centerText(String text, int width) {
        int padding = (width - stripANSI(text).length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text + " ".repeat(Math.max(0, width - stripANSI(text).length() - padding));
    }
    
    /**
     * Pad text to width
     */
    private static String padText(String text, int width) {
        int textLength = stripANSI(text).length();
        return "  " + text + " ".repeat(Math.max(0, width - textLength - 2));
    }
    
    /**
     * Strip ANSI codes from text
     */
    private static String stripANSI(String text) {
        return text.replaceAll("\u001B\\[[;\\d]*m", "");
    }
    
    /**
     * Print progress bar
     */
    public static void printProgressBar(int percent, String label) {
        int barWidth = 40;
        int filled = (int) ((percent / 100.0) * barWidth);
        
        String bar = BRIGHT_GREEN + "█".repeat(filled) + 
                     BRIGHT_BLACK + "░".repeat(barWidth - filled) + RESET;
        
        System.out.print("\r" + label + " [" + bar + "] " + percent + "%");
        System.out.flush();
        
        if (percent >= 100) {
            System.out.println();
        }
    }
    
    /**
     * Animated loading spinner
     */
    public static void showSpinner(String message, int durationMs) {
        String[] frames = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
        long endTime = System.currentTimeMillis() + durationMs;
        int i = 0;
        
        hideCursor();
        while (System.currentTimeMillis() < endTime) {
            System.out.print("\r" + BRIGHT_CYAN + frames[i % frames.length] + " " + message + RESET);
            System.out.flush();
            try {
                Thread.sleep(80);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            i++;
        }
        System.out.print("\r" + CLEAR_LINE);
        showCursor();
    }
    
    /**
     * Print header
     */
    public static void printHeader(String title) {
        int width = 70;
        String border = "═".repeat(width);
        
        System.out.println(ATM_PRIMARY + "╔" + border + "╗" + RESET);
        System.out.println(ATM_PRIMARY + "║" + RESET + centerText(BOLD + title + RESET, width) + ATM_PRIMARY + "║" + RESET);
        System.out.println(ATM_PRIMARY + "╚" + border + "╝" + RESET);
        System.out.println();
    }
    
    /**
     * Print separator
     */
    public static void printSeparator() {
        System.out.println(MUTED + "─".repeat(70) + RESET);
    }
    
    /**
     * Print thick separator
     */
    public static void printThickSeparator() {
        System.out.println(ATM_BORDER + "═".repeat(70) + RESET);
    }
    
    /**
     * Typewriter effect
     */
    public static void typewriter(String text, int delayMs) {
        for (char c : text.toCharArray()) {
            System.out.print(c);
            System.out.flush();
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println();
    }
    
    /**
     * Flash text (blink effect)
     */
    public static void flashText(String text, int times) {
        for (int i = 0; i < times; i++) {
            System.out.print("\r" + BOLD + BRIGHT_YELLOW + text + RESET);
            System.out.flush();
            sleep(300);
            System.out.print("\r" + " ".repeat(text.length()));
            System.out.flush();
            sleep(300);
        }
        System.out.println("\r" + BOLD + BRIGHT_YELLOW + text + RESET);
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
     * Print ASCII art banner
     */
    public static void printBanner() {
        String banner = 
            BRIGHT_CYAN + "  ███████╗ ██████╗ ██████╗ ████████╗██╗███████╗\n" +
            "  ██╔════╝██╔═══██╗██╔══██╗╚══██╔══╝██║██╔════╝\n" +
            "  █████╗  ██║   ██║██████╔╝   ██║   ██║███████╗\n" +
            "  ██╔══╝  ██║   ██║██╔══██╗   ██║   ██║╚════██║\n" +
            "  ██║     ╚██████╔╝██║  ██║   ██║   ██║███████║\n" +
            "  ╚═╝      ╚═════╝ ╚═╝  ╚═╝   ╚═╝   ╚═╝╚══════╝\n" + RESET +
            BRIGHT_YELLOW + "              ATM BANKING SYSTEM v2.0\n" + RESET +
            MUTED + "         Industry-Level Terminal Banking\n" + RESET;
        
        System.out.println(banner);
    }


    public static String header(String title) {
        return BOLD_CYAN + "=== " + title + " ===" + RESET;
    }
    

}
