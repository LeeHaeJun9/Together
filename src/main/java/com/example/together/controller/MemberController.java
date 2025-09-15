package com.example.together.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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
}