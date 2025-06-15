package com.sadi.backend.dtos.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sadi.backend.enums.ChatType;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface ChatResponse{
    UUID getId();
    ChatType getType();
    String getMessage();
    Instant getCreatedAt();

    @JsonIgnore
    default Message getMessageObject(
    ) {
        Map<String, Object> enrichedMetadata = new HashMap<>();

        enrichedMetadata.put("timestamp", getCreatedAt());
        enrichedMetadata.put("type", getType());

        if(getType() == ChatType.ASSISTANT){
            return new AssistantMessage(getMessage(), enrichedMetadata);
        }
        return UserMessage.builder().text(getMessage()).metadata(enrichedMetadata).build();
    }
}
