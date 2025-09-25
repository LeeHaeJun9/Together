package com.example.together.repository;

import com.example.together.domain.Trade;
import com.example.together.domain.TradeCategory;
import com.example.together.repository.search.TradeSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long>, TradeSearch {

  // 카테고리별 페이지 조회 (CategoryService에서 사용)
  Page<Trade> findByCategory(TradeCategory category, Pageable pageable);

  // 카테고리별 목록 조회 + 정렬 (TradeServiceImpl에서 사용)
  List<Trade> findByCategory(TradeCategory category, Sort sort);
}
