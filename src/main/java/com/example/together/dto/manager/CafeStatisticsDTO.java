package com.example.together.dto.manager;

public class CafeStatisticsDTO {

    private Long cafeId;           // 카페 ID
    private int memberCount;       // 회원 수
    private int postCount;         // 게시글 수
    private int commentCount;      // 댓글 수
    private int monthlyVisitor;    // 월 방문자 수
    private String popularPost;    // 인기 게시글

    // 기본 생성자
    public CafeStatisticsDTO() {}

    // 전체 생성자
    public CafeStatisticsDTO(Long cafeId, int memberCount, int postCount,
                             int commentCount, int monthlyVisitor, String popularPost) {
        this.cafeId = cafeId;
        this.memberCount = memberCount;
        this.postCount = postCount;
        this.commentCount = commentCount;
        this.monthlyVisitor = monthlyVisitor;
        this.popularPost = popularPost;
    }

    // Getter & Setter
    public Long getCafeId() { return cafeId; }
    public void setCafeId(Long cafeId) { this.cafeId = cafeId; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public int getPostCount() { return postCount; }
    public void setPostCount(int postCount) { this.postCount = postCount; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public int getMonthlyVisitor() { return monthlyVisitor; }
    public void setMonthlyVisitor(int monthlyVisitor) { this.monthlyVisitor = monthlyVisitor; }

    public String getPopularPost() { return popularPost; }
    public void setPopularPost(String popularPost) { this.popularPost = popularPost; }
}
