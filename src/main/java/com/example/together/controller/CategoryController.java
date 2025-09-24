package com.example.together.controller;

import com.example.together.domain.*;
import com.example.together.dto.PageRequestDTO;
import com.example.together.dto.PageResponseDTO;
import com.example.together.dto.cafe.CafeResponseDTO;
import com.example.together.dto.meeting.MeetingDTO;
import com.example.together.dto.trade.TradeDTO;
import com.example.together.repository.CafeRepository;
import com.example.together.repository.MeetingRepository;
import com.example.together.repository.TradeRepository;
import com.example.together.service.category.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/categories")
    public String getCategoryPage(
            @RequestParam String category,
            @RequestParam(defaultValue = "1", name = "m_page") int mPage,
            @RequestParam(defaultValue = "10", name = "m_size") int mSize,
            @RequestParam(defaultValue = "1", name = "c_page") int cPage,
            @RequestParam(defaultValue = "4", name = "c_size") int cSize,
            @RequestParam(defaultValue = "1", name = "t_page") int tPage,
            @RequestParam(defaultValue = "10", name = "t_size") int tSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "regdate") String sort,
            Model model
    ) {
        PageRequestDTO meetingPageRequestDTO = PageRequestDTO.builder()
                .page(mPage).size(mSize).type(type).keyword(keyword).sort(sort).build();
        PageRequestDTO cafePageRequestDTO = PageRequestDTO.builder()
                .page(cPage).size(cSize).type(type).keyword(keyword).sort(sort).build();
        PageRequestDTO tradePageRequestDTO = PageRequestDTO.builder()
                .page(tPage).size(tSize).type(type).keyword(keyword).sort(sort).build();

        PageResponseDTO<MeetingDTO> meetings = categoryService.getMeetings(category, meetingPageRequestDTO);
        PageResponseDTO<CafeResponseDTO> cafes = categoryService.getCafes(category, cafePageRequestDTO);
        PageResponseDTO<TradeDTO> trades = categoryService.getTrades(category, tradePageRequestDTO);
        model.addAttribute("category", category);
        model.addAttribute("meetings", meetings.getDtoList());
        model.addAttribute("meetingsPage", meetings);
        model.addAttribute("cafes", cafes.getDtoList());
        model.addAttribute("cafesPage", cafes);
        model.addAttribute("trades", trades.getDtoList());
        model.addAttribute("tradesPage", trades);

        return "category/categoryPage";
    }
}