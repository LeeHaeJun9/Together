package com.example.together.controller;

import com.example.together.dto.demandSurvey.DemandSurveyCreateRequestDTO;
import com.example.together.service.demandSurvey.DemandSurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;

@RestController
@RequestMapping("/cafe/{cafeId}/posts/{postId}/demandSurvey")
@RequiredArgsConstructor
public class DemandSurveyController {

    private final DemandSurveyService demandSurveyService;

    // 현재 사용자의 ID를 가져오는 메서드 (실제 구현 필요)
    private Long getLoggedInUserId(Principal principal) {
        return 1L;
    }

    @PostMapping
    public ResponseEntity<?> createDemandSurvey(@PathVariable Long cafeId,
                                                @PathVariable Long postId,
                                                @RequestBody DemandSurveyCreateRequestDTO requestDTO,
                                                Principal principal) {
        Long userId = getLoggedInUserId(principal);
        try {
            demandSurveyService.createDemandSurvey(postId, requestDTO, userId);
            return ResponseEntity.created(URI.create("/cafe/" + cafeId + "/posts/" + postId)).build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}