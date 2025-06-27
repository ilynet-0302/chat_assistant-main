package com.example.chat_assistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import com.example.chat_assistant.controller.ChatController;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class AiConfig {
    @Autowired
    private ChatController chatController;

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        // Register ChatController as a tool provider
        return ChatClient.builder(chatModel)
                .build();
    }
}