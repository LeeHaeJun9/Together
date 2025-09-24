package com.example.together.repository;

import com.example.together.domain.Trade;
import com.example.together.domain.TradeCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByCategory(TradeCategory category);

    Page<Trade> findByCategory(TradeCategory category, Pageable pageable);
}