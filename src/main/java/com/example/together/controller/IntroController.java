// src/main/java/com/example/together/controller/IntroController.java
package com.example.together.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IntroController {

  @GetMapping({"/", "/intro"})
  public String intro(HttpServletRequest request) {
    // preview=1 이면 항상 인트로 (테스트 편의)
    if ("1".equals(request.getParameter("preview"))) {
      return "intro/intro";
    }
    // 쿠키 intro_seen=1 이면 메인으로
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie c : cookies) {
        if ("intro_seen".equals(c.getName()) && "1".equals(c.getValue())) {
          return "redirect:/mainPage";
        }
      }
    }
    return "intro/intro";
  }
}
