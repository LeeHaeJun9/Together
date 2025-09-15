package com.example.together.controller;

import com.example.together.dto.member.LoginDTO;
import com.example.together.dto.member.MyPageDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/member")
public class MemberController {

    @GetMapping("/login")
    public String loginPage() {
        return "member/login";  // member/login.html 반환
    }

    @GetMapping("/register")
    public String registerPage() {
        return "member/memberRegister";  // member/memberRegister.html 반환
    }
    // 🆕 로그인 처리 추가
    @PostMapping("/login")
    public String loginProcess(LoginDTO loginDTO, Model model) {

        // 임시 검증 로직 (다음 단계에서 UserService 연동 예정)
        if (loginDTO.getUserId() != null && loginDTO.getPassword() != null) {
            // 로그인 성공 (임시)
            System.out.println("로그인 시도: " + loginDTO.getUserId());
            return "redirect:/main";  // 메인 페이지로 이동
        } else {
            // 로그인 실패
            model.addAttribute("error", "아이디와 비밀번호를 입력해주세요.");
            return "member/login";  // 로그인 페이지로 돌아가기
        }
    }
    // 🆕 마이페이지 추가
    @GetMapping("/mypage")
    public String myPage(Model model) {

        // TODO: 세션에서 현재 로그인한 사용자 정보 가져오기
        // 임시로 하드코딩 (실제로는 세션에서 userId를 가져와야 함)
        String currentUserId = "testUser";  // 임시값

        // UserService를 통해 사용자 정보 조회 (다음 단계에서 구현)
        // User user = userService.findByUserId(currentUserId);
        // MyPageDTO myPageDTO = MyPageDTO.fromUser(user);

        // 임시 MyPageDTO 생성
        MyPageDTO myPageDTO = MyPageDTO.builder()
                .userId(currentUserId)
                .nickname("임시 닉네임")
                .name("홍길동")
                .email("test@example.com")
                .build();

        model.addAttribute("myPageData", myPageDTO);
        return "member/mypage";  // member/mypage.html 반환
    }

}



