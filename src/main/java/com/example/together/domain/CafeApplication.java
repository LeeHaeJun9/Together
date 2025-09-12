package com.example.together.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CafeApplication extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    private CafeCategory category;

    @Enumerated(EnumType.STRING)
    private CafeApplicationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    private User applicant;

    @Builder
    public CafeApplication(String name, String description, CafeCategory category, User applicant, CafeApplicationStatus status) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.applicant = applicant;
        this.status = status;
    }

    public void approve() {
        this.status = CafeApplicationStatus.APPROVED;
    }
}