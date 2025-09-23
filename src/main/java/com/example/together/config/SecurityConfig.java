package com.example.together.config;

import com.example.together.domain.User;
import com.example.together.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .authorizeHttpRequests(auth -> auth
                        // 누구나 접근 가능한 공통 경로를 허용
                        .requestMatchers(
                                "/", "/login", "/register", "/member/register", "/member/findId", "/member/findPw",
                                "/css/**", "/js/**", "/images/**", "/assets/**", "/lib/**", "/resources/**",
                                "/join", "/member/register/check-userid", "/member/register/check-email"
                        ).permitAll()
                        // 특정 역할(Role)이 필요한 경로 설정
                        .requestMatchers("/admin/**", "/manager/**").hasRole("ADMIN")
                        // 위에 해당하지 않는 모든 요청은 로그인한 사용자만 허용
                        .anyRequest().authenticated()
                )
                // 폼 로그인 설정
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(customSuccessHandler())
                        .failureUrl("/login?error=true")
                )
                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    // 로그인 성공 후 역할을 확인하여 리디렉션하는 핸들러
    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            String authenticatedUserId = authentication.getName();
            User user = userRepository.findByUserId(authenticatedUserId)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + authenticatedUserId));

            HttpSession session = request.getSession();
            session.setAttribute("loginUser", user);
            session.setAttribute("userId", user.getUserId());

            // 사용자의 권한을 확인하고 리디렉션
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                response.sendRedirect("/manager");
            } else {
                response.sendRedirect("/mainPage");
            }
        };
    }
}
