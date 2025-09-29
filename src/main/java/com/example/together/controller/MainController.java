package com.example.together.controller;

import com.example.together.domain.Status;
import com.example.together.domain.User;
import com.example.together.repository.*;
import com.example.together.service.cafe.CafeService;
import com.example.together.service.trade.TradeService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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

  @Autowired private UserRepository userRepository;
  @Autowired private CafeRepository cafeRepository;
  @Autowired private TradeRepository tradeRepository;
  @Autowired private MeetingRepository meetingRepository;
  @Autowired private ReportRepository reportRepository;
  @Autowired private ChatRoomRepository chatRoomRepository;

  private final CafeService cafeService;
  private final TradeService tradeService;

  // ✅ 메인페이지
  @GetMapping("/mainPage")
  public String mainPage(HttpServletRequest request, Model model, Principal principal) {
    // 1) intro_seen 세션 쿠키 없으면 인트로로 먼저 보냄
    if (!hasIntroSeen(request)) {
      return "redirect:/intro";
    }

    log.info("GET /mainPage - 메인페이지 요청");

    // 2) 메인에 필요한 데이터 구성
    model.addAttribute("recomCafes", cafeService.getRecommendedCafes(8));
    model.addAttribute("categories", cafeService.getAllCategories());
    model.addAttribute("recomTrades", tradeService.getPopularTradesByFavoriteCount(8));

    String message = (principal != null)
        ? "환영합니다, " + principal.getName() + "님!"
        : "모여라에 오신 것을 환영합니다!";
    model.addAttribute("message", message);

    return "mainPage";
  }

  // 세션 쿠키 intro_seen=1 여부 확인
  private boolean hasIntroSeen(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) return false;
    for (Cookie c : cookies) {
      if ("intro_seen".equals(c.getName()) && "1".equals(c.getValue())) {
        return true;
      }
    }
    return false;
  }

  @GetMapping("/member/mypage")
  public String mypage(Model model, Principal principal) {
    log.info("GET /member/mypage - 마이페이지 요청");

    if (principal == null) {
      return "redirect:/member/login";
    }

    String userId = principal.getName();
    Optional<User> userOptional = userRepository.findByUserId(userId);

    if (userOptional.isPresent()) {
      User user = userOptional.get();
      model.addAttribute("userName", user.getName());
      model.addAttribute("userEmail", user.getEmail());
      log.info("사용자 마이페이지 로딩: {}", userId);
      return "member/mypage";
    } else {
      log.error("데이터베이스에서 사용자 정보를 찾을 수 없습니다: {}", userId);
      return "error/userNotFound";
    }
  }

  @GetMapping("/manager")
  public String managerPage(Model model, HttpSession session) {
    log.info("GET /manager - 관리자 페이지 요청");
    try {
      long totalUsers = userRepository.count();
      long totalCafes = cafeRepository.count();
      long totalTrades = tradeRepository.count();
      long totalMeetings = meetingRepository.count();

      long pendingReports = reportRepository.count();
      long pendingRecovery = userRepository.countByStatus(Status.LOCKED);

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
      model.addAttribute("totalUsers", 0);
      model.addAttribute("totalCafes", 0);
      model.addAttribute("totalTrades", 0);
      model.addAttribute("totalMeetings", 0);
    }
    return "manager/manager";
  }
}
