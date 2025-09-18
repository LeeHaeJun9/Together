package com.example.together.config;

import com.example.together.domain.User;
import com.example.together.repository.UserRepository;
import com.example.together.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserService userService;
    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

                .csrf(Customizer.withDefaults()) // Enable CSRF
                .authorizeHttpRequests(auth -> auth
                        // 1. ✅ 정적 리소스를 가장 먼저 허용합니다.
                        .requestMatchers(toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/assets/**", "/lib/**").permitAll()

                        // 2. ✅ 누구나 접근 가능한 공통 경로를 허용합니다.
                        .requestMatchers("/", "/login", "/register", "/member/register", "/member/register/**", "/member/findId", "/member/findPw", "/admin/login").permitAll()

                        // 3. ✅ 특정 역할(Role)이 필요한 경로를 설정합니다.
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 4. ✅ 인증(로그인)이 필요한 경로를 설정합니다.
                        //    - /meeting/register, /api/** 등 로그인해야만 접근 가능한 페이지
                        .requestMatchers("/meeting/**").authenticated()
                        .requestMatchers("/api/**").authenticated()

                        // 5. ✅ 위에 해당하지 않는 모든 요청은 로그인한 사용자만 접근 허용
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .authorizeHttpRequests(authorize -> authorize
                        // 로그인, 회원가입 페이지는 모든 사용자에게 허용
                        .requestMatchers(
                                new AntPathRequestMatcher("/login"),
                                new AntPathRequestMatcher("/join"),
                                new AntPathRequestMatcher("/css/**"),
                                new AntPathRequestMatcher("/js/**"),
                                new AntPathRequestMatcher("/images/**"),
                                new AntPathRequestMatcher("/resources/**")  // ADDED: Static resources
                        ).permitAll()
                        // FIXED: Changed from hasAuthority("ADMIN") to hasRole("ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/admin/**")).hasRole("ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/manager/**")).hasRole("ADMIN")  // ADDED: Manager pages
                        // 나머지 모든 경로는 인증된 사용자만 접근 가능
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login") // 로그인 페이지 URL
                        .loginProcessingUrl("/login") // 로그인 폼 제출 URL
                        .usernameParameter("username") // 사용자 ID 파라미터 이름
                        .passwordParameter("password") // 비밀번호 파라미터 이름
                        .successHandler(customSuccessHandler()) // 로그인 성공 시 처리할 핸들러
                        .failureUrl("/login?error=true") // 로그인 실패 시 URL
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    // 로그인 성공 후 역할을 확인하여 리디렉션하는 핸들러
    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                // 사용자 정보를 세션에 저장
                String email = authentication.getName(); // This is the email used for login

                // Changed from findByUserId to findByEmail since authentication.getName() returns the email
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

                HttpSession session = request.getSession();
                session.setAttribute("loginUser", user);
                session.setAttribute("userId", user.getUserId()); // Store the actual userId from the User object

                // FIXED: Changed from "ADMIN" to "ROLE_ADMIN" to match what's in the logs
                if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                    response.sendRedirect("/manager");  // CHANGED: Redirect to /manager instead of /admin
                } else {
                    response.sendRedirect("/mainPage");
                }
            }
        };
    }
}