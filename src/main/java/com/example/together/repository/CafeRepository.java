package com.example.together.repository;

import com.example.together.domain.Cafe;
import com.example.together.domain.CafeCategory;
import com.example.together.repository.search.CafeSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CafeRepository extends JpaRepository<Cafe,Long>, CafeSearch {
    Optional<Object> findByName(String name);

    List<Cafe> findByCategoryAndIdNot(CafeCategory category, Long id, Pageable pageable);

    List<Cafe> findByOrderByMemberCountDesc(Pageable pageable);

    List<Cafe> findByCategory(CafeCategory category);

    @Query("SELECT c FROM Cafe c WHERE " +
            "(:keyword IS NULL OR TRIM(:keyword) = '' OR " +
            "c.name LIKE %:keyword% OR " +
            "c.description LIKE %:keyword%) AND " +
            "(:categoryEnumList IS NULL OR c.category IN :categoryEnumList)")
    Page<Cafe> searchAll(
            @Param("keyword") String keyword,
            @Param("categoryEnumList") List<CafeCategory> categoryEnumList,
            Pageable pageable);

    @Query("SELECT c FROM Cafe c " +
            "WHERE c.category = :category AND " +
            "(:keyword IS NULL OR c.name LIKE %:keyword% OR c.description LIKE %:keyword%)")
    Page<Cafe> searchByCategoryAndKeyword(
            @Param("category") CafeCategory cafeCategory,
            @Param("keyword") String cafeKeyword,
            Pageable pageable);


    Page<Cafe> findPageByCategory(CafeCategory cafeCategory, Pageable pageable);
}
