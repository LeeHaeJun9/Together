package com.example.together.service;

import com.example.together.domain.Status;
import com.example.together.domain.SystemRole;
import com.example.together.domain.User;
import com.example.together.dto.member.memberRegisterDTO;
import com.example.together.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
}
