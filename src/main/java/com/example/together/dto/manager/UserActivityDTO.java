package com.example.together.dto.manager;

public class UserActivityDTO {
    private Long userId;           // 사용자 ID
    private int postCount;         // 작성 게시글 수
    private int commentCount;      // 작성 댓글 수
    private int cafeJoinCount;     // 가입 카페 수
    private String lastActivity;   // 마지막 활동일
    private int reportedCount;     // 신고당한 횟수

    // 기본 생성자
    public UserActivityDTO() {}

    // 전체 생성자
    public UserActivityDTO(Long userId, int postCount, int commentCount,
                           int cafeJoinCount, String lastActivity, int reportedCount) {
        this.userId = userId;
        this.postCount = postCount;
        this.commentCount = commentCount;
        this.cafeJoinCount = cafeJoinCount;
        this.lastActivity = lastActivity;
        this.reportedCount = reportedCount;
    }

    // Getter & Setter
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public int getPostCount() { return postCount; }
    public void setPostCount(int postCount) { this.postCount = postCount; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public int getCafeJoinCount() { return cafeJoinCount; }
    public void setCafeJoinCount(int cafeJoinCount) { this.cafeJoinCount = cafeJoinCount; }

    public String getLastActivity() { return lastActivity; }
    public void setLastActivity(String lastActivity) { this.lastActivity = lastActivity; }

    public int getReportedCount() { return reportedCount; }
    public void setReportedCount(int reportedCount) { this.reportedCount = reportedCount; }

}
