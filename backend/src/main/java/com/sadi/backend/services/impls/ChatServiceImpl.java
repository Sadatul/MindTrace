package com.sadi.backend.services.impls;

import com.sadi.backend.dtos.responses.ChatResponse;
import com.sadi.backend.entities.Chat;
import com.sadi.backend.entities.User;
import com.sadi.backend.enums.ChatType;
import com.sadi.backend.repositories.ChatRepository;
import com.sadi.backend.services.abstractions.ChatService;
import com.sadi.backend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;
    private final ChatMemoryRepository redisChatMemoryRepository;

    @Override
    public void saveChat(String message, ChatType type, String userId) {
        User user = new User(userId);
        Chat userChat = new Chat(user, type, message);
        chatRepository.save(userChat);
    }

    @Override
    public Page<ChatResponse> getChats(Integer page, Integer size) {
        String userId = SecurityUtils.getName();
        User user = new User(userId);
        return chatRepository.findByUser(user,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @Override
    public void updateChatHistory() {
        String userId = SecurityUtils.getName();
        List<Message> results = getChats(0, 20).stream().map(
                ChatResponse::getMessageObject
        ).toList().reversed();
        redisChatMemoryRepository.saveAll(userId, results);
    }
}
