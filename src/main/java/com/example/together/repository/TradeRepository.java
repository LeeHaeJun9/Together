package com.example.together.repository;

import com.example.together.domain.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface TradeRepository extends JpaRepository<Trade, Long> {

  Page<Trade> findByTitleContainingIgnoreCase(String title, Pageable pageable);

  }