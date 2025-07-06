package com.sadi.backend;

import com.sadi.backend.controllers.TestTokenController;
import com.sadi.backend.dtos.requests.TestTokenReq;
import com.sadi.backend.dtos.responses.ChatResponse;
import com.sadi.backend.enums.ChatType;
import com.sadi.backend.repositories.ChatRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

@AutoConfigureWebTestClient
@Slf4j
public class ChatControllerTests extends AbstractBaseIntegrationTest{
    @Autowired
    private TestTokenController testTokenController;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ChatRepository chatRepository;

    @Test
    void testChatStreamingEndpoint() {
        TestTokenReq req = new TestTokenReq("user-1", "alice@example.com", true, "Alice Patient", null, "PATIENT");
        String token = Objects.requireNonNull(testTokenController.createJwt(req).getBody()).value();

        List<String> results = webTestClient.post()
                .uri("/v1/chat")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                {
                    "query": "Hello, how are you?",
                    "zone": "Asia/Dhaka"
                }
            """)
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody()
                .collectList()
                .block();

        Assertions.assertNotNull(results);
        assertFalse(results.isEmpty());
        log.debug("Chat response: {}", String.join("", results));

        ChatPageResponse page = webTestClient.get()
                .uri("/v1/chat")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ChatPageResponse.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(page);
        assertFalse(page.getContent().isEmpty());

        ChatPageResponse.PageInfo pageInfo = page.getPage();
        Assertions.assertNotNull(pageInfo);
        Assertions.assertTrue(pageInfo.getSize() > 0);
        Assertions.assertTrue(pageInfo.getTotalElements() >= 0);
        Assertions.assertTrue(pageInfo.getTotalPages() >= 0);

        List<ChatResponseDto> chats = page.getContent();
        assertThat(chats.stream().anyMatch(c -> c.getType() == ChatType.USER)).isTrue();
        assertThat(chats.stream().anyMatch(c -> c.getType() == ChatType.ASSISTANT)).isTrue();

        // Clean up the chats created during the test
        for (ChatResponse chat : chats) {
            chatRepository.deleteById(chat.getId());
        }
    }


    @Getter
    @Setter
    public static class ChatPageResponse {
        private List<ChatResponseDto> content;
        private PageInfo page;

        public ChatPageResponse() {}

        @Getter
        @Setter
        public static class PageInfo {
            private int size;
            private int number;
            private long totalElements;
            private int totalPages;

            public PageInfo() {}
        }
    }


    @Setter
    public static class ChatResponseDto implements ChatResponse {
        // Setters (required for Jackson deserialization)
        private UUID id;
        private ChatType type;
        private String message;
        private Instant createdAt;

        // Default constructor (required for Jackson)
        public ChatResponseDto() {}

        // Constructor with all fields
        public ChatResponseDto(UUID id, ChatType type, String message, Instant createdAt) {
            this.id = id;
            this.type = type;
            this.message = message;
            this.createdAt = createdAt;
        }

        // Implement interface methods
        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public ChatType getType() {
            return type;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public Instant getCreatedAt() {
            return createdAt;
        }

    }
}
