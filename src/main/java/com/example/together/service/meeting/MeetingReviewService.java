package com.example.together.service.meeting;

import com.example.together.domain.MeetingReview;
import com.example.together.dto.PageRequestDTO;
import com.example.together.dto.PageResponseDTO;
import com.example.together.dto.meeting.MeetingDTO;
import com.example.together.dto.meeting.MeetingReviewDTO;

public interface MeetingReviewService {
    MeetingReviewDTO EntitytoDTO(MeetingReview entity);

    Long MeetingReview(MeetingReviewDTO meetingReviewDTO); // 모임 후기 작성
    MeetingReviewDTO MeetingReviewDetail(Long id); // 모임 후기 상세
    void MeetingReviewModify(MeetingReviewDTO meetingReviewDTO); // 모임 후기 수정
    void MeetingReviewDelete(Long id); // 모임 후기 삭제

    PageResponseDTO<MeetingReviewDTO> list(PageRequestDTO pageRequestDTO);
    MeetingReview createReview(String userId, Long meetingId, String title, String content);
    MeetingReview writeReview(String userId, String title, String content);

    MeetingDTO getMeetingDTOById(Long meetingId);
}
