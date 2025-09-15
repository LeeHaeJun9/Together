package com.example.together.dto.trade;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeReadDTO {
  private Long id;
  
  private String title;
  
  private String description;
  
  private Integer price;
  
  private String thumbnail;
  
  private String tradeCategory;
  
  private String tradeStatus;
  
  private Long sellerId;
  
  private String regDate;
  
  private String modDate;

  //이미지 목록
  private List<String> imageUrls;
}