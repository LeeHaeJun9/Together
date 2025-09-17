//// 3. AdminUserServiceImpl.java - 사용자 관리 기능 구현
//package com.example.together.service.manager.impl;
//
//import com.example.together.dto.manager.AdminUserDTO;
//import com.example.together.repository.manager.AdminUserRepository;
//import com.example.together.service.manager.AdminUserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import java.util.List;
//
//@Service
//public class AdminUserServiceImpl implements AdminUserService {
//
//    @Autowired
//    private AdminUserRepository adminUserRepository;
//
//    @Override
//    public List<AdminUserDTO> getAllUsers() {
//        return adminUserRepository.findAllUsers();
//    }
//
//    @Override
//    public List<AdminUserDTO> searchUsers(String keyword) {
//        // 검색어가 비어있으면 전체 목록 반환
//        if (keyword == null || keyword.trim().isEmpty()) {
//            return getAllUsers();
//        }
//        return adminUserRepository.searchUsers(keyword);
//    }
//
//    @Override
//    public AdminUserDTO getUserById(Long userId) {
//        return adminUserRepository.findByUserId(userId);
//    }
//
//    @Override
//    public boolean changeUserStatus(Long userId, String status) {
//        // 유효한 상태인지 확인
//        if (!isValidUserStatus(status)) {
//            return false;
//        }
//
//        int result = adminUserRepository.updateUserStatus(userId, status);
//        return result > 0;
//    }
//
//    @Override
//    public boolean giveWarningToUser(Long userId, String reason) {
//        // 경고 사유가 비어있으면 실패
//        if (reason == null || reason.trim().isEmpty()) {
//            return false;
//        }
//
//        int result = adminUserRepository.insertUserWarning(userId, reason);
//        return result > 0;
//    }
//
//    @Override
//    public int getUserWarningCount(Long userId) {
//        return adminUserRepository.getUserWarningCount(userId);
//    }
//
//    @Override
//    public List<AdminUserDTO> getUsersByStatus(String status) {
//        return adminUserRepository.findByStatus(status);
//    }
//
//    @Override
//    public boolean forceDeleteUser(Long userId) {
//        int result = adminUserRepository.deleteUser(userId);
//        return result > 0;
//    }
//
//    @Override
//    public List<AdminUserDTO> getReportedUsers() {
//        return adminUserRepository.findReportedUsers();
//    }
//
//    // 유효한 사용자 상태인지 확인하는 메서드
//    private boolean isValidUserStatus(String status) {
//        return "ACTIVE".equals(status) ||
//                "INACTIVE".equals(status) ||
//                "SUSPENDED".equals(status) ||
//                "BANNED".equals(status);
//    }
//}
