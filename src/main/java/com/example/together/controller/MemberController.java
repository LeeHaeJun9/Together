package com.example.together.controller;

import com.example.together.domain.User;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.together.dto.member.memberRegisterDTO;
import com.example.together.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final UserService userService;

    // 회원가입 페이지
    @GetMapping("/join")
    public String showRegisterForm(Model model) {
        log.info("GET /member/register - 회원가입 페이지 요청");
        model.addAttribute("memberRegisterDTO", new memberRegisterDTO());
        return "member/register";
    }

    // 회원가입 처리
    @PostMapping("/join")
    public String processRegistration(@Valid @ModelAttribute("memberRegisterDTO") memberRegisterDTO registerDTO,
                                      BindingResult bindingResult,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {

        log.info("POST /member/register - 회원가입 시도: userId = {}", registerDTO.getUserId());

        // 비밀번호 일치 확인
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "비밀번호가 일치하지 않습니다.");
        }
        // 아이디 중복 확인
        if (userService.isUserIdExists(registerDTO.getUserId())) {
            bindingResult.rejectValue("userId", "userId.duplicate", "이미 사용 중인 아이디입니다.");
        }
        // 이메일 중복 확인
        if (userService.isEmailExists(registerDTO.getEmail())) {
            bindingResult.rejectValue("email", "email.duplicate", "이미 사용 중인 이메일입니다.");
        }

        // 유효성 검사 실패 시
        if (bindingResult.hasErrors()) {
            log.warn("회원가입 폼 유효성 검사 실패");
            return "member/register";
        }

        // 모든 검증 통과 후 회원가입 진행
        try {
            userService.register(registerDTO);
            redirectAttributes.addFlashAttribute("message", "회원가입에 성공했습니다. 로그인해주세요.");
            return "redirect:/main";
        } catch (IllegalArgumentException e) {
            log.warn("회원가입 처리 중 예외 발생: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "member/register";
        }
    }

    // 사용자 ID 중복 확인 AJAX
    @PostMapping("/member/register/check-userid")
    @ResponseBody
    public String checkUserId(@RequestParam("userId") String userId) {
        log.info("아이디 중복 확인: userId = {}", userId);
        boolean exists = userService.isUserIdExists(userId);
        return exists ? "{\"available\": false}" : "{\"available\": true}";
    }

    // 이메일 중복 확인 AJAX
    @PostMapping("/member/register/check-email")
    @ResponseBody
    public String checkEmail(@RequestParam("email") String email) {
        log.info("이메일 중복 확인: email = {}", email);
        boolean exists = userService.isEmailExists(email);
        return exists ? "{\"available\": false}" : "{\"available\": true}";
    }
    @GetMapping("/member/myCafes")
    public String myCafes(Model model, Principal principal) {
        if (principal != null) {
            String userId = principal.getName();
            // 사용자가 참여한 카페 목록 조회
            // List<Cafe> myCafes = cafeService.findByUserId(userId);
            // model.addAttribute("cafes", myCafes);

            // 임시로 빈 목록 전달
            model.addAttribute("cafes", new ArrayList<>());
        }
        return "member/myCafes";
    }

    @Autowired
