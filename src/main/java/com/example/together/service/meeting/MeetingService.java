package com.example.together.service.meeting;

import com.example.together.domain.*;
import com.example.together.dto.PageRequestDTO;
import com.example.together.dto.PageResponseDTO;
import com.example.together.dto.meeting.MeetingDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MeetingService {
    Long MeetingCreate(MeetingDTO meetingDTO, Long cafeId); // 모임 생성

    MeetingDTO MeetingDetail(Long id); // 모임 상세

    void MeetingModify(MeetingDTO meetingDTO); // 모임 수정

    void MeetingDelete(Long id);

    PageResponseDTO<MeetingDTO> list(PageRequestDTO pageRequestDTO);

    PageResponseDTO<MeetingDTO> listByCafeId(Long cafeId, PageRequestDTO pageRequestDTO);

    void applyToMeeting(User user, Long meetingId);

    void cancelMeeting(User user, Long meetingId);

    List<MeetingUser> getApplicantsByMeeting(Long meetingId);

    String getUserNicknameById(Long userId);

    PageResponseDTO<MeetingDTO> listByCategory(String category, PageRequestDTO pageRequestDTO);
}