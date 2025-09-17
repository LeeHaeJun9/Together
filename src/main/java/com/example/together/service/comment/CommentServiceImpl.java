package com.example.together.service.comment;

import com.example.together.domain.Comment;
import com.example.together.domain.Post;
import com.example.together.domain.User;
import com.example.together.dto.comment.CommentCreateRequestDTO;
import com.example.together.dto.comment.CommentResponseDTO;
import com.example.together.repository.CommentRepository;
import com.example.together.repository.PostRepository;
import com.example.together.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    @Override
    public CommentResponseDTO createComment(CommentCreateRequestDTO requestDTO, Long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다."));
        Post post = postRepository.findById(requestDTO.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .content(requestDTO.getContent())
                .author(author)
                .post(post)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return new CommentResponseDTO(savedComment, userId);
    }

    @Override
    public List<CommentResponseDTO> getCommentsByPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        List<Comment> comments = commentRepository.findByPost(post);
        return comments.stream()
                .map(comment -> new CommentResponseDTO(comment, userId))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("댓글을 삭제할 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }

    @Override
    public CommentResponseDTO getCommentById(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        return new CommentResponseDTO(comment, userId);
    }

    @Transactional
    @Override
    public void updateComment(Long commentId, String newContent, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("댓글을 수정할 권한이 없습니다.");
        }

        // 내용 업데이트
        comment.setContent(newContent);
        commentRepository.save(comment);
    }
}
