package com.example.together.repository;

import com.example.together.domain.TradeImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface TradeImageRepository extends JpaRepository<TradeImage, Long> {

  @Query("""
        select image
        from TradeImage image
        where image.trade.id = :tradeId
        order by image.sortOrder asc, image.id asc
    """)
  List<TradeImage> findImages(@Param("tradeId") Long tradeId);

  void deleteByTrade_Id(Long tradeId);
}
