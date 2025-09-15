package com.example.together.dto.cafe;

import com.example.together.domain.CafeCategory;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CafeUpdateDTO {
    private String name;
    private String description;
    private CafeCategory category;
    private MultipartFile cafeImage;
    private MultipartFile cafeThumbnail;
}