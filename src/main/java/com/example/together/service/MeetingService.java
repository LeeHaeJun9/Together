package com.example.together.service;

import com.example.together.dto.meeting.MeetingDTO;

public interface MeetingService {
    Long MeetingCreate(MeetingDTO meetingDTO); // 모임 생성
    MeetingDTO MeetingDetail(Long id); //모임 상세
    void MeetingModify(MeetingDTO meetingDTO); // 모임 수정
    void MeetingDelete(Long id);
}