package com.example.together.repository;

import com.example.together.domain.Cafe;
import com.example.together.domain.Post;
import com.example.together.domain.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post,Long> {
    // 특정 카페의 모든 게시글을 조회
    List<Post> findByCafe(Cafe cafe);

    // 특정 카페의 특정 유형(예: 공지사항) 게시글을 조회
    List<Post> findByCafeAndPostType(Cafe cafe, PostType postType);

    void deleteByCafe(Cafe cafe);

    List<Post> findTop5ByCafeAndPostTypeOrderByRegDateDesc(Cafe cafe, PostType postType);

    List<Post> findByCafeOrderByViewCountDesc(Cafe cafe, Pageable pageable);

    Page<Post> findByCafeOrderByPinnedDescRegDateDesc(Cafe cafe, Pageable pageable);

    long countByCafeAndPinnedIsTrue(Cafe cafe);

    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.demandSurvey ds " +
            "WHERE p.id = :postId")
    Optional<Post> findPostWithSurvey(@Param("postId") Long postId);

//    Page<Post> findByCafeId(Long cafeId, Pageable pageable);
}
