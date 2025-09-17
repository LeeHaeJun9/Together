package com.example.together.dto.demandSurvey;

import com.example.together.domain.DemandSurvey;
import com.example.together.domain.VoteType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DemandSurveyResponseDTO {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime deadline;
    private VoteType voteType;

    public DemandSurveyResponseDTO(DemandSurvey demandSurvey) {
        this.id = demandSurvey.getId();
        this.title = demandSurvey.getTitle();
        this.content = demandSurvey.getContent();
        this.deadline = demandSurvey.getDeadline();
        this.voteType = demandSurvey.getVoteType();
    }
}