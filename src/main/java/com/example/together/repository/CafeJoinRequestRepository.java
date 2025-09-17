package com.example.together.repository;

import com.example.together.domain.Cafe;
import com.example.together.domain.CafeJoinRequest;
import com.example.together.domain.CafeJoinRequestStatus;
import com.example.together.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CafeJoinRequestRepository extends JpaRepository<CafeJoinRequest, Long> {

    // 특정 카페에 대한 대기 중인 신청 목록을 조회합니다.
    List<CafeJoinRequest> findByCafeAndStatus(Cafe cafe, CafeJoinRequestStatus status);

    // 특정 사용자가 이미 해당 카페에 대해 대기 중인 신청이 있는지 확인합니다.
    boolean existsByCafeAndUserAndStatus(Cafe cafe, User user, CafeJoinRequestStatus status);

    // 사용자 엔티티를 포함하여 특정 신청을 조회합니다.
    @Query("SELECT j FROM CafeJoinRequest j JOIN FETCH j.user WHERE j.id = :id")
    Optional<CafeJoinRequest> findByIdWithUser(@Param("id") Long id);

    void deleteByCafe(Cafe cafe);
}