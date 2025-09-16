package com.example.together.repository.search;

import com.example.together.domain.Meeting;
import com.example.together.domain.QMeeting;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

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
}
