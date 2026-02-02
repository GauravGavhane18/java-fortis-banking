package com.fortis.service;

import java.io.*;
import java.util.*;

/**
 * Notification Service - Manages user notifications
 */
public class NotificationService {
    private static NotificationService instance;
    private final File notifFile = new File("data/notifications.csv");

    private NotificationService() {
        if (!notifFile.exists()) {
            try {
                File dir = notifFile.getParentFile();
                if (dir != null) dir.mkdirs();
                notifFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Could not create notifications file: " + e.getMessage());
            }
        }
    }

    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    public void addNotification(long userId, String message) {
        try (FileWriter fw = new FileWriter(notifFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            // UserId,Message,Date
            out.println(String.format("%d,%s,%s", userId, message, new java.util.Date()));
        } catch (IOException e) {
            System.err.println("Error saving notification: " + e.getMessage());
        }
    }

    public List<String> getNotifications(long userId) {
        List<String> notifs = new ArrayList<>();
        if (!notifFile.exists()) return notifs;

        try (BufferedReader br = new BufferedReader(new FileReader(notifFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 3); // Limit split to handle message with commas if careful, or just simple
                if (parts.length >= 3) {
                    try {
                        long uid = Long.parseLong(parts[0]);
                        if (uid == userId) {
                             notifs.add(String.format("[%s] %s", parts[2], parts[1]));
                        }
                    } catch (NumberFormatException e) {
                        // skip
                    }
                }
            }
        } catch (IOException e) {
             System.err.println("Error reading notifications: " + e.getMessage());
        }
        return notifs;
    }
}
