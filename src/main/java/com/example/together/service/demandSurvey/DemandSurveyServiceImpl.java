package com.example.together.service.demandSurvey;

import com.example.together.domain.DemandSurvey;
import com.example.together.domain.Post;
import com.example.together.domain.PostType;
import com.example.together.domain.User;
import com.example.together.dto.demandSurvey.DemandSurveyCreateRequestDTO;
import com.example.together.repository.DemandSurveyRepository;
import com.example.together.repository.PostRepository;
import com.example.together.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DemandSurveyServiceImpl implements DemandSurveyService {
    private final DemandSurveyRepository demandSurveyRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DemandSurvey createDemandSurvey(Long postId, DemandSurveyCreateRequestDTO requestDTO, Long userId) { // ✅ userId 매개변수 추가
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (post.getPostType() != PostType.NOTICE) {
            throw new IllegalStateException("설문조사는 공지 게시글에만 추가할 수 있습니다.");
        }

        if (post.getDemandSurvey() != null) {
            throw new IllegalStateException("이미 이 게시글에 설문조사가 존재합니다.");
        }

        // ✅ 전달받은 userId로 User 엔티티를 찾아서 작성자로 설정
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        DemandSurvey demandSurvey = DemandSurvey.builder()
                .title(requestDTO.getTitle())
                .content(requestDTO.getContent())
                .deadline(requestDTO.getDeadline())
                .voteType(requestDTO.getVoteType())
                .post(post)
                .author(author)
                .build();

        return demandSurveyRepository.save(demandSurvey);
    }
}