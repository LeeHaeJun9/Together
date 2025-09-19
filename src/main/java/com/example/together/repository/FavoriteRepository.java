package com.example.together.repository;

import com.example.together.domain.Favorite;
import com.example.together.domain.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

  // trade_id 기준 개수
  long countByTrade_Id(Long tradeId);

  // user_id + trade_id 조합 존재 여부
  boolean existsByTrade_IdAndUser_UserId(Long tradeId, String userId);

  // user_id + trade_id로 단건 조회
  Optional<Favorite> findByTrade_IdAndUser_UserId(Long tradeId, String userId);

  // user_id + trade_id로 삭제
  long deleteByTrade_IdAndUser_UserId(Long tradeId, String userId);
}

