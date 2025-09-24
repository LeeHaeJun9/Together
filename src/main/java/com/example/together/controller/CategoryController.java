package com.example.together.controller;

import com.example.together.domain.*;
import com.example.together.repository.CafeRepository;
import com.example.together.repository.MeetingRepository;
import com.example.together.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CategoryController {
    private final MeetingRepository meetingRepository;
    private final CafeRepository cafeRepository;
    private final TradeRepository tradeRepository;


    @GetMapping("/categories")
    public String getCategoryPage(@RequestParam String category, Model model) {
        List<Cafe> cafes;
        List<Trade> trades;
        List<Meeting> meetings;

        if ("ALL".equalsIgnoreCase(category)) {
            cafes = cafeRepository.findAll();
            trades = tradeRepository.findAll();
            meetings = meetingRepository.findByVisibility(Visibility.PUBLIC); // 전체 공개 모임
        } else {
            CafeCategory cafeCategory = CafeCategory.valueOf(category.toUpperCase());
            TradeCategory tradeCategory = TradeCategory.valueOf(category.toUpperCase());

            cafes = cafeRepository.findByCategory(cafeCategory);
            trades = tradeRepository.findByCategory(tradeCategory);
            meetings = meetingRepository.findByCafe_CategoryAndVisibility(cafeCategory, Visibility.PUBLIC);
        }

        model.addAttribute("cafes", cafes);
        model.addAttribute("trades", trades);
        model.addAttribute("meetings", meetings);
        model.addAttribute("category", category);

        return "category/categoryPage";
    }
}
