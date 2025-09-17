package com.example.together.service;

import com.example.together.domain.Status;
import com.example.together.domain.SystemRole;
import com.example.together.domain.User;
import com.example.together.dto.member.memberRegisterDTO;
import com.example.together.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Spring Security용 사용자 로드 메서드
     * Spring Security의 DaoAuthenticationProvider가 이 메서드를 호출하여 사용자 정보를 가져온 후,
     * 비밀번호 비교는 내부적으로 처리합니다. 따라서 이 메서드에서는 비밀번호를 직접 비교할 필요가 없습니다.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 사용자 조회
        User user = userRepository.findByUserId(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // 2. 계정 상태 확인
        if (user.getStatus() != Status.ACTIVE) {
            throw new UsernameNotFoundException("비활성 계정입니다: " + username);
        }

        // 3. UserDetails 객체 생성 및 반환
        // Spring Security가 이 객체의 password 필드와 사용자가 입력한 비밀번호를 비교합니다.
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUserId())
                .password(user.getPassword()) // DB에 저장된 해시된 비밀번호를 그대로 전달
                .roles(user.getSystemRole().name()) // 하드코딩된 "USER" 대신 실제 역할을 사용
                .build();
    }

    /**
     * 회원가입
     */
    @Override
    @Transactional
    public User register(memberRegisterDTO registerDTO) {
        if (isUserIdExists(registerDTO.getUserId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        if (isEmailExists(registerDTO.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        User user = User.builder()
                .userId(registerDTO.getUserId())
                .password(passwordEncoder.encode(registerDTO.getPassword())) // 비밀번호 암호화
                .name(registerDTO.getName())
                .email(registerDTO.getEmail())
                .phone(registerDTO.getPhone())
                .nickname(registerDTO.getNickname())
                .systemRole(SystemRole.USER)
                .status(Status.ACTIVE)
                .build();

        return userRepository.save(user);
    }

    /**
     * 프로필 업데이트
     * JPA의 변경 감지(Dirty Checking) 기능을 활용하도록 수정했습니다.
     * @Transactional 환경에서 엔티티를 조회하고 setter로 필드를 변경하면, 트랜잭션이 끝날 때 자동으로 UPDATE 쿼리가 실행됩니다.
     */
    @Override
    @Transactional
    public User updateProfile(Long id, memberRegisterDTO registerDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // DTO에 값이 있을 경우에만 필드를 업데이트합니다.
        if (registerDTO.getName() != null) {
            user.setName(registerDTO.getName());
        }
        if (registerDTO.getPhone() != null) {
            user.setPhone(registerDTO.getPhone());
        }
        if (registerDTO.getNickname() != null) {
            user.setNickname(registerDTO.getNickname());
        }

        // user.builder()로 새 객체를 만들 필요 없이, 변경된 user 엔티티가 자동으로 저장됩니다.
        return user;
    }

    /**
     * 개별 필드 업데이트
     */
    @Override
    @Transactional
    public boolean updateUserField(String userId, String field, String value) {
        try {
            User user = findByUserId(userId);
            if (user == null) {
                return false;
            }

            // 필드별로 업데이트 처리
            switch (field) {
                case "nickname":
                    user.setNickname(value);
                    break;
                case "name":
                    user.setName(value);
                    break;
                case "email":
                    // 이메일 중복 확인 (본인 제외)
                    if (isEmailExistsExcludeUser(value, userId)) {
                        return false; // 중복된 이메일
                    }
                    user.setEmail(value);
                    break;
                case "phone":
                    user.setPhone(value);
                    break;
                default:
                    return false; // 지원하지 않는 필드
            }

            userRepository.save(user);
            return true;

        } catch (Exception e) {
            log.error("사용자 정보 업데이트 실패: userId = {}, field = {}, error = {}",
                    userId, field, e.getMessage());
            return false;
        }
    }

    /**
     * 비밀번호 변경
     */
    @Override
    @Transactional
    public boolean changePassword(String userId, String currentPassword, String newPassword) {
        try {
            User user = findByUserId(userId);
            if (user == null) {
                return false;
            }

            // 현재 비밀번호 확인
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                return false; // 현재 비밀번호 불일치
            }

            // 새 비밀번호 암호화 후 저장
            String encodedNewPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedNewPassword);

            userRepository.save(user);
            return true;

        } catch (Exception e) {
            log.error("비밀번호 변경 실패: userId = {}, error = {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * 이메일 중복 확인 (본인 제외)
     */
    @Override
    public boolean isEmailExistsExcludeUser(String email, String excludeUserId) {
        try {
            User existingUser = userRepository.findByEmail(email).orElse(null);
            return existingUser != null && !existingUser.getUserId().equals(excludeUserId);
        } catch (Exception e) {
            log.error("이메일 중복 확인 실패: email = {}, error = {}", email, e.getMessage());
            return true; // 오류 시 중복으로 처리하여 안전하게
        }
    }

    /**
     * 회원 탈퇴 (논리적 삭제)
     * 마찬가지로 변경 감지를 활용하여 상태만 변경하도록 수정했습니다.
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setStatus(Status.DELETED); // 상태만 DELETED로 변경
        // userRepository.save()를 호출할 필요가 없습니다.
    }

    // --- 나머지 유틸리티 메소드들 ---

    @Override
    public boolean isUserIdExists(String userId) {
        return userRepository.findByUserId(userId).isPresent();
    }

    @Override
    public boolean isEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId).orElse(null);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * 이 메소드는 UserDetailsService 구현으로 인해 더 이상 필요하지 않습니다.
     * Spring Security가 인증을 처리하도록 두는 것이 좋습니다.
     * 만약 다른 곳에서 직접 인증이 필요하다면 이 로직을 사용할 수 있습니다.
     */
    @Override
    public User authenticate(String userId, String password) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getStatus() == Status.ACTIVE && passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    @Override
    public String findUserIdByNameAndEmail(String name, String email) {
        Optional<User> userOpt = userRepository.findByNameAndEmail(name, email);
        return userOpt.isPresent() ? userOpt.get().getUserId() : null;
    }

    @Override
    public boolean isAdmin(Long adminId) {
        // 1. userRepository를 사용해 ID로 사용자 조회
        Optional<User> userOpt = userRepository.findById(adminId);

        // 2. 사용자가 존재하고, 그 사용자의 역할이 SystemRole.ADMIN인지 확인
        //    userOpt.isPresent()는 사용자가 존재하는지 확인하고,
        //    userOpt.get().getSystemRole().equals(SystemRole.ADMIN)는 역할이 ADMIN인지 확인합니다.
        return userOpt.isPresent() && userOpt.get().getSystemRole().equals(SystemRole.ADMIN);
    }

    @Transactional(readOnly = true)
    public String getUserNicknameById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return user.getNickname();
    }
}