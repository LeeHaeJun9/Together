package com.example.together.service.category;

import com.example.together.domain.*;
import com.example.together.dto.PageRequestDTO;
import com.example.together.dto.PageResponseDTO;
import com.example.together.dto.cafe.CafeResponseDTO;
import com.example.together.dto.meeting.MeetingDTO;
import com.example.together.dto.trade.TradeDTO;
import com.example.together.repository.CafeRepository;
import com.example.together.repository.MeetingRepository;
import com.example.together.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final MeetingRepository meetingRepository;
    private final CafeRepository cafeRepository;
    private final TradeRepository tradeRepository;

    // -------------------- Meeting --------------------
    public PageResponseDTO<MeetingDTO> getMeetings(String category, PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("regDate"); // 정렬 기준 regDate

        Page<Meeting> meetingPage;
        if ("ALL".equalsIgnoreCase(category)) {
            meetingPage = meetingRepository.findByVisibilityWithCafeAndUser(Visibility.PUBLIC, pageable);
        } else {
            CafeCategory cafeCategory = CafeCategory.valueOf(category.toUpperCase());
            meetingPage = meetingRepository.findByCafeCategoryAndVisibilityWithCafeAndUser(cafeCategory, Visibility.PUBLIC, pageable);
        }
        List<MeetingDTO> dtoList = meetingPage.stream()
                .map(MeetingDTO::fromEntity)
                .toList();

        return PageResponseDTO.<MeetingDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int) meetingPage.getTotalElements())
                .build();
    }

    // -------------------- Cafe --------------------
    public PageResponseDTO<CafeResponseDTO> getCafes(String category, PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("regDate");

        Page<Cafe> cafePage;
        if ("ALL".equalsIgnoreCase(category)) {
            cafePage = cafeRepository.findAll(pageable);
        } else {
            CafeCategory cafeCategory = CafeCategory.valueOf(category.toUpperCase());
            cafePage = cafeRepository.findPageByCategory(cafeCategory, pageable);
        }

        List<CafeResponseDTO> dtoList = cafePage.stream()
                .map(CafeResponseDTO::fromEntity)
                .toList();

        return PageResponseDTO.<CafeResponseDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int) cafePage.getTotalElements())
                .build();
    }

    // -------------------- Trade --------------------
    public PageResponseDTO<TradeDTO> getTrades(String category, PageRequestDTO pageRequestDTO) {
      // ✨ 엔티티 필드명이 regDate 라면 아래처럼 CamelCase 사용
      Pageable pageable = pageRequestDTO.getPageable("regDate");

      Page<Trade> tradePage;
      if ("ALL".equalsIgnoreCase(category)) {
        tradePage = tradeRepository.findAll(pageable);
      } else {
        TradeCategory tradeCategory = TradeCategory.valueOf(category.toUpperCase());
        tradePage = tradeRepository.findByCategory(tradeCategory, pageable);
        // (선택) 위에 주석 처리한 메서드를 쓴다면:
        // tradePage = tradeRepository.findByCategoryOrderByRegDateDesc(tradeCategory, pageable);
      }

      List<TradeDTO> dtoList = tradePage.stream()
          .map(TradeDTO::fromEntity)
          .toList();

      return PageResponseDTO.<TradeDTO>withAll()
          .pageRequestDTO(pageRequestDTO)
          .dtoList(dtoList)
          .total((int) tradePage.getTotalElements())
          .build();
    }
}
