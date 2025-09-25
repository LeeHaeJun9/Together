package com.example.together.repository.search;

import com.example.together.domain.Cafe;
import com.example.together.domain.CafeCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CafeSearch {
    // Service에서 호출하는 메소드를 이곳에 정의합니다.
    Page<Cafe> findBySearch(String type, String keyword, Pageable pageable);

    Page<Cafe> findByCategoryAndSearch(CafeCategory cafeCategory, String type, String keyword, Pageable pageable);
}