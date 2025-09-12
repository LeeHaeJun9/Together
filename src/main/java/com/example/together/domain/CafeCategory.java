package com.example.together.domain;

public enum CafeCategory {
    STUDY("스터디"),
    SPORTS("스포츠"),
    MUSIC("음악");


    private final String koreanName;

    CafeCategory(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }
}
