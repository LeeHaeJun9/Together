package com.example.together.repository;

import com.example.together.domain.MeetingReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingReviewRepository extends JpaRepository<MeetingReview,Long> {
}
