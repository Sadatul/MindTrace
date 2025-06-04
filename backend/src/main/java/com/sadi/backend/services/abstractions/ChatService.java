package com.sadi.backend.services.abstractions;

public interface ChatService {
    void saveChat(String userMessage, String assistantMessage, String userId);
}
