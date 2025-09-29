package com.example.together.dto.meeting;

import com.example.together.domain.Cafe;
import com.example.together.domain.Meeting;
import com.example.together.domain.User;
import com.example.together.dto.cafe.CafeResponseDTO;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

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

    private Meeting meeting;
    private Long meetingId;
    private LocalDateTime meetingDate;
    private String meetingLocation;
    private String meetingAddress;

    private List<MultipartFile> files;
    private List<MeetingReviewImageDTO> imageList; // 이미지 리스트
    private List<String> removedImageUuids; // 삭제할 이미지 리스트

    private LocalDateTime regDate;  // 생성일
    private LocalDateTime modDate;  // 수정일

    private CafeResponseDTO cafe;
    private String cafeName;
    private Long cafeId;
    private String category;
}