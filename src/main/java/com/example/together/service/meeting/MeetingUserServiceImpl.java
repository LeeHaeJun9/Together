package com.example.together.service.meeting;

import com.example.together.domain.Meeting;
import com.example.together.domain.MeetingJoinStatus;
import com.example.together.domain.MeetingUser;
import com.example.together.domain.User;
import com.example.together.dto.meeting.MeetingDTO;
import com.example.together.dto.meeting.MeetingUserDTO;
import com.example.together.dto.meeting.MyJoinedMeetingsDTO;
import com.example.together.repository.MeetingRepository;
import com.example.together.repository.MeetingUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class MeetingUserServiceImpl implements MeetingUserService{
    private final MeetingRepository meetingRepository;
    private final MeetingUserRepository meetingUserRepository;

    @Transactional
    public List<MeetingUserDTO> getMeetingUsersByMeetingId(Long meetingId) {
        // 1. Meeting 엔티티 찾기
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));

        // 2. MeetingUser 리스트 조회
        List<MeetingUser> meetingUsers = meetingUserRepository.findByMeeting(meeting);

        // 3. DTO 변환
        return meetingUsers.stream()
                .map(mu -> {
                    User user = mu.getUser();
                    return MeetingUserDTO.builder()
                            .id(mu.getId())
                            .userId(user.getId())
                            .userIdName(user.getUserId())      // 로그인 ID
                            .username(user.getName())      // 진짜 이름
                            .nickname(user.getNickname())      // 닉네임
                            .joinStatus(mu.getJoinStatus())
                            .regDate(mu.getRegDate())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean isUserJoined(Long meetingId, Long userId) {
        return meetingUserRepository.existsByMeetingIdAndUserIdAndJoinStatus(meetingId, userId, MeetingJoinStatus.ACCEPTED);
    }
}
