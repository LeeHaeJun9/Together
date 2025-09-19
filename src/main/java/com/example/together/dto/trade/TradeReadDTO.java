package com.example.together.dto.trade;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeReadDTO {
  private Long tno;
  private String title;
  private String content;
  private int price;
  private String category;
  private String writerNickname;
  private String sellerId;
  private List<String> imagePaths;

  private LocalDateTime regDate;
  private LocalDateTime modDate;

  private int likeCount;      // 찜 개수
  private boolean likedByMe;  // 현재 로그인한 사용자가 찜했는지 여부
}

