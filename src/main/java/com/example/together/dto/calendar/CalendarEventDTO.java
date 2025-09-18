package com.example.together.dto.calendar;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CalendarEventDTO {
    private Long id;
    private String title;
    private String start;
    private String url;
}
