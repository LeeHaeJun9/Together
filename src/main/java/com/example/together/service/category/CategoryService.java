package com.example.together.service.category;

import com.example.together.domain.*;
import com.example.together.dto.PageRequestDTO;
import com.example.together.dto.PageResponseDTO;
import com.example.together.dto.cafe.CafeResponseDTO;
import com.example.together.dto.meeting.MeetingDTO;
import com.example.together.dto.trade.TradeDTO;
import com.example.together.repository.CafeRepository;
import com.example.together.repository.search.CafeSearch;
import com.example.together.repository.MeetingRepository;
import com.example.together.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final MeetingRepository meetingRepository;
    private final CafeRepository cafeRepository;
    private final TradeRepository tradeRepository;

    // -------------------- Meeting --------------------
    public PageResponseDTO<MeetingDTO> getMeetings(String category, PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("regDate");
        Page<Meeting> meetingPage;

        if (pageRequestDTO.getKeyword() != null && !pageRequestDTO.getKeyword().isEmpty()) {
            if ("ALL".equalsIgnoreCase(category)) {
                // 새로운 Repository 메소드 호출: 카테고리 관계없이 검색
                meetingPage = meetingRepository.findByVisibilityAndSearch(Visibility.PUBLIC, pageRequestDTO.getType(), pageRequestDTO.getKeyword(), pageable);
            } else {
                CafeCategory cafeCategory = CafeCategory.valueOf(category.toUpperCase());
                // 새로운 Repository 메소드 호출: 특정 카테고리 내에서 검색
                meetingPage = meetingRepository.findByCategoryAndVisibilityAndSearch(cafeCategory, Visibility.PUBLIC, pageRequestDTO.getType(), pageRequestDTO.getKeyword(), pageable);
            }
        } else {
            if ("ALL".equalsIgnoreCase(category)) {
                meetingPage = meetingRepository.findByVisibilityWithCafeAndUser(Visibility.PUBLIC, pageable);
            } else {
                CafeCategory cafeCategory = CafeCategory.valueOf(category.toUpperCase());
                meetingPage = meetingRepository.findByCafeCategoryAndVisibilityWithCafeAndUser(cafeCategory, Visibility.PUBLIC, pageable);
            }
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

        if (pageRequestDTO.getKeyword() != null && !pageRequestDTO.getKeyword().isEmpty()) {
            if ("ALL".equalsIgnoreCase(category)) {
                // 새로운 Repository 메소드 호출: 카테고리 관계없이 검색
                cafePage = cafeRepository.findBySearch(pageRequestDTO.getType(), pageRequestDTO.getKeyword(), pageable);
            } else {
                CafeCategory cafeCategory = CafeCategory.valueOf(category.toUpperCase());
                // 새로운 Repository 메소드 호출: 특정 카테고리 내에서 검색
                cafePage = cafeRepository.findByCategoryAndSearch(cafeCategory, pageRequestDTO.getType(), pageRequestDTO.getKeyword(), pageable);
            }
        } else { // 💡 검색 조건이 없는 경우
            if ("ALL".equalsIgnoreCase(category)) {
                cafePage = cafeRepository.findAll(pageable);
            } else {
                CafeCategory cafeCategory = CafeCategory.valueOf(category.toUpperCase());
                cafePage = cafeRepository.findPageByCategory(cafeCategory, pageable);
            }
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
        Pageable pageable = pageRequestDTO.getPageable("regdate");
        Page<Trade> tradePage;

        if (pageRequestDTO.getKeyword() != null && !pageRequestDTO.getKeyword().isEmpty()) {
            TradeCategory tradeCategory = mapLabelToEnum(category);
            // 새로운 Repository 메소드 호출
            tradePage = tradeRepository.findByCategoryAndSearch(tradeCategory, pageRequestDTO.getType(), pageRequestDTO.getKeyword(), pageable);
        } else { // 💡 검색 조건이 없는 경우
            if ("ALL".equalsIgnoreCase(category)) {
                tradePage = tradeRepository.findAll(pageable);
            } else {
                TradeCategory tradeCategory = mapLabelToEnum(category);
                if (tradeCategory == null) {
                    return PageResponseDTO.<TradeDTO>withAll()
                            .pageRequestDTO(pageRequestDTO)
                            .dtoList(Collections.emptyList())
                            .total(0)
                            .build();
                }
                tradePage = tradeRepository.findByCategory(tradeCategory, pageable);
            }
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
    private TradeCategory mapLabelToEnum(String t) {
        if (t == null) return null;
        t = t.trim();
        if (t.isEmpty()) return null;

        // enum 이름 그대로 들어온 경우
        try {
            // TradeCategory.valueOf("SPORTS") 등
            return TradeCategory.valueOf(t.toUpperCase(Locale.ROOT));
        } catch (Exception ignore) {}

        // 한글 라벨 매핑
        try {
            return switch (t) {
                // TradeController의 mapLabelToEnum 로직을 그대로 사용
                case "운동"     -> TradeCategory.valueOf("SPORTS");
                case "예술"     -> TradeCategory.valueOf("ART");
                case "음악"     -> TradeCategory.valueOf("MUSIC");
                case "반려동물" -> TradeCategory.valueOf("PETS"); // 팀원 코드를 따라 PETS 사용
                case "수집"     -> TradeCategory.valueOf("COLLECTION");
                case "언어"     -> TradeCategory.valueOf("LANGUAGE");
                case "요리"     -> TradeCategory.valueOf("COOKING");
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }
}
