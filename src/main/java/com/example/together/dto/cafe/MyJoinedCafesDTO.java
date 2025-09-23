package com.example.together.dto.cafe;

import com.example.together.domain.Membership;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MyJoinedCafesDTO {
    private List<Membership> memberships;
    private long totalCafes;
    private long musicCafes;
    private long sportsCafes;
    private long studyCafes;
}