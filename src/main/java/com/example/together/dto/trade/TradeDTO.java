package com.example.together.dto.trade;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeDTO {

  private Long id;

  @NotBlank @Size(max = 255)
  private String title;

  @NotBlank @Size(max = 255)
  private String description;

  @NotNull @Positive
  private Integer price;

  @NotBlank @Size(max = 255)
  private String thumbnail;      // 썸네일 경로/URL

  @NotBlank
  private String tradeCategory;  // 카테고리 미정

  @NotBlank
  private String tradeStatus;    // "FOR_SALE", "RESERVED", "COMPLETED"

  @NotNull
  private Long sellerId;

  private String regDate;

  private String modDate;
}