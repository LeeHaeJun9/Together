package com.example.together.dto.trade;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeSaveRequest {
  private String title;
  private String content;
  private BigDecimal price;
  private String status; // null이면 서비스에서 "판매중" 기본값
}
