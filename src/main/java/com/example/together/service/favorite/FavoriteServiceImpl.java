package com.example.together.service.favorite;

import com.example.together.domain.Favorite;
import com.example.together.domain.Trade;
import com.example.together.domain.User;
import com.example.together.repository.FavoriteRepository;
import com.example.together.repository.TradeRepository;
import com.example.together.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

  private final FavoriteRepository favoriteRepository;
  private final TradeRepository tradeRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public boolean toggle(Long tradeId, String userId) {
    return favoriteRepository.findByTrade_IdAndUser_UserId(tradeId, userId)
        .map(f -> {
          favoriteRepository.delete(f);
          return false; // 취소됨
        })
        .orElseGet(() -> {
          Trade trade = tradeRepository.getReferenceById(tradeId);
          User user = userRepository.findByUserId(userId).orElseThrow();
          Favorite f = new Favorite();
          f.setTrade(trade);
          f.setUser(user);
          favoriteRepository.save(f);
          return true; // 추가됨
        });
  }

  @Override
  public long count(Long tradeId) {
    return favoriteRepository.countByTrade_Id(tradeId);
  }

  @Override
  public boolean isFavorited(Long tradeId, String userId) {
    if (userId == null) return false;
    return favoriteRepository.existsByTrade_IdAndUser_UserId(tradeId, userId);
  }
}