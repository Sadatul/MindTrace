package com.sadi.backend.controllers;

import com.sadi.backend.services.abstractions.ChatService;
import com.sadi.backend.services.abstractions.LoggingTools;
import com.sadi.backend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    @GetMapping
    public Flux<String> chat(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "Asia/Dhaka") String zone
    ) {
        log.debug("Received query: {}", query);
        SystemPromptTemplate template = new SystemPromptTemplate(systemPrompt);
        Message sysPrompt = template.createMessage(Map.of("time", Instant.now().toString()));
        StringBuilder response = new StringBuilder();
        String userId = SecurityUtils.getName();
        return chatClient
                .prompt()
                .system(sysPrompt.getText())
                .user(query)
                .tools(loggingTools)
                .toolContext(Map.of("userId", userId, "zone", zone))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                .stream().chatResponse().doOnNext(
                        chatResponse -> {
                            response.append(chatResponse.getResult().getOutput().getText());
                            log.info("Tokens {}", chatResponse.getMetadata().getUsage().getTotalTokens());
                        }
                )
                .doOnComplete(() ->
                    chatService.saveChat(query, response.toString(), userId)
                )
                .mapNotNull(chatResponse -> chatResponse.getResult().getOutput().getText());
    }
}
