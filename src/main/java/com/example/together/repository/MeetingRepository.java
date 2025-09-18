package com.example.together.repository;

import com.example.together.domain.Meeting;
import com.example.together.repository.search.MeetingSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting,Long>, MeetingSearch {
    Page<Meeting> findByCafeId(Long cafeId, Pageable pageable);
}
