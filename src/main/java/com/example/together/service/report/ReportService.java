package com.example.together.service.report;

import com.example.together.domain.ReportType;
import com.example.together.dto.report.ReportAdminRow;
import com.example.together.dto.report.ReportCreateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportService {
  Long create(Long reporterUserPk, ReportCreateDTO dto);
  Page<ReportAdminRow> listAdmin(ReportType type, Pageable pageable);
  void deleteTarget(Long reportId);
  void deleteCafeWithChildren(Long cafeId);

  void banUser(Long reportId);
  void removeReportOnly(Long reportId);
}
