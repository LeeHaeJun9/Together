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


    public static MeetingDTO fromEntity(Meeting meeting) {
        return MeetingDTO.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .content(meeting.getContent())
                .meetingDate(meeting.getMeetingDate())
                .recruiting(meeting.getRecruiting())
                .visibility(meeting.getVisibility())
                .organizerId(meeting.getOrganizer() != null ? meeting.getOrganizer().getId() : null)
                .organizerName(meeting.getOrganizer() != null ? meeting.getOrganizer().getNickname() : null)
                .userId(meeting.getOrganizer() != null ? meeting.getOrganizer().getName() : null)
                .cafe(meeting.getCafe() != null ? CafeResponseDTO.fromEntity(meeting.getCafe()) : null)
                .regDate(meeting.getRegDate())
                .modDate(meeting.getModDate())
                .address(meeting.getAddress())
                .location(meeting.getLocation())
                .build();
    }
}