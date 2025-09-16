package com.example.together.service.meeting;

import com.example.together.domain.MeetingReview;
import com.example.together.dto.PageRequestDTO;
import com.example.together.dto.PageResponseDTO;
import com.example.together.dto.meeting.MeetingReviewDTO;
import com.example.together.repository.MeetingReviewRepository;
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
public class MeetingReviewServiceImpl implements MeetingReviewService {
    private final ModelMapper modelMapper;
    private final MeetingReviewRepository meetingReviewRepository;

    @Override
    public Long MeetingReview(MeetingReviewDTO meetingReviewDTO) {
        MeetingReview meetingReview = modelMapper.map(meetingReviewDTO, MeetingReview.class);
        Long id = meetingReviewRepository.save(meetingReview).getId();
        return id;
    }

    @Override
    public MeetingReviewDTO MeetingReviewDetail(Long id) {
        Optional<MeetingReview> review = meetingReviewRepository.findById(id);
        MeetingReview meetingReview = review.orElseThrow();
        MeetingReviewDTO meetingReviewDTO = modelMapper.map(meetingReview, MeetingReviewDTO.class);
        return meetingReviewDTO;
    }

    @Override
    public void MeetingReviewModify(MeetingReviewDTO meetingReviewDTO) {
        Optional<MeetingReview> review = meetingReviewRepository.findById(meetingReviewDTO.getId());
        MeetingReview meetingReview = review.orElseThrow();
        meetingReview.change(meetingReviewDTO.getTitle(), meetingReviewDTO.getContent());
        meetingReviewRepository.save(meetingReview);
    }

    @Override
    public void MeetingReviewDelete(Long id) {
        meetingReviewRepository.deleteById(id);
    }


    // 모임 리뷰는 검색 기능 없음
    @Override
    public PageResponseDTO<MeetingReviewDTO> list(PageRequestDTO pageRequestDTO) {
//        String[] types = pageRequestDTO.getTypes();
//        String keyword = pageRequestDTO.getKeyword();

        Pageable pageable = pageRequestDTO.getPageable("id");

        Page<MeetingReview> result = meetingReviewRepository.findAll(pageable);

        List<MeetingReviewDTO> dtoList = result.getContent().stream()
                .map(meetingReview -> modelMapper.map(meetingReview, MeetingReviewDTO.class)).collect(Collectors.toList());

        return PageResponseDTO.<MeetingReviewDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int)result.getTotalElements())
                .build();
    }
}