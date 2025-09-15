package com.example.together.dto.trade;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TradeUploadDTO {

  @NotBlank
  @Size(max = 255)
  private String title;

  @NotBlank
  @Size(max = 255)
  private String description;

  @NotNull
  @Positive
  private Integer price;

  @NotBlank
  @Size(max = 255)
  private String thumbnail;

  @NotBlank
  private String tradeCategory;

  @NotBlank
  private String tradeStatus;

  @Size(max = 10)
  private List<MultipartFile> images;
}

