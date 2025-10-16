package com.example.together.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "report")
public class Report {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @UpdateTimestamp
  private LocalDateTime moddate;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime regdate;   // ✅ 등록일 표시

  @Column(nullable = false, length = 255)
  private String reason;

  @Enumerated(EnumType.STRING)
  @Column(name="report_type", length = 20, nullable = false)
  private ReportType reportType;

  @Enumerated(EnumType.STRING)
  @Column(name="post_sub_type", length = 20)
  private PostSubType postSubType;

  private Long targetId;           // 신고 대상의 PK (카페/게시글/댓글/거래/유저 등)

  @Column(name = "reporter_id")
  private Long reporterId;         // 신고자 user.id
}
