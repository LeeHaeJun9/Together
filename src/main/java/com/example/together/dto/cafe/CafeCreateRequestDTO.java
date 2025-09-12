package com.example.together.dto.cafe;

import lombok.Data;

@Data
public class CafeCreateRequestDTO {
    private String name;

    private String description;

    private String category;
}
