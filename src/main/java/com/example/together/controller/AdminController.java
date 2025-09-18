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
     * 관리자 로그인 페이지
     */
    @GetMapping("/login")
    public String adminLogin(@RequestParam(value = "error", required = false) String error,
                             Model model) {
        log.info("Admin 로그인 페이지 요청");

        if (error != null) {
            model.addAttribute("errorMsg", "로그인에 실패했습니다.");
        }

        return "admin/login";  // 기존 admin/login.html 사용
    }

    /**
     * 로그인 성공 후 리다이렉트
     */
    @GetMapping("/manager/list")
    public String managerList(Principal principal) {
        log.info("Admin manager list 요청: {}", principal != null ? principal.getName() : "anonymous");
        return "redirect:/manager";
    }

    /**
     * 관리자 로그인 처리 (POST)
     */
    @PostMapping("/login")
    public String adminLoginProcess(@RequestParam String username,
                                    @RequestParam String password,
                                    Model model) {
        log.info("Admin 로그인 처리 시도: {}", username);
        return "redirect:/manager";
    }

    /**
     * 관리자 대시보드 페이지
     */
    @GetMapping("/manager")
    public String manager() {
        return "manager/manager";
    }

    // ========================== 관리 페이지들 ==========================

    /**
     * 사용자 관리 페이지
     */
    @GetMapping("/users")
    public String userManagement(Model model) {
        log.info("GET /admin/users - 사용자 관리 페이지 요청");

        try {
            model.addAttribute("totalUsers", 7);
            model.addAttribute("activeUsers", 7);

            return "manager/mUser";
        } catch (Exception e) {
            log.error("사용자 관리 페이지 로드 실패", e);
            model.addAttribute("error", "사용자 데이터를 불러오는데 실패했습니다.");
            return "manager/mUser";
        }
    }

    /**
     * 카페 관리 페이지
     */
    @GetMapping("/cafes")
    public String cafeManagement(Model model) {
        log.info("GET /admin/cafes - 카페 관리 페이지 요청");

        try {
            model.addAttribute("totalCafes", 0);

            return "manager/mCafe";
        } catch (Exception e) {
            log.error("카페 관리 페이지 로드 실패", e);
            model.addAttribute("error", "카페 데이터를 불러오는데 실패했습니다.");
            return "manager/mCafe";
        }
    }

    /**
     * 중고거래 관리 페이지
     */
    @GetMapping("/trades")
    public String tradeManagement(Model model) {
        log.info("GET /admin/trades - 중고거래 관리 페이지 요청");

        try {
            model.addAttribute("totalTrades", 0);
            return "manager/mTrade"; // Updated to new template
        } catch (Exception e) {
            log.error("중고거래 관리 페이지 로드 실패", e);
            return "redirect:/manager";
        }
    }

    /**
     * 채팅 관리 페이지
     */
    @GetMapping("/chats")
    public String chatManagement(Model model) {
        log.info("GET /admin/chats - 채팅 관리 페이지 요청");

        try {
            model.addAttribute("totalChatRooms", 0);
            model.addAttribute("activeChatRooms", 0);
            return "manager/mChat"; // Updated to new template
        } catch (Exception e) {
            log.error("채팅 관리 페이지 로드 실패", e);
            return "redirect:/manager";
        }
    }

    /**
     * 통계 분석 페이지
     */
    @GetMapping("/statistics")
    public String statisticsAnalysis(Model model) {
        log.info("GET /admin/statistics - 통계 분석 페이지 요청");

        try {
            model.addAttribute("userCount", 7);
            model.addAttribute("cafeCount", 0);
            model.addAttribute("tradeCount", 0);
            model.addAttribute("meetingCount", 0);
            model.addAttribute("reportCount", 0);
            model.addAttribute("chatRoomCount", 0);
            return "manager/mStats"; // Updated to new template
        } catch (Exception e) {
            log.error("통계 페이지 로드 실패", e);
            return "redirect:/manager";
        }
    }

    /**
     * 시스템 설정 페이지
     */
    @GetMapping("/settings")
    public String systemSettings(Model model) {
        log.info("GET /admin/settings - 시스템 설정 페이지 요청");

        try {
            return "manager/mSettings"; // Updated to new template
        } catch (Exception e) {
            log.error("시스템 설정 페이지 로드 실패", e);
            return "redirect:/manager";
        }
    }

    // ========================== API 엔드포인트들 ==========================

    /**
     * 간단한 정보 페이지 (임시)
     */
    @GetMapping("/info")
    public String adminInfo(Model model) {
        model.addAttribute("message", "관리자 기능이 준비 중입니다.");
        return "manager/manager";
    }

    @PostMapping("/users/{userId}/status")
    @ResponseBody
    public String updateUserStatus(@PathVariable Long userId, @RequestParam String status) {
        log.info("사용자 상태 변경 요청: userId={}, status={}", userId, status);
        return "success";
    }

    @PostMapping("/cafes/{cafeId}/delete")
    @ResponseBody
    public String deleteCafe(@PathVariable Long cafeId) {
        log.info("카페 삭제 요청: cafeId={}", cafeId);
        return "success";
    }

    @PostMapping("/trades/{tradeId}/status")
    @ResponseBody
    public String updateTradeStatus(@PathVariable Long tradeId, @RequestParam String status) {
        log.info("거래 상태 변경 요청: tradeId={}, status={}", tradeId, status);
        return "success";
    }

    @PostMapping("/chats/{chatRoomId}/delete")
    @ResponseBody
    public String deleteChatRoom(@PathVariable Long chatRoomId) {
        log.info("채팅방 삭제 요청: chatRoomId={}", chatRoomId);
        return "success";
    }
}