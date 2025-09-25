package com.example.together.repository;

import com.example.together.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

  ChatMessage findTop1ByChatRoomIdOrderByRegDateDesc(Long chatRoomId);

  List<ChatMessage> findTop50ByChatRoomIdOrderByRegDateAsc(Long chatRoomId);

  List<ChatMessage> findByChatRoomIdAndRegDateAfterOrderByRegDateAsc(Long chatRoomId, LocalDateTime after);
}
