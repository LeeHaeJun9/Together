package com.example.together.service.chat;

import com.example.together.domain.ChatMessage;
import com.example.together.domain.ChatRoom;
import com.example.together.repository.ChatMessageRepository;
import com.example.together.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {

  private final ChatRoomRepository roomRepo;
  private final ChatMessageRepository msgRepo;


  @Override
  public ChatRoom start(Long tradeId, Long sellerId, Long buyerId) {
    return roomRepo.findTopByTradeIdAndBuyerIdOrderByIdDesc(tradeId, buyerId)
        .orElseGet(() -> {
          ChatRoom r = ChatRoom.builder()
              .tradeId(tradeId)
              .sellerId(sellerId)
              .buyerId(buyerId)
              .build();
          return roomRepo.save(r);
        });
  }

  @Transactional(readOnly = true)
  @Override
  public List<ChatRoom> listRooms(Long myUserId) {
    return roomRepo.findBySellerIdOrBuyerIdOrderByModDateDesc(myUserId, myUserId);
  }

  @Transactional(readOnly = true)
  @Override
  public ChatRoom getRoomIfParticipant(Long roomId, Long myUserId) {
    return roomRepo.findIfParticipant(roomId, myUserId).orElse(null);
  }

  @Transactional(readOnly = true)
  @Override
  public List<ChatMessage> latest(Long roomId) {
    return msgRepo.findTop50ByChatRoomIdOrderByRegDateAsc(roomId);
  }

  @Transactional(readOnly = true)
  @Override
  public List<ChatMessage> after(Long roomId, LocalDateTime after) {
    return msgRepo.findByChatRoomIdAndRegDateAfterOrderByRegDateAsc(roomId, after);
  }

  @Override
  public ChatMessage send(Long roomId, Long senderId, String content) {
    ChatMessage m = ChatMessage.builder()
        .chatRoomId(roomId)
        .senderId(senderId)
        .content(content)
        .build();
    ChatMessage saved = msgRepo.save(m);
    roomRepo.touchModDate(roomId);
    return saved;
  }

  @Transactional(readOnly = true)
  @Override
  public int countRooms(Long myUserId) {
    if (myUserId == null) return 0;
    return (int) roomRepo.countBySellerIdOrBuyerId(myUserId, myUserId);
  }


  @Override
  public void deleteRoom(Long roomId, Long requesterId) {
    roomRepo.findIfParticipant(roomId, requesterId)
        .orElseThrow(() -> new IllegalStateException("No permission"));
    msgRepo.deleteByChatRoomId(roomId);
    roomRepo.deleteById(roomId);
  }
}
