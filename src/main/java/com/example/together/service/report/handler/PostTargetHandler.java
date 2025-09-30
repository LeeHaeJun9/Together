package com.example.together.service.report.handler;

import com.example.together.domain.ReportType;
import com.example.together.service.report.ReportTargetHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostTargetHandler implements ReportTargetHandler {
  // private final TradeService tradeService;
  // private final CafeService cafeService;
  // private final CommentService commentService;
  // 하나의 "POST" 타입에 여러 모듈이 매핑되므로, targetId의 네임스페이스가 필요하면
  // 2단계에서 관리자 UI에 "콘텐츠 종류" 필터/버튼을 분리합니다.

  @Override
  public boolean supports(ReportType type) {
    return type == ReportType.POST;
  }

  @Override
  public void handleAction(ReportType type, Long targetId) {
    // 예) tradeService.deleteByAdmin(targetId);
    // 예) cafeService.deletePostByAdmin(targetId);
    // 예) commentService.deleteByAdmin(targetId);
    // 현재는 뼈대. 실제 연결은 2단계에서 선택적으로 바인딩합니다.
  }
}
