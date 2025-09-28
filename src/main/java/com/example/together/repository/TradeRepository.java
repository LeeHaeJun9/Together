package com.example.together.repository;

import com.example.together.domain.Trade;
import com.example.together.domain.TradeCategory;
import com.example.together.repository.search.TradeSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long>, TradeSearch {

  // 내가 작성한 글 (정렬 포함)
  List<Trade> findBySellerUserId(Long sellerUserId, Sort sort);
  List<Trade> findBySellerUserIdAndCategory(Long sellerUserId, TradeCategory category, Sort sort);

  long countBySellerUserId(Long sellerUserId);

  // 카테고리별 페이지 조회/목록
  Page<Trade> findByCategory(TradeCategory category, Pageable pageable);
  List<Trade> findByCategory(TradeCategory category, Sort sort);

  // 간단 검색 (정렬 포함)
  List<Trade> findByTitleContainingIgnoreCase(String title, Sort sort);
  List<Trade> findByDescriptionContainingIgnoreCase(String description, Sort sort);
  List<Trade> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description, Sort sort);

  // 카테고리 + 검색 조합
  List<Trade> findByCategoryAndTitleContainingIgnoreCase(TradeCategory category, String title, Sort sort);
  List<Trade> findByCategoryAndDescriptionContainingIgnoreCase(TradeCategory category, String desc, Sort sort);
  List<Trade> findByCategoryAndTitleContainingIgnoreCaseOrCategoryAndDescriptionContainingIgnoreCase(
      TradeCategory c1, String title, TradeCategory c2, String desc, Sort sort
  );


  List<Trade> findBySellerUserIdAndStatusIn(Long sellerUserId, Collection<String> statuses, Sort sort);
  long countBySellerUserIdAndStatusIn(Long sellerUserId, Collection<String> statuses);

  @Query("SELECT t FROM Trade t LEFT JOIN Favorite f ON t.id = f.trade.id " +
          "GROUP BY t " +
          "ORDER BY COUNT(f) DESC, t.regdate DESC")
  List<Trade> findPopularTradesByFavoriteCount(Pageable pageable);
}
