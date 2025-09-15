package com.example.together.service;

import com.example.together.domain.User;
import com.example.together.domain.SystemRole;
import com.example.together.domain.CafeRole;
import com.example.together.domain.Status;
import com.example.together.dto.member.memberRegisterDTO;
import com.example.together.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * 로그인 인증
     */
    @Override
    public User authenticate(String userId, String password) {
        log.info("사용자 인증 시도: userId = {}", userId);

        try {
            // 1. userId로 사용자 찾기
            Optional<User> userOpt = userRepository.findByUserId(userId);

            if (userOpt.isEmpty()) {
                log.warn("사용자를 찾을 수 없음: userId = {}", userId);
                return null;
            }

            User user = userOpt.get();

            // 2. 계정 상태 확인
            if (user.getStatus() != Status.ACTIVE) {
                log.warn("비활성 계정 로그인 시도: userId = {}, status = {}", userId, user.getStatus());
                return null;
            }

            // 3. 비밀번호 확인 (실제로는 암호화된 비밀번호와 비교해야 함)
            if (password.equals(user.getPassword())) {
                log.info("로그인 성공: userId = {}", userId);
                return user;
            } else {
                log.warn("비밀번호 불일치: userId = {}", userId);
                return null;
            }

        } catch (Exception e) {
            log.error("인증 처리 중 오류 발생: userId = {}, error = {}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 회원가입
     */
    @Override
    @Transactional
    public User register(memberRegisterDTO registerDTO) {
        log.info("회원가입 시도: userId = {}", registerDTO.getUserId());

        try {
            // 1. 중복 체크
            if (isUserIdExists(registerDTO.getUserId())) {
                throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
            }

            if (isEmailExists(registerDTO.getEmail())) {
                throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
            }

            // 2. User 엔티티 생성
            User user = User.builder()
                    .userId(registerDTO.getUserId())
                    .password(registerDTO.getPassword()) // 실제로는 암호화 필요
                    .name(registerDTO.getName())
                    .email(registerDTO.getEmail())
                    .phone(registerDTO.getPhone())
                    .nickname(registerDTO.getNickname())
                    .systemRole(SystemRole.USER) // 기본값
                    .cafeRole(CafeRole.CAFE_USER) // 기본값
                    .status(Status.ACTIVE) // 기본값
                    .build();

            // 3. 저장
            User savedUser = userRepository.save(user);
            log.info("회원가입 성공: userId = {}", savedUser.getUserId());

            return savedUser;

        } catch (Exception e) {
            log.error("회원가입 처리 중 오류 발생: userId = {}, error = {}", registerDTO.getUserId(), e.getMessage());
            throw e;
        }
    }

    /**
     * 아이디 중복 체크
     */
    @Override
    public boolean isUserIdExists(String userId) {
        return userRepository.findByUserId(userId).isPresent();
    }

    /**
     * 이메일 중복 체크
     */
    @Override
    public boolean isEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    /**
     * 아이디로 사용자 찾기
     */
    @Override
    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId).orElse(null);
    }

    /**
     * 이메일로 사용자 찾기
     */
    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * 프로필 업데이트
     */
    @Override
    @Transactional
    public User updateProfile(Long id, memberRegisterDTO registerDTO) {
        log.info("프로필 업데이트 시도: id = {}", id);

        try {
            Optional<User> userOpt = userRepository.findById(id);

            if (userOpt.isEmpty()) {
                throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
            }

            User user = userOpt.get();

            // 필드 업데이트 (null이 아닌 값만)
            if (registerDTO.getName() != null) {
                user = User.builder()
                        .id(user.getId())
                        .userId(user.getUserId())
                        .password(user.getPassword())
                        .name(registerDTO.getName())
                        .email(user.getEmail())
                        .phone(registerDTO.getPhone() != null ? registerDTO.getPhone() : user.getPhone())
                        .nickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : user.getNickname())
                        .systemRole(user.getSystemRole())
                        .cafeRole(user.getCafeRole())
                        .status(user.getStatus())
                        .build();
            }

            User updatedUser = userRepository.save(user);
            log.info("프로필 업데이트 성공: id = {}", id);

            return updatedUser;

        } catch (Exception e) {
            log.error("프로필 업데이트 중 오류 발생: id = {}, error = {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * 회원 탈퇴
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("회원 탈퇴 시도: id = {}", id);

        try {
            Optional<User> userOpt = userRepository.findById(id);

            if (userOpt.isEmpty()) {
                throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
            }

            User user = userOpt.get();

            // Soft Delete - 상태를 DELETED로 변경
            User deletedUser = User.builder()
                    .id(user.getId())
                    .userId(user.getUserId())
                    .password(user.getPassword())
                    .name(user.getName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .nickname(user.getNickname())
                    .systemRole(user.getSystemRole())
                    .cafeRole(user.getCafeRole())
                    .status(Status.DELETED) // 삭제 상태로 변경
                    .build();

            userRepository.save(deletedUser);
            log.info("회원 탈퇴 성공: id = {}", id);

        } catch (Exception e) {
            log.error("회원 탈퇴 처리 중 오류 발생: id = {}, error = {}", id, e.getMessage());
            throw e;
        }
    }
}
