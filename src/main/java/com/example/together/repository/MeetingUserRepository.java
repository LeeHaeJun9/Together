package com.example.together.repository;

import com.example.together.domain.Meeting;
import com.example.together.domain.MeetingJoinStatus;
import com.example.together.domain.MeetingUser;
import com.example.together.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingUserRepository extends JpaRepository<MeetingUser,Long> {
    boolean existsByUserAndMeeting(User user, Meeting meeting);
    boolean existsByMeetingIdAndUserId(Long meetingId, Long userId);
    List<MeetingUser> findByMeeting(Meeting meeting);
    Optional<MeetingUser> findByMeetingIdAndUserId(Long meetingId, Long userId);
    boolean existsByMeetingIdAndUserIdAndJoinStatus(Long meetingId, Long userId, MeetingJoinStatus joinStatus);

}
