package com.example.together.service.meeting;

import com.example.together.domain.MeetingReview;
import com.example.together.domain.MeetingReviewReply;
import com.example.together.dto.meeting.MeetingReviewReplyDTO;
import com.example.together.repository.MeetingReviewReplyRepository;
import com.example.together.repository.MeetingReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingReviewReplyService {
    private final MeetingReviewReplyRepository replyRepository;
    private final MeetingReviewRepository reviewRepository; // 주입받을 필드 선언

    public Long register(MeetingReviewReplyDTO replyDTO) {
        // DTO에서 받은 reviewId를 사용해 MeetingReview 엔티티를 찾습니다.
        MeetingReview review = reviewRepository.findById(replyDTO.getReviewId())
                .orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + replyDTO.getReviewId()));

        // DTO를 Entity로 변환하며, 찾은 review 엔티티를 설정합니다.
        MeetingReviewReply reply = MeetingReviewReply.builder()
                .text(replyDTO.getText())
                .replyer(replyDTO.getReplyer())
                .review(review) // <<-- 이 부분이 가장 중요합니다!
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
        if (!reply.getReplyer().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to modify this reply.");
        }

        reply.changeText(replyDTO.getText());
        replyRepository.save(reply);
    }

    public void remove(Long replyId) {
        MeetingReviewReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new EntityNotFoundException("Reply not found with id: " + replyId));

        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!reply.getReplyer().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to delete this reply.");
        }

        replyRepository.deleteById(replyId);
    }

    // DTO를 Entity로 변환하는 헬퍼 메서드
    private MeetingReviewReply dtoToEntity(MeetingReviewReplyDTO dto) {
        return MeetingReviewReply.builder()
                .id(dto.getId())
                .text(dto.getText())
                .replyer(dto.getReplyer())
                // 주의: 여기에 review 엔티티를 설정하는 로직이 필요합니다.
                .build();
    }

    // Entity를 DTO로 변환하는 헬퍼 메서드
    private MeetingReviewReplyDTO entityToDto(MeetingReviewReply entity) {
        return MeetingReviewReplyDTO.builder()
                .id(entity.getId())
                .text(entity.getText())
                .replyer(entity.getReplyer())
                .build();
    }
}
