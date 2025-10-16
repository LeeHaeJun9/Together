package com.example.together.service.demandSurvey;

import com.example.together.domain.DemandSurvey;
import com.example.together.domain.Post;
import com.example.together.domain.PostType;
import com.example.together.domain.User;
import com.example.together.dto.demandSurvey.DemandSurveyCreateRequestDTO;
import com.example.together.dto.demandSurvey.DemandSurveyResponseDTO;
import com.example.together.dto.post.PostCreateRequestDTO;
import com.example.together.repository.CafeRepository;
import com.example.together.repository.DemandSurveyRepository;
import com.example.together.repository.PostRepository;
import com.example.together.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class DemandSurveyServiceImpl implements DemandSurveyService {
    private final DemandSurveyRepository demandSurveyRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CafeRepository cafeRepository;

    @Override
    @Transactional
    public DemandSurvey createDemandSurvey(Long postId, DemandSurveyCreateRequestDTO requestDTO, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // ✅ 이 조건문을 수정하여 오직 DEMAND 타입만 허용합니다.
        if (post.getPostType() != PostType.DEMAND) {
            throw new IllegalStateException("설문조사는 수요조사 게시글에만 추가할 수 있습니다.");
        }

        if (post.getDemandSurvey() != null) {
            throw new IllegalStateException("이미 이 게시글에 설문조사가 존재합니다.");
        }

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        DemandSurvey demandSurvey = DemandSurvey.builder()
                .title(requestDTO.getTitle())
                .content(requestDTO.getContent())
                .deadline(requestDTO.getDeadline())
                .voteType(requestDTO.getVoteType())
                .options(requestDTO.getOptions())
                .post(post)
                .author(author)
                .build();

        return demandSurveyRepository.save(demandSurvey);
    }

    @Override
    @Transactional(readOnly = true)
    public DemandSurveyResponseDTO getDemandSurveyByPostId(Long postId) {
        DemandSurvey survey = demandSurveyRepository.findByPostId(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글의 수요조사가 없습니다."));
        return new DemandSurveyResponseDTO(survey);
    }

    @Override
    public DemandSurvey getSurveyById(Long surveyId) {
        return demandSurveyRepository.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 수요조사가 존재하지 않습니다. id=" + surveyId));
    }

}