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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

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
        Long userId = 1L; // 로그인 시스템이 완성될 때까지 임시로 1L을 사용합니다.
        cafeService.applyForCafe(cafeRequest, userId);
        return "redirect:/success-page";
    }

    @GetMapping("/register")
    public ResponseEntity<String> handleInvalidGetRequest() {
        return ResponseEntity.badRequest().body("잘못된 접근입니다. 카페 신청은 POST 요청으로만 가능합니다.");
    }

    @GetMapping("/{cafeId}")
    public ResponseEntity<CafeResponseDTO> getCafe(@PathVariable Long cafeId) {
        CafeResponseDTO response = cafeService.getCafeById(cafeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/applications")
    public String getCafeApplications(Model model) {
        model.addAttribute("applications", cafeService.getPendingApplications());
        return "admin/applicationList";
    }

    @GetMapping("/admin/applications/{applicationId}")
    public String getCafeApplicationDetail(
            @PathVariable Long applicationId,
            Model model,
            RedirectAttributes redirectAttributes) {

        System.out.println("Controller 호출됨: applicationId = " + applicationId);

        CafeApplicationResponseDTO applicationDTO = cafeService.getCafeApplicationDetail(applicationId);

        if (applicationDTO == null) {
            redirectAttributes.addFlashAttribute("error", "해당 신청서를 찾을 수 없습니다.");
            return "redirect:/cafe/admin/applications";
        }

        // **이 부분이 수정되었습니다.**
        // DTO의 각 속성을 개별적으로 모델에 추가합니다.
        model.addAttribute("name", applicationDTO.getName());
        model.addAttribute("description", applicationDTO.getDescription());
        model.addAttribute("category", applicationDTO.getCategory());
        model.addAttribute("applicantId", applicationDTO.getApplicantId());
        model.addAttribute("regDate", applicationDTO.getRegDate());

        System.out.println("DTO 확인: " + applicationDTO);

        return "admin/applicationDetail";
    }

    @PostMapping("/admin/approve")
    public String approveCafe(@RequestParam Long applicationId, RedirectAttributes redirectAttributes) {
        Long adminId = 1L; // 임시로 1L을 사용

        try {
            CafeResponseDTO approvedCafe = cafeService.approveCafe(applicationId, adminId);
            redirectAttributes.addFlashAttribute("message", approvedCafe.getName() + " 카페 개설 신청이 승인되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "카페 신청 승인 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/cafe/admin/applications";
    }

    @PostMapping("/admin/reject")
    public String rejectCafe(@RequestParam Long applicationId, RedirectAttributes redirectAttributes) {
        try {
            cafeService.rejectCafe(applicationId);
            redirectAttributes.addFlashAttribute("message", "카페 신청이 성공적으로 거절되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "카페 신청 거절 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/cafe/admin/applications";
    }
}