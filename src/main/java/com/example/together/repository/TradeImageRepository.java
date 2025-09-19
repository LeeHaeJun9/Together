package com.example.together.repository;



import com.example.together.domain.TradeImage;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface TradeImageRepository extends JpaRepository<TradeImage, Long> {
  Optional<TradeImage> findFirstByTrade_IdOrderBySortOrderAsc(Long tradeId);

}

