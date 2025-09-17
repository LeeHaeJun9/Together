package com.example.together.repository;

import com.example.together.domain.Cafe;
import com.example.together.domain.Membership;
import com.example.together.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Integer countByCafe(Cafe cafe);

    void deleteByCafe(Cafe cafe);

    boolean existsByCafeAndUser(Cafe cafe, User user);

    Optional<Object> findByCafeAndUser(Cafe cafe, User user);

    @EntityGraph(attributePaths = "cafe")
    List<Membership> findByUserId(Long userId);

}
