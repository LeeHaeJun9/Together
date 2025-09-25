package com.example.together.repository;

import com.example.together.domain.Meeting;
import com.example.together.domain.MeetingJoinStatus;
import com.example.together.domain.MeetingUser;
import com.example.together.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeetingUserRepository extends JpaRepository<MeetingUser,Long> {
    boolean existsByUserAndMeeting(User user, Meeting meeting);
    boolean existsByMeetingIdAndUserId(Long meetingId, Long userId);
    List<MeetingUser> findByMeeting(Meeting meeting);
    Optional<MeetingUser> findByMeetingIdAndUserId(Long meetingId, Long userId);
    boolean existsByMeetingIdAndUserIdAndJoinStatus(Long meetingId, Long userId, MeetingJoinStatus joinStatus);

    List<MeetingUser> findByUserIdAndJoinStatus(Long userId, MeetingJoinStatus joinStatus);

    // 예정된 모임 리스트
    @Query("SELECT mu.meeting FROM MeetingUser mu " +
            "WHERE mu.user.id = :userId " +
            "AND mu.joinStatus = 'ACCEPTED' " +
            "AND mu.meeting.meetingDate > CURRENT_TIMESTAMP")
    List<Meeting> findUpcomingMeetingsByUserId(Long userId);

    // 완료된 모임 리스트
    @Query("SELECT mu.meeting FROM MeetingUser mu " +
            "WHERE mu.user.id = :userId " +
            "AND mu.joinStatus = 'ACCEPTED' " +
            "AND mu.meeting.meetingDate <= CURRENT_TIMESTAMP")
    List<Meeting> findCompletedMeetingsByUserId(Long userId);
}
