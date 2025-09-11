package com.example.together.domain;

public enum TradeCategory {
    ELECTRONICS("전자기기"),
    FASHION("의류/잡화"),
    BOOKS("도서");

    private final String koreanName;

    TradeCategory(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }
}
