package com.example.together.dto.manager;

public class DashBoardDTO {

    private int totalUsers;        // 전체 사용자 수
    private int totalCafes;        // 전체 카페 수
    private int totalPosts;        // 전체 게시글 수
    private int todayJoinUsers;    // 오늘 가입자 수
    private int activeCafes;       // 활성 카페 수
    private int reportCount;       // 신고 건수

    // 기본 생성자
    public DashBoardDTO() {}

    // 전체 생성자
    public DashBoardDTO(int totalUsers, int totalCafes, int totalPosts,
                        int todayJoinUsers, int activeCafes, int reportCount) {
        this.totalUsers = totalUsers;
        this.totalCafes = totalCafes;
        this.totalPosts = totalPosts;
        this.todayJoinUsers = todayJoinUsers;
        this.activeCafes = activeCafes;
        this.reportCount = reportCount;
    }

    // Getter & Setter
    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }

    public int getTotalCafes() { return totalCafes; }
    public void setTotalCafes(int totalCafes) { this.totalCafes = totalCafes; }

    public int getTotalPosts() { return totalPosts; }
    public void setTotalPosts(int totalPosts) { this.totalPosts = totalPosts; }

    public int getTodayJoinUsers() { return todayJoinUsers; }
    public void setTodayJoinUsers(int todayJoinUsers) { this.todayJoinUsers = todayJoinUsers; }

    public int getActiveCafes() { return activeCafes; }
    public void setActiveCafes(int activeCafes) { this.activeCafes = activeCafes; }

    public int getReportCount() { return reportCount; }
    public void setReportCount(int reportCount) { this.reportCount = reportCount; }
}
