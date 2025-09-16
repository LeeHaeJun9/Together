package com.example.together.repository;

import com.example.together.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 로그인 시 userId로 사용자 찾기
    Optional<User> findByUserId(String userId);

    // 이메일로 사용자 찾기 (중복 체크용)
    Optional<User> findByEmail(String email);

    // 닉네임으로 사용자 찾기 (중복 체크용)
    Optional<User> findByNickname(String nickname);

    // 전화번호로 사용자 찾기
    Optional<User> findByPhone(String phone);

    Optional<User> findByNameAndEmail(String name, String email);

    // userId 존재 여부 체크
    boolean existsByUserId(String userId);

    // 이메일 존재 여부 체크
    boolean existsByEmail(String email);

    // 닉네임 존재 여부 체크
    boolean existsByNickname(String nickname);
}
