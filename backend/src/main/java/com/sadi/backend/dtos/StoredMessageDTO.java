package com.sadi.backend.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import org.springframework.ai.chat.messages.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoredMessageDTO implements Message {
    private final String text;
    private final MessageType messageType;
    private final Map<String, Object> metadata;
    private final Instant timestamp;

    // Primary constructor with JsonCreator for Jackson
    @JsonCreator
    public StoredMessageDTO(
            @JsonProperty("text") String text,
            @JsonProperty("messageType") @NonNull MessageType messageType,
            @JsonProperty("metadata") Map<String, Object> metadata,
            @JsonProperty("timestamp") Instant timestamp) {
        this.text = text;
        this.messageType = messageType;
        this.metadata = metadata != null ? metadata : new HashMap<>();
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    // Convenience constructor for creating new messages
    public StoredMessageDTO(String text, @NonNull MessageType type, Map<String, Object> metadata) {
        this.text = text;
        this.messageType = type;
        this.metadata = metadata != null ? metadata : new HashMap<>();
        this.timestamp = Instant.now();
    }

    public Message toMessage() {
        return switch (this.messageType) {
            case ASSISTANT -> new AssistantMessage(this.text, this.metadata);
            case SYSTEM -> new SystemMessage(this.text);
            default -> new UserMessage(this.text);
        };
    }

    // Constructor from existing Message
    public StoredMessageDTO(Message message) {
        this.text = message.getText();
        this.messageType = message.getMessageType();
        this.metadata = message.getMetadata() != null ? message.getMetadata() : new HashMap<>();
        this.timestamp = Instant.now();
    }

    @Override
    @NonNull
    @JsonProperty("messageType")
    public MessageType getMessageType() {
        return messageType;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }


    @JsonProperty("timestamp")
    public Instant getTimestamp() {
        return timestamp;
    }
}