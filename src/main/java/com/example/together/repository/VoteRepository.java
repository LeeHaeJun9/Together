package com.example.together.repository;

import com.example.together.domain.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    // 특정 설문조사에 대해 특정 사용자가 이미 투표했는지 확인
    Optional<Vote> findBySurveyIdAndVoterId(Long surveyId, Long voterId);

    @Query("SELECT v.option, COUNT(v) FROM Vote v WHERE v.survey.id = :surveyId GROUP BY v.option")
    List<Object[]> countVotesByOption(@Param("surveyId") Long surveyId);
}