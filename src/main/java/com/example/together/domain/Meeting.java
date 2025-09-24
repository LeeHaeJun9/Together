package com.example.together.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meeting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime meetingDate;

    @Enumerated(EnumType.STRING)
    private RecruitingStatus recruiting;

    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    @ManyToOne(fetch = FetchType.LAZY)
    private User organizer;

    @ManyToOne(fetch = FetchType.LAZY)
    private Cafe cafe;

//    @ManyToOne(fetch = FetchType.LAZY)
//    private Address addressId;
    private String address;
    private String location;

    public void change(String title, String content, LocalDateTime meetingDate, RecruitingStatus recruiting, Visibility visibility, String address, String location) {
        this.title = title;
        this.content = content;
        this.meetingDate = meetingDate;
        this.recruiting = recruiting;
        this.visibility = visibility;
        this.address = address;
        this.location = location;
    }
}
