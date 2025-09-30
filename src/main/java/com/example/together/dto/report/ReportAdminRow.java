package com.example.together.dto.report;

import com.example.together.domain.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class ReportAdminRow {

  private final Long id;
  private final ReportType type;

  private final Long targetId;
  private final String targetLabel;
  private final String targetUrl;

  // 대상 유저(채팅/댓글/유저 신고일 때)
  private final String targetUserNickname;
  private final String targetUserLoginId;

  private final String reason;
  private final LocalDateTime regdate;

  // 신고자
  private final String reporterNickname;
  private final String reporterLoginId;

  // ▶ 버튼 제어
  private final boolean canDelete;    // 콘텐츠 삭제 가능
  private final boolean canBan;       // 회원 정지 가능
  private final String  deleteLabel;  // "카페 삭제" | "카페 게시물 삭제" | "거래 삭제"
}
