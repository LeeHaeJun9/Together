package com.example.together.service.vote;

import com.example.together.domain.DemandSurvey;
import com.example.together.domain.User;
import com.example.together.domain.Vote;
import com.example.together.domain.VoteType;
import com.example.together.dto.vote.VoteCreateRequestDTO;
import com.example.together.dto.vote.VoteResponseDTO;
import com.example.together.repository.DemandSurveyRepository;
import com.example.together.repository.UserRepository;
import com.example.together.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        // 1. 수요조사 존재 여부와 투표 유형 확인
        DemandSurvey survey = demandSurveyRepository.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 설문조사입니다."));

        // 2. 해당 수요조사의 모든 투표를 가져옵니다.
        List<Vote> votes = voteRepository.findBySurveyId(surveyId);

        // 3. 결과를 옵션별로 그룹화합니다.
        Map<String, List<Vote>> votesByOption = votes.stream()
                .collect(Collectors.groupingBy(Vote::getOption));

        List<VoteResponseDTO> results = new ArrayList<>();

        // 4. 투표 유형에 따라 DTO를 구성합니다.
        for (Map.Entry<String, List<Vote>> entry : votesByOption.entrySet()) {
            String option = entry.getKey();
            List<Vote> votesForOption = entry.getValue();

            VoteResponseDTO.VoteResponseDTOBuilder builder = VoteResponseDTO.builder()
                    .option(option)
                    .count((long) votesForOption.size());

            // 5. 공개 투표(PUBLIC)인 경우 투표자 닉네임을 추가
            if (survey.getVoteType() == VoteType.PUBLIC) {
                List<String> nicknames = votesForOption.stream()
                        .map(vote -> vote.getVoter().getNickname())
                        .collect(Collectors.toList());
                builder.voterNicknames(nicknames);
            }

            results.add(builder.build());
        }

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasVoted(Long surveyId, Long userId) {
        // ✅ 변경된 메서드 이름으로 호출
        return voteRepository.existsBySurveyIdAndVoterId(surveyId, userId);
    }
}
