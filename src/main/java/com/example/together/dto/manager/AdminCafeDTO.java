package com.example.together.dto.manager;

public class AdminCafeDTO {

    private Long cafeId;           // 카페 ID
    private String cafeName;       // 카페 이름
    private String description;    // 카페 설명
    private String ownerName;      // 카페 운영자
    private String ownerEmail;     // 운영자 이메일
    private String createDate;     // 생성일
    private String status;         // 상태 (ACTIVE, INACTIVE, SUSPENDED)
    private int memberCount;       // 회원 수
    private int postCount;         // 게시글 수
    private String lastActivity;   // 마지막 활동일

    // 기본 생성자
    public AdminCafeDTO() {}

    // 전체 생성자
    public AdminCafeDTO(Long cafeId, String cafeName, String description,
                        String ownerName, String ownerEmail, String createDate,
                        String status, int memberCount, int postCount, String lastActivity) {
        this.cafeId = cafeId;
        this.cafeName = cafeName;
        this.description = description;
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
        this.createDate = createDate;
        this.status = status;
        this.memberCount = memberCount;
        this.postCount = postCount;
        this.lastActivity = lastActivity;
    }

    // Getter & Setter
    public Long getCafeId() { return cafeId; }
    public void setCafeId(Long cafeId) { this.cafeId = cafeId; }

    public String getCafeName() { return cafeName; }
    public void setCafeName(String cafeName) { this.cafeName = cafeName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public String getCreateDate() { return createDate; }
    public void setCreateDate(String createDate) { this.createDate = createDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public int getPostCount() { return postCount; }
    public void setPostCount(int postCount) { this.postCount = postCount; }

    public String getLastActivity() { return lastActivity; }
    public void setLastActivity(String lastActivity) { this.lastActivity = lastActivity; }
}
