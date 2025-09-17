package com.example.together.dto.manager;

public class ManagerLoginDTO {

    private String email;      // 로그인 이메일
    private String password;   // 로그인 비밀번호
    private boolean rememberMe; // 로그인 유지

    // 기본 생성자
    public ManagerLoginDTO() {}

    // 생성자
    public ManagerLoginDTO(String email, String password, boolean rememberMe) {
        this.email = email;
        this.password = password;
        this.rememberMe = rememberMe;
    }

    // Getter & Setter
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isRememberMe() { return rememberMe; }
    public void setRememberMe(boolean rememberMe) { this.rememberMe = rememberMe; }
}
