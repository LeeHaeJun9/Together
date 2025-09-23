package com.example.together.service.meeting;

import com.example.together.domain.Meeting;
import com.example.together.domain.MeetingReview;
import com.example.together.domain.User;
import com.example.together.dto.PageRequestDTO;
import com.example.together.dto.PageResponseDTO;
import com.example.together.dto.meeting.MeetingDTO;
import com.example.together.dto.meeting.MeetingReviewDTO;
import com.example.together.dto.meeting.MeetingReviewImageDTO;
import com.example.together.repository.MeetingRepository;
import com.example.together.repository.MeetingReviewRepository;
import com.example.together.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnailator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class MeetingReviewServiceImpl implements MeetingReviewService {
    private final ModelMapper modelMapper;
    private final MeetingReviewRepository meetingReviewRepository;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    // application.properties에 설정된 업로드 경로 주입
    @Value("${org.zerock.upload.path}")
    private String uploadPath;

    public MeetingReviewDTO EntitytoDTO(MeetingReview mtReview) {
//        List<String> imageUrls = entity.getImages().stream()
//                .map(MeetingReviewImage::getUrl)  // getUrl() 메서드 가정
//                .collect(Collectors.toList());

        Meeting meeting = mtReview.getMeeting();
        MeetingDTO meetingDTO = null;
        if (meeting != null) {
            meetingDTO = getMeetingDTOById(meeting.getId());
        }

        List<MeetingReviewImageDTO> imageDTOList = mtReview.getImages().stream()
                .map(image -> MeetingReviewImageDTO.builder()
                        .uuid(image.getUuid())
                        .fileName(image.getFileName())
                        .sortOrder(image.getSortOrder())
                        .build())
                .collect(Collectors.toList());

        return MeetingReviewDTO.builder()
                .id(mtReview.getId())
                .title(mtReview.getTitle())
                .content(mtReview.getContent())
                .reviewerId(mtReview.getReviewer().getId())
                .reviewerNickname(mtReview.getReviewer().getNickname())
                .reviewerUserId(mtReview.getReviewer().getUserId())
                .meetingId(meetingDTO != null ? meetingDTO.getId() : null)
                .meetingDate(meetingDTO != null ? meetingDTO.getMeetingDate() : mtReview.getMeetingDate())
                .meetingAddress(meetingDTO != null ? meetingDTO.getAddress() : mtReview.getMeetingAddress())
                .meetingLocation(meetingDTO != null ? meetingDTO.getLocation() : mtReview.getMeetingLocation())
                .imageList(imageDTOList)
                .regDate(mtReview.getRegDate())
                .modDate(mtReview.getModDate())
                .build();
    }



    @Override
    public Long MeetingReview(MeetingReviewDTO meetingReviewDTO) {
        MeetingReview meetingReview = modelMapper.map(meetingReviewDTO, MeetingReview.class);
        Long id = meetingReviewRepository.save(meetingReview).getId();
        return id;
    }

    @Override
    public MeetingReviewDTO MeetingReviewDetail(Long id) {
        Optional<MeetingReview> result = meetingReviewRepository.findById(id);
        MeetingReview review = result.orElseThrow(() -> new IllegalArgumentException("Meeting not found with ID: " + id));
//        엔티티를 DTO로 직접 변환하는 헬퍼 메서드 사용
        return EntitytoDTO(review);
    }

    @Override
    public void MeetingReviewModify(MeetingReviewDTO meetingReviewDTO) {
        Optional<MeetingReview> review = meetingReviewRepository.findById(meetingReviewDTO.getId());
        MeetingReview meetingReview = review.orElseThrow();

        meetingReview.change(
                meetingReviewDTO.getTitle(),
                meetingReviewDTO.getContent(),
                meetingReviewDTO.getMeetingDate(),
                meetingReviewDTO.getMeetingLocation(),
                meetingReviewDTO.getMeetingAddress()
        );

        meetingReviewRepository.save(meetingReview);
    }

    @Override
    public void MeetingReviewDelete(Long id) {
        meetingReviewRepository.deleteById(id);
    }


    // 모임 리뷰는 검색 기능 없음
    @Override
    public PageResponseDTO<MeetingReviewDTO> list(PageRequestDTO pageRequestDTO) {
        String[] types = pageRequestDTO.getTypes();
        String keyword = pageRequestDTO.getKeyword();
        Pageable pageable = pageRequestDTO.getPageable("id");

        Page<MeetingReview> result = meetingReviewRepository.findAll(pageable);

        List<MeetingReviewDTO> dtoList = result.getContent().stream()
                .map(this::EntitytoDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<MeetingReviewDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int)result.getTotalElements())
                .build();
    }

    @Override
    @Transactional
    public MeetingReview createReviewWithImages(String userId, Long meetingId, String title, String content, List<MultipartFile> files) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다. userId=" + userId));

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모임이 없습니다. meetingId=" + meetingId));

        MeetingReview review = MeetingReview.builder()
                .title(title)
                .content(content)
                .reviewer(user)
                .meeting(meeting)
                .build();

        if (files != null && !files.isEmpty()) {
            processAndAddImages(review, files);
        }

        return meetingReviewRepository.save(review);
    }
    @Override
    @Transactional
    public MeetingReview writeReviewWithImages(String userId, String title, String content, LocalDateTime meetingDate, String meetingLocation, String meetingAddress, List<MultipartFile> files) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다. userId=" + userId));


        MeetingReview review = MeetingReview.builder()
                .title(title)
                .content(content)
                .reviewer(user)
                .meetingDate(meetingDate)
                .meetingLocation(meetingLocation)
                .meetingAddress(meetingAddress)
                .build();

        if (files != null && !files.isEmpty()) {
            processAndAddImages(review, files);
        }

        return meetingReviewRepository.save(review);
    }

    public MeetingDTO getMeetingDTOById(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found with id: " + meetingId));
        // 엔티티 -> DTO 변환 로직 필요
        return entityToDTO(meeting);
    }

    private MeetingDTO entityToDTO(Meeting meeting) {
        return MeetingDTO.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .content(meeting.getContent())
                .meetingDate(meeting.getMeetingDate())
                .address(meeting.getAddress())
                .location(meeting.getLocation())
                .organizerId(meeting.getOrganizer().getId())
                .organizerName(meeting.getOrganizer().getName())
                .userId(meeting.getOrganizer().getUserId())
                // 필요한 필드 추가
                .build();
    }

    @Override
    public void saveReviewWithImages(String userId, MeetingReviewDTO dto) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("No user: " + userId));

        Meeting meeting = null;
        if (dto.getMeetingId() != null) {
            meeting = meetingRepository.findById(dto.getMeetingId())
                    .orElseThrow(() -> new IllegalArgumentException("No meeting: " + dto.getMeetingId()));
        }

        MeetingReview review = MeetingReview.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .reviewer(user)
                .meeting(meeting)
                .meetingDate(dto.getMeetingDate())
                .meetingLocation(dto.getMeetingLocation())
                .meetingAddress(dto.getMeetingAddress())
                .build();

        // 이미지 파일 처리
        if (dto.getFiles() != null && !dto.getFiles().isEmpty()) {
            processAndAddImages(review, dto.getFiles());
        }

        meetingReviewRepository.save(review);
    }

    @Override
    @Transactional
    public void modifyReviewWithImages(String userId, MeetingReviewDTO dto) {
        MeetingReview review = meetingReviewRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("No review with id: " + dto.getId()));

        // ✅ 기존 이미지 삭제 로직
        if (dto.getRemovedImageUuids() != null && !dto.getRemovedImageUuids().isEmpty()) {
            List<String> removedUuids = dto.getRemovedImageUuids();

            // 파일 시스템 및 엔티티에서 이미지 삭제
            review.getImages().stream()
                    .filter(img -> removedUuids.contains(img.getUuid())) // 삭제할 이미지 필터링
                    .collect(Collectors.toList()) // ConcurrentModificationException 방지
                    .forEach(image -> {
                        // 1. 파일 시스템에서 이미지 삭제
                        String fileName = image.getFileName();
                        File originalFile = new File(uploadPath, image.getUuid() + "_" + fileName);
                        File thumbnailFile = new File(uploadPath, "s_" + image.getUuid() + "_" + fileName);

                        if (originalFile.exists()) {
                            originalFile.delete();
                        }
                        if (thumbnailFile.exists()) {
                            thumbnailFile.delete();
                        }
                        // 2. 엔티티에서 이미지 정보 삭제
                        review.removeImage(image);
                    });
        }

        // ✅ 새로운 이미지 추가
        if (dto.getFiles() != null && !dto.getFiles().isEmpty()) {
            processAndAddImages(review, dto.getFiles());
        }

        // 기존 텍스트 정보 업데이트
        review.change(
                dto.getTitle(),
                dto.getContent(),
                dto.getMeetingDate(),
                dto.getMeetingLocation(),
                dto.getMeetingAddress()
        );

        // 변경된 리뷰 엔티티 저장
        meetingReviewRepository.save(review);
    }

    private void processAndAddImages(MeetingReview review, List<MultipartFile> files) {
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String uuid = UUID.randomUUID().toString();
            String originalName = file.getOriginalFilename();
            String saveName = uuid + "_" + originalName;

            try {
                // 파일 저장
                File saveFile = new File(uploadPath, saveName);
                file.transferTo(saveFile);

                // 썸네일 생성
                if (file.getContentType().startsWith("image")) {
                    File thumbnailFile = new File(uploadPath, "s_" + saveName);
                    Thumbnailator.createThumbnail(saveFile, thumbnailFile, 200, 200);
                }

                // 엔티티에 이미지 정보 추가
                review.addImage(uuid, originalName);

            } catch (IOException e) {
                log.error("파일 업로드 실패: " + originalName, e);
                // 예외 처리 로직 (e.g., 예외 던지기, null 반환)
            }
        }
    }
}