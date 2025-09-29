package com.example.together.repository;

import com.example.together.domain.TradeImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TradeImageRepository extends JpaRepository<TradeImage, Long> {

  // 특정 거래 이미지 목록(정렬)
  List<TradeImage> findByTrade_IdOrderBySortOrderAsc(Long tradeId);

  // 첫 번째/마지막 이미지 (개별 조회용)
  Optional<TradeImage> findFirstByTrade_IdOrderBySortOrderAsc(Long tradeId);
  Optional<TradeImage> findFirstByTrade_IdOrderBySortOrderDesc(Long tradeId);

  // 다건 삭제
  void deleteByIdIn(List<Long> ids);

  // 거래별 일괄 삭제
  void deleteByTrade_Id(Long tradeId);

  // 최대 정렬값 (없으면 null)
  @Query("select max(i.sortOrder) from TradeImage i where i.trade.id = :tradeId")
  Optional<Integer> findMaxSortOrderByTradeId(Long tradeId);

  // ===== 썸네일 벌크 조회 (trades 집합에 대해 각 trade의 첫 이미지 URL을 한 번에) =====
  interface ThumbProj {
    Long getTradeId();
    String getImageUrl();
  }

  @Query(value = """
      SELECT ti.trade_id AS tradeId, ti.image_url AS imageUrl
      FROM trade_image ti
      JOIN (
          SELECT trade_id, MIN(sort_order) AS min_sort
          FROM trade_image
          WHERE trade_id IN (:tradeIds)
          GROUP BY trade_id
      ) x ON x.trade_id = ti.trade_id AND x.min_sort = ti.sort_order
      """, nativeQuery = true)
  List<ThumbProj> findFirstImageUrlsByTradeIds(@Param("tradeIds") List<Long> tradeIds);
}
