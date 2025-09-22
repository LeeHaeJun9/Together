package com.example.together.domain;

public enum CafeCategory {
    STUDY("스터디", "/images/category-study.jpg"),
    SPORTS("스포츠", "/images/category-sports.jpg"),
    MUSIC("음악", "/images/category-music.png"),
    ART("예술", "/images/category-art.jpg"),
    PET("반려동물", "/images/category-pet.jpg"),
    TRAVEL("여행", "/images/category-travel.jpg"),
    COOK("요리", "/images/category-cooking.jpg");

    private final String koreanName;
    private final String imageUrl; // 이미지 URL 필드 추가

    CafeCategory(String koreanName, String imageUrl) {
        this.koreanName = koreanName;
        this.imageUrl = imageUrl;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public String getImageUrl() { // imageUrl getter 메서드 추가
        return imageUrl;
    }
}