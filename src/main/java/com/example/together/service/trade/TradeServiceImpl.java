package com.example.together.service.trade;


import com.example.together.domain.Trade;
import com.example.together.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TradeServiceImpl implements TradeService {
  private final TradeRepository tradeRepository;


  @Override
  public Trade save(Trade trade) { return tradeRepository.save(trade); }


  @Transactional(readOnly = true)
  public Trade find(Long id) { return tradeRepository.findById(id).orElseThrow(); }


  @Transactional(readOnly = true)
  public List<Trade> list() { return tradeRepository.findAll(Sort.by(Sort.Direction.DESC, "id")); }


  public void remove(Long id) { tradeRepository.deleteById(id); }
}