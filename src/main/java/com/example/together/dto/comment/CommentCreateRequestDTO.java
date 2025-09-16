package com.example.together.dto.comment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentCreateRequestDTO {
    private String content;

    private Long postId;
}
