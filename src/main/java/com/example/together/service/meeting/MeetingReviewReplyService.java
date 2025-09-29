package com.example.together.service.meeting;

import com.example.together.domain.MeetingReview;
import com.example.together.domain.MeetingReviewReply;
import com.example.together.domain.User;
import com.example.together.dto.meeting.MeetingReviewReplyDTO;
import com.example.together.repository.MeetingReviewReplyRepository;
import com.example.together.repository.MeetingReviewRepository;
import com.example.together.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingReviewReplyService {
    private final MeetingReviewReplyRepository replyRepository;
    private final MeetingReviewRepository reviewRepository; // 주입받을 필드 선언
    private final UserRepository userRepository;

    public Long register(MeetingReviewReplyDTO replyDTO) {
        User user = userRepository.findByUsername(replyDTO.getReplyer())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + replyDTO.getReplyer()));

        // DTO에서 받은 reviewId를 사용해 MeetingReview 엔티티를 찾습니다.
        MeetingReview review = reviewRepository.findById(replyDTO.getReviewId())
                .orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + replyDTO.getReviewId()));

        // DTO를 Entity로 변환하며, 찾은 review 엔티티를 설정합니다.
        MeetingReviewReply reply = MeetingReviewReply.builder()
                .text(replyDTO.getText())
                .replyer(user)
                .review(review)
                .build();

        replyRepository.save(reply);
        return reply.getId();
    }

    public List<MeetingReviewReplyDTO> getList(Long reviewId) {
        List<MeetingReviewReply> replies = replyRepository.findByReview_Id(reviewId);
        // Entity -> DTO 변환
        return replies.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    public void modify(MeetingReviewReplyDTO replyDTO) {
        // ID로 엔티티를 찾고, 없으면 예외 발생
        MeetingReviewReply reply = replyRepository.findById(replyDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("Reply not found with id: " + replyDTO.getId()));

        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!reply.getReplyer().getUserId().equals(currentUserId)) {  // User 객체에서 아이디를 꺼내서 비교
            throw new AccessDeniedException("You do not have permission to modify this reply.");
        }

        reply.changeText(replyDTO.getText());
        replyRepository.save(reply);
    }

    public void remove(Long replyId) {
        MeetingReviewReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new EntityNotFoundException("Reply not found with id: " + replyId));

        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!reply.getReplyer().getUserId().equals(currentUserId)) {  // User 객체에서 아이디를 꺼내서 비교
            throw new AccessDeniedException("You do not have permission to modify this reply.");
        }

        replyRepository.deleteById(replyId);
    }

    // DTO를 Entity로 변환하는 헬퍼 메서드
    private MeetingReviewReply dtoToEntity(MeetingReviewReplyDTO dto) {
        User user = userRepository.findByUsername(dto.getReplyer())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + dto.getReplyer()));

        MeetingReview review = reviewRepository.findById(dto.getReviewId())
                .orElseThrow(() -> new EntityNotFoundException("Review not found: " + dto.getReviewId()));

        return MeetingReviewReply.builder()
                .id(dto.getId())
                .text(dto.getText())
                .replyer(user)
                .review(review)
                .build();
    }

    // Entity를 DTO로 변환하는 헬퍼 메서드
    private MeetingReviewReplyDTO entityToDto(MeetingReviewReply entity) {
        return MeetingReviewReplyDTO.builder()
                .id(entity.getId())
                .text(entity.getText())
                .replyer(entity.getReplyer().getUserId())
                .replyerNickname(entity.getReplyer().getNickname())
                .reviewId(entity.getReview().getId())
                .regDate(entity.getRegDate())
                .modDate(entity.getModDate())
                .build();
    }
}
