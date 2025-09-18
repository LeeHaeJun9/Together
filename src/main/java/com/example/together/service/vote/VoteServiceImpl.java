package com.example.together.service.vote;

import com.example.together.domain.DemandSurvey;
import com.example.together.domain.User;
import com.example.together.domain.Vote;
import com.example.together.dto.vote.VoteCreateRequestDTO;
import com.example.together.dto.vote.VoteResponseDTO;
import com.example.together.repository.DemandSurveyRepository;
import com.example.together.repository.UserRepository;
import com.example.together.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {
    private final VoteRepository voteRepository;
    private final DemandSurveyRepository demandSurveyRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createVote(Long surveyId, VoteCreateRequestDTO requestDTO, Long voterId) {
        DemandSurvey survey = demandSurveyRepository.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 설문조사입니다."));

        if (voteRepository.findBySurveyIdAndVoterId(surveyId, voterId).isPresent()) {
            throw new IllegalStateException("이미 투표에 참여하셨습니다.");
        }

        if (LocalDateTime.now().isAfter(survey.getDeadline())) {
            throw new IllegalStateException("이 설문조사는 마감되었습니다.");
        }

        User voter = userRepository.findById(voterId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Vote vote = Vote.builder()
                .survey(survey)
                .voter(voter)
                .option(requestDTO.getOption())
                .build();

        voteRepository.save(vote);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoteResponseDTO> getVoteResults(Long surveyId) {
        // ✅ 수요조사 존재 여부 확인
        demandSurveyRepository.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 설문조사입니다."));

        // 투표 결과를 집계하는 커스텀 쿼리 실행 (VoteRepository에 추가 필요)
        List<Object[]> results = voteRepository.countVotesByOption(surveyId);

        // 집계된 결과를 DTO로 변환하여 반환
        return results.stream()
                .map(result -> new VoteResponseDTO((String) result[0], (Long) result[1]))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasVoted(Long surveyId, Long userId) {
        // ✅ 변경된 메서드 이름으로 호출
        return voteRepository.existsBySurveyIdAndVoterId(surveyId, userId);
    }
}
