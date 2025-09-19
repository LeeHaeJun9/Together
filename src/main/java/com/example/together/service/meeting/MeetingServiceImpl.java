package com.example.together.service.meeting;

import com.example.together.domain.Cafe;
import com.example.together.domain.CafeCalendar;
import com.example.together.domain.Meeting;
import com.example.together.domain.User;
import com.example.together.dto.PageRequestDTO;
import com.example.together.dto.PageResponseDTO;
import com.example.together.dto.meeting.MeetingDTO;
import com.example.together.repository.CafeCalendarRepository;
import com.example.together.repository.CafeRepository;
import com.example.together.repository.MeetingRepository;
import com.example.together.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
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

    private final ModelMapper modelMapper;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final CafeRepository cafeRepository;
    private final CafeCalendarRepository cafeCalendarRepository;

    @PostConstruct
    public void setupModelMapper() {
        // Integer -> Boolean 변환을 위한 Converter 생성
        Converter<Integer, Boolean> integerToBooleanConverter = context -> {
            Integer source = context.getSource();
            return source != null && source == 1; // 1은 true, 0은 false로 간주
        };

        // User -> Long 변환을 위한 Converter 생성 (DTO로 변환할 때 사용)
        Converter<User, Long> userToLongConverter = context -> {
            User source = context.getSource();
            return source != null ? source.getId() : null;
        };

        // DTO -> User 변환을 위한 Converter 생성 (엔티티로 변환할 때 사용)
        Converter<Long, User> longToUserConverter = context -> {
            Long source = context.getSource();
            return userRepository.findById(source).orElse(null);
        };

        // Meeting 엔티티 -> MeetingDTO 매핑 설정
        modelMapper.typeMap(Meeting.class, MeetingDTO.class).addMappings(mapper -> {
            mapper.using(integerToBooleanConverter).map(Meeting::isRecruiting, MeetingDTO::setRecruiting);
            mapper.using(integerToBooleanConverter).map(Meeting::getVisibility, MeetingDTO::setVisibility);
            mapper.using(userToLongConverter).map(Meeting::getOrganizer, MeetingDTO::setOrganizerId);
            mapper.map(src -> src.getOrganizer().getUserId(), MeetingDTO::setOrganizerName);
        });

        // MeetingDTO -> Meeting 엔티티 매핑 설정
        modelMapper.typeMap(MeetingDTO.class, Meeting.class).addMappings(mapper -> {
            mapper.using(longToUserConverter).map(MeetingDTO::getOrganizerId, Meeting::setOrganizer);
        });
    }


    @Override
    public Long MeetingCreate(MeetingDTO meetingDTO, Long cafeId) {
        // 1. DTO를 엔티티로 변환합니다. 이때 ModelMapper의 매핑 규칙을 따릅니다.
        //    (organizerId -> User 객체로 자동 변환)
        Meeting meeting = modelMapper.map(meetingDTO, Meeting.class);

        // 2. Cafe 객체를 직접 가져와 설정합니다.
        Cafe cafe = cafeRepository.findById(meetingDTO.getCafe().getId())
                .orElseThrow(() -> new IllegalArgumentException("카페를 찾을 수 없습니다."));
        meeting.setCafe(cafe);

        // 3. Meeting 테이블에 저장합니다.
        Long savedMeetingId = meetingRepository.save(meeting).getId();

        // 4. 캘린더 이벤트를 생성하여 CafeCalendar 테이블에 저장합니다.
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
        // PostConstruct에 설정된 ModelMapper 사용
        return modelMapper.map(meeting, MeetingDTO.class);
    }

    @Override
    public void MeetingModify(MeetingDTO meetingDTO) {
        Optional<Meeting> result = meetingRepository.findById(meetingDTO.getId());
        Meeting meeting = result.orElseThrow(() -> new IllegalArgumentException("Meeting not found with ID: " + meetingDTO.getId()));
        meeting.change(
                meetingDTO.getTitle(),
                meetingDTO.getContent(),
                meetingDTO.getMeetingDate(),
                meetingDTO.isRecruiting(),
                meetingDTO.getVisibility(),
                meetingDTO.getAddress()
        );
        // @Transactional 어노테이션이 변경 사항을 자동으로 커밋하지만, 명시적으로 저장하는 것도 좋은 방법입니다.
        meetingRepository.save(meeting);
    }

    @Override
    public void MeetingDelete(Long id) {
        meetingRepository.deleteById(id);
    }

    @Override
    public PageResponseDTO<MeetingDTO> list(PageRequestDTO pageRequestDTO) {
        String[] types = pageRequestDTO.getTypes();
        String keyword = pageRequestDTO.getKeyword();
        Pageable pageable = pageRequestDTO.getPageable("id");

        Page<Meeting> result = meetingRepository.searchAll(types, keyword, pageable);

        List<MeetingDTO> dtoList = result.getContent().stream()
                // PostConstruct에 설정된 ModelMapper 사용
                .map(meeting -> modelMapper.map(meeting, MeetingDTO.class)).collect(Collectors.toList());

        return PageResponseDTO.<MeetingDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int)result.getTotalElements())
                .build();

    }

    @Override
    public PageResponseDTO<MeetingDTO> listByCafeId(Long cafeId, PageRequestDTO pageRequestDTO) {
        // 1. Pageable 객체 생성
        Pageable pageable = pageRequestDTO.getPageable("meetingDate");

        // 2. Repository를 통해 특정 cafeId에 해당하는 모임 목록을 페이징하여 조회
        Page<Meeting> result = meetingRepository.findByCafeId(cafeId, pageable);

        // 3. 조회된 Meeting 엔티티 리스트를 MeetingDTO 리스트로 변환
        List<MeetingDTO> dtoList = result.getContent().stream()
                // PostConstruct에 설정된 ModelMapper 사용
                .map(meeting -> modelMapper.map(meeting, MeetingDTO.class))
                .collect(Collectors.toList());

        // 4. PageResponseDTO 객체를 생성하여 반환
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
}
