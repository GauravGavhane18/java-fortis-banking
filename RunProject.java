
import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.nio.file.Files;

public class RunProject {
    public static void main(String[] args) throws Exception {
        System.out.println("=========================================");
        System.out.println("   FORTIS BANKING SYSTEM LAUNCHER");
        System.out.println("=========================================");
        
        File srcDir = new File("src");
        File binDir = new File("bin");
        File libDir = new File("lib");
        
        // 1. Clean and Create Bin
        if (binDir.exists()) deleteDir(binDir);
        binDir.mkdirs();
        
        // 2. Find all sources
        List<String> sources = Files.walk(srcDir.toPath())
            .map(p -> p.toFile())
            .filter(f -> f.getName().endsWith(".java"))
            .map(File::getAbsolutePath)
            .collect(Collectors.toList());
            
        System.out.println("\nFound " + sources.size() + " source files.");
        
        // 3. Create sources list file
        File sourcesFile = new File("sources.txt");
        try (PrintWriter pw = new PrintWriter(sourcesFile)) {
            for (String s : sources) pw.println("\"" + s.replace("\\", "\\\\") + "\"");
        }
        
        // 4. Compile
        System.out.println("Compiling...");
        String cp = "lib" + File.separator + "*";
        
        ProcessBuilder pb = new ProcessBuilder(
            "C:\\Program Files\\Java\\jdk-25\\bin\\javac.exe",
            "-d", "bin",
            "-cp", cp,
            "@sources.txt"
        );
        pb.inheritIO();
        Process p = pb.start();
        int result = p.waitFor();
        
        if (result == 0) {
            System.out.println("✅ Compilation Success!");
            System.out.println("\nLaunching Application...\n");
            
            // 5. Run
            ProcessBuilder runPb = new ProcessBuilder(
                "cmd", "/c", "start", "Fortis Banking System", "/WAIT",
                "C:\\Program Files\\Java\\jdk-25\\bin\\java.exe",
                "-cp", "bin;" + cp,
                "com.fortis.ui.EnhancedCLI"
            );
            runPb.inheritIO();
            Process runP = runPb.start();
            runP.waitFor();
        } else {
            System.out.println("❌ Compilation Failed.");
        }
    }
    
    private static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) deleteDir(f);
        }
        file.delete();
    }
}

