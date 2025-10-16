package com.example.together.dto.demandSurvey;

import com.example.together.domain.DemandSurvey;
import com.example.together.domain.VoteType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class DemandSurveyResponseDTO {
    private Long id;
    private String title;
    private String content;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime deadline;
    private List<String> options; // 작성자가 입력한 옵션
    private VoteType voteType;

    public DemandSurveyResponseDTO(DemandSurvey demandSurvey) {
        this.id = demandSurvey.getId();
        this.title = demandSurvey.getTitle();
        this.content = demandSurvey.getContent();
        this.deadline = demandSurvey.getDeadline();
        this.options = demandSurvey.getOptions();
        this.voteType = demandSurvey.getVoteType();
    }
}