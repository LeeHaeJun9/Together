package com.example.together.repository;

import com.example.together.domain.CafeApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CafeApplicationRepository extends JpaRepository<CafeApplication,Long> {
}
