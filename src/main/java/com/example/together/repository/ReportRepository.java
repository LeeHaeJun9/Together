package com.example.together.repository;

import com.example.together.domain.Report;
import com.example.together.domain.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
  Page<Report> findByReportType(ReportType type, Pageable pageable);
}
