package com.example.together.dto.post;

import com.example.together.domain.PostType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCreateRequestDTO {
    private String title;
    private String content;
    private PostType postType;
    private String image;
    private Long cafeId;
}
