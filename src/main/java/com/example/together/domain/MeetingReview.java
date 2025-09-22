package com.example.together.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private User reviewer;

//    @ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne(optional = true)
    private Meeting meeting;

//    @ManyToOne(fetch = FetchType.LAZY)
//    private Cafe cafe;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetingReviewImage> images = new ArrayList<>();


    public void change(String title, String content) {
        this.title = title;
        this.content = content;
    }
}