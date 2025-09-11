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

    private String description;

    private String cafeImage;

    private String cafeThumbnail;

    private Integer memberCount;

    @Enumerated(EnumType.STRING) // Enum으로 설정
    private CafeCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;
}
