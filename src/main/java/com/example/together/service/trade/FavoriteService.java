package com.example.together.service.trade;

import com.example.together.domain.Favorite;

import java.util.List;

public interface FavoriteService {
  long count(Long tradeId);
  boolean isLiked(Long tradeId, Long userId);
  boolean toggle(Long tradeId, Long userId);

  List<Favorite> listMine(Long userPk);
  List<Favorite> listMineByLoginId(String loginId);
}
