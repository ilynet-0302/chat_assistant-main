package com.example.chat_assistant;

import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
public class AuthService {
    private static final File USER_FILE = new File("users.txt");

    public boolean register(String email, String password, String site) {
        Map<String, String> users = loadUsers(site);
        if (users.containsKey(email)) {
            return false; // вече съществува
        }
        try (FileWriter fw = new FileWriter(USER_FILE, true)) {
            fw.write(site + "," + email + "," + password + "\n");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean login(String email, String password, String site) {
        Map<String, String> users = loadUsers(site);
        return password.equals(users.get(email));
    }

    private Map<String, String> loadUsers(String site) {
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
}