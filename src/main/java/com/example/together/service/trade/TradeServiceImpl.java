package com.example.together.service.trade;

import com.example.together.domain.*;
import com.example.together.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {

  private final TradeRepository tradeRepository;

  @Override
  @Transactional
  public Trade create(Trade trade) {
    return tradeRepository.save(trade);
  }

  @Override
  @Transactional
  public void update(Long id, Trade updated) {
    tradeRepository.save(updated);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    tradeRepository.deleteById(id);
  }

  @Override
  public Optional<Trade> findOne(Long id) {
    return tradeRepository.findById(id);
  }

  @Override
  public Page<Trade> findList(Pageable pageable, String q) {
    if (q == null || q.isBlank()) {
      return tradeRepository.findAll(pageable);
    }
    return tradeRepository.findByTitleContainingIgnoreCase(q, pageable);
  }
}

