package com.example.together.config;

import com.example.together.domain.Status;
import com.example.together.domain.User;
import com.example.together.repository.UserRepository;
import com.example.together.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

  private final UserRepository userRepository;

  @Autowired
  private CustomOAuth2UserService customOAuth2UserService;

  // ⚠️ PasswordEncoder는 AppConfig에 이미 있으므로 여기서 만들지 않습니다(중복 방지).
  // ✅ 어떤 UserDetailsService를 쓸지 명시
  @Bean
  public DaoAuthenticationProvider daoAuthenticationProvider(
      @Qualifier("customUserDetailsService") UserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder
  ) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
                                         DaoAuthenticationProvider daoAuthenticationProvider) throws Exception {

    // CSRF
    CookieCsrfTokenRepository csrfRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();
    csrfRepo.setCookieName("TOGETHER-XSRF");
    csrfRepo.setCookiePath("/");
    csrfRepo.setHeaderName("X-XSRF-TOKEN");

    CsrfTokenRequestAttributeHandler attr = new CsrfTokenRequestAttributeHandler();
    attr.setCsrfRequestAttributeName("_csrf");

    http
        .csrf(csrf -> csrf
            .csrfTokenRepository(csrfRepo)
            .csrfTokenRequestHandler(attr)
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/", "/intro","/mainPage", "/index", "/home",
                "/login", "/register", "/member/register", "/member/findId", "/member/findPw",
                "/css/**", "/js/**", "/images/**", "/assets/**", "/lib/**", "/resources/**",
                "/join",
                "/member/register/check-userid", "/member/register/check-email",
                "/member/register/check-name", "/member/register/check-nickname", "/member/register/check-phone",
                "/api/member/**",
                "/upload/**"
            ).permitAll()
            .requestMatchers(HttpMethod.GET, "/trade/list").permitAll()
            .requestMatchers(HttpMethod.GET, "/trade/read/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/report").authenticated()
            .requestMatchers("/admin/**", "/manager/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        // ✅ 명시한 DaoAuthenticationProvider 사용
        .authenticationProvider(daoAuthenticationProvider)
        // 폼 로그인
        .formLogin(formLogin -> formLogin
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .usernameParameter("userId")     // 폼 name과 일치
            .passwordParameter("password")
            .successHandler(customSuccessHandler())
            .failureHandler(customFailureHandler()) // failureUrl 대신 handler만 사용
        )
        // OAuth2 로그인
        .oauth2Login(oauth2 -> oauth2
            .loginPage("/login")
            .successHandler(oAuth2SuccessHandler())
            .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
        )
        // 로그아웃
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
            .logoutSuccessUrl("/login?logout=1")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
        );

    // (옵션) CSRF 유틸 필터
    http.addFilterBefore(new CsrfCookieSanitizerFilter(), CsrfFilter.class);
    http.addFilterAfter(new CsrfCookieFilter("TOGETHER-XSRF"), CsrfFilter.class);
    http.addFilterAfter(new CsrfDebugLoggingFilter("TOGETHER-XSRF"), CsrfFilter.class);

    return http.build();
  }

  // ✔ 일반 로그인 성공
  @Bean
  public AuthenticationSuccessHandler customSuccessHandler() {
    return (request, response, authentication) -> {
      String authenticatedUserId = authentication.getName();
      User user = userRepository.findByUserId(authenticatedUserId)
          .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + authenticatedUserId));

      if (user.getStatus() == Status.LOCKED) {
        HttpSession s = request.getSession(false);
        if (s != null) s.invalidate();
        SecurityContextHolder.clearContext();
        response.sendRedirect("/login?locked=1");
        return;
      }

      HttpSession session = request.getSession();
      session.setAttribute("loginUser", user);
      session.setAttribute("userId", user.getUserId());

      response.sendRedirect("/mainPage");
    };
  }

  // ✔ 로그인 실패 (잠김 우선 확인)
  @Bean
  public AuthenticationFailureHandler customFailureHandler() {
    return (request, response, exception) -> {
      String typedId = request.getParameter("userId");
      if (typedId != null) {
        User u = userRepository.findByUserId(typedId).orElse(null);
        if (u != null && u.getStatus() == Status.LOCKED) {
          response.sendRedirect("/login?locked=1");
          return;
        }
      }
      response.sendRedirect("/login?error=1");
    };
  }

  // ✔ OAuth2 성공
  @Bean
  public AuthenticationSuccessHandler oAuth2SuccessHandler() {
    return (request, response, authentication) -> {
      OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
      String socialId = String.valueOf(oauth2User.getAttributes().get("id"));

      User user = userRepository.findByUserId(socialId)
          .orElseThrow(() -> new UsernameNotFoundException("OAuth2 사용자를 찾을 수 없습니다: " + socialId));

      if (user.getStatus() == Status.LOCKED) {
        HttpSession s = request.getSession(false);
        if (s != null) s.invalidate();
        SecurityContextHolder.clearContext();
        response.sendRedirect("/login?locked=1");
        return;
      }

      HttpSession session = request.getSession();
      session.setAttribute("loginUser", user);
      session.setAttribute("userId", user.getUserId());

      response.sendRedirect("/mainPage");
    };
  }
}


//.