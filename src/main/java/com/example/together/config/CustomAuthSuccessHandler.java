package com.example.together.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
@Slf4j
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication ) throws IOException, ServletException {

        String username = authentication.getName();
        log.info("로그인 성공! 사용자: {}", username);

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // 사용자의 첫 번째 권한을 확인합니다.
        authorities.stream().findFirst().ifPresent(authority -> {
            String role = authority.getAuthority();
            log.info("사용자 역할: {}", role);

            try {
                if (role.equals("ROLE_ADMIN")) {
                    log.info("관리자 페이지로 리다이렉트합니다.");
                    response.sendRedirect("/admin/manager"); // 관리자 페이지 경로
                } else {
                    log.info("마이페이지로 리다이렉트합니다.");
                    response.sendRedirect("/mypage"); // 일반 사용자 페이지 경로
                }
            } catch (IOException e) {
                log.error("리다이렉트 중 오류 발생", e);
            }
        });
    }
}

