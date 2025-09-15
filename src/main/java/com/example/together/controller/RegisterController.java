package com.example.together.controller;

import com.example.together.domain.User;
import com.example.together.dto.member.memberRegisterDTO;
import com.example.together.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RegisterController {

    private final UserService userService;

    /**
     * 회원가입 페이지 표시
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        log.info("회원가입 페이지 요청");

        // 빈 DTO 객체를 모델에 추가 (Thymeleaf 폼 바인딩용)
        model.addAttribute("registerDTO", new memberRegisterDTO());

        return "member/register";
    }

    /**
     * 회원가입 처리
     */
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerDTO") memberRegisterDTO registerDTO,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        log.info("회원가입 시도: userId = {}, email = {}", registerDTO.getUserId(), registerDTO.getEmail());

        try {
            // 1. 유효성 검사 오류 확인
            if (bindingResult.hasErrors()) {
                log.warn("회원가입 유효성 검사 실패: userId = {}", registerDTO.getUserId());
                model.addAttribute("registerDTO", registerDTO);
                return "member/register";
            }

            // 2. 비밀번호 확인 검증
            if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
                log.warn("비밀번호 확인 불일치: userId = {}", registerDTO.getUserId());
                model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
                model.addAttribute("registerDTO", registerDTO);
                return "member/register";
            }

            // 3. 아이디 중복 검사
            if (userService.isUserIdExists(registerDTO.getUserId())) {
                log.warn("아이디 중복: userId = {}", registerDTO.getUserId());
                model.addAttribute("error", "이미 사용 중인 아이디입니다.");
                model.addAttribute("registerDTO", registerDTO);
                return "member/register";
            }

            // 4. 이메일 중복 검사
            if (userService.isEmailExists(registerDTO.getEmail())) {
                log.warn("이메일 중복: email = {}", registerDTO.getEmail());
                model.addAttribute("error", "이미 사용 중인 이메일입니다.");
                model.addAttribute("registerDTO", registerDTO);
                return "member/register";
            }

            // 5. 회원가입 처리
            User newUser = userService.register(registerDTO);

            if (newUser != null) {
                log.info("회원가입 성공: userId = {}, id = {}", newUser.getUserId(), newUser.getId());
                redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해주세요.");
                return "redirect:/login";
            } else {
                log.error("회원가입 실패: 사용자 생성 실패 - userId = {}", registerDTO.getUserId());
                model.addAttribute("error", "회원가입 처리 중 오류가 발생했습니다.");
                model.addAttribute("registerDTO", registerDTO);
                return "member/register";
            }

        } catch (IllegalArgumentException e) {
            log.warn("회원가입 검증 실패: userId = {}, error = {}", registerDTO.getUserId(), e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("registerDTO", registerDTO);
            return "member/register";

        } catch (Exception e) {
            log.error("회원가입 처리 중 예외 발생: userId = {}, error = {}", registerDTO.getUserId(), e.getMessage());
            model.addAttribute("error", "회원가입 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            model.addAttribute("registerDTO", registerDTO);
            return "member/register";
        }
    }

    /**
     * 아이디 중복 확인 AJAX
     */
    @PostMapping("/register/check-userid")
    public String checkUserId(@RequestParam("userId") String userId,
                              HttpServletResponse response) throws IOException {

        log.info("아이디 중복 확인: userId = {}", userId);

        boolean exists = userService.isUserIdExists(userId);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String result = exists ? "{\"available\": false}" : "{\"available\": true}";
        response.getWriter().write(result);

        return null;
    }

    /**
     * 이메일 중복 확인 AJAX
     */
    @PostMapping("/register/check-email")
    public String checkEmail(@RequestParam("email") String email,
                             HttpServletResponse response) throws IOException {

        log.info("이메일 중복 확인: email = {}", email);

        boolean exists = userService.isEmailExists(email);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String result = exists ? "{\"available\": false}" : "{\"available\": true}";
        response.getWriter().write(result);

        return null;
    }
}