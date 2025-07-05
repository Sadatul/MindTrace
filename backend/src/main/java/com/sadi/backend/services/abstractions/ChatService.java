package com.sadi.backend.services.abstractions;

import com.sadi.backend.dtos.responses.ChatResponse;
import com.sadi.backend.enums.ChatType;
import org.springframework.data.domain.Page;

public interface ChatService {
    void saveChat(String message, ChatType type, String userId);
    Page<ChatResponse> getChats(Integer page, Integer size);
    void updateChatHistory();
}
