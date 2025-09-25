package com.example.together.repository.search;

import com.example.together.domain.Trade;
import com.example.together.domain.TradeCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TradeSearch {
    Page<Trade> findByCategoryAndSearch(TradeCategory tradeCategory, String type, String keyword, Pageable pageable);
}
