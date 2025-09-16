package com.example.together.controller;

import com.example.together.dto.vote.VoteCreateRequestDTO;
import com.example.together.dto.vote.VoteResponseDTO;
import com.example.together.service.vote.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/cafe/{cafeId}/posts/{postId}/demand-survey/{surveyId}/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    // 현재 사용자의 ID를 가져오는 메서드 (실제 구현 필요)
    private Long getLoggedInUserId(Principal principal) {
        return 1L;
    }

    @PostMapping
    public ResponseEntity<String> createVote(@PathVariable Long cafeId,
                                             @PathVariable Long postId,
                                             @PathVariable Long surveyId,
                                             @RequestBody VoteCreateRequestDTO requestDTO,
                                             Principal principal) {
        Long userId = getLoggedInUserId(principal);
        try {
            voteService.createVote(surveyId, requestDTO, userId);
            return ResponseEntity.ok("투표가 성공적으로 완료되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<VoteResponseDTO>> getVoteResults(@PathVariable Long surveyId) {
        try {
            List<VoteResponseDTO> results = voteService.getVoteResults(surveyId);
            return ResponseEntity.ok(results);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}