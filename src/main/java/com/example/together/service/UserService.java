package com.example.together.service;

import com.example.together.domain.User;
import com.example.together.dto.member.memberRegisterDTO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    // 로그인 관련
    User authenticate(String userId, String password);
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    // 회원가입 관련 - 두 가지 방식 지원
    User register(memberRegisterDTO registerDTO);
    void register(memberRegisterDTO registerDTO, MultipartFile profilePhoto);

    // 중복 확인
    boolean isUserIdExists(String userId);
    boolean isEmailExists(String email);
    boolean isNameExists(String name);
    boolean isNicknameExists(String nickname);
    boolean isPhoneExists(String phone);
    boolean isEmailExistsExcludeUser(String email, String excludeUserId);
    boolean isNicknameAvailable(String nickname, String currentUserId);

    // 사용자 정보 조회
    User findByUserId(String userId);
    User findByEmail(String email);
    User findUserForPasswordReset(String userId, String email, String name);
    String findUserIdByNameAndEmail(String name, String email);

    // 프로필 관리
    User updateProfile(Long id, memberRegisterDTO registerDTO);
    boolean updateUserField(String userId, String field, String value);
    void deleteUser(Long id);

    // 비밀번호 관리
    boolean changePassword(String userId, String currentPassword, String newPassword);
    boolean updateTempPassword(String userId, String tempPassword);
    void updateUserPassword(String userId, String newPassword);

    // 파일 업로드
    String uploadProfilePhoto(String userId, MultipartFile photo);

    // 기타 유틸리티
    boolean isAdmin(Long adminId);
    String getUserNicknameById(Long userId);
}