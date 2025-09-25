package com.example.together.repository;

import com.example.together.domain.TradeImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TradeImageRepository extends JpaRepository<TradeImage, Long> {

  // 특정 거래 이미지 목록(정렬)
  List<TradeImage> findByTrade_IdOrderBySortOrderAsc(Long tradeId);

  // 첫 번째/마지막 이미지 (썸네일 선정 등에 유용)
  Optional<TradeImage> findFirstByTrade_IdOrderBySortOrderAsc(Long tradeId);
  Optional<TradeImage> findFirstByTrade_IdOrderBySortOrderDesc(Long tradeId);

  // 다건 삭제
  void deleteByIdIn(List<Long> ids);

  // 거래별 일괄 삭제
  void deleteByTrade_Id(Long tradeId);

  // 최대 정렬값 (없으면 null)
  @Query("select max(i.sortOrder) from TradeImage i where i.trade.id = :tradeId")
  Optional<Integer> findMaxSortOrderByTradeId(Long tradeId);
}
