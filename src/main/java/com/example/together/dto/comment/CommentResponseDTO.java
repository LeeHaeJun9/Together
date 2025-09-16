package com.example.together.dto.comment;

import com.example.together.domain.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponseDTO {
    private final Long id;
    private final String content;
    private final Long authorId;
    private final String authorName;
    private final LocalDateTime regDate;
    private final boolean isOwner;

    public CommentResponseDTO(Comment comment,  Long userId) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.authorId = comment.getAuthor().getId();
        this.authorName = comment.getAuthor().getNickname();
        this.regDate = comment.getRegDate();
        this.isOwner = comment.getAuthor().getId().equals(userId);
    }
}
