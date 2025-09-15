package com.example.together.repository;

import com.example.together.domain.Trade;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.Optional;

public interface TradeRepository extends JpaRepository<Trade,Long> {
  @EntityGraph(attributePaths = "images")
  @Query("select t from Trade t where t.id = :id")
  Optional<Trade> findByIdWithImages(Long id);
}
