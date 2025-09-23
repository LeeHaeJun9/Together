package com.example.together.service.meeting;

import com.example.together.domain.MeetingReview;
import com.example.together.dto.PageRequestDTO;
import com.example.together.dto.PageResponseDTO;
import com.example.together.dto.meeting.MeetingDTO;
import com.example.together.dto.meeting.MeetingReviewDTO;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetingReviewService {
    MeetingReviewDTO EntitytoDTO(MeetingReview entity);

    Long MeetingReview(MeetingReviewDTO meetingReviewDTO); // 모임 후기 작성
    MeetingReviewDTO MeetingReviewDetail(Long id); // 모임 후기 상세
    void MeetingReviewModify(MeetingReviewDTO meetingReviewDTO); // 모임 후기 수정
    void MeetingReviewDelete(Long id); // 모임 후기 삭제

    PageResponseDTO<MeetingReviewDTO> list(PageRequestDTO pageRequestDTO);

    // 이미지 파일까지 함께 저장하는 메서드들 추가
    MeetingReview createReviewWithImages(String userId, Long meetingId, String title, String content, List<MultipartFile> files);
    MeetingReview writeReviewWithImages(String userId, String title, String content, LocalDateTime meetingDate, String meetingLocation, String meetingAddress, List<MultipartFile> files);
//    MeetingReview createReview(String userId, Long meetingId, String title, String content);
//    public MeetingReview writeReview(String userId, String title, String content, LocalDateTime meetingDate, String meetingLocation, String meetingAddress);

    MeetingDTO getMeetingDTOById(Long meetingId);

    // 이미지를 포함한 리뷰 저장
    void saveReviewWithImages(String userId, MeetingReviewDTO dto);
    // 이미지를 포함한 리뷰 수정
    void modifyReviewWithImages(String userId, MeetingReviewDTO dto);
}
