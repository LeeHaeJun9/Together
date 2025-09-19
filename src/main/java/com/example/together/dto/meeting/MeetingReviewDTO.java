package com.example.together.dto.meeting;

import com.example.together.domain.Cafe;
import com.example.together.domain.Meeting;
import com.example.together.domain.User;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeetingReviewDTO {

    private Long id;

    @NotEmpty
    private String title;

    @NotEmpty
    private String content;

    private Long reviewerId; // id 번호
    private String reviewerNickname; // 닉네임
    private String reviewerUserId; // 유저 아이디

    private Long meetingId;
    private LocalDateTime meetingDate;
    private String meetingLocation;
    private String meetingAddress;

    private List<String> imageUrls;

    private LocalDateTime regDate;  // 생성일
    private LocalDateTime modDate;  // 수정일
}