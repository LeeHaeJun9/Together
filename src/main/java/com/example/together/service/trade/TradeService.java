package com.example.together.service.trade;


import com.example.together.domain.Trade;

import java.util.List;

public interface TradeService {
  Trade save(Trade trade);
  Trade find(Long id);
  List<Trade> list();
  void remove(Long id);
}