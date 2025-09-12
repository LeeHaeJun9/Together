// src/main/java/com/example/together/service/UserService.java
package com.example.together.service;

import com.example.together.domain.User;

import com.example.together.dto.cafe.memberRegisterDTO;
import com.example.together.dto.cafe.memberRegisterDTO;

public interface UserService {
    // 로그인 관련
    User authenticate(String userId, String password);

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
}
