package com.example.chat_assistant;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;
import jakarta.servlet.http.HttpSession;

@Service
public class ChatService {
    private static final String OLLAMA_URL = "http://172.31.168.2:11434/api/chat";
    private static final String MODEL = "llama3.1";
    private final RestTemplate restTemplate = new RestTemplate();

    public String getLLMResponse(String message, HttpSession session) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);
            requestBody.put("stream", false);
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
            "role", "system",
            "content", "You are a helpful assistant. Only call the `simulateLogin` tool when the user clearly wants to log in by providing an email and password. If they just say hello or ask a general question, do not call the tool."
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
} //./mvnw spring-boot:run