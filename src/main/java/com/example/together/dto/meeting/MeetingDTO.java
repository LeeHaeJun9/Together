package com.example.together.dto.meeting;

import com.example.together.domain.Address;
import com.example.together.domain.Cafe;
import com.example.together.domain.User;
import com.example.together.domain.Visibility;
import com.example.together.dto.cafe.CafeResponseDTO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    private Long id;

    @NotEmpty
    private String title;

    @NotEmpty
    private String content;

    @NotNull
    private LocalDateTime meetingDate;

    private boolean recruiting;

    private Visibility visibility = Visibility.PUBLIC;

    private Long organizerId;
    private String organizerName;
    private String userId;

    private CafeResponseDTO cafe;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

    private String address;
    private String location;


}