package com.example.chat_assistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.chat.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import java.util.*;

@Service
public class ChatService {
    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    private OpenAiService openAiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        this.openAiService = new OpenAiService(openAiApiKey);
    }

    // --- Parameter POJOs for function-calling ---
    public static class RegisterParams {
        public String email;
        public String password;
    }
    public static class LoginParams {
        public String email;
        public String password;
    }
    public static class ChangePasswordParams {
        public String email;
        public String oldPassword;
        public String newPassword;
    }
    public static class DeleteAccountParams {
        public String email;
        public String password;
    }
    // For functions with no parameters
    public static class NoParams {}

    public String chatWithFunctionCalling(String message, HttpSession session) {
        ChatFunction registerFunction = ChatFunction.builder()
            .name("register")
            .description("Register a new user")
            .executor(RegisterParams.class, params -> this.simulateRegister(params.email, params.password, session))
            .build();
        ChatFunction loginFunction = ChatFunction.builder()
            .name("login")
            .description("Login a user")
            .executor(LoginParams.class, params -> this.simulateLogin("example.com", params.email, params.password, session))
            .build();
        ChatFunction getCurrentUserFunction = ChatFunction.builder()
            .name("getCurrentUser")
            .description("Get current user info")
            .executor(NoParams.class, v -> this.getCurrentUser(session))
            .build();
        ChatFunction changePasswordFunction = ChatFunction.builder()
            .name("changePassword")
            .description("Change user password")
            .executor(ChangePasswordParams.class, params -> this.changePassword(params.email, params.oldPassword, params.newPassword, session))
            .build();
        ChatFunction listUsersFunction = ChatFunction.builder()
            .name("listUsers")
            .description("List all users")
            .executor(NoParams.class, v -> this.listUsers())
            .build();
        ChatFunction deleteAccountFunction = ChatFunction.builder()
            .name("deleteAccount")
            .description("Delete user account")
            .executor(DeleteAccountParams.class, params -> this.simulateAccountDeletion(params.email, params.password, session))
            .build();

        ChatMessage userMessage = new ChatMessage("user", message);
        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model("gpt-3.5-turbo-1106")
            .messages(List.of(userMessage))
            .functions(List.of(
                registerFunction,
                loginFunction,
                getCurrentUserFunction,
                changePasswordFunction,
                listUsersFunction,
                deleteAccountFunction
            ))
            .functionCall(new ChatCompletionRequest.ChatCompletionRequestFunctionCall("auto"))
            .build();

        ChatCompletionResult result = openAiService.createChatCompletion(request);
        ChatCompletionChoice choice = result.getChoices().get(0);
        if (choice.getMessage().getFunctionCall() != null) {
            String functionName = choice.getMessage().getFunctionCall().getName();
            String argumentsJson = choice.getMessage().getFunctionCall().getArguments().toString();
            try {
                switch (functionName) {
                    case "register": {
                        RegisterParams args = objectMapper.readValue(argumentsJson, RegisterParams.class);
                        Map<String, Object> resultMap = this.simulateRegister(args.email, args.password, session);
                        return (String) resultMap.get("message");
                    }
                    case "login": {
                        LoginParams args = objectMapper.readValue(argumentsJson, LoginParams.class);
                        Map<String, Object> resultMap = this.simulateLogin("example.com", args.email, args.password, session);
                        return (String) resultMap.get("message");
                    }
                    case "getCurrentUser": {
                        Map<String, Object> resultMap = this.getCurrentUser(session);
                        return resultMap.toString();
                    }
                    case "changePassword": {
                        ChangePasswordParams args = objectMapper.readValue(argumentsJson, ChangePasswordParams.class);
                        Map<String, Object> resultMap = this.changePassword(
                            args.email,
                            args.oldPassword,
                            args.newPassword,
                            session
                        );
                        return (String) resultMap.get("message");
                    }
                    case "listUsers": {
                        List<String> users = this.listUsers();
                        return users.toString();
                    }
                    case "deleteAccount": {
                        DeleteAccountParams args = objectMapper.readValue(argumentsJson, DeleteAccountParams.class);
                        Map<String, Object> resultMap = this.simulateAccountDeletion(
                            args.email,
                            args.password,
                            session
                        );
                        return (String) resultMap.get("message");
                    }
                }
            } catch (Exception e) {
                return "[Error handling function call: " + e.getMessage() + "]";
            }
        }
        return choice.getMessage().getContent();
    }

    // --- Your tool methods (simulateRegister, simulateLogin, getCurrentUser, changePassword, listUsers, simulateAccountDeletion) go here ---
    public Map<String, Object> simulateRegister(String email, String password, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        if (email == null || !email.contains("@") || password == null) {
            result.put("success", false);
            result.put("message", "Missing or invalid registration data.");
            return result;
        }
        boolean registered = false;
        if (authService != null) {
            registered = authService.register(email, password, "example.com");
        }
        result.put("action", "register");
        result.put("email", email);
        result.put("success", registered);
        result.put("message", registered ? "Registration successful." : "Email already registered.");
        return result;
    }

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

    public Map<String, Object> getCurrentUser(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        result.put("loggedIn", Boolean.TRUE.equals(session.getAttribute("loggedIn")));
        result.put("email", session.getAttribute("email"));
        result.put("site", "example.com");
        return result;
    }

    public Map<String, Object> changePassword(String email, String oldPassword, String newPassword, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        boolean loggedIn = Boolean.TRUE.equals(session.getAttribute("loggedIn"));
        if (!loggedIn || !email.equals(session.getAttribute("email"))) {
            result.put("success", false);
            result.put("message", "Not logged in or email mismatch.");
            return result;
        }
        boolean valid = false;
        if (authService != null) {
            valid = authService.login(email, oldPassword, "example.com");
        }
        if (!valid) {
            result.put("success", false);
            result.put("message", "Old password incorrect.");
            return result;
        }
        boolean changed = false;
        if (authService != null) {
            changed = authService.changePassword(email, newPassword, "example.com");
        }
        result.put("success", changed);
        result.put("message", changed ? "Password changed." : "Failed to change password.");
        return result;
    }

    public List<String> listUsers() {
        if (authService != null) {
            return authService.getAllEmails("example.com");
        }
        return Collections.emptyList();
    }

    public Map<String, Object> simulateAccountDeletion(String email, String password, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        boolean loggedIn = Boolean.TRUE.equals(session.getAttribute("loggedIn"));
        if (!loggedIn || !email.equals(session.getAttribute("email"))) {
            result.put("success", false);
            result.put("message", "Not logged in or email mismatch.");
            return result;
        }
        boolean valid = false;
        if (authService != null) {
            valid = authService.login(email, password, "example.com");
        }
        if (!valid) {
            result.put("success", false);
            result.put("message", "Password incorrect.");
            return result;
        }
        boolean deleted = false;
        if (authService != null) {
            deleted = authService.changePassword(email, "__DELETED__", "example.com");
        }
        if (deleted) {
            session.invalidate();
        }
        result.put("success", deleted);
        result.put("message", deleted ? "Account deleted." : "Failed to delete account.");
        return result;
    }

    // Inject AuthService
    private final com.example.chat_assistant.service.AuthService authService;

    public ChatService(com.example.chat_assistant.service.AuthService authService) {
        this.authService = authService;
    }
} 