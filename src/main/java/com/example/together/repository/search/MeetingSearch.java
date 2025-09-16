package com.example.together.repository.search;

import com.example.together.domain.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MeetingSearch {
    Page<Meeting> search1(Pageable pageable);
    Page<Meeting> searchAll(String[] types, String keyword, Pageable pageable);
}
