package com.fortis.model;

import java.time.LocalDateTime;

/**
 * User entity representing system users (Admin/Customer)
 * Demonstrates Encapsulation with private fields and validation
 */
public class User {
    private final long userId;
    private final String username;
    private String pin; // Encrypted PIN
    private final UserRole role;
    private final LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private boolean isLocked;
    private int failedLoginAttempts;
    
    public enum UserRole {
        ADMIN("Administrator", "Full system access"),
        CUSTOMER("Customer", "Account operations only");
        
        private final String displayName;
        private final String description;
        
        UserRole(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public User(long userId, String username, String pin, UserRole role, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.pin = pin;
        this.role = role;
        this.createdAt = createdAt;
        this.isLocked = false;
        this.failedLoginAttempts = 0;
    }
    
    // Getters
    public long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPin() { return pin; }
    public UserRole getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public boolean isLocked() { return isLocked; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    
    // Setters with validation
    public void setPin(String pin) {
        if (pin == null || pin.length() < 4) {
            throw new IllegalArgumentException("PIN must be at least 4 characters");
        }
        this.pin = pin;
    }
    
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    public void setLocked(boolean locked) {
        this.isLocked = locked;
    }
    
    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 3) {
            this.isLocked = true;
        }
    }
    
    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
    }
    
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
    
    public boolean isCustomer() {
        return role == UserRole.CUSTOMER;
    }
}
