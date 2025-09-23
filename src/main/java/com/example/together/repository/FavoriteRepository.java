package com.example.together.repository;

import com.example.together.domain.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

  boolean existsByTrade_IdAndUser_Id(Long tradeId, Long userId);
  long countByTrade_Id(Long tradeId);
  Optional<Favorite> findByTrade_IdAndUser_Id(Long tradeId, Long userId);

  // PK 기반 조회 (trade 즉시 로딩)
  @Query("select f from Favorite f join fetch f.trade t where f.user.id = :userId order by f.id desc")
  List<Favorite> findAllWithTradeByUserIdOrderByIdDesc(@Param("userId") Long userId);

  // 로그인ID 기반 조회 (레거시 데이터용)
  @Query("select f from Favorite f join fetch f.trade t join f.user u where u.userId = :loginId order by f.id desc")
  List<Favorite> findAllWithTradeByUserLoginIdOrderByIdDesc(@Param("loginId") String loginId);
}
