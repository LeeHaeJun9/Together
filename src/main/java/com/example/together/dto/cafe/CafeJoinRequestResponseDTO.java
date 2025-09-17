package com.example.together.dto.cafe;

import com.example.together.domain.CafeJoinRequest;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CafeJoinRequestResponseDTO {

    private final Long requestId;
    private final Long userId;
    private final String username;
    private final LocalDateTime requestDate;
    private final String status;

    public CafeJoinRequestResponseDTO(CafeJoinRequest joinRequest) {
        this.requestId = joinRequest.getId();
        this.userId = joinRequest.getUser().getId();
        this.username = joinRequest.getUser().getName();
        this.requestDate = joinRequest.getRequestDate();
        this.status = joinRequest.getStatus().toString();
    }
}
