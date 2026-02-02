package com.fortis.ui;

import com.fortis.utils.ANSIColors;
import java.io.File;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLTerminal {
    public static void main(String[] args) {
        new SQLTerminal().start();
    }
    
    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println(ANSIColors.BOLD_BLUE + "╔═══════════════════════════════════════════════════════╗");
        System.out.println("║            FORTIS SQL QUERY TERMINAL v1.1             ║");
        System.out.println("║    Connected to: data/accounts.csv (Read-Only)        ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝" + ANSIColors.RESET);
        System.out.println("Type 'exit' to quit. Supported: SELECT * FROM [accounts|transactions] [WHERE col=val] [LIMIT n]");
        
        while (true) {
            System.out.print(ANSIColors.BOLD_YELLOW + "\nSQL> " + ANSIColors.RESET);
            String query = scanner.nextLine().trim();
            
            if (query.equalsIgnoreCase("exit")) break;
            if (query.isEmpty()) continue;
            
            processQuery(query);
        }
    }
    
    private void processQuery(String query) {
        // Basic regex for parsing: SELECT * FROM <table> [WHERE <col>=<val>] [LIMIT <n>]
        // This is a simple implementation, not a full SQL parser
        Pattern p = Pattern.compile("(?i)SELECT \\* FROM\\s+(\\w+)(?:\\s+WHERE\\s+([^=<>]+)\\s*(=|>|<)\\s*([^\\s]+))?(?:\\s+LIMIT\\s+(\\d+))?");
        Matcher m = p.matcher(query.trim());
        
        if (!m.find()) {
            System.out.println(ANSIColors.error("Syntax Error."));
            System.out.println("Supported: SELECT * FROM <table_name> [WHERE column=value] [LIMIT n]");
            return;
        }
        
        String tableName = m.group(1).toLowerCase();
        String colName = m.group(2);
        String operator = m.group(3);
        String val = m.group(4);
        String limitStr = m.group(5);
        
        if (!tableName.equals("accounts") && !tableName.equals("transactions")) {
             System.out.println(ANSIColors.error("Table '" + tableName + "' not found. Available: accounts, transactions"));
             return;
        }
        
        int limit = -1;
        if (limitStr != null) {
            try {
                limit = Integer.parseInt(limitStr);
            } catch (NumberFormatException e) {
                System.out.println(ANSIColors.error("Invalid LIMIT value"));
                return;
            }
        }
        
        printCsv("data/" + tableName + ".csv", colName, operator, val, limit);
    }
    
    private void printCsv(String path, String colName, String operator, String val, int limit) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                System.out.println(ANSIColors.error("Table not found (No data yet)."));
                return;
            }
            Scanner sc = new Scanner(f);
            
            if (!sc.hasNextLine()) { sc.close(); return; }
            
            String header = sc.nextLine();
            System.out.println(ANSIColors.BOLD_CYAN + header.replace(",", " | ") + ANSIColors.RESET);
            String[] columns = header.split(",");
            
            int filterCol = -1;
            
            if (colName != null) {
                for(int i=0; i<columns.length; i++) {
                    if (columns[i].equalsIgnoreCase(colName.trim())) {
                        filterCol = i;
                        break;
                    }
                }
                if (filterCol == -1) {
                    System.out.println(ANSIColors.warning("Warning: Column '" + colName + "' not found. Showing all rows."));
                }
            }
            
            // Cleanup value quotes
            if (val != null) {
                 val = val.replace("'", "").replace("\"", "");
            }
            
            int count = 0;
            int totalMatches = 0;
            while (sc.hasNextLine()) {
                if (limit != -1 && count >= limit) break;
                
                String line = sc.nextLine();
                if (line.trim().isEmpty()) continue;
                
                boolean match = true;
                if (filterCol != -1 && val != null) {
                    String[] data = line.split(",");
                    if (data.length > filterCol) {
                        String rowVal = data[filterCol];
                        // Basic type inference for comparison
                        try {
                            double numRow = Double.parseDouble(rowVal);
                            double numVal = Double.parseDouble(val);
                            
                            if (operator.equals("=")) match = numRow == numVal;
                            else if (operator.equals(">")) match = numRow > numVal;
                            else if (operator.equals("<")) match = numRow < numVal;
                            
                        } catch (NumberFormatException e) {
                            // String comparison (only equality supported nicely here)
                            if (operator.equals("=")) match = rowVal.equalsIgnoreCase(val);
                            else match = false; // String inequality not really supported in this simple parser
                        }
                    }
                }
                
                if (match) {
                    System.out.println(line.replace(",", " | "));
                    count++;
                    totalMatches++;
                }
            }
            sc.close();
            System.out.println(ANSIColors.success("\nQuery returned " + count + " rows" + (limit != -1 && totalMatches >= limit ? " (Limited)" : ".") ));
        } catch (Exception e) {
            System.out.println(ANSIColors.error("Read Error: " + e.getMessage()));
        }
    }
}
