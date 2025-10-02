package com.example.together.service.report;

import com.example.together.domain.ReportType;

public interface ReportTargetHandler {
  boolean supports(ReportType type);
  /**
   * 신고 대상 삭제(POST/CHAT), 또는 USER 정지/탈퇴 등
   * 구현체에서 type 별 동작을 구분하거나, 전용 메서드를 나눠도 됩니다.
   */
  void handleAction(ReportType type, Long targetId);
}
