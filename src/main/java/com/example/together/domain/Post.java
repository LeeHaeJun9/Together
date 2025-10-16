package com.example.together.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    private String image;

    @Column(nullable = false)
    private int viewCount;

    @Enumerated(EnumType.STRING)
    @Column(length = 300)
    private PostType postType;

    @Enumerated(EnumType.STRING)
    private PostSubType postSubType = PostSubType.GENERAL;

    private boolean pinned;

    @ManyToOne(fetch = FetchType.LAZY)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    private Cafe cafe;

    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private DemandSurvey demandSurvey;

    @PrePersist
    public void prePersist() {
        if (postSubType == null) postSubType = PostSubType.GENERAL;
    }
}
