package com.example.together.dto.cafe;

import com.example.together.domain.Membership;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MyJoinedCafesDTO {
    private List<Membership> memberships;
    private long totalCafes;
    private long totalJoinedCafes;
    private long totalOwnedCafes;

    // 💡 선택된 카테고리 1 (이름과 개수)
    private String selectedCategory1Name;
    private long selectedCategory1Count;

    // 💡 최근 7일 내 가입한 카페 수 추가
    private long recentlyJoinedCount;
}