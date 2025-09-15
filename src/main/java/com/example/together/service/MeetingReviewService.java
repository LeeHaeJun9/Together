package com.example.together.service;

import com.example.together.dto.meeting.MeetingReviewDTO;

public interface MeetingReviewService {
    Long MeetingReview(MeetingReviewDTO meetingReviewDTO); // 모임 후기 작성
    MeetingReviewDTO MeetingReviewDetail(Long id); // 모임 후기 상세
    void MeetingReviewModify(MeetingReviewDTO meetingReviewDTO); // 모임 후기 수정
    void MeetingReviewDelete(Long id); // 모임 후기 삭제
}
