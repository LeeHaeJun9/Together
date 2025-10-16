package com.example.together.repository;

import com.example.together.domain.DemandSurvey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DemandSurveyRepository extends JpaRepository<DemandSurvey,Long> {
    Optional<DemandSurvey> findByPostId(Long postId);
}
