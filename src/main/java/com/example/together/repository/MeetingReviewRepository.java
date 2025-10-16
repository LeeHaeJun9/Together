package com.example.together.repository;

import com.example.together.domain.Meeting;
import com.example.together.domain.MeetingJoinStatus;
import com.example.together.domain.MeetingReview;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MeetingReviewRepository extends JpaRepository<MeetingReview,Long> {

//    @Query("SELECT mr FROM MeetingReview mr JOIN FETCH mr.reviewer WHERE mr.reviewer.userId = :userId")
//    List<MeetingReview> findAllByReviewer(@Param("userId") String userId);
    @Query("SELECT mr FROM MeetingReview mr JOIN FETCH mr.reviewer WHERE mr.reviewer.id = :userId ORDER BY mr.modDate DESC")
    List<MeetingReview> findAllByReviewer(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"images"})
    @Query("select r from MeetingReview r where r.id =:id")
    Optional<MeetingReview> findByIdWithImages(Long id);

    List<MeetingReview> findByMeetingId(Long meetingId);
    Page<MeetingReview> findByCafe_Id(Long cafeId, Pageable pageable);

    // 특정 모임(meeting)에 속한 후기 삭제
    void deleteByMeeting(Meeting meeting);

    // 필요 시 특정 모임 후기 조회
    List<MeetingReview> findByMeeting(Meeting meeting);
}
