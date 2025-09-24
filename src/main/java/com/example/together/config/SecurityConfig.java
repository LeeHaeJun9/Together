package com.example.together.config;

import com.example.together.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;

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
import org.springframework.security.oauth2.core.user.OAuth2User;
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

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .authorizeHttpRequests(auth -> auth
                        // 누구나 접근 가능한 공통 경로를 허용
                        .requestMatchers(
                                "/", "/login", "/register", "/member/register", "/member/findId", "/member/findPw",
                                "/css/**", "/js/**", "/images/**", "/assets/**", "/lib/**", "/resources/**",
                                "/join", "/member/register/check-userid", "/member/register/check-email",
                                "/member/register/check-name", "/member/register/check-nickname", "/member/register/check-phone",
                                "/api/member/**"  // API 경로 추가
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
                // OAuth2 로그인 설정 수정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(oAuth2SuccessHandler()) // OAuth2 전용 성공 핸들러 추가
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
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

    // 일반 로그인 성공 핸들러
    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            String authenticatedUserId = authentication.getName();
            User user = userRepository.findByUserId(authenticatedUserId)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + authenticatedUserId));

            HttpSession session = request.getSession();
            session.setAttribute("loginUser", user);
            session.setAttribute("userId", user.getUserId());

            response.sendRedirect("/mainPage");
        };
    }

    // OAuth2 로그인 성공 핸들러 (새로 추가)
    @Bean
    public AuthenticationSuccessHandler oAuth2SuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String socialId = (String) oauth2User.getAttributes().get("id");

            // 소셜 로그인 사용자 정보를 DB에서 조회
            User user = userRepository.findByUserId(socialId)
                    .orElseThrow(() -> new UsernameNotFoundException("OAuth2 사용자를 찾을 수 없습니다: " + socialId));

            HttpSession session = request.getSession();
            session.setAttribute("loginUser", user);
            session.setAttribute("userId", user.getUserId());

            response.sendRedirect("/mainPage");
        };
    }
}