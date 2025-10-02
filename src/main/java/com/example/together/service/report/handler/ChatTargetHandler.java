package com.example.together.service.report.handler;

import com.example.together.domain.ReportType;
import com.example.together.service.report.ReportTargetHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatTargetHandler implements ReportTargetHandler {
  // private final ChatService chatService;

  @Override
  public boolean supports(ReportType type) {
    return type == ReportType.CHAT;
  }

  @Override
  public void handleAction(ReportType type, Long targetId) {
    // chatService.deleteMessageOrRoomByAdmin(targetId);
  }
}
