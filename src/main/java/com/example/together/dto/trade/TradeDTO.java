package com.example.together.dto.trade;

import com.example.together.domain.Trade;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeDTO {
  private Long id;
  private String title;
  private String content;
  private BigDecimal price;
  private String status;
  private String sellerNickname;
  private LocalDateTime regdate;
  private LocalDateTime moddate;
  private int favoriteCount;
  private String thumbnail;

}
