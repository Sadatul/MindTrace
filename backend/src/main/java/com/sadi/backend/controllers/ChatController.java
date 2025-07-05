package com.sadi.backend.controllers;

import com.sadi.backend.dtos.requests.ChatRequest;
import com.sadi.backend.dtos.responses.ChatResponse;
import com.sadi.backend.enums.ChatType;
import com.sadi.backend.services.abstractions.ChatService;
import com.sadi.backend.services.abstractions.LoggingTools;
import com.sadi.backend.utils.BasicUtils;
import com.sadi.backend.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/v1/chat")
@Slf4j
@RequiredArgsConstructor
public class ChatController {
    private final ChatClient chatClient;
    private final ChatService chatService;
    private final LoggingTools loggingTools;

    @Value("classpath:/prompts/sysprompt.st")
    private Resource systemPrompt;

    @PostMapping
    public Flux<String> chat(
            @Valid @RequestBody ChatRequest req
            ) {
        log.debug("Received query: {}", req);
        SystemPromptTemplate template = new SystemPromptTemplate(systemPrompt);
        Message sysPrompt = template.createMessage(Map.of("time", BasicUtils.getISOStringFromZoneIdAndInstant(req.zone(), Instant.now())));
        StringBuilder response = new StringBuilder();
        String userId = SecurityUtils.getName();
        chatService.saveChat(req.query(), ChatType.USER, userId);
        return chatClient
                .prompt()
                .system(sysPrompt.getText())
                .user(req.query())
                .tools(loggingTools)
                .toolContext(Map.of("userId", userId, "zone", req.zone()))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                .stream().chatResponse().doOnNext(
                        chatResponse ->
                            response.append(chatResponse.getResult().getOutput().getText())
                )
                .doOnComplete(() ->
                    chatService.saveChat(response.toString(), ChatType.ASSISTANT, userId)
                )
                .mapNotNull(chatResponse -> chatResponse.getResult().getOutput().getText());
    }

    @GetMapping
    public ResponseEntity<PagedModel<ChatResponse>> getChats(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        return ResponseEntity.ok(new PagedModel<>(chatService.getChats(page, size)));
    }

    @PutMapping("/history")
    public ResponseEntity<Void> updateChatHistory()
    {
        chatService.updateChatHistory();
        return ResponseEntity.noContent().build();
    }
}
