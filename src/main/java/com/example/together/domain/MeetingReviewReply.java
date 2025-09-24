package com.example.together.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(exclude = "review")
public class MeetingReviewReply extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    private String replyer;

    @ManyToOne(fetch = FetchType.LAZY)
    private MeetingReview review; // 이 댓글이 속한 리뷰

    // 댓글 내용 수정 메서드
    public void changeText(String text) {
        this.text = text;
    }
}
