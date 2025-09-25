package com.example.together.dto.meeting;

import com.example.together.domain.Membership;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyJoinedMeetingsDTO {
    private List<MeetingDTO> upcomingMeetings;  // 예정된 모임 리스트
    private List<MeetingDTO> completedMeetings; // 완료된 모임 리스트
    private List<MeetingDTO> hostedMeetings;    // 주최한 모임 리스트
    private List<MeetingReviewDTO> myReviews;   // 작성한 후기 리스트

    private long countUpcoming;
    private long countComplete;
    private long countHosted;
    private long countMyReviews;
}
