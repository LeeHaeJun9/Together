package com.example.together.repository;

import com.example.together.domain.Cafe;
import com.example.together.domain.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Integer countByCafe(Cafe cafe);
}
