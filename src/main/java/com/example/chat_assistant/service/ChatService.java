package com.example.chat_assistant.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;
import jakarta.servlet.http.HttpSession;
import java.util.regex.*;

@Service
public class ChatService {
    private static final String OLLAMA_URL = "http://172.31.168.2:11434/api/chat";
    private static final String MODEL = "llama3.1";
    private final RestTemplate restTemplate = new RestTemplate();

    public enum IntentType { REGISTER, LOGIN, CHAT }

    public static class Intent {
        public IntentType type;
        public String email;
        public String password;
    }

    public Intent parseIntent(String message) {
        // Registration intent
        Pattern regPattern = Pattern.compile("register.*email\\s*([\\w.@-]+).*password\\s*([\\w!@#$%^&*()_+=-]+)", Pattern.CASE_INSENSITIVE);
        Matcher regMatcher = regPattern.matcher(message);
        if (regMatcher.find()) {
            Intent intent = new Intent();
            intent.type = IntentType.REGISTER;
            intent.email = regMatcher.group(1);
            intent.password = regMatcher.group(2);
            return intent;
        }
        // Login intent
        Pattern loginPattern = Pattern.compile("log ?in.*email\\s*([\\w.@-]+).*password\\s*([\\w!@#$%^&*()_+=-]+)", Pattern.CASE_INSENSITIVE);
        Matcher loginMatcher = loginPattern.matcher(message);
        if (loginMatcher.find()) {
            Intent intent = new Intent();
            intent.type = IntentType.LOGIN;
            intent.email = loginMatcher.group(1);
            intent.password = loginMatcher.group(2);
            return intent;
        }
        // Default: treat as chat
        Intent intent = new Intent();
        intent.type = IntentType.CHAT;
        return intent;
    }

    public String getLLMResponse(String message, HttpSession session) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);
            requestBody.put("stream", false);
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
            "role", "system",
            "content", "You are a helpful assistant. If the user wants to register, output ONLY a JSON object like: {\"tool\": \"register\", \"email\": \"user@example.com\", \"password\": \"1234\"}. If the user wants to log in, output ONLY a JSON object like: {\"tool\": \"login\", \"email\": \"user@example.com\", \"password\": \"1234\"}. If the user wants to see their account info, output ONLY a JSON object like: {\"tool\": \"getCurrentUser\"}. If the user wants to change their password, output ONLY a JSON object like: {\"tool\": \"changePassword\", \"email\": \"user@example.com\", \"oldPassword\": \"oldpass\", \"newPassword\": \"newpass\"}. If the user wants to list all users, output ONLY a JSON object like: {\"tool\": \"listUsers\"}. If the user wants to delete their account, output ONLY a JSON object like: {\"tool\": \"deleteAccount\", \"email\": \"user@example.com\", \"password\": \"1234\"}. For all other questions, answer normally."
            ));
            messages.add(Map.of("role", "user", "content", message));
            requestBody.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(OLLAMA_URL, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map respBody = response.getBody();
                Map msg = (Map) respBody.get("message");
                if (msg != null && msg.get("content") != null) {
                    String content = msg.get("content").toString();
                    // Try to extract a JSON object from the response
                    String json = extractJson(content);
                    if (json != null) {
                        try {
                            Map<String, Object> action = new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, Map.class);
                            if ("login".equals(action.get("action"))) {
                                String email = (String) action.getOrDefault("email", "[no email]");
                                // Store login state in session
                                session.setAttribute("loggedIn", true);
                                session.setAttribute("site", "example.com");
                                session.setAttribute("email", email);
                                return "Simulated login to example.com as " + email + ". You are now logged in!";
                            } else if ("register".equals(action.get("action"))) {
                                String email = (String) action.getOrDefault("email", "[no email]");
                                return "Simulated registration at example.com for " + email + ".";
                            } else if ("getCurrentUser".equals(action.get("action"))) {
                                return "Simulated account info retrieval.";
                            } else if ("changePassword".equals(action.get("action"))) {
                                String email = (String) action.getOrDefault("email", "[no email]");
                                String oldPassword = (String) action.getOrDefault("oldPassword", "[no old password]");
                                String newPassword = (String) action.getOrDefault("newPassword", "[no new password]");
                                return "Simulated password change at example.com for " + email + ". Old password: " + oldPassword + ", New password: " + newPassword;
                            } else if ("listUsers".equals(action.get("action"))) {
                                return "Simulated list of all users.";
                            } else if ("deleteAccount".equals(action.get("action"))) {
                                String email = (String) action.getOrDefault("email", "[no email]");
                                String password = (String) action.getOrDefault("password", "[no password]");
                                return "Simulated account deletion at example.com for " + email + ". Password: " + password;
                            } else if ("none".equals(action.get("action"))) {
                                return "No actionable command detected.";
                            }
                        } catch (Exception ignore) {}
                    }
                    // If user is logged in, show simulated account info
                    if (Boolean.TRUE.equals(session.getAttribute("loggedIn"))) {
                        String email = (String) session.getAttribute("email");
                        return content + "\n\n[Logged in as " + email + " at example.com]";
                    }
                    return content;
                }
            }
            return "[No response from LLM]";
        } catch (Exception e) {
            return "[Error contacting LLM: " + e.getMessage() + "]";
        }
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        return null;
    }

    private static final Pattern TOOL_CALL_PATTERN = Pattern.compile(
        "\\{\\s*\\\"tool\\\"\\s*:\\s*\\\"(register|login|getCurrentUser|changePassword|listUsers|deleteAccount)\\\"(,\\s*\\\"email\\\"\\s*:\\s*\\\"([^\\\"]*)\\\")?(,\\s*\\\"password\\\"\\s*:\\s*\\\"([^\\\"]*)\\\")?(,\\s*\\\"oldPassword\\\"\\s*:\\s*\\\"([^\\\"]*)\\\")?(,\\s*\\\"newPassword\\\"\\s*:\\s*\\\"([^\\\"]*)\\\")?\\s*\\}");

    public Optional<Map<String, String>> parseToolCall(String llmOutput) {
        Matcher matcher = TOOL_CALL_PATTERN.matcher(llmOutput);
        if (matcher.find()) {
            Map<String, String> toolCall = new HashMap<>();
            toolCall.put("tool", matcher.group(1));
            if (matcher.group(3) != null) toolCall.put("email", matcher.group(3));
            if (matcher.group(5) != null) toolCall.put("password", matcher.group(5));
            if (matcher.group(7) != null) toolCall.put("oldPassword", matcher.group(7));
            if (matcher.group(9) != null) toolCall.put("newPassword", matcher.group(9));
            return Optional.of(toolCall);
        }
        return Optional.empty();
    }
} 