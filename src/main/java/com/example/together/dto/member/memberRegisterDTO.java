package com.example.together.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class memberRegisterDTO {

    @NotBlank(message = "아이디는 필수입니다")
    @Size(min = 4, max = 20, message = "아이디는 4~20자 사이여야 합니다")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문자와 숫자만 사용할 수 있습니다")
    private String userId;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 6, max = 20, message = "비밀번호는 6~20자 사이여야 합니다")
    private String password;

    // ✅ 추가된 필드 1: 비밀번호 확인
    @NotBlank(message = "비밀번호 확인은 필수입니다")
    private String confirmPassword;

    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 30, message = "이름은 30자 이하여야 합니다")
    private String name;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다")
    private String email;

    @NotBlank(message = "전화번호는 필수입니다")
    @Pattern(regexp = "^01[0-9]-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다 (예: 010-1234-5678)")
    private String phone;

    // ✅ 추가된 필드 2: 닉네임 (UserServiceImpl에서 사용됨)
    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 30, message = "닉네임은 2~30자 사이여야 합니다")
    private String nickname;

    // Lombok @Data가 자동으로 만들어주는 메소드들:
    // getUserId(), setUserId()
    // getPassword(), setPassword()
    // getConfirmPassword(), setConfirmPassword()  ← 컴파일 오류 해결
    // getName(), setName()
    // getEmail(), setEmail()
    // getPhone(), setPhone()
    // getNickname(), setNickname()  ← 컴파일 오류 해결
}
