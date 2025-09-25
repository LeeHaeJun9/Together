package com.example.together.service.meeting;

import com.example.together.dto.meeting.MeetingUserDTO;
import com.example.together.dto.meeting.MyJoinedMeetingsDTO;

import java.util.List;

public interface MeetingUserService {
    List<MeetingUserDTO> getMeetingUsersByMeetingId(Long meetingId);
    boolean isUserJoined(Long meetingId, Long userId);
}
