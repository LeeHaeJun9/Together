package com.example.together.dto.trade;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeUpdateRequest {
  private String title;
  private String content;
  private BigDecimal price;
  private String status; // 판매중/예약/거래완료
}
