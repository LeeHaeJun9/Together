package com.example.together.dto.cafe;

import com.example.together.domain.CafeApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CafeApplicationResponseDTO {
    private Long applicationId;
    private String name;
    private String description;
    private String category;
    private CafeApplicationStatus status;
    private LocalDateTime regDate;
    private Long applicantId;
}
