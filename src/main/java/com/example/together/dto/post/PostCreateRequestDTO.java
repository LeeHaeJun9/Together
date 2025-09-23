package com.example.together.dto.post;

import com.example.together.domain.PostType;
import com.example.together.dto.demandSurvey.DemandSurveyCreateRequestDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostCreateRequestDTO {
    private Long id;
    private String title;
    private String content;
    private PostType postType = PostType.GENERAL;
    private String image;
    private Long cafeId;
    private boolean pinned;

    private DemandSurveyCreateRequestDTO demandSurvey;


}
