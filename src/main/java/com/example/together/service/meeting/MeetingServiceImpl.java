package com.example.together.service.meeting;

import com.example.together.domain.*;
import com.example.together.dto.PageRequestDTO;
import com.example.together.dto.PageResponseDTO;
import com.example.together.dto.cafe.CafeResponseDTO;
import com.example.together.dto.meeting.MeetingDTO;
import com.example.together.repository.CafeCalendarRepository;
import com.example.together.repository.CafeRepository;
import com.example.together.repository.MeetingRepository;
import com.example.together.repository.MeetingUserRepository;
import com.example.together.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final MeetingUserRepository meetingUserRepository;
    private final CafeRepository cafeRepository;
    private final CafeCalendarRepository cafeCalendarRepository;

    @Override
    public Long MeetingCreate(MeetingDTO meetingDTO, Long cafeId) {
        // 1. DTO에서 엔티티로 직접 변환합니다.
        // DTO에 있는 userId를 사용하여 User 엔티티를 찾습니다.
        User organizer = userRepository.findByUserId(meetingDTO.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // cafeId를 사용하여 Cafe 엔티티를 찾습니다.
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("카페를 찾을 수 없습니다."));

        // 빌더 패턴으로 Meeting 엔티티를 생성하고 organizer와 cafe를 직접 설정합니다.
        Meeting meeting = Meeting.builder()
                .title(meetingDTO.getTitle())
                .content(meetingDTO.getContent())
                .meetingDate(meetingDTO.getMeetingDate())
                .recruiting(meetingDTO.getRecruiting())
                .visibility(meetingDTO.getVisibility())
                .location(meetingDTO.getLocation())
                .address(meetingDTO.getAddress())
                .organizer(organizer)
                .cafe(cafe)
                .build();

        Long savedMeetingId = meetingRepository.save(meeting).getId();

        // 2. 캘린더 이벤트를 생성하여 CafeCalendar 테이블에 저장합니다.
        CafeCalendar calendarEvent = CafeCalendar.builder()
                .cafe(cafe)
                .meeting(meeting)
                .build();
        cafeCalendarRepository.save(calendarEvent);

        return savedMeetingId;
    }

    @Override
    public MeetingDTO MeetingDetail(Long id) {
        Optional<Meeting> result = meetingRepository.findById(id);
        Meeting meeting = result.orElseThrow(() -> new IllegalArgumentException("Meeting not found with ID: " + id));
        // 엔티티를 DTO로 직접 변환하는 헬퍼 메서드 사용
        return entityToDto(meeting);
    }

    @Override
    public void MeetingModify(MeetingDTO meetingDTO) {
        Optional<Meeting> result = meetingRepository.findById(meetingDTO.getId());
        Meeting meeting = result.orElseThrow(() -> new IllegalArgumentException("Meeting not found with ID: " + meetingDTO.getId()));

        // DTO의 변경사항을 엔티티의 change 메서드를 통해 직접 반영합니다.
        meeting.change(
                meetingDTO.getTitle(),
                meetingDTO.getContent(),
                meetingDTO.getMeetingDate(),
                meetingDTO.getRecruiting(),
                meetingDTO.getVisibility(),
                meetingDTO.getAddress(),
                meetingDTO.getLocation()
        );

        meetingRepository.save(meeting);
    }

    @Override
    public void MeetingDelete(Long id) {
        Optional<CafeCalendar> calendarEntry = cafeCalendarRepository.findByMeetingId(id);


        calendarEntry.ifPresent(cafeCalendarRepository::delete);

        meetingRepository.deleteById(id);
    }

    @Override
    public PageResponseDTO<MeetingDTO> list(PageRequestDTO pageRequestDTO) {
        String[] types = pageRequestDTO.getTypes();
        String keyword = pageRequestDTO.getKeyword();
        Pageable pageable = pageRequestDTO.getPageable("id");

        Page<Meeting> result = meetingRepository.searchAll(types, keyword, pageable);

        // 엔티티 리스트를 DTO 리스트로 직접 변환합니다.
        List<MeetingDTO> dtoList = result.getContent().stream()
                .map(this::entityToDto).collect(Collectors.toList());

        return PageResponseDTO.<MeetingDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int)result.getTotalElements())
                .build();
    }

    @Override
    @Transactional
    public void applyToMeeting(User user, Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("모임이 존재하지 않습니다."));

        // 중복 신청 방지
        if (meetingUserRepository.existsByUserAndMeeting(user, meeting)) {
            throw new IllegalStateException("이미 이 모임에 신청하셨습니다.");

        }

        // Enum으로 변경되었으므로, Enum 상수를 사용하여 모집 상태를 확인합니다.
        if (meeting.getRecruiting() == RecruitingStatus.END) {
            throw new IllegalStateException("모집이 마감된 모임입니다.");
        }

        MeetingUser meetingUser = MeetingUser.builder()
                .user(user)
                .meeting(meeting)
                .joinStatus(MeetingJoinStatus.ACCEPTED)
                .build();

        log.info("Applying user {} to meeting {}", user.getUserId(), meetingId);

        meetingUserRepository.save(meetingUser);
    }

    public void cancelMeeting(User user, Long meetingId) {
        MeetingUser meetingUser = meetingUserRepository
                .findByMeetingIdAndUserId(meetingId, user.getId())
                .orElseThrow(() -> new IllegalStateException("신청 정보가 없습니다."));

        meetingUserRepository.delete(meetingUser);
    }



    public List<MeetingUser> getApplicantsByMeeting(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("모임이 존재하지 않습니다."));
        return meetingUserRepository.findByMeeting(meeting);
    }

    @Override
    public PageResponseDTO<MeetingDTO> listByCafeId(Long cafeId, PageRequestDTO pageRequestDTO) {
//        Pageable pageable = pageRequestDTO.getPageable("meetingDate"); // meetingDate 순으로 정렬
        Pageable pageable = pageRequestDTO.getPageable("id"); // id 순으로 정렬

        Page<Meeting> result = meetingRepository.findByCafeId(cafeId, pageable);

        List<MeetingDTO> dtoList = result.getContent().stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());

        return PageResponseDTO.<MeetingDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int) result.getTotalElements())
                .build();
    }

    @Transactional
    public String getUserNicknameById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return user.getNickname();
    }

    @Override
    public PageResponseDTO<MeetingDTO> listByCategory(String category, PageRequestDTO pageRequestDTO) {

        PageRequest pageable = PageRequest.of(pageRequestDTO.getPage(), pageRequestDTO.getSize());
        Page<Meeting> meetingPage;

        if ("ALL".equalsIgnoreCase(category)) {
            meetingPage = (Page<Meeting>) meetingRepository.findByVisibility(Visibility.PUBLIC, pageable);
        } else {
            CafeCategory cafeCategory = CafeCategory.valueOf(category.toUpperCase());
            meetingPage = (Page<Meeting>) meetingRepository.findByCafe_CategoryAndVisibility(cafeCategory, Visibility.PUBLIC, pageable);
        }

        List<MeetingDTO> dtoList = meetingPage.stream()
                .map(MeetingDTO::fromEntity) // fromEntity 메서드 만들어서 Meeting → MeetingDTO 변환
                .collect(Collectors.toList());

        return PageResponseDTO.<MeetingDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int) meetingPage.getTotalElements())
                .build();
    }

    // 엔티티를 DTO로 변환하는 헬퍼 메서드
    private MeetingDTO entityToDto(Meeting meeting) {
        // organizer가 null일 경우를 대비하여 NullPointerException 방지
        Long organizerId = (meeting.getOrganizer() != null) ? meeting.getOrganizer().getId() : null;
        String organizerName = (meeting.getOrganizer() != null) ? meeting.getOrganizer().getUserId() : null;
        String organizerNickname = (meeting.getOrganizer() != null) ? meeting.getOrganizer().getNickname() : null;

        // Cafe가 null일 경우를 대비하여 NullPointerException 방지
        CafeResponseDTO cafeResponseDTO = null;
        if (meeting.getCafe() != null) {
            cafeResponseDTO = CafeResponseDTO.builder()
                    .id(meeting.getCafe().getId())
                    .name(meeting.getCafe().getName())
                    .category(meeting.getCafe().getCategory().name())
                    .build();
        }

        return MeetingDTO.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .content(meeting.getContent())
                .meetingDate(meeting.getMeetingDate())
                .recruiting(meeting.getRecruiting())
                .visibility(meeting.getVisibility())
                .location(meeting.getLocation())
                .address(meeting.getAddress())
                .organizerId(organizerId)
                .organizerName(organizerName)
                .organizerNickname(organizerNickname)
                .cafe(cafeResponseDTO)
                .regDate(meeting.getRegDate())
                .modDate(meeting.getModDate())
                .build();
    }
}