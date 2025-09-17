// 3. AdminUserRepository.java - 사용자 데이터 처리
package com.example.together.repository.manager;

import com.example.together.dto.manager.AdminUserDTO;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdminUserRepository {

    // 전체 사용자 목록
    List<AdminUserDTO> findAllUsers();

    // 사용자 검색 (이름, 이메일, 닉네임)
    List<AdminUserDTO> searchUsers(String keyword);

    // 사용자 ID로 조회
    AdminUserDTO findByUserId(Long userId);

    // 사용자 상태 변경
    int updateUserStatus(Long userId, String status);

    // 사용자 경고 부여
    int insertUserWarning(Long userId, String reason);

    // 사용자 경고 횟수 조회
    int getUserWarningCount(Long userId);

    // 상태별 사용자 목록
    List<AdminUserDTO> findByStatus(String status);

    // 사용자 강제 삭제
    int deleteUser(Long userId);

    // 신고된 사용자 목록
    List<AdminUserDTO> findReportedUsers();

    // 전체 사용자 수 조회
    int getTotalUserCount();

    // 오늘 가입한 사용자 수
    int getTodayJoinUserCount();

    // 전체 게시글 수 조회
    int getTotalPostCount();

    // 신고 건수 조회
    int getReportCount();
}

