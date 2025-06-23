package com.example.chat_assistant;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

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
        payload.get("site")
        );
        return ResponseEntity.ok(Map.of("success", success));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload, HttpSession session) {
        boolean success = authService.login(
            payload.get("email"),
            payload.get("password"),
            payload.get("site")
        );
        if (success) {
            session.setAttribute("loggedIn", true);
            session.setAttribute("email", payload.get("email"));
            session.setAttribute("site", payload.get("site"));
        }
        return ResponseEntity.ok(Map.of("success", success));
    }
}