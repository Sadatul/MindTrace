package com.sadi.backend.services.impls;

import com.sadi.backend.entities.Chat;
import com.sadi.backend.entities.User;
import com.sadi.backend.enums.ChatType;
import com.sadi.backend.repositories.ChatRepository;
import com.sadi.backend.services.abstractions.ChatService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;

    @Override
    @Transactional
    public void saveChat(String userMessage, String assistantMessage, String userId) {
        User user = new User(userId);
        Chat userChat = new Chat(user, ChatType.USER, userMessage);
        Chat assistantChat = new Chat(user, ChatType.ASSISTANT, assistantMessage);
        chatRepository.save(userChat);
        chatRepository.save(assistantChat);
    }
}
