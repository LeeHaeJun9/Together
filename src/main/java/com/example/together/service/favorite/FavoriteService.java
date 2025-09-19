package com.example.together.service.favorite;


public interface FavoriteService {
  boolean toggle(Long tradeId, String userId);
  long count(Long tradeId);
  boolean isFavorited(Long tradeId, String userId);
}


