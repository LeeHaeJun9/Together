package com.example.together.dto.manager;

public class ManagerDTO {

    private Long managerId;        // 관리자 ID
    private String managerName;    // 관리자 이름
    private String email;          // 이메일
    private String password;       // 비밀번호
    private String role;           // 권한 (SUPER_ADMIN, ADMIN 등)
    private String createDate;     // 생성일
    private String status;         // 상태 (ACTIVE, INACTIVE)

    // 기본 생성자
    public ManagerDTO() {}

    // 전체 생성자
    public ManagerDTO(Long managerId, String managerName, String email,
                      String password, String role, String createDate, String status) {
        this.managerId = managerId;
        this.managerName = managerName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createDate = createDate;
        this.status = status;
    }

    // Getter & Setter
    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCreateDate() { return createDate; }
    public void setCreateDate(String createDate) { this.createDate = createDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
