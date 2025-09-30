package com.example.together.dto.report;

import com.example.together.domain.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReportCreateDTO {
  @NotNull
  private ReportType reportType;

  @NotNull
  private Long targetId;

  @NotBlank
  private String reason;
}
