package com.example.together.service.meeting;

import com.example.together.dto.PageRequestDTO;
import com.example.together.dto.PageResponseDTO;
import com.example.together.dto.meeting.MeetingDTO;

public interface MeetingService {
    Long MeetingCreate(MeetingDTO meetingDTO, Long cafeId); // 모임 생성
    MeetingDTO MeetingDetail(Long id); //모임 상세
    void MeetingModify(MeetingDTO meetingDTO); // 모임 수정
    void MeetingDelete(Long id);

    PageResponseDTO<MeetingDTO> list(PageRequestDTO pageRequestDTO);
    PageResponseDTO<MeetingDTO> listByCafeId(Long cafeId, PageRequestDTO pageRequestDTO);
    String getUserNicknameById(Long userId);
}