package com.example.together.service.trade;

import com.example.together.domain.Trade;
import com.example.together.dto.trade.TradeDTO;
import com.example.together.dto.trade.TradeSaveRequest;
import com.example.together.dto.trade.TradeUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;


public interface TradeService {
  Trade create(Trade trade);
  void update(Long id, Trade updated);
  void delete(Long id);
  Optional<Trade> findOne(Long id);
  Page<Trade> findList(Pageable pageable, String q);
}
