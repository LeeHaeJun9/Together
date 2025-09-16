package com.example.together.service;

import com.example.together.domain.Meeting;
import com.example.together.dto.PageRequestDTO;
import com.example.together.dto.PageResponseDTO;
import com.example.together.dto.meeting.MeetingDTO;
import com.example.together.repository.MeetingRepository;
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
                meetingDTO.getAddressId()
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
}
