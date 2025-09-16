package com.example.together.dto.demandSurvey;

import com.example.together.domain.VoteType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DemandSurveyCreateRequestDTO {
    private String title;
    private String content;
    private LocalDateTime deadline;
    private VoteType voteType;
}