//    private UserService userService; // 사용자 서비스

    // 아이디 찾기 페이지 표시
    @GetMapping("/member/findId")
    public String findIdPage() {
        return "member/findId";
    }

    // 아이디 찾기 요청 처리
    @PostMapping("/member/findId")
    @ResponseBody
    public Map<String, Object> findId(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String name = request.get("name");
            String email = request.get("email");

            // 데이터베이스에서 사용자 찾기
            String userId = userService.findUserIdByNameAndEmail(name, email);

            if (userId != null) {
                response.put("success", true);
                response.put("userId", userId);
            } else {
                response.put("success", false);
                response.put("message", "일치하는 회원 정보를 찾을 수 없습니다.");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
        }

        return response;
    }
    // 비밀번호 찾기 페이지 표시
    @GetMapping("/member/findPw")
    public String findPwPage() {
        return "member/findPw";
    }

    // 비밀번호 찾기 요청 처리 (임시 비밀번호 발급)
    @PostMapping("/member/findPw")
    @ResponseBody
    public Map<String, Object> findPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String userId = request.get("userId");
            String email = request.get("email");
            String name = request.get("name");

            // 사용자 정보 확인 (임시로 간단하게 구현)
            boolean userExists = userService.isUserIdExists(userId) &&
                    userService.isEmailExists(email);

            if (userExists) {
                // 임시 비밀번호 생성 (간단한 랜덤 문자열)
                String tempPassword = "temp" + System.currentTimeMillis() % 10000;

                response.put("success", true);
                response.put("tempPassword", tempPassword);
                response.put("message", "임시 비밀번호가 발급되었습니다.");

                log.info("임시 비밀번호 발급 성공: userId = {}", userId);

            } else {
                response.put("success", false);
                response.put("message", "입력하신 정보와 일치하는 회원을 찾을 수 없습니다.");
                log.warn("비밀번호 찾기 실패: 사용자 정보 불일치 - userId = {}", userId);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            log.error("비밀번호 찾기 처리 중 오류: {}", e.getMessage());
        }

        return response;
    }
    // 프로필 페이지 표시
    @GetMapping("/member/profile")
    public String profilePage(Model model, Principal principal) {
        if (principal != null) {
            String userId = principal.getName();
            User user = userService.findByUserId(userId);
            model.addAttribute("user", user);
            log.info("프로필 페이지 요청: userId = {}", userId);
        }
        return "member/profile";
    }

    // 개별 필드 업데이트
    @PostMapping("/member/profile/update")
    @ResponseBody
    public Map<String, Object> updateProfile(@RequestBody Map<String, String> request,
                                             Principal principal) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }

            String userId = principal.getName();
            String field = request.get("field");
            String value = request.get("value");

            // 필드별 업데이트 처리
            boolean success = userService.updateUserField(userId, field, value);

            if (success) {
                response.put("success", true);
                response.put("message", "정보가 성공적으로 수정되었습니다.");
                log.info("프로필 수정 성공: userId = {}, field = {}", userId, field);
            } else {
                response.put("success", false);
                response.put("message", "정보 수정에 실패했습니다.");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            log.error("프로필 수정 오류: {}", e.getMessage());
        }

        return response;
    }

    // 비밀번호 변경
    @PostMapping("/member/profile/password")
    @ResponseBody
    public Map<String, Object> changePassword(@RequestBody Map<String, String> request,
                                              Principal principal) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }

            String userId = principal.getName();
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            // 현재 비밀번호 확인 및 새 비밀번호로 변경
            boolean success = userService.changePassword(userId, currentPassword, newPassword);

            if (success) {
                response.put("success", true);
                response.put("message", "비밀번호가 성공적으로 변경되었습니다.");
                log.info("비밀번호 변경 성공: userId = {}", userId);
            } else {
                response.put("success", false);
                response.put("message", "현재 비밀번호가 일치하지 않습니다.");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            log.error("비밀번호 변경 오류: {}", e.getMessage());
        }

        return response;
    }
    // 거래 내역 페이지 표시
    @GetMapping("/member/myTrade")
    public String myTradePage(Model model, Principal principal) {
        if (principal != null) {
            String userId = principal.getName();

            // 임시로 빈 리스트 전달 (실제 구현 전)
            model.addAttribute("sellCount", 0);
            model.addAttribute("soldCount", 0);
            model.addAttribute("favoriteCount", 0);
            model.addAttribute("chatCount", 0);

            log.info("거래 내역 페이지 요청: userId = {}", userId);
        }
        return "member/myTrade";
    }
    // 나의 모임 페이지 표시
    @GetMapping("/member/myMeetings")
    public String myMeetingsPage(Model model, Principal principal) {
        if (principal != null) {
            String userId = principal.getName();

            // 임시로 빈 리스트 전달 (실제 구현 전)
            model.addAttribute("upcomingMeetings", 0);
            model.addAttribute("completedMeetings", 0);
            model.addAttribute("hostedMeetings", 0);
            model.addAttribute("reviewCount", 0);

            log.info("나의 모임 페이지 요청: userId = {}", userId);
        }
        return "member/myMeetings";
    }
    // 찜한 상품 페이지 표시
    @GetMapping("/member/favorites")
    public String favoritesPage(Model model, Principal principal) {
        if (principal != null) {
            String userId = principal.getName();

            // 임시로 빈 리스트 전달 (실제 구현 전)
            model.addAttribute("totalFavorites", 0);
            model.addAttribute("availableCount", 0);
            model.addAttribute("soldOutCount", 0);
            model.addAttribute("recentCount", 0);
            model.addAttribute("favorites", new ArrayList<>());

            log.info("찜한 상품 페이지 요청: userId = {}", userId);
        }
        return "member/favorites";
    }
    // 계정 설정 페이지 표시
    @GetMapping("/member/settings")
    public String settingsPage(Model model, Principal principal) {
        if (principal != null) {
            String userId = principal.getName();
            User user = userService.findByUserId(userId);
            model.addAttribute("user", user);
            log.info("계정 설정 페이지 요청: userId = {}", userId);
        }
        return "member/settings";
    }
    @GetMapping("/login")
    public String loginPage() {
        log.info("GET /login - 로그인 페이지 요청");
        return "member/login";
    }
}