package com.example.together.service.demandSurvey;

import com.example.together.domain.DemandSurvey;
import com.example.together.dto.demandSurvey.DemandSurveyCreateRequestDTO;

public interface DemandSurveyService {
    DemandSurvey createDemandSurvey(Long postId, DemandSurveyCreateRequestDTO requestDTO, Long userId);
}
