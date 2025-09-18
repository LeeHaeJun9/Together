package com.example.together.dto.vote;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteResponseDTO {
    private String option;
    private Long count;
    private List<String> voterNicknames;
}