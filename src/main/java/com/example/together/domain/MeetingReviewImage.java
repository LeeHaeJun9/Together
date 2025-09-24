package com.example.together.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "review")
public class MeetingReviewImage extends BaseEntity implements Comparable<MeetingReviewImage> {
    @Id
    private String uuid;
    private String fileName;
//    private String imageUrl;
    private int sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    private MeetingReview review;

    @Override
    public int compareTo(MeetingReviewImage other) {
        return this.sortOrder - other.sortOrder;
    }
    public void changeReview(MeetingReview review) {
        this.review = review;
    }
}
