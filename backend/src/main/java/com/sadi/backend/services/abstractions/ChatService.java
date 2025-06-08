package com.sadi.backend.services.abstractions;

import com.sadi.backend.dtos.responses.ChatResponse;
import org.springframework.data.domain.Page;

public interface ChatService {
    void saveChat(String userMessage, String assistantMessage, String userId);
    Page<ChatResponse> getChats(Integer page, Integer size);
    void updateChatHistory();
}
