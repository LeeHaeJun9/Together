package com.example.together.service.trade;

import com.example.together.domain.Trade;
import com.example.together.domain.TradeCategory;
import com.example.together.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {

  private final TradeRepository tradeRepository;

  @Override
  public Trade save(Trade trade) {
    return tradeRepository.save(trade);
  }

  @Override
  public Trade find(Long id) {
    return tradeRepository.findById(id).orElse(null);
  }

  @Override
  public void remove(Long id) {
    tradeRepository.deleteById(id);
  }

  @Override
  public List<Trade> list() {
    // 최신순: id 대신 regDate가 있다면 "regDate"로 바꾸세요.
    return tradeRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
  }

  @Override
  public List<Trade> listByCategory(TradeCategory category) {
    // 최신순 정렬. regDate 필드가 있다면 "regDate"로 교체 가능
    return tradeRepository.findByCategory(category, Sort.by(Sort.Direction.DESC, "id"));
  }

  @Override
  public List<Trade> getPopularTradesByFavoriteCount(int count) {
    Pageable pageable = PageRequest.of(0, count);

    return tradeRepository.findPopularTradesByFavoriteCount(pageable);
  }
}
