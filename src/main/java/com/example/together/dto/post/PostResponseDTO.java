package com.example.together.dto.post;

import com.example.together.domain.Post;
import com.example.together.domain.PostType;
import com.example.together.dto.demandSurvey.DemandSurveyResponseDTO;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostResponseDTO {
    private final Long id;
    private final String title;
    private final String content;
    private final String image;
    private final int viewCount;
    private final PostType postType;
    private final boolean pinned;
    private final Long authorId;
    private final String authorName;
    private final Long cafeId;
    private final boolean isOwner;

    private DemandSurveyResponseDTO demandSurvey;
    private LocalDateTime regDate;

    public PostResponseDTO(Post post, Long userId) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.image = post.getImage();
        this.viewCount = post.getViewCount();
        this.postType = post.getPostType();
        this.pinned = post.isPinned();
        this.authorId = post.getAuthor().getId();
        this.authorName = post.getAuthor().getName();
        this.cafeId = post.getCafe().getId();
        this.isOwner = post.getAuthor().getId().equals(userId);
        this.regDate = post.getRegDate();

        if (post.getDemandSurvey() != null) {
            this.demandSurvey = new DemandSurveyResponseDTO(post.getDemandSurvey());
        } else {
            this.demandSurvey = null;
        }
    }
}