package com.example.together.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    /**
     * 관리자 로그인 페이지 - 404 에러 해결
     */
    @GetMapping("/login")
    public String adminLogin(@RequestParam(value = "error", required = false) String error,
                             Model model) {
        log.info("Admin 로그인 페이지 요청");

        if (error != null) {
            model.addAttribute("errorMsg", "로그인에 실패했습니다.");
        }

        return "admin/login";  // 기존 admin/dashboard.html 사용
    }

    /**
     * 로그인 성공 후 리다이렉트
     */
    @GetMapping("/manager/list")
    public String managerList(Principal principal) {
        log.info("Admin manager list 요청: {}", principal != null ? principal.getName() : "anonymous");

        // 기존 ManagerController로 리다이렉트
        return "redirect:/admin/dashboard";
    }
    /**
     * 관리자 로그인 처리 (POST) - 여기에 추가
     */
    @PostMapping("/login")
    public String adminLoginProcess(@RequestParam String username,
                                    @RequestParam String password,
                                    Model model) {
        log.info("Admin 로그인 처리 시도: {}", username);

        // Spring Security가 실제 인증 처리
        return "redirect:/admin/dashboard";
    }
    /**
     * 관리자 대시보드 페이지
     */
    @GetMapping("/dashboard")
    public String adminDashboard(Principal principal, Model model) {
        log.info("Admin 대시보드 요청: {}", principal != null ? principal.getName() : "anonymous");

        // 필요한 데이터를 model에 추가
        // model.addAttribute("userCount", userService.getTotalUserCount());
        // model.addAttribute("cafeCount", cafeService.getTotalCafeCount());

        return "admin/dashboard";  // admin/dashboard.html 템플릿 반환
    }
}
