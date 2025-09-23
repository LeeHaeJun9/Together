
package com.example.together.service.trade;

import com.example.together.domain.Favorite;
import com.example.together.domain.Trade;
import com.example.together.domain.User;
import com.example.together.repository.FavoriteRepository;
import com.example.together.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteServiceImpl implements FavoriteService {

  private final FavoriteRepository favoriteRepository;
  private final TradeRepository tradeRepository;

  @Override @Transactional(readOnly = true)
  public boolean isLiked(Long tradeId, Long userPk) {
    return favoriteRepository.existsByTrade_IdAndUser_Id(tradeId, userPk);
  }

  @Override @Transactional(readOnly = true)
  public long count(Long tradeId) {
    return favoriteRepository.countByTrade_Id(tradeId);
  }

  @Override
  public boolean toggle(Long tradeId, Long userPk) {
    return favoriteRepository.findByTrade_IdAndUser_Id(tradeId, userPk)
        .map(existing -> { favoriteRepository.delete(existing); return false; })
        .orElseGet(() -> {
          Trade trade = tradeRepository.findById(tradeId)
              .orElseThrow(() -> new IllegalArgumentException("Trade not found: " + tradeId));
          Favorite fav = Favorite.builder()
              .trade(trade)
              .user(User.builder().id(userPk).build())
              .build();
          favoriteRepository.save(fav);
          return true;
        });
  }

  @Override @Transactional(readOnly = true)
  public List<Favorite> listMine(Long userPk) {
    return favoriteRepository.findAllWithTradeByUserIdOrderByIdDesc(userPk);
  }

  @Override @Transactional(readOnly = true)
  public List<Favorite> listMineByLoginId(String loginId) {
    if (loginId == null || loginId.isBlank()) return List.of();
    return favoriteRepository.findAllWithTradeByUserLoginIdOrderByIdDesc(loginId);
  }
}
