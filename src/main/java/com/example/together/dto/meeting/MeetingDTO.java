package com.example.together.dto.meeting;

import com.example.together.domain.Address;
import com.example.together.domain.Cafe;
import com.example.together.domain.User;
import com.example.together.domain.Visibility;
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
public class MeetingDTO {
    @NotEmpty
    private Long id;

    @NotEmpty
    private String title;

    @NotEmpty
    private String content;

    private LocalDateTime meetingDate;

    private boolean recruiting;

    private Visibility visibility;

    private User organizer;

    private Cafe cafe;

    private Address addressId;

    private LocalDateTime regDate;

    private LocalDateTime modDate;
}