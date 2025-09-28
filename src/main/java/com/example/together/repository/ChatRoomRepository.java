package com.example.together.repository;

import com.example.together.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

  Optional<ChatRoom> findTopByTradeIdAndBuyerIdOrderByIdDesc(Long tradeId, Long buyerId);

  List<ChatRoom> findBySellerIdOrBuyerIdOrderByModDateDesc(Long sellerId, Long buyerId);

  @Query("select r from ChatRoom r where r.id = :roomId and (r.sellerId = :userId or r.buyerId = :userId)")
  Optional<ChatRoom> findIfParticipant(@Param("roomId") Long roomId, @Param("userId") Long userId);

  @Modifying
  @Query("update ChatRoom r set r.modDate = CURRENT_TIMESTAMP where r.id = :roomId")
  int touchModDate(@Param("roomId") Long roomId);

  long countBySellerIdOrBuyerId(Long sellerId, Long buyerId);

  // 구매자가 '판매완료' 상태의 거래에 참여한 tradeIds
  @Query("""
         select cr.tradeId
         from ChatRoom cr
         where cr.buyerId = :userId
           and exists (
               select 1 from Trade t
               where t.id = cr.tradeId
                 and t.status in :soldStatuses
           )
         """)
  List<Long> findCompletedTradeIdsByBuyer(@Param("userId") Long userId,
                                          @Param("soldStatuses") Collection<String> soldStatuses);

  // 구매 완료 개수
  @Query("""
         select count(cr)
         from ChatRoom cr
         where cr.buyerId = :userId
           and exists (
               select 1 from Trade t
               where t.id = cr.tradeId
                 and t.status in :soldStatuses
           )
         """)
  long countCompletedByBuyer(@Param("userId") Long userId,
                             @Param("soldStatuses") Collection<String> soldStatuses);

  // 판매 완료 개수(판매자 기준)
  @Query("""
         select count(cr)
         from ChatRoom cr
         where cr.sellerId = :userId
           and exists (
               select 1 from Trade t
               where t.id = cr.tradeId
                 and t.status in :soldStatuses
           )
         """)
  long countCompletedBySeller(@Param("userId") Long userId,
                              @Param("soldStatuses") Collection<String> soldStatuses);
}
