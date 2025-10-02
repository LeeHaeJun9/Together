package com.example.together.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * /report POST 시 서버가 실제 비교하는 CSRF 토큰/헤더/쿠키 값을 로그로 찍어 진단하는 필터.
 * 문제 해결 후 주석 처리하거나 제거해도 됩니다.
 */
public class CsrfDebugLoggingFilter extends OncePerRequestFilter {

  private final String cookieName;

  /** 기본 쿠키명(XSRF-TOKEN) 사용 */
  public CsrfDebugLoggingFilter() {
    this("XSRF-TOKEN");
  }

  /** 커스텀 쿠키명 사용 시 (예: "TOGETHER-XSRF") */
  public CsrfDebugLoggingFilter(String cookieName) {
    this.cookieName = cookieName;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {

    if ("/report".equals(req.getRequestURI()) && "POST".equalsIgnoreCase(req.getMethod())) {
      CsrfToken t = (CsrfToken) req.getAttribute(CsrfToken.class.getName());
      if (t == null) t = (CsrfToken) req.getAttribute("_csrf");

      String hx = req.getHeader("X-XSRF-TOKEN");
      String hc = req.getHeader("X-CSRF-TOKEN");

      String ck = null;
      Cookie[] cookies = req.getCookies();
      if (cookies != null) {
        ck = Arrays.stream(cookies)
            .filter(c -> cookieName.equals(c.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
      }

      // OncePerRequestFilter 가 제공하는 logger 사용
      logger.info("[CSRF DEBUG] expectedToken=" + (t == null ? null : t.getToken())
          + " header(X-XSRF-TOKEN)=" + hx
          + " header(X-CSRF-TOKEN)=" + hc
          + " cookie(" + cookieName + ")=" + ck
          + " headerName=" + (t == null ? null : t.getHeaderName()));
    }

    chain.doFilter(req, res);
  }
}
