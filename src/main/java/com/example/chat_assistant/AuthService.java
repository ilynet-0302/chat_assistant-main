package com.example.chat_assistant;

import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
public class AuthService {
    private static final File USER_FILE = new File("users.txt");

    public boolean register(String email, String password, String ignoredSite) {
        String site = "example.com";
        Map<String, String> users = loadUsers(site);
        if (users.containsKey(email)) {
            return false; // already exists
        }
        try (FileWriter fw = new FileWriter(USER_FILE, true)) {
            fw.write(site + "," + email + "," + password + "\n");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean login(String email, String password, String ignoredSite) {
        String site = "example.com";
        Map<String, String> users = loadUsers(site);
        return password.equals(users.get(email));
    }

    private Map<String, String> loadUsers(String ignoredSite) {
        String site = "example.com";
        Map<String, String> users = new HashMap<>();
        if (!USER_FILE.exists()) return users;

        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length == 3 && parts[0].equals(site)) {
                    users.put(parts[1], parts[2]); // email, password
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean changePassword(String email, String newPassword, String ignoredSite) {
        String site = "example.com";
        if (!USER_FILE.exists()) return false;
        List<String> lines = new ArrayList<>();
        boolean changed = false;
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length == 3 && parts[0].equals(site) && parts[1].equals(email)) {
                    lines.add(site + "," + email + "," + newPassword);
                    changed = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if (changed) {
            try (FileWriter fw = new FileWriter(USER_FILE, false)) {
                for (String l : lines) fw.write(l + "\n");
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return changed;
    }

    public List<String> getAllEmails(String ignoredSite) {
        String site = "example.com";
        List<String> emails = new ArrayList<>();
        if (!USER_FILE.exists()) return emails;
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length == 3 && parts[0].equals(site)) {
                    emails.add(parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return emails;
    }
}