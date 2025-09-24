package com.example.together.repository;

import com.example.together.domain.CafeCategory;
import com.example.together.domain.Meeting;
import com.example.together.domain.Visibility;
import com.example.together.repository.search.MeetingSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting,Long>, MeetingSearch {
    Page<Meeting> findByCafeId(Long cafeId, Pageable pageable);

    @Query("SELECT m FROM Meeting m JOIN FETCH m.cafe JOIN FETCH m.organizer WHERE m.cafe.category = :cafeCategory AND m.visibility = :visibility")
    Page<Meeting> findByCafeCategoryAndVisibilityWithCafeAndUser(@Param("cafeCategory") CafeCategory cafeCategory, @Param("visibility") Visibility visibility, Pageable pageable);

    @Query("SELECT m FROM Meeting m JOIN FETCH m.cafe JOIN FETCH m.organizer WHERE m.visibility = :visibility")
    Page<Meeting> findByVisibilityWithCafeAndUser(@Param("visibility") Visibility visibility, Pageable pageable);
    // 필요에 따라 기존 List용 메서드 유지 가능
    List<Meeting> findByCafe_CategoryAndVisibility(CafeCategory cafeCategory, Visibility visibility, PageRequest pageable);

    List<Meeting> findByVisibility(Visibility visibility, PageRequest pageable);

}
