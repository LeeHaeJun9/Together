package com.example.together.service.meeting;

import com.example.together.domain.*;
import com.example.together.dto.cafe.CafeResponseDTO;
import com.example.together.dto.meeting.MeetingDTO;
import com.example.together.dto.meeting.MeetingReviewDTO;
import com.example.together.dto.meeting.MyJoinedMeetingsDTO;
import com.example.together.repository.MeetingRepository;
import com.example.together.repository.MeetingReviewRepository;
import com.example.together.repository.MeetingUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class MeetingMyPageService {
    private final MeetingUserRepository meetingUserRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingReviewRepository meetingReviewRepository;

    private MeetingDTO toMeetingDto(Meeting meeting) {
        if (meeting == null) return null;

        return MeetingDTO.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .meetingDate(meeting.getMeetingDate())
                .location(meeting.getLocation())
                .cafe(toCafeDto(meeting.getCafe()))
                .build();
    }

    private MeetingReviewDTO toMeetingReviewDto(MeetingReview review) {
        if (review == null) return null;

//        CafeResponseDTO cafeDto = null;
//        if (review.getMeeting() != null && review.getMeeting().getCafe() != null) {
//            cafeDto = toCafeDto(review.getMeeting().getCafe());
//        }

        String cafeName = null;
        if (review.getMeeting() != null && review.getMeeting().getCafe() != null) {
            cafeName = review.getMeeting().getCafe().getName();
        }

        return MeetingReviewDTO.builder()
                .id(review.getId())
                .title(review.getTitle())
                .meetingLocation(review.getMeetingLocation())
                .meetingDate(review.getMeetingDate())
//                .cafeName(cafeDto.getName())
//                .cafeId(cafeDto.getId())
                .cafeName(cafeName)
                .build();
    }

    private CafeResponseDTO toCafeDto(Cafe cafe) {
        if (cafe == null) return null;

        return CafeResponseDTO.builder()
                .id(cafe.getId())
                .name(cafe.getName())
                .cafeImage(cafe.getCafeImage())
                .cafeThumbnail(cafe.getCafeThumbnail())
                .build();
    }


    @Transactional
    public MyJoinedMeetingsDTO getMyJoinedMeetings(Long userId, MeetingJoinStatus joinStatus) {
        // meetingUser 가져옴
//        List<MeetingUser> meetingUsers = meetingUserRepository.findByUserIdAndJoinStatus(userId, joinStatus);

        // 예정된 모임 리스트
        List<Meeting> upcomingMeetings = meetingUserRepository.findUpcomingMeetingsByUserId(userId);
        List<MeetingDTO> upcomings = upcomingMeetings.stream()
                .map(this::toMeetingDto)   // 직접 만든 매핑 함수 사용
                .collect(Collectors.toList());

        long countUpcoming = upcomings.size();

        // 완료된 모임 리스트
        List<Meeting> completedMeetings = meetingUserRepository.findCompletedMeetingsByUserId(userId);
        List<MeetingDTO> completes = completedMeetings.stream()
                .map(this::toMeetingDto)
                .collect(Collectors.toList());

        long countComplete = completes.size();

        // 주최한 모임
        List<Meeting> hostedMeetings = meetingRepository.findMeetingsHostedByUserId(userId);
        List<MeetingDTO> hosteds = hostedMeetings.stream()
                .map(this::toMeetingDto)
                .collect(Collectors.toList());
        long countHosted = hosteds.size();

        // 작성한 후기 리스트
        List<MeetingReview> reviews = meetingReviewRepository.findAllByReviewer(userId);
        List<MeetingReviewDTO> myReviews = reviews.stream()
                .map(this::toMeetingReviewDto)
                .collect(Collectors.toList());
        long countMyReviews = myReviews.size();

        return MyJoinedMeetingsDTO.builder()
                .upcomingMeetings(upcomings)
                .completedMeetings(completes)
                .hostedMeetings(hosteds)
                .myReviews(myReviews)
                .countUpcoming(countUpcoming)
                .countComplete(countComplete)
                .countHosted(countHosted)
                .countMyReviews(countMyReviews)
                .build();
    }





}
