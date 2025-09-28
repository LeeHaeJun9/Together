package com.example.together.dto.meeting;

import com.example.together.domain.MeetingJoinStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeetingUserDTO {
    private Long id;          // MeetingUser ID
    private Long userId;      // User ID 번호
    private String username;  // User 이름
    private String userIdName;
    private String nickname;
    private MeetingJoinStatus joinStatus;
    private LocalDateTime regDate;
}
