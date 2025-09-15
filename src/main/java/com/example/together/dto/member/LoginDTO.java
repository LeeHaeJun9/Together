package com.example.together.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO {

    @NotBlank(message = "사용자 ID는 필수입니다")
    @Size(min = 3, max = 50, message = "사용자 ID는 3자 이상 50자 이하여야 합니다")
    private String userId;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 4, message = "비밀번호는 4자 이상이어야 합니다")
    private String password;
}

