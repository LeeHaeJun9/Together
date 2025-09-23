package com.example.together.repository;

import com.example.together.domain.MeetingReview;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MeetingReviewRepository extends JpaRepository<MeetingReview,Long> {

    @Query("SELECT mr FROM MeetingReview mr JOIN FETCH mr.reviewer WHERE mr.reviewer.userId = :userId")
    List<MeetingReview> findAllByReviewer(@Param("userId") String userId);

    @EntityGraph(attributePaths = {"images"})
    @Query("select r from MeetingReview r where r.id =:id")
    Optional<MeetingReview> findByIdWithImages(Long id);
}
