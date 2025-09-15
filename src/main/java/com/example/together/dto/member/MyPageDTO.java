package com.example.together.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPageDTO {

    // 기본 사용자 정보
    private String userId;
    private String nickname;
    private String name;
    private String email;
    private String phone;

    // 프로필 이미지
    private String profileImage;

    // 계정 정보
    private LocalDateTime regDate;
    private String systemRole;

    // 가입한 카페 목록 (간단한 정보만)
    private List<JoinedCafeInfo> joinedCafes;

    // 구매 내역 요약
    private int purchaseCount;
    private int favoriteCount;

    // 내부 클래스: 가입한 카페 정보
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinedCafeInfo {
        private Long cafeId;
        private String cafeName;
        private String cafeImage;
        private int memberCount;
        private LocalDateTime lastMeetingDate;
    }

    // User Entity에서 MyPageDTO로 변환하는 정적 메서드
    public static MyPageDTO fromUser(com.example.together.domain.User user) {
        return MyPageDTO.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())

                .regDate(user.getRegDate())
                .systemRole(user.getSystemRole().toString())
                .joinedCafes(List.of()) // 나중에 Service에서 설정
                .purchaseCount(0) // 나중에 Service에서 설정
                .favoriteCount(0) // 나중에 Service에서 설정
                .build();
    }
}
