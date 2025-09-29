package com.example.together.repository.search;

import com.example.together.domain.CafeCategory;
import com.example.together.domain.Meeting;
import com.example.together.domain.QMeeting;
import com.example.together.domain.Visibility;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.together.domain.QUser.user;

@Repository
public class MeetingSearchImpl extends QuerydslRepositorySupport implements MeetingSearch {
    public MeetingSearchImpl() {
        super(Meeting.class);
    }

    @Override
    public Page<Meeting> search1(Pageable pageable) {
        QMeeting meeting = QMeeting.meeting;
        JPQLQuery<Meeting> query = from(meeting);

        query.where(meeting.title.contains("1"));

        this.getQuerydsl().applyPagination(pageable, query);

        List<Meeting> list = query.fetch();
        long count = query.fetchCount();

        return null;
    }

    @Override
    public Page<Meeting> searchAll(String[] types, String keyword, Pageable pageable) {
        QMeeting meeting = QMeeting.meeting;
        JPQLQuery<Meeting> query = from(meeting);

        query.leftJoin(meeting.organizer, user).fetchJoin();

        if((types != null && types.length > 0) && keyword != null) {
            BooleanBuilder booleanBuilder = new BooleanBuilder();

            for (String type: types) {
                switch (type) {
                    case "t":
                        booleanBuilder.or(meeting.title.contains(keyword));
                        break;
                    case "c":
                        booleanBuilder.or(meeting.content.contains(keyword));
                        break;
                }
            }

            query.where(booleanBuilder);
        }
        query.where(meeting.id.gt(0L));

        this.getQuerydsl().applyPagination(pageable, query);
        List<Meeting> list = query.fetch();
        long count = query.fetchCount();

        return new PageImpl<>(list, pageable,count);
    }

    @Override
    public Page<Meeting> findByVisibilityAndSearch(Visibility visibility, String type, String keyword, Pageable pageable) {
        return findByConditionAndSearch(null, visibility, type, keyword, pageable);
    }

    @Override
    public Page<Meeting> findByCategoryAndVisibilityAndSearch(CafeCategory cafeCategory, Visibility visibility, String type, String keyword, Pageable pageable) {
        return findByConditionAndSearch(cafeCategory, visibility, type, keyword, pageable);
    }

    // 실제 검색 로직 (Meeting은 title/content를 검색한다고 가정)
    private Page<Meeting> findByConditionAndSearch(CafeCategory category, Visibility visibility, String type, String keyword, Pageable pageable) {
        QMeeting meeting = QMeeting.meeting;

        JPQLQuery<Meeting> query = from(meeting);
        query.where(meeting.visibility.eq(visibility));

        // 1. 카테고리 조건
        if (category != null) {
            // Meeting 엔티티가 CafeCategory와 연결되어 있어야 합니다.
            // (예: Meeting 엔티티 안에 cafe.category 필드가 있다고 가정)
            // 실제 엔티티 구조에 따라 이 부분은 수정이 필요할 수 있습니다.
            query.where(meeting.cafe.category.eq(category));
        }

        // 2. 검색 조건
        if (type != null && keyword != null && !keyword.isEmpty()) {
            BooleanBuilder conditionBuilder = new BooleanBuilder();
            String searchKeyword = "%" + keyword.toLowerCase() + "%";

            // 제목(t) + 내용(c) 검색 (tc)
            if (type.contains("tc")) {
                conditionBuilder.or(meeting.title.toLowerCase().like(searchKeyword));
                conditionBuilder.or(meeting.content.toLowerCase().like(searchKeyword));
            }
            // 제목만 검색 (t)
            else if (type.contains("t")) {
                conditionBuilder.or(meeting.title.toLowerCase().like(searchKeyword));
            }
            // 내용만 검색 (c)
            else if (type.contains("c")) {
                conditionBuilder.or(meeting.content.toLowerCase().like(searchKeyword));
            }

            query.where(conditionBuilder);
        }

        this.getQuerydsl().applyPagination(pageable, query);

        List<Meeting> meetings = query.fetch();
        long totalCount = query.fetchCount();

        return new PageImpl<>(meetings, pageable, totalCount);
    }
}
