//package com.example.together.service.manager;
//
//import com.example.together.dto.manager.AdminUserDTO;
//
//import java.util.List;
//
//public interface AdminUserService {
//
//    // 전체 사용자 목록 조회
//    List<AdminUserDTO> getAllUsers();
//
//    // 사용자 검색 (이름, 이메일, 닉네임으로)
//    List<AdminUserDTO> searchUsers(String keyword);
//
//    // 사용자 상세 정보 조회
//    AdminUserDTO getUserById(Long userId);
//
//    // 사용자 상태 변경 (활성/비활성/정지/차단)
//    boolean changeUserStatus(Long userId, String status);
//
//    // 사용자 경고 부여
//    boolean giveWarningToUser(Long userId, String reason);
//
//    // 사용자 경고 횟수 조회
//    int getUserWarningCount(Long userId);
//
////    // 사용자 활동 내역 조회
////    UserActivityDTO getUserActivity(Long userId);
//
//    // 상태별 사용자 목록
//    List<AdminUserDTO> getUsersByStatus(String status);
//
//    // 사용자 강제 탈퇴
//    boolean forceDeleteUser(Long userId);
//
//    // 신고된 사용자 목록
//    List<AdminUserDTO> getReportedUsers();
//}
