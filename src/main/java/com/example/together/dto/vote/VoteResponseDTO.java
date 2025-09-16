package com.example.together.dto.vote;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteResponseDTO {
    private String option;
    private Long count;
}