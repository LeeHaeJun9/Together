package com.example.together.controller;

import com.example.together.domain.Status;
import com.example.together.domain.SystemRole;
import com.example.together.domain.User;
import com.example.together.repository.*;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CafeRepository cafeRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    // 메인페이지
    @GetMapping("/mainPage")
    public String mainPage(Model model, Principal principal) {
        log.info("GET /main - 메인페이지 요청");

        // 메인페이지 데이터 추가 (예: 인기 카페 목록 등)
        model.addAttribute("message", "모여라에 오신 것을 환영합니다!");

        return "mainPage";  // mainPage.html
    }
    // MainController.java의 기존 mypage 메서드를 이것으로 교체

//    @GetMapping("/member/mypage")
//    public String mypage(Model model, Principal principal) {
//        log.info("GET /member/mypage - 마이페이지 요청");
//
//        // 로그인 체크
//        if (principal == null) {
//            return "redirect:/member/login";
//        }
//
//        // 사용자 정보를 모델에 추가
//        String userId = principal.getName();
//        model.addAttribute("userId", userId);
//
//        return "member/mypage";
//    }

    @GetMapping("/member/mypage")
    public String mypage(Model model, Principal principal) {
        log.info("GET /member/mypage - 마이페이지 요청");

        if (principal == null) {
            return "redirect:/member/login";
        }

        String userId = principal.getName();

        // 1. userRepository의 findByUserId 메서드를 호출하여
        //    데이터베이스에서 사용자 ID에 해당하는 정보를 조회합니다.
        Optional<User> userOptional = userRepository.findByUserId(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // 2. 조회된 User 엔티티에서 이름과 이메일 정보를 가져와 모델에 담습니다.
            model.addAttribute("userName", user.getName());
            model.addAttribute("userEmail", user.getEmail());

            log.info("사용자 마이페이지 로딩: {}", userId);
            return "member/mypage";
        } else {
            // 사용자를 찾을 수 없는 경우 예외 처리
            log.error("데이터베이스에서 사용자 정보를 찾을 수 없습니다: {}", userId);
            return "error/userNotFound"; // 예시 오류 페이지
        }
    }
    // MainController.java 클래스 안에 추가할 메서드

    @GetMapping("/manager")
    public String managerPage(Model model, HttpSession session) {
        log.info("GET /manager - 관리자 페이지 요청");

        try {
            // 통계 데이터 수집
            long totalUsers = userRepository.count();
            long totalCafes = cafeRepository.count();
            long totalTrades = tradeRepository.count();
            long totalMeetings = meetingRepository.count();

            // 처리 대기 항목들
            long pendingReports = reportRepository.count();
            long pendingRecovery = userRepository.countByStatus(Status.LOCKED);

            // 모델에 데이터 추가
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalCafes", totalCafes);
            model.addAttribute("totalTrades", totalTrades);
            model.addAttribute("totalMeetings", totalMeetings);
            model.addAttribute("pendingReports", pendingReports);
            model.addAttribute("pendingRecovery", pendingRecovery);
            model.addAttribute("pendingCafes", 3);
            model.addAttribute("pendingInquiry", 5);
            model.addAttribute("activeChats", chatRoomRepository.count());

            log.info("통계 데이터 로드 완료 - 사용자: {}, 카페: {}, 거래: {}",
                    totalUsers, totalCafes, totalTrades);

        } catch (Exception e) {
            log.error("관리자 페이지 데이터 로드 중 오류: ", e);
            // 오류 시 기본값 설정
            model.addAttribute("totalUsers", 0);
            model.addAttribute("totalCafes", 0);
            model.addAttribute("totalTrades", 0);
            model.addAttribute("totalMeetings", 0);
        }

        return "manager/manager";
    }
}
