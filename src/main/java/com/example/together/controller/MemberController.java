package com.example.together.controller;

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
    @GetMapping("/member/register")
    public String showRegisterForm(Model model) {
        log.info("GET /member/register - 회원가입 페이지 요청");
        model.addAttribute("memberRegisterDTO", new memberRegisterDTO());
        return "member/register";
    }

    // 회원가입 처리
    @PostMapping("/member/register")
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
            return "redirect:/login";
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
    @GetMapping("/member/my-cafes")
    public String myCafes(Model model, Principal principal) {
        if (principal != null) {
            String userId = principal.getName();
            // 사용자가 참여한 카페 목록 조회
            // List<Cafe> myCafes = cafeService.findByUserId(userId);
            // model.addAttribute("cafes", myCafes);

            // 임시로 빈 목록 전달
            model.addAttribute("cafes", new ArrayList<>());
        }
        return "member/my-cafes";
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
}