package com.fortis.service;

import com.fortis.model.User;
import com.fortis.utils.SecurityUtils;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Service - Implements Authenticatable interface
 * Handles user login, logout, and session management
 */
public class AuthenticationService implements Authenticatable {
    private static final String USERS_FILE = "data/users.csv";
    private static AuthenticationService instance;
    private User currentUser;
    private final Map<String, User> userDatabase;

    public User getUserByUsername(String username) {
        return userDatabase.get(username);
    }

    private AuthenticationService() {
        this.userDatabase = new HashMap<>();
        loadUsers();
    }
    
    public static synchronized AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }
    
    private void loadUsers() {
        java.io.File file = new java.io.File(USERS_FILE);
        if (!file.exists()) {
            initializeDefaultUsers();
            saveUsers();
            return;
        }
        
        try (java.util.Scanner scanner = new java.util.Scanner(file)) {
            if (scanner.hasNextLine()) scanner.nextLine(); // Skip header
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    long id = Long.parseLong(parts[0]);
                    String username = parts[1];
                    String pinHash = parts[2];
                    User.UserRole role = User.UserRole.valueOf(parts[3]);
                    boolean locked = parts.length > 4 && parts[4].equals("LOCKED");
                    
                    User user = new User(id, username, pinHash, role, LocalDateTime.now());
                    if (locked) user.setLocked(true);
                    userDatabase.put(username, user);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load users: " + e.getMessage());
            initializeDefaultUsers();
        }
    }
    
    private void saveUsers() {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(USERS_FILE))) {
            pw.println("ID,USERNAME,PIN_HASH,ROLE,STATUS");
            for (User user : userDatabase.values()) {
                pw.printf("%d,%s,%s,%s,%s%n",
                    user.getUserId(),
                    user.getUsername(),
                    user.getPin(),
                    user.getRole(),
                    user.isLocked() ? "LOCKED" : "ACTIVE"
                );
            }
        } catch (Exception e) {
            System.err.println("Failed to save users: " + e.getMessage());
        }
    }
    
    private void initializeDefaultUsers() {
        // Create default admin user
        User admin = new User(1L, "admin", 
            SecurityUtils.hashPin("1234"), 
            User.UserRole.ADMIN, 
            LocalDateTime.now());
        userDatabase.put("admin", admin);
        
        // Create default customer users
        User customer1 = new User(2L, "virat", 
            SecurityUtils.hashPin("1111"), 
            User.UserRole.CUSTOMER, 
            LocalDateTime.now());
        userDatabase.put("virat", customer1);
        
        User customer2 = new User(3L, "rohit", 
            SecurityUtils.hashPin("2222"), 
            User.UserRole.CUSTOMER, 
            LocalDateTime.now());
        userDatabase.put("rohit", customer2);
        
        User customer3 = new User(4L, "dhoni", 
            SecurityUtils.hashPin("3333"), 
            User.UserRole.CUSTOMER, 
            LocalDateTime.now());
        userDatabase.put("dhoni", customer3);
    }
    
    @Override
    public User authenticate(String username, String pin) {
        User user = userDatabase.get(username);
        
        if (user == null) {
            return null;
        }
        
        if (user.isLocked()) {
            throw new SecurityException("Account is locked due to multiple failed attempts");
        }
        
        String hashedPin = SecurityUtils.hashPin(pin);
        if (user.getPin().equals(hashedPin)) {
            user.resetFailedAttempts();
            user.setLastLoginAt(LocalDateTime.now());
            this.currentUser = user;
            return user;
        } else {
            user.incrementFailedAttempts();
            if (user.isLocked()) saveUsers(); // Save lock state
            return null;
        }
    }
    
    @Override
    public boolean validatePin(String pin) {
        if (currentUser == null) return false;
        String hashedPin = SecurityUtils.hashPin(pin);
        return currentUser.getPin().equals(hashedPin);
    }
    
    @Override
    public void logout() {
        this.currentUser = null;
    }
    
    @Override
    public User getCurrentUser() {
        return currentUser;
    }
    
    @Override
    public boolean isAuthenticated() {
        return currentUser != null;
    }
    
    public void registerUser(String username, String pin, User.UserRole role) {
        if (userDatabase.containsKey(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        long userId = userDatabase.size() + 1L;
        User newUser = new User(userId, username, 
            SecurityUtils.hashPin(pin), 
            role, 
            LocalDateTime.now());
        userDatabase.put(username, newUser);
        saveUsers();
    }
    
    public void unlockUser(String username) {
        User user = userDatabase.get(username);
        if (user != null) {
            user.setLocked(false);
            user.resetFailedAttempts();
            saveUsers();
        }
    }
    
    public boolean resetPassword(String username, String newPlainPin) {
        User user = userDatabase.get(username);
        if (user != null) {
             user.setPin(SecurityUtils.hashPin(newPlainPin));
             user.setLocked(false);
             user.resetFailedAttempts();
             saveUsers();
             return true;
        }
        return false;
    }
}
