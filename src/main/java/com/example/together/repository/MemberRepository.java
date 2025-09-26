package com.example.together.repository;

import com.example.together.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<User, Long> {
    // 사용자 ID로 사용자 찾기
    Optional<User> findByUserId(String userId);

    // 닉네임으로 사용자 찾기
    Optional<User> findByNickname(String nickname);
}
