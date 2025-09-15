package com.example.together.service;

import com.example.together.domain.MeetingReview;
import com.example.together.dto.meeting.MeetingReviewDTO;
import com.example.together.repository.MeetingReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
}