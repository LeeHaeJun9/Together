package com.example.together.repository.search;

import com.example.together.domain.CafeCategory;
import com.example.together.domain.Meeting;
import com.example.together.domain.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MeetingSearch {
    Page<Meeting> search1(Pageable pageable);
    Page<Meeting> searchAll(String[] types, String keyword, Pageable pageable);

    Page<Meeting> findByVisibilityAndSearch(Visibility visibility, String type, String keyword, Pageable pageable);

    Page<Meeting> findByCategoryAndVisibilityAndSearch(CafeCategory cafeCategory, Visibility visibility, String type, String keyword, Pageable pageable);
}
