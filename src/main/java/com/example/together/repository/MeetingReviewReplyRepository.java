package com.example.together.repository;

import com.example.together.domain.MeetingReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingReviewReplyRepository extends JpaRepository<MeetingReviewReply, Long> {
    // 특정 리뷰 ID에 속한 모든 댓글을 찾는 메서드
    List<MeetingReviewReply> findByReview_Id(Long reviewId);
}
