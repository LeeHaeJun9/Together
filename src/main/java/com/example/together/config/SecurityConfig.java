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

import java.util.Map;

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
            // ✅ 누구나 접근 가능
            .requestMatchers(
                    "/", "/intro","/mainPage", "/index", "/home",
                    "/login", "/register", "/member/register", "/member/findId", "/member/findPw",
                    "/css/**", "/js/**", "/images/**", "/assets/**", "/lib/**", "/resources/**",
                    "/join", "/categories", "/cafe/{id}", // 두 번째 코드 블록에서 추가된 경로
                    "/member/register/check-userid", "/member/register/check-email",
                    "/member/register/check-name", "/member/register/check-nickname", "/member/register/check-phone",
                    "/api/member/**",
                    "/upload/**"
            ).permitAll()

            .requestMatchers(HttpMethod.GET, "/trade/list").permitAll()

            .requestMatchers(HttpMethod.GET, "/trade/read/*").permitAll()


            // 역할 필요한 경로
            .requestMatchers("/admin/**", "/manager/**").hasRole("ADMIN")
            // 그 외는 인증 필요
            .anyRequest().authenticated()
        )
        // 폼 로그인
        .formLogin(formLogin -> formLogin
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .usernameParameter("userId")
            .passwordParameter("password")
            .successHandler(customSuccessHandler())
            .failureUrl("/login?error=true")
        )
        // OAuth2 로그인
        .oauth2Login(oauth2 -> oauth2
            .loginPage("/login")
            .successHandler(oAuth2SuccessHandler())
            .userInfoEndpoint(userInfo -> userInfo
                .userService(customOAuth2UserService)
            )
        )
        // 로그아웃
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

  // OAuth2 로그인 성공 핸들러
  @Bean
  public AuthenticationSuccessHandler oAuth2SuccessHandler() {
    return (request, response, authentication) -> {
      OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
      String socialId = (String) oauth2User.getAttributes().get("id");

      User user = userRepository.findByUserId(socialId)
          .orElseThrow(() -> new UsernameNotFoundException("OAuth2 사용자를 찾을 수 없습니다: " + socialId));

      HttpSession session = request.getSession();
      session.setAttribute("loginUser", user);
      session.setAttribute("userId", user.getUserId());

      response.sendRedirect("/mainPage");
    };
  }
}
=======
    private final UserRepository userRepository;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    // CustomUserDetailsService를 사용하여 DaoAuthenticationProvider 설정
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

    // SecurityFilterChain 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           DaoAuthenticationProvider daoAuthenticationProvider) throws Exception {

        // CSRF 설정
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
                        // 누구나 접근 가능 (permitAll) 경로 통합
                        .requestMatchers(
                                "/", "/intro","/mainPage", "/index", "/home",
                                "/login", "/register", "/member/register", "/member/findId", "/member/findPw",
                                "/css/**", "/js/**", "/images/**", "/assets/**", "/lib/**", "/resources/**",
                                "/join", "/categories", "/cafe/{id}", // 두 번째 코드 블록에서 추가된 경로
                                "/member/register/check-userid", "/member/register/check-email",
                                "/member/register/check-name", "/member/register/check-nickname", "/member/register/check-phone",
                                "/api/member/**",
                                "/upload/**"
                        ).permitAll()
                        // 특정 HTTP 메서드/경로 접근 설정
                        .requestMatchers(HttpMethod.GET, "/trade/list").permitAll()
                        .requestMatchers(HttpMethod.GET, "/trade/read/**", "/trade/read/*").permitAll() // 와일드카드 통합
                        .requestMatchers(HttpMethod.POST, "/report").authenticated() // 인증 필요
                        // 역할 기반 접근 제어
                        .requestMatchers("/admin/**", "/manager/**").hasRole("ADMIN")
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // DaoAuthenticationProvider 사용 명시
                .authenticationProvider(daoAuthenticationProvider)
                // 폼 로그인 설정
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("userId")
                        .passwordParameter("password")
                        .successHandler(customSuccessHandler())
                        .failureHandler(customFailureHandler()) // 잠금 계정 확인 로직이 있는 핸들러 유지
                )
                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(oAuth2SuccessHandler())
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                )
                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/login?logout=1") // 또는 logout=true
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true) // 두 번째 코드 블록의 설정 추가
                );

        // (옵션) CSRF 유틸 필터 - 정의되지 않은 필터는 주석 처리하거나 실제 구현이 필요합니다.
        // http.addFilterBefore(new CsrfCookieSanitizerFilter(), CsrfFilter.class);
        // http.addFilterAfter(new CsrfCookieFilter("TOGETHER-XSRF"), CsrfFilter.class);
        // http.addFilterAfter(new CsrfDebugLoggingFilter("TOGETHER-XSRF"), CsrfFilter.class);

        return http.build();
    }

    // ✔ 일반 로그인 성공 핸들러: 계정 잠김(LOCKED) 상태 확인 로직 포함
    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            String authenticatedUserId = authentication.getName();
            User user = userRepository.findByUserId(authenticatedUserId)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + authenticatedUserId));

          if (user.getStatus() != Status.ACTIVE) {
            request.getSession().invalidate();
            SecurityContextHolder.clearContext();
            response.sendRedirect("/login?inactive=1");
            return;
          }

            HttpSession session = request.getSession();
            session.setAttribute("loginUser", user);
            session.setAttribute("userId", user.getUserId());

            response.sendRedirect("/mainPage");
        };
    }

    // ✔ 로그인 실패 핸들러: 계정 잠김(LOCKED) 상태 우선 확인 로직 포함
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

    // ✔ OAuth2 로그인 성공 핸들러: 계정 잠김(LOCKED) 상태 확인 로직 포함
    @Bean
    public AuthenticationSuccessHandler oAuth2SuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

            // CustomOAuth2UserService에서 userId로 사용하도록 설정한 값 사용 (예: socialId)
            String socialId = (String) oauth2User.getAttributes().get("id"); // 또는 authentication.getName()

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
