package com.example.together.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cafe extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String cafeImage;

    @Column(nullable = false)
    private String cafeThumbnail;

    @Column(nullable = false)
    private Integer memberCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CafeCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;
}
