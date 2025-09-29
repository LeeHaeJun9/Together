package com.example.together.repository.search;

import com.example.together.domain.QTrade;
import com.example.together.domain.Trade;
import com.example.together.domain.TradeCategory;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // 💡 필수! 빈으로 등록
public class TradeSearchImpl extends QuerydslRepositorySupport implements TradeSearch {

    public TradeSearchImpl() {
        super(Trade.class);
    }

    @Override
    public Page<Trade> findByCategoryAndSearch(TradeCategory tradeCategory, String type, String keyword, Pageable pageable) {
        QTrade trade = QTrade.trade;

        JPQLQuery<Trade> query = from(trade);

        // 1. 카테고리 조건
        // ALL 카테고리가 아닐 때만 조건을 적용합니다. (CategoryService의 mapLabelToEnum에서 ALL은 null로 반환될 수 있음)
        if (tradeCategory != null) {
            query.where(trade.category.eq(tradeCategory));
        }

        // 2. 검색 조건 (Trade는 title/content/writer를 검색한다고 가정)
        if (type != null && keyword != null && !keyword.isEmpty()) {
            BooleanBuilder conditionBuilder = new BooleanBuilder();
            String searchKeyword = "%" + keyword.toLowerCase() + "%";

            // 제목(t) + 작성자(w) 검색 (tcw)
            if (type.contains("tw")) {
                conditionBuilder.or(trade.title.toLowerCase().like(searchKeyword));
                conditionBuilder.or(trade.description.toLowerCase().like(searchKeyword));
                conditionBuilder.or(trade.sellerNickname.toLowerCase().like(searchKeyword)); // writer 필드 가정
            }
            // 제목만 검색 (t)
            else if (type.contains("t")) {
                conditionBuilder.or(trade.title.toLowerCase().like(searchKeyword));
            }
            // 작성자만 검색 (w)
            else if (type.contains("w")) {
                conditionBuilder.or(trade.sellerNickname.toLowerCase().like(searchKeyword)); // writer 필드 가정
            }

            query.where(conditionBuilder);
        }

        this.getQuerydsl().applyPagination(pageable, query);

        List<Trade> trades = query.fetch();
        long totalCount = query.fetchCount();

        return new PageImpl<>(trades, pageable, totalCount);
    }
}