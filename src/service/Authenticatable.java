package com.fortis.service;

import com.fortis.model.User;

/**
 * Interface for authentication operations
 * Demonstrates Interface usage in OOP
 */
public interface Authenticatable {
    User authenticate(String username, String pin);
    boolean validatePin(String pin);
    void logout();
    User getCurrentUser();
    boolean isAuthenticated();
}
