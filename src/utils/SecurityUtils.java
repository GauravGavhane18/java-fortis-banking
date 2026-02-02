package com.fortis.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Security utilities for PIN hashing and validation
 */
public class SecurityUtils {
    
    public static String hashPin(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pin.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing PIN", e);
        }
    }
    
    public static boolean validatePinFormat(String pin) {
        return pin != null && pin.matches("\\d{4,6}");
    }
    
    public static String maskPin(String pin) {
        return "*".repeat(pin.length());
    }
}
