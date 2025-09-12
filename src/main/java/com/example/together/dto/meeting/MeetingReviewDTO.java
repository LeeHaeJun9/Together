package com.example.together.dto.meeting;

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

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeetingReviewDTO {

    @NotEmpty
    private Long id;

    @NotEmpty
    private String title;

    @NotEmpty
    private String content;

    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    private Meeting meeting;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

//    private Cafe cafe;
}
