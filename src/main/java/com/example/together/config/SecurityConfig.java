package com.example.together.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;
import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toStaticResources;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // CustomAuthSuccessHandler가 없다면 이 줄을 삭제하거나 주석 처리하세요.
    // private final CustomAuthSuccessHandler customAuthSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http ) throws Exception {
        http
//                .csrf(csrf -> csrf.disable( ))
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
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        // .successHandler(customAuthSuccessHandler) // 만약 customAuthSuccessHandler가 없다면 이 줄도 주석 처리
                        .defaultSuccessUrl("/", true) // 기본 성공 URL 설정
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );
        return http.build( );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
