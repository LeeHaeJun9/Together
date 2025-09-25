package com.example.together.repository;

import com.example.together.domain.Favorite;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

  long countByTrade_Id(Long tradeId);

  boolean existsByTrade_IdAndUser_Id(Long tradeId, Long userId);

  @EntityGraph(attributePaths = {"trade"})
  Optional<Favorite> findByTrade_IdAndUser_Id(Long tradeId, Long userId);

  @EntityGraph(attributePaths = {"trade"})
  List<Favorite> findByUser_Id(Long userId);
}
