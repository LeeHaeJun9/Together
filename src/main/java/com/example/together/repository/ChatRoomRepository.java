package com.example.together.repository;

import com.example.together.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

  Optional<ChatRoom> findTopByTradeIdAndBuyerIdOrderByIdDesc(Long tradeId, Long buyerId);

  List<ChatRoom> findBySellerIdOrBuyerIdOrderByModDateDesc(Long sellerId, Long buyerId);

  @Query("select r from ChatRoom r where r.id = :roomId and (r.sellerId = :userId or r.buyerId = :userId)")
  Optional<ChatRoom> findIfParticipant(Long roomId, Long userId);

  @Modifying
  @Query("update ChatRoom r set r.modDate = CURRENT_TIMESTAMP where r.id = :roomId")
  int touchModDate(Long roomId);

  long countBySellerIdOrBuyerId(Long sellerId, Long buyerId);

}
