package com.example.together.repository;

import com.example.together.domain.TradeImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TradeImageRepository extends JpaRepository<TradeImage,Long> {

  // Trade의 이미지 목록을 정렬
  @Query("""
        select image
        from TradeImage image
        where image.trade.id = :tradeId
        order by image.sortOrder asc, image.id asc
    """)
  List<TradeImage> findImages(@Param("tradeId") Long tradeId);


}
