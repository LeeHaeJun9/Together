package com.example.together.repository;

import com.example.together.domain.CafeCategory;
import com.example.together.domain.Meeting;
import com.example.together.domain.Visibility;
import com.example.together.repository.search.MeetingSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting,Long>, MeetingSearch {
    Page<Meeting> findByCafeId(Long cafeId, Pageable pageable);

    List<Meeting> findByCafe_CategoryAndVisibility(CafeCategory cafeCategory, Visibility visibility);

    List<Meeting> findByVisibility(Visibility visibility);

    // 주최한 모임 리스트
    @Query("SELECT m FROM Meeting m WHERE m.organizer.id = :userId")
    List<Meeting> findMeetingsHostedByUserId(Long userId);

}
