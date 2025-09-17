package com.example.together.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    // 메인페이지
    @GetMapping("/mainPage")
    public String mainPage(Model model, Principal principal) {
        log.info("GET /main - 메인페이지 요청");

        // 메인페이지 데이터 추가 (예: 인기 카페 목록 등)
        model.addAttribute("message", "모여라에 오신 것을 환영합니다!");

        return "mainPage";  // mainPage.html
    }
}
