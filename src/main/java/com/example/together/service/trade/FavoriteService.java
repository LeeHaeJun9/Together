package com.example.together.service.trade;

import com.example.together.domain.Favorite;
import java.util.List;

public interface FavoriteService {

  boolean isLiked(Long tradeId, Long userPk);
  long count(Long tradeId);
  boolean toggle(Long tradeId, Long userPk);

  // PK 기반
  List<Favorite> listMine(Long userPk);

  // 레거시 로그인ID 기반(기존에 찜해둔 데이터 복구용)
  List<Favorite> listMineByLoginId(String loginId);
}
