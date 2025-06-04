package com.sadi.backend.controllers;

import com.sadi.backend.services.abstractions.ChatService;
import com.sadi.backend.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/v1/chat")
@Slf4j
public class ChatController {
    private final ChatClient chatClient;
    private final ChatService chatService;

    public ChatController(ChatClient chatClient, ChatService chatService) {
        this.chatClient = chatClient;
        this.chatService = chatService;
    }

    @GetMapping
    public Flux<String> chat(
            @RequestParam String query
    ) {
        log.debug("Received query: {}", query);
        StringBuilder response = new StringBuilder();
        String userId = SecurityUtils.getName();
        return chatClient
                .prompt()
                .user(query)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                .stream().chatResponse().doOnNext(
                        chatResponse -> {
                            response.append(chatResponse.getResult().getOutput().getText());
                        }
                )
                .doOnComplete(() -> {
                    chatService.saveChat(query, response.toString(), userId);
                })
                .mapNotNull(chatResponse -> chatResponse.getResult().getOutput().getText());
    }
}
