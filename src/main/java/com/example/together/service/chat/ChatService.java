package com.example.together.service.chat;

import com.example.together.domain.ChatMessage;
import com.example.together.domain.ChatRoom;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatService {
  ChatRoom start(Long tradeId, Long sellerId, Long buyerId);
  List<ChatRoom> listRooms(Long myUserId);
  ChatRoom getRoomIfParticipant(Long roomId, Long myUserId);
  List<ChatMessage> latest(Long roomId);
  List<ChatMessage> after(Long roomId, LocalDateTime after);
  ChatMessage send(Long roomId, Long senderId, String content);
}
