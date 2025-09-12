package com.example.together.controller;

import com.example.together.dto.cafe.CafeApplicationResponseDTO;
import com.example.together.dto.cafe.CafeCreateRequestDTO;
import com.example.together.dto.cafe.CafeResponseDTO;
import com.example.together.service.CafeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cafe")
@RequiredArgsConstructor
public class CafeController {

    private final CafeService cafeService;

//    @PostMapping("/register")
//    public String applyForCafe(
//            @ModelAttribute CafeCreateRequestDTO requestDTO,
//            @AuthenticationPrincipal Long userId) {
//
//        cafeService.applyForCafe(requestDTO, userId);
//
//        return "redirect:/success-page";
//    }

    @PostMapping("/register")
    public String applyForCafe(@ModelAttribute CafeCreateRequestDTO cafeRequest) {
        // 로그인 시스템이 완성될 때까지 임시로 1L을 사용합니다.
        Long userId = 1L;

        // userId가 null일 경우를 처리하는 로직은 임시로 제거합니다.

        cafeService.applyForCafe(cafeRequest, userId);
        return "redirect:/success-page";
    }


    @GetMapping("/register")
    public ResponseEntity<String> handleInvalidGetRequest() {
        return ResponseEntity.badRequest().body("잘못된 접근입니다. 카페 신청은 POST 요청으로만 가능합니다.");
    }


    @PostMapping("/admin/approve/{applicationId}")
    public ResponseEntity<CafeResponseDTO> approveCafe(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal Long adminId) {

        // 실제로는 adminId를 사용하여 관리자 권한을 확인하는 로직이 필요합니다.
        CafeResponseDTO response = cafeService.approveCafe(applicationId, adminId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{cafeId}")
    public ResponseEntity<CafeResponseDTO> getCafe(@PathVariable Long cafeId) {
        CafeResponseDTO response = cafeService.getCafeById(cafeId);
        return ResponseEntity.ok(response);
    }
}