package com.example.together.repository;

import com.example.together.domain.Cafe;
import com.example.together.domain.CafeCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CafeRepository extends JpaRepository<Cafe,Long> {
    Optional<Object> findByName(String name);

    List<Cafe> findByCategoryAndIdNot(CafeCategory category, Long id, Pageable pageable);

    List<Cafe> findByOrderByMemberCountDesc(Pageable pageable);

    List<Cafe> findByCategory(CafeCategory category);
}
