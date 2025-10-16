package com.example.together.service.demandSurvey;

import com.example.together.domain.DemandSurvey;
import com.example.together.dto.demandSurvey.DemandSurveyCreateRequestDTO;
import com.example.together.dto.demandSurvey.DemandSurveyResponseDTO;

public interface DemandSurveyService {
    DemandSurvey createDemandSurvey(Long postId, DemandSurveyCreateRequestDTO requestDTO, Long userId);
    DemandSurveyResponseDTO getDemandSurveyByPostId(Long postId);
    DemandSurvey getSurveyById(Long surveyId);
}
