package com.example.together.service.trade;

import com.example.together.domain.Favorite;
import com.example.together.repository.FavoriteRepository;

import java.util.List;

public interface FavoriteService {
  long count(Long tradeId);
  boolean isLiked(Long tradeId, Long userId);
  boolean toggle(Long tradeId, Long userId);

  List<Favorite> listMine(Long userPk);
  List<Favorite> listMineByLoginId(String loginId);

  // 추가: trades 집합에 대한 찜수 벌크 집계
  List<FavoriteRepository.CountProj> countByTradeIds(List<Long> tradeIds);

  // ★ 추가: 즐겨찾기 목록 프로젝션(엔티티 직접 접근 금지용)
  List<FavoriteRepository.FavoriteItemView> listItemViewsByUser(Long userId);
}
