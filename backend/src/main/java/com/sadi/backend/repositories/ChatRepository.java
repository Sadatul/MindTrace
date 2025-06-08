package com.sadi.backend.repositories;

import com.sadi.backend.dtos.responses.ChatResponse;
import com.sadi.backend.entities.Chat;
import com.sadi.backend.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {
    Page<ChatResponse> findByUser(User user, Pageable pageable);
}
