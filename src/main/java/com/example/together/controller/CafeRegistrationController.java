package com.example.together.controller;

import com.example.together.dto.cafe.CafeCreateRequestDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/CafeOpen")
public class CafeRegistrationController {

    @GetMapping
    public String showRegistrationForm(Model model) {
        model.addAttribute("cafeRequest", new CafeCreateRequestDTO());
        return "cafeOpen/register";
    }
}