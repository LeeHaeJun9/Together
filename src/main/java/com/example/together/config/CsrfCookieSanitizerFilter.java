// CsrfCookieSanitizerFilter.java  (구 쿠키 제거)
package com.example.together.config;
import jakarta.servlet.*; import jakarta.servlet.http.*; import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

public class CsrfCookieSanitizerFilter extends OncePerRequestFilter {
  @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain fc)
      throws ServletException, IOException {
    // 흔히 남아있는 예전 이름들 정리
    String[] legacy = { "XSRF-TOKEN", "X-CSRF-TOKEN" };
    for (String name : legacy) {
      Cookie del = new Cookie(name, "");
      del.setPath("/"); del.setMaxAge(0); del.setHttpOnly(false); del.setSecure(false);
      res.addCookie(del);
    }
    fc.doFilter(req, res);
  }
}
