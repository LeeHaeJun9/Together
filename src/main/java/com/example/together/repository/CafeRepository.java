package com.example.together.repository;

import com.example.together.domain.Cafe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CafeRepository extends JpaRepository<Cafe,Long> {
    Optional<Object> findByName(String name);
}
