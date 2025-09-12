package com.example.together.repository;

import com.example.together.domain.CafeApplication;
import com.example.together.domain.CafeApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CafeApplicationRepository extends JpaRepository<CafeApplication,Long> {
    List<CafeApplication> findByStatus(CafeApplicationStatus status);

    @Query("SELECT a FROM CafeApplication a JOIN FETCH a.applicant WHERE a.id = :id")
    Optional<CafeApplication> findByIdWithApplicant(@Param("id") Long id);
    @Query("SELECT a FROM CafeApplication a JOIN FETCH a.applicant WHERE a.status = :status")
    List<CafeApplication> findByStatusWithApplicant(@Param("status") CafeApplicationStatus status);
}
