package com.example.chat_assistant.controller;

import com.example.chat_assistant.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> payload) {
        boolean success = authService.register(
            payload.get("email"),
            payload.get("password"),
            "example.com"
        );
        return ResponseEntity.ok(Map.of("success", success));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload, HttpSession session) {
        boolean success = authService.login(
            payload.get("email"),
            payload.get("password"),
            "example.com"
        );
        if (success) {
            session.setAttribute("loggedIn", true);
            session.setAttribute("email", payload.get("email"));
            session.setAttribute("site", "example.com");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "email", payload.get("email"),
                "site", "example.com"
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Invalid email or password."
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("success", true));
    }
} 