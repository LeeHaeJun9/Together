package com.example.together.dto.cafe;

import com.example.together.domain.CafeApplication;
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

    public CafeApplicationResponseDTO(CafeApplication application) {
        this.applicationId = application.getId();
        this.name = application.getName();
        this.description = application.getDescription();
        this.category = application.getCategory().name();
        this.status = application.getStatus();
        this.regDate = application.getRegDate();
        if (application.getApplicant() != null) {
            this.applicantId = application.getApplicant().getId();
        } else {
            this.applicantId = null;
        }
    }
}
