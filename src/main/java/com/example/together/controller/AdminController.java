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
    @GetMapping("/manager")
    public String manager() {
        return "admin/manager";  // admin/dashboard.html 템플릿 반환
    }

    // ========================== 새로 추가된 메서드들 ==========================

    /**
     * 사용자 관리 페이지
     */
    @GetMapping("/users")
    public String userManagement(Model model) {
        log.info("GET /admin/users - 사용자 관리 페이지 요청");

        try {
            // 기본 데이터 설정 (나중에 실제 서비스 연결)
            model.addAttribute("totalUsers", 7);  // 로그에서 확인된 사용자 수
            model.addAttribute("activeUsers", 7);
            model.addAttribute("users", null);  // 빈 리스트로 시작

            return "admin/user-management";
        } catch (Exception e) {
            log.error("사용자 관리 페이지 로드 실패", e);
            model.addAttribute("error", "사용자 데이터를 불러오는데 실패했습니다.");
            return "admin/user-management";
        }
    }

    /**
     * 카페 관리 페이지
     */
    @GetMapping("/cafes")
    public String cafeManagement(Model model) {
        log.info("GET /admin/cafes - 카페 관리 페이지 요청");

        try {
            model.addAttribute("totalCafes", 0);  // 로그에서 확인된 카페 수
            model.addAttribute("cafes", null);

            return "admin/cafe-management";
        } catch (Exception e) {
            log.error("카페 관리 페이지 로드 실패", e);
            model.addAttribute("error", "카페 데이터를 불러오는데 실패했습니다.");
            return "admin/cafe-management";
        }
    }

    /**
     * 중고거래 관리 페이지
     */
    @GetMapping("/trades")
    public String tradeManagement(Model model) {
        log.info("GET /admin/trades - 중고거래 관리 페이지 요청");

        try {
            model.addAttribute("totalTrades", 0);  // 로그에서 확인된 거래 수
            model.addAttribute("trades", null);

            return "admin/trade-management";
        } catch (Exception e) {
            log.error("중고거래 관리 페이지 로드 실패", e);
            model.addAttribute("error", "거래 데이터를 불러오는데 실패했습니다.");
            return "admin/trade-management";
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
            model.addAttribute("chatRooms", null);

            return "admin/chat-management";
        } catch (Exception e) {
            log.error("채팅 관리 페이지 로드 실패", e);
            model.addAttribute("error", "채팅 데이터를 불러오는데 실패했습니다.");
            return "admin/chat-management";
        }
    }

    /**
     * 통계 분석 페이지
     */
    @GetMapping("/statistics")
    public String statisticsAnalysis(Model model) {
        log.info("GET /admin/statistics - 통계 분석 페이지 요청");

        try {
            // 기본 통계 데이터
            model.addAttribute("userStats", "기본 통계");
            model.addAttribute("cafeStats", "기본 통계");
            model.addAttribute("tradeStats", "기본 통계");
            model.addAttribute("chatStats", "기본 통계");

            return "admin/statistics";
        } catch (Exception e) {
            log.error("통계 페이지 로드 실패", e);
            model.addAttribute("error", "통계 데이터를 불러오는데 실패했습니다.");
            return "admin/statistics";
        }
    }

    /**
     * 시스템 설정 페이지
     */
    @GetMapping("/settings")
    public String systemSettings(Model model) {
        log.info("GET /admin/settings - 시스템 설정 페이지 요청");

        try {
            model.addAttribute("systemInfo", "시스템 정보");

            return "admin/system-settings";
        } catch (Exception e) {
            log.error("시스템 설정 페이지 로드 실패", e);
            model.addAttribute("error", "시스템 설정을 불러오는데 실패했습니다.");
            return "admin/system-settings";
        }
    }

    // ========================== 임시 응답 메서드들 ==========================
    // 실제 서비스가 준비될 때까지 사용할 임시 메서드들

    /**
     * 사용자 상태 변경 (임시)
     */
    @PostMapping("/users/{userId}/status")
    @ResponseBody
    public String updateUserStatus(@PathVariable Long userId,
                                   @RequestParam String status) {
        log.info("사용자 상태 변경 요청: userId={}, status={}", userId, status);
        // 임시로 성공 응답
        return "success";
    }

    /**
     * 카페 삭제 (임시)
     */
    @PostMapping("/cafes/{cafeId}/delete")
    @ResponseBody
    public String deleteCafe(@PathVariable Long cafeId) {
        log.info("카페 삭제 요청: cafeId={}", cafeId);
        // 임시로 성공 응답
        return "success";
    }

    /**
     * 거래 상태 변경 (임시)
     */
    @PostMapping("/trades/{tradeId}/status")
    @ResponseBody
    public String updateTradeStatus(@PathVariable Long tradeId,
                                    @RequestParam String status) {
        log.info("거래 상태 변경 요청: tradeId={}, status={}", tradeId, status);
        // 임시로 성공 응답
        return "success";
    }

    /**
     * 채팅방 삭제 (임시)
     */
    @PostMapping("/chats/{chatRoomId}/delete")
    @ResponseBody
    public String deleteChatRoom(@PathVariable Long chatRoomId) {
        log.info("채팅방 삭제 요청: chatRoomId={}", chatRoomId);
        // 임시로 성공 응답
        return "success";
    }
}