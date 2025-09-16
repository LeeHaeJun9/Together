package com.example.together.service.vote;

import com.example.together.dto.vote.VoteCreateRequestDTO;
import com.example.together.dto.vote.VoteResponseDTO;

import java.util.List;

public interface VoteService {
    void createVote(Long surveyId, VoteCreateRequestDTO requestDTO, Long voterId);

    List<VoteResponseDTO> getVoteResults(Long surveyId);
}