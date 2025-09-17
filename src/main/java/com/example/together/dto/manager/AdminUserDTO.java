package com.example.together.dto.manager;

public class AdminUserDTO {

    private Long userId;           // 사용자 ID
    private String userName;       // 사용자 이름
    private String email;          // 이메일
    private String nickname;       // 닉네임
    private String joinDate;       // 가입일
    private String lastLogin;      // 마지막 로그인
    private String status;         // 상태 (ACTIVE, INACTIVE, SUSPENDED, BANNED)
    private int cafeCount;         // 가입한 카페 수
    private int postCount;         // 작성한 게시글 수
    private int commentCount;      // 작성한 댓글 수
    private String warningCount;   // 경고 횟수

    // 기본 생성자
    public AdminUserDTO() {}

    // 전체 생성자
    public AdminUserDTO(Long userId, String userName, String email, String nickname,
                        String joinDate, String lastLogin, String status,
                        int cafeCount, int postCount, int commentCount, String warningCount) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.nickname = nickname;
        this.joinDate = joinDate;
        this.lastLogin = lastLogin;
        this.status = status;
        this.cafeCount = cafeCount;
        this.postCount = postCount;
        this.commentCount = commentCount;
        this.warningCount = warningCount;
    }

    // Getter & Setter
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getJoinDate() { return joinDate; }
    public void setJoinDate(String joinDate) { this.joinDate = joinDate; }

    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getCafeCount() { return cafeCount; }
    public void setCafeCount(int cafeCount) { this.cafeCount = cafeCount; }

    public int getPostCount() { return postCount; }
    public void setPostCount(int postCount) { this.postCount = postCount; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public String getWarningCount() { return warningCount; }
    public void setWarningCount(String warningCount) { this.warningCount = warningCount; }
}
