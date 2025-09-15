package com.example.together.controller;

import com.example.together.domain.User;
import com.example.together.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final UserService userService;

    /**
     * 로그인 페이지 표시
     */
    @GetMapping("/login")
    public String loginPage(Model model) {
        log.info("로그인 페이지 요청");
        return "member/login";
    }

    /**
     * 로그인 처리
     */
    @PostMapping("/login")
    public String login(@RequestParam("userId") String userId,
                        @RequestParam("password") String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

        log.info("로그인 시도: userId = {}", userId);

        try {
            // 사용자 인증
            User user = userService.authenticate(userId, password);

            if (user != null) {
                // 로그인 성공
                session.setAttribute("loginUser", user);
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("userRole", user.getSystemRole());

                log.info("로그인 성공: userId = {}", userId);

                // 관리자면 관리자 페이지로, 일반 사용자면 마이페이지로
                if (user.getSystemRole().name().equals("ADMIN")) {
                    return "redirect:/admin/manager";
                } else {
                    return "redirect:/mypage";
                }

            } else {
                // 로그인 실패
                log.warn("로그인 실패: 잘못된 아이디 또는 비밀번호 - userId = {}", userId);
                redirectAttributes.addFlashAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
                return "redirect:/login";
            }

        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생: userId = {}, error = {}", userId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "로그인 처리 중 오류가 발생했습니다.");
            return "redirect:/login";
        }
    }

    /**
     * 로그아웃 처리
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {

        String userId = (String) session.getAttribute("userId");
        log.info("로그아웃 요청: userId = {}", userId);

        // 세션 무효화
        session.invalidate();

        redirectAttributes.addFlashAttribute("message", "성공적으로 로그아웃되었습니다.");
        return "redirect:/login";
    }

    /**
     * 메인 페이지 (홈) - 로그인 후 기본 이동
     */
    @GetMapping({"/", "/home"})
    public String home(HttpSession session) {

        User loginUser = (User) session.getAttribute("loginUser");

        if (loginUser != null) {
            // 로그인 상태면 마이페이지로
            return "redirect:/mypage";
        } else {
            // 로그인하지 않은 상태면 로그인 페이지로
            return "redirect:/login";
        }
    }
}
