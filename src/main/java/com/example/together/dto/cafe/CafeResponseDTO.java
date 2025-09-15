package com.example.together.dto.cafe;

import com.example.together.domain.CafeCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CafeResponseDTO {
    private Long id;

    private String name;

    private String description;

    private String category;

    private LocalDateTime regDate;

    private Long ownerId;

    private Integer memberCount;

    private String cafeImage;
    private String cafeThumbnail;
    private boolean isOwner;
    private boolean isMember;
}
