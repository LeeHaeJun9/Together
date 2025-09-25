package com.example.together.repository.search;

import com.example.together.domain.Cafe;
import com.example.together.domain.CafeCategory;
import com.example.together.domain.QCafe;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CafeSearchImpl extends QuerydslRepositorySupport implements CafeSearch {

    public CafeSearchImpl() {
        // 부모 클래스에 엔티티 클래스 정보를 전달합니다.
        super(Cafe.class);
    }

    // ----------------------------------------------------
    // 전체 카페 검색 (카테고리 조건 없음)
    // ----------------------------------------------------
    @Override
    public Page<Cafe> findBySearch(String type, String keyword, Pageable pageable) {
        // 내부 구현 메소드를 호출합니다. 카테고리는 null로 전달합니다.
        return findByConditionAndSearch(null, type, keyword, pageable);
    }

    // ----------------------------------------------------
    // 특정 카테고리 내 카페 검색
    // ----------------------------------------------------
    @Override
    public Page<Cafe> findByCategoryAndSearch(CafeCategory cafeCategory, String type, String keyword, Pageable pageable) {
        // 내부 구현 메소드를 호출합니다.
        return findByConditionAndSearch(cafeCategory, type, keyword, pageable);
    }


    // ----------------------------------------------------
    // 실제 검색 로직을 담당하는 핵심 프라이빗 메소드
    // ----------------------------------------------------
    private Page<Cafe> findByConditionAndSearch(CafeCategory category, String type, String keyword, Pageable pageable) {
        // 💡 Querydsl Q클래스 인스턴스 (컴파일 시 자동 생성됨)
        QCafe cafe = QCafe.cafe;

        // 쿼리 시작
        JPQLQuery<Cafe> query = from(cafe);

        // 1. 카테고리 조건 (ALL 카테고리가 아닐 때)
        if (category != null) {
            query.where(cafe.category.eq(category));
        }

        // 2. 검색 조건 (Type과 Keyword가 모두 있을 때만 적용)
        if (type != null && keyword != null && !keyword.isEmpty()) {
            BooleanBuilder conditionBuilder = new BooleanBuilder();

            // 키워드 대소문자 무시 검색을 위해 %keyword% 포맷을 사용합니다.
            String searchKeyword = "%" + keyword.toLowerCase() + "%";

            // 이름 + 설명 검색 (nd)
            if (type.contains("nd")) {
                conditionBuilder.or(cafe.name.toLowerCase().like(searchKeyword));
                conditionBuilder.or(cafe.description.toLowerCase().like(searchKeyword));
            }
            // 이름만 검색 (n)
            else if (type.contains("n")) {
                conditionBuilder.or(cafe.name.toLowerCase().like(searchKeyword));
            }
            // 설명만 검색 (d)
            else if (type.contains("d")) {
                conditionBuilder.or(cafe.description.toLowerCase().like(searchKeyword));
            }

            query.where(conditionBuilder);
        }

        // 3. 페이징 처리 및 정렬
        // Order by regDate (정렬 기준은 Service에서 pageable에 이미 적용됨)
        this.getQuerydsl().applyPagination(pageable, query);

        List<Cafe> cafes = query.fetch();
        long totalCount = query.fetchCount();

        // Page 객체로 반환
        return new PageImpl<>(cafes, pageable, totalCount);
    }
}