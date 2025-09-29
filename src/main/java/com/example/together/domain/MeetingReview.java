package com.example.together.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
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

    private LocalDateTime meetingDate;
    private String meetingLocation;
    private String meetingAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id")
    private Cafe cafe;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MeetingReviewReply> replies = new ArrayList<>();


    @OneToMany(mappedBy = "review", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    @OrderBy("sortOrder ASC")
    private List<MeetingReviewImage> images = new ArrayList<>();

    public void change(String title, String content, LocalDateTime meetingDate, String meetingLocation, String meetingAddress) {
        this.title = title;
        this.content = content;
        this.meetingDate = meetingDate;
        this.meetingLocation = meetingLocation;
        this.meetingAddress = meetingAddress;
    }

    public void addImage(String uuid, String fileName) {
        MeetingReviewImage reviewImage = MeetingReviewImage.builder()
                .uuid(uuid)
                .fileName(fileName)
                .review(this)
                .sortOrder(images.size())
                .build();
        images.add(reviewImage);
    }

    public void removeImage(MeetingReviewImage image) {
        this.images.remove(image);
        image.setReview(null); // 양방향 관계에서 참조 끊기
    }
}