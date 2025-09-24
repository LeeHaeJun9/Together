package com.example.together.repository;

import com.example.together.domain.TradeImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TradeImageRepository extends JpaRepository<TradeImage, Long> {

  List<TradeImage> findAllByTrade_IdOrderBySortOrderAsc(Long tradeId);

  void deleteByTrade_Id(Long tradeId);

  int countByTrade_Id(Long tradeId);

  Optional<TradeImage> findFirstByTrade_IdOrderBySortOrderAsc(Long tradeId);
}
