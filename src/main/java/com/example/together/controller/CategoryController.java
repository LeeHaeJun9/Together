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
            @RequestParam(defaultValue = "4", name = "t_size") int tSize,
            @RequestParam(required = false, name = "m_type") String mType,
            @RequestParam(required = false, name = "m_keyword") String mKeyword,
            @RequestParam(required = false, name = "c_type") String cType,
            @RequestParam(required = false, name = "c_keyword") String cKeyword,
            @RequestParam(required = false, name = "t_type") String tType,
            @RequestParam(required = false, name = "t_keyword") String tKeyword,

            @RequestParam(defaultValue = "regdate") String sort,
            Model model
    ) {
        if (tKeyword == null || tKeyword.isEmpty()) {
            tType = null;
        }

        // 💡 c_keyword와 m_keyword에 대해서도 동일하게 적용해야 합니다.
        if (cKeyword == null || cKeyword.isEmpty()) {
            cType = null;
        }
        if (mKeyword == null || mKeyword.isEmpty()) {
            mType = null;
        }


        PageRequestDTO meetingPageRequestDTO = PageRequestDTO.builder()
                .page(mPage).size(mSize).type(mType).keyword(mKeyword).sort(sort).build();
        PageRequestDTO cafePageRequestDTO = PageRequestDTO.builder()
                .page(cPage).size(cSize).type(cType).keyword(cKeyword).sort(sort).build();
        PageRequestDTO tradePageRequestDTO = PageRequestDTO.builder()
                .page(tPage).size(tSize).type(tType).keyword(tKeyword).sort(sort).build();

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