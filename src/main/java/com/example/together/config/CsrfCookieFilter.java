// CsrfCookieFilter.java
package com.example.together.config;
import jakarta.servlet.*; import jakarta.servlet.http.*; import java.io.IOException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

public class CsrfCookieFilter extends OncePerRequestFilter {
  private final String cookieName;
  public CsrfCookieFilter(String cookieName){ this.cookieName = cookieName; }
  @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain fc)
      throws ServletException, IOException {
    CsrfToken t = (CsrfToken) (req.getAttribute(CsrfToken.class.getName()) != null
        ? req.getAttribute(CsrfToken.class.getName()) : req.getAttribute("_csrf"));
    if (t != null) {
      String token = t.getToken();
      Cookie c = new Cookie(cookieName, token);
      c.setPath("/"); c.setHttpOnly(false); c.setSecure(false); c.setMaxAge(-1);
      res.addCookie(c);
    }
    fc.doFilter(req, res);
  }
}
