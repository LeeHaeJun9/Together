package com.example.together.service.trade;

import com.example.together.domain.Favorite;
import com.example.together.domain.User;
import com.example.together.repository.FavoriteRepository;
import com.example.together.repository.TradeRepository;
import com.example.together.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteServiceImpl implements FavoriteService {

  private final FavoriteRepository favoriteRepository;
  private final UserRepository userRepository;
  private final TradeRepository tradeRepository;

  @Override
  @Transactional(readOnly = true)
  public long count(Long tradeId) {
    return favoriteRepository.countByTrade_Id(tradeId);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isLiked(Long tradeId, Long userId) {
    return favoriteRepository.existsByTrade_IdAndUser_Id(tradeId, userId);
  }

  @Override
  public boolean toggle(Long tradeId, Long userId) {
    var existing = favoriteRepository.findByTrade_IdAndUser_Id(tradeId, userId).orElse(null);
    if (existing != null) {
      favoriteRepository.delete(existing);
      return false; // 찜 해제
    }
    // 연관관계로 설정해서 저장
    var fav = new Favorite();
    fav.setTrade(tradeRepository.getReferenceById(tradeId));
    fav.setUser(userRepository.getReferenceById(userId));
    favoriteRepository.save(fav);
    return true; // 찜 추가
  }

  @Override
  @Transactional(readOnly = true)
  public List<Favorite> listMine(Long userPk) {
    if (userPk == null) return Collections.emptyList();
    return favoriteRepository.findByUser_Id(userPk);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Favorite> listMineByLoginId(String loginId) {
    if (loginId == null || loginId.isBlank()) return Collections.emptyList();
    return userRepository.findByUserId(loginId)
        .map(User::getId)
        .map(favoriteRepository::findByUser_Id)
        .orElseGet(Collections::emptyList);
  }
}
