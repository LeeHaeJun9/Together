package com.example.together.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    // 관리자 로그인 폼 페이지
    @GetMapping("/login")
    public String loginForm(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if (logout != null) {
            model.addAttribute("message", "성공적으로 로그아웃되었습니다.");
        }

        return "admin/login";
    }

    // 관리자 로그인 처리
    @PostMapping("/login")
    public String processLogin(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String rememberMe,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // 간단한 테스트용 로그인 검증
        if (isValidLogin(email, password)) {
            // 세션에 로그인 정보 저장
            session.setAttribute("adminEmail", email);
            session.setAttribute("adminLoggedIn", true);

            // Remember Me 처리
            if ("on".equals(rememberMe)) {
                session.setMaxInactiveInterval(30 * 24 * 60 * 60); // 30일
            }

            System.out.println("관리자 로그인 성공: " + email);

            // 성공 시 관리자 목록으로 리다이렉트
            return "redirect:/admin/manager/list";

        } else {
            // 실패 시 로그인 페이지로 리다이렉트 (에러 메시지와 함께)
            return "redirect:/admin/login?error=true";
        }
    }

    // 관리자 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 세션 무효화
        return "redirect:/admin/login?logout=true";
    }

    // 관리자 대시보드 (기존 manager.html과 연결)
    @GetMapping
    public String adminDashboard(HttpSession session, Model model) {
        // 로그인 체크
        Boolean isLoggedIn = (Boolean) session.getAttribute("adminLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/admin/login";
        }

        String adminEmail = (String) session.getAttribute("adminEmail");
        model.addAttribute("adminEmail", adminEmail);

        return "admin-dashboard"; // 기존 admin-dashboard.html 사용
    }

    // 테스트용 로그인 검증 메서드
    private boolean isValidLogin(String email, String password) {
        // 테스트용 계정들
        return ("admin@test.com".equals(email) && "1234".equals(password)) ||
                ("super@admin.com".equals(email) && "admin123".equals(password)) ||
                ("manager@test.com".equals(email) && "manager".equals(password));
    }
}