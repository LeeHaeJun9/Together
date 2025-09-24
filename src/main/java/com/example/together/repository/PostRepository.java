package com.example.together.repository;

import com.example.together.domain.Cafe;
import com.example.together.domain.Post;
import com.example.together.domain.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

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

//    Page<Post> findByCafeId(Long cafeId, Pageable pageable);
}
