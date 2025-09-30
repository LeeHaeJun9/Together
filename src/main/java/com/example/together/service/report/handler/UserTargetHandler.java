package com.example.together.service.report.handler;

import com.example.together.domain.ReportType;
import com.example.together.service.report.ReportTargetHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserTargetHandler implements ReportTargetHandler {
  // private final UserService userService;

  @Override
  public boolean supports(ReportType type) {
    return type == ReportType.USER;
  }

  @Override
  public void handleAction(ReportType type, Long targetId) {
    // userService.banOrDeactivateByAdmin(targetId); // 강제탈퇴/정지
  }
}
