package com.example.together.dto.demandSurvey;

import com.example.together.domain.VoteType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class DemandSurveyCreateRequestDTO {
    private Long id;
    private String title;
    private String content;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime deadline;
    private VoteType voteType;
    private List<String> options;
}