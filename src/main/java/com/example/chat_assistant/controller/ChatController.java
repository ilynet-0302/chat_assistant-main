package com.example.chat_assistant.controller;

import com.example.chat_assistant.service.ChatService;
import com.example.chat_assistant.service.AuthService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private AuthService authService;

    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> payload, HttpSession session) {
        String message = payload.get("message");
        String llmResponse = chatService.chatWithFunctionCalling(message, session);
        return ResponseEntity.ok(Map.of("response", llmResponse));
    }

    @GetMapping("/session")
    public Map<String, Object> sessionInfo(HttpSession session) {
        boolean loggedIn = Boolean.TRUE.equals(session.getAttribute("loggedIn"));
        String email = (String) session.getAttribute("email");
        String site = "example.com";
        Map<String, Object> result = new HashMap<>();
        result.put("loggedIn", loggedIn);
        result.put("email", email);
        result.put("site", site);
        return result;
    }

    @Tool
    public Map<String, Object> simulateLogin(String ignoredSite, String email, String password, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        if (email == null || !email.contains("@") || password == null) {
            result.put("success", false);
            result.put("message", "Missing or invalid login data.");
            return result;
        }
        if (email == null || password == null) {
            result.put("action", "none");
            result.put("message", "Not enough data to simulate login.");
            return result;
        }
        session.setAttribute("loggedIn", true);
        session.setAttribute("email", email);
        session.setAttribute("site", "example.com");
        result.put("action", "login");
        result.put("email", email);
        result.put("password", password);
        result.put("site", "example.com");
        result.put("message", "Logged in successfully.");
        return result;
    }

    @Tool
    public Map<String, Object> simulateRegister(String email, String password, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        if (email == null || !email.contains("@") || password == null) {
            result.put("success", false);
            result.put("message", "Missing or invalid registration data.");
            return result;
        }
        boolean registered = authService.register(email, password, "example.com");
        result.put("action", "register");
        result.put("email", email);
        result.put("success", registered);
        result.put("message", registered ? "Registration successful." : "Email already registered.");
        return result;
    }

    @Tool
    public Map<String, Object> getCurrentUser(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        result.put("loggedIn", Boolean.TRUE.equals(session.getAttribute("loggedIn")));
        result.put("email", session.getAttribute("email"));
        result.put("site", "example.com");
        return result;
    }

    @Tool
    public Map<String, Object> changePassword(String email, String oldPassword, String newPassword, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        boolean loggedIn = Boolean.TRUE.equals(session.getAttribute("loggedIn"));
        if (!loggedIn || !email.equals(session.getAttribute("email"))) {
            result.put("success", false);
            result.put("message", "Not logged in or email mismatch.");
            return result;
        }
        boolean valid = authService.login(email, oldPassword, "example.com");
        if (!valid) {
            result.put("success", false);
            result.put("message", "Old password incorrect.");
            return result;
        }
        boolean changed = authService.changePassword(email, newPassword, "example.com");
        result.put("success", changed);
        result.put("message", changed ? "Password changed." : "Failed to change password.");
        return result;
    }

    @Tool
    public List<String> listUsers() {
        return authService.getAllEmails("example.com");
    }

    @Tool
    public Map<String, Object> simulateAccountDeletion(String email, String password, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        boolean loggedIn = Boolean.TRUE.equals(session.getAttribute("loggedIn"));
        if (!loggedIn || !email.equals(session.getAttribute("email"))) {
            result.put("success", false);
            result.put("message", "Not logged in or email mismatch.");
            return result;
        }
        boolean valid = authService.login(email, password, "example.com");
        if (!valid) {
            result.put("success", false);
            result.put("message", "Password incorrect.");
            return result;
        }
        boolean deleted = authService.changePassword(email, "__DELETED__", "example.com");
        if (deleted) {
            session.invalidate();
        }
        result.put("success", deleted);
        result.put("message", deleted ? "Account deleted." : "Failed to delete account.");
        return result;
    }
} 