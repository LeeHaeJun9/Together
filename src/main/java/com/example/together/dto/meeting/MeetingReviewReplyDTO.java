package com.example.together.dto.meeting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeetingReviewReplyDTO {
    private Long id;
    private Long reviewId;
    private String text;

    private String replyer;
    private String replyerNickname;

    private LocalDateTime regDate;
    private LocalDateTime modDate;
}
