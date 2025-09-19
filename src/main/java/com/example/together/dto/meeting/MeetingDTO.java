package com.example.together.dto.meeting;

import com.example.together.domain.*;
import com.example.together.dto.cafe.CafeResponseDTO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

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
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime meetingDate;

    private RecruitingStatus recruiting;

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