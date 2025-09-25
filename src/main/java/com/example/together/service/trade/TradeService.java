package com.example.together.service.trade;

import com.example.together.domain.Trade;
import com.example.together.domain.TradeCategory;

import java.util.List;

public interface TradeService {
  Trade save(Trade trade);
  Trade find(Long id);
  void remove(Long id);

  List<Trade> list();                               // 최신순
  List<Trade> listByCategory(TradeCategory category);
}
