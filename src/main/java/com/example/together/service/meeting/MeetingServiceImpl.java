package com.example.together.service.meeting;

import com.example.together.domain.Meeting;
import com.example.together.domain.User;
import com.example.together.dto.PageRequestDTO;
import com.example.together.dto.PageResponseDTO;
import com.example.together.dto.meeting.MeetingDTO;
import com.example.together.repository.MeetingRepository;
import com.example.together.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Override
    public Long MeetingCreate(MeetingDTO meetingDTO) {
        Meeting meeting = modelMapper.map(meetingDTO, Meeting.class);
        Long id = meetingRepository.save(meeting).getId();
        return id;
    }

    @Override
    public MeetingDTO MeetingDetail(Long id) {
        Optional<Meeting> result = meetingRepository.findById(id);
        Meeting meeting = result.orElseThrow();
        MeetingDTO meetingDTO = modelMapper.map(meeting, MeetingDTO.class);
        return meetingDTO;
    }

    @Override
    public void MeetingModify(MeetingDTO meetingDTO) {
        Optional<Meeting> result = meetingRepository.findById(meetingDTO.getId());
        Meeting meeting = result.orElseThrow();
        meeting.change(
                meetingDTO.getTitle(),
                meetingDTO.getContent(),
                meetingDTO.getMeetingDate(),
                meetingDTO.isRecruiting(),
                meetingDTO.getVisibility(),
                meetingDTO.getAddress()
        );
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
