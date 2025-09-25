// UserService.java - 올바른 인터페이스 형식

package com.example.together.service;

import com.example.together.domain.User;
import com.example.together.dto.member.memberRegisterDTO;
import com.example.together.dto.member.RegisterDTO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    // 로그인 관련
    User authenticate(String userId, String password);
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    // 회원가입 관련
    User register(memberRegisterDTO registerDTO);
    boolean isUserIdExists(String userId);
    boolean isEmailExists(String email);

    // 사용자 정보 조회
    User findByUserId(String userId);
    User findByEmail(String email);

    // 프로필 관리
    User updateProfile(Long id, memberRegisterDTO registerDTO);
    void deleteUser(Long id);

    // =============== 새로 추가되는 메소드들 ===============
    boolean updateUserField(String userId, String field, String value);
    boolean changePassword(String userId, String currentPassword, String newPassword);

    User findUserForPasswordReset(String userId, String email, String name);
    // ===============================================

    boolean isEmailExistsExcludeUser(String email, String excludeUserId);
    String findUserIdByNameAndEmail(String name, String email);
    boolean isAdmin(Long adminId);
    String getUserNicknameById(Long userId);
    boolean updateTempPassword(String userId, String tempPassword);
    void updateUserPassword(String userId, String newPassword);

    String uploadProfilePhoto(String userId, MultipartFile photo);

    // 닉네임 중복 확인 메소드 선언 추가
    boolean isNicknameAvailable(String nickname, String currentUserId);
    // 추가 중복 확인 메소드들
    boolean isNameExists(String name);
    boolean isNicknameExists(String nickname);
    boolean isPhoneExists(String phone);
}