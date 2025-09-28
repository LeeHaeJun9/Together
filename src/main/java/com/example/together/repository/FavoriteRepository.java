package com.example.together.repository;

import com.example.together.domain.Favorite;
import com.example.together.domain.TradeCategory;
import com.example.together.domain.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

  long countByTrade_Id(Long tradeId);
  boolean existsByTrade_IdAndUser_Id(Long tradeId, Long userId);
  Optional<Favorite> findByTrade_IdAndUser_Id(Long tradeId, Long userId);
  List<Favorite> findByUser_Id(Long userId);

  // ===== trades 집합에 대한 찜 개수 벌크 집계 =====
  interface CountProj {
    Long getTradeId();
    Long getCnt();
  }

  @Query("""
      select f.trade.id as tradeId, count(f) as cnt
      from Favorite f
      where f.trade.id in :tradeIds
      group by f.trade.id
      """)
  List<CountProj> countByTradeIds(@Param("tradeIds") List<Long> tradeIds);

  // ===== 즐겨찾기 목록용 인터페이스 프로젝션 (DTO 파일 없이 사용) =====
  public interface FavoriteItemView {
    Long getFavoriteId();
    Long getTradeId();
    String getTitle();
    String getStatus();
    TradeCategory getCategory();
    Long getPrice();
    String getSellerNickname();
    /** Trade.thumbnail (엔티티에 저장된 썸네일, 없을 수 있음) */
    String getThumbnail();
  }

  @Query("""
      select 
        f.id as favoriteId,
        t.id as tradeId,
        t.title as title,
        t.status as status,
        t.category as category,
        t.price as price,
        t.sellerNickname as sellerNickname,
        t.thumbnail as thumbnail
      from Favorite f
      join f.trade t
      where f.user.id = :uid
      order by f.id desc
      """)
  List<FavoriteItemView> findItemViewsByUserId(@Param("uid") Long userId);
}
