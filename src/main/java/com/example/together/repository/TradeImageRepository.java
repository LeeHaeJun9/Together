package com.example.together.repository;

import com.example.together.domain.TradeImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeImageRepository extends JpaRepository<TradeImage, Long> {
  List<TradeImage> findAllByTradeIdOrderBySortOrderAsc(Long tradeId);
  void deleteByTradeId(Long tradeId);
  int countByTradeId(Long tradeId);
}
