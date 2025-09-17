package com.example.together.controller;

import com.example.together.dto.demandSurvey.DemandSurveyCreateRequestDTO;
import com.example.together.service.UserService;
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
    private final UserService userService;

    private Long getLoggedInUserId(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("로그인된 사용자가 없습니다.");
        }
        String userIdString = principal.getName();
        return userService.findByUserId(userIdString).getId();
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