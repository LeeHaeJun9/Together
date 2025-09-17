// 4. DashboardRepository.java - 대시보드 통계 데이터
package com.example.together.repository.manager;

import com.example.together.dto.manager.DashBoardDTO;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository {

    // 대시보드 통계 데이터 조회
    DashBoardDTO getDashboardStatistics();

    // 전체 사용자 수
    int getTotalUsers();

    // 전체 카페 수
    int getTotalCafes();

    // 전체 게시글 수
    int getTotalPosts();

    // 오늘 가입한 사용자 수
    int getTodayJoinUsers();

    // 활성 카페 수
    int getActiveCafes();

    // 신고 건수
    int getReportCount();
}
