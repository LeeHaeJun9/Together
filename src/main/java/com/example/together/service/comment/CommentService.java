package com.example.together.service.comment;

import com.example.together.dto.comment.CommentCreateRequestDTO;
import com.example.together.dto.comment.CommentResponseDTO;

import java.util.List;

public interface CommentService {
    CommentResponseDTO createComment(CommentCreateRequestDTO requestDTO, Long userId);

    List<CommentResponseDTO> getCommentsByPost(Long postId, Long userId);

    void deleteComment(Long commentId, Long userId);
    CommentResponseDTO getCommentById(Long commentId, Long userId);
    void updateComment(Long commentId, String newContent, Long userId);
}
