package com.example.together.repository;

import com.example.together.domain.Comment;
import com.example.together.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findByPost(Post post);
    void deleteByPost(Post post);
}
