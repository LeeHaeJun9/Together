package com.example.together.controller.cafe;

import com.example.together.domain.CafeApplication;
import com.example.together.domain.CafeApplicationStatus;
import com.example.together.dto.cafe.*;
import com.example.together.service.cafe.CafeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cafe")
@RequiredArgsConstructor
@Slf4j
public class CafeController {

    private final CafeService cafeService;

    @GetMapping("/main")
    public String mainPage(Model model, Principal principal) {
        log.info("GET /main - 메인페이지 요청");
        List<CafeResponseDTO> cafes = cafeService.getAllCafes();
        model.addAttribute("cafes", cafes);
        return "mainpage";
    }

    // TODO: 실제 사용자 ID를 Principal 객체에서 가져오도록 변경해야 합니다.
    // 현재는 임시로 하드코딩된 userId를 사용합니다.
    // **중요: 실제 애플리케이션에서는 이 임시 코드를 로그인된 사용자의 ID를 반환하는 실제 로직으로 교체해야 합니다.**
    private Long getLoggedInUserId(Principal principal) {
        if (principal == null) {
            return null; // 로그인되지 않은 사용자
        }
        // 실제 구현에서는 principal.getName() (username)을 사용하여 User 엔티티를 찾고 ID를 반환해야 합니다.
        // 예: User user = userService.findByUsername(principal.getName()); return user.getId();
        return 1L; // 임시 userId (로그인된 사용자라고 가정)
    }

    @PostMapping("/apply")
    public String applyForCafe(@ModelAttribute CafeCreateRequestDTO cafeRequest, Principal principal) {
        Long userId = getLoggedInUserId(principal); // 로그인된 사용자 ID 사용
        if (userId == null) {
            // TODO: 로그인 페이지로 리다이렉트 또는 에러 처리
            return "redirect:/login"; // 예시: 로그인 페이지로 리다이렉트
        }
        cafeService.applyForCafe(cafeRequest, userId);
        // TODO: 신청 완료 페이지로 리다이렉트하거나 메시지를 표시하는 것이 좋습니다.
        return "redirect:/cafe/application-status"; // 가상의 신청 현황 페이지
    }

    // TODO: 카페 신청 폼을 보여주는 GET 매핑도 필요합니다 (예: /cafe/apply-form)
    @GetMapping("/apply-form")
    public String showApplyForm(Model model) {
        model.addAttribute("cafeCreateRequestDTO", new CafeCreateRequestDTO());
        return "cafe/applyForm"; // cafe/applyForm.html 뷰
    }

    @GetMapping("/admin/applications")
    public String getPendingApplications(Model model) {
        // CafeService에서 엔티티 리스트를 가져와서 DTO로 변환
        List<CafeApplication> applications = cafeService.getPendingApplications();
        List<CafeApplicationResponseDTO> applicationDTOs = applications.stream()
                .map(CafeApplicationResponseDTO::fromEntity) // ✅ fromEntity 사용
                .collect(Collectors.toList());

        model.addAttribute("applications", applicationDTOs);
        return "admin/applicationList";
    }

    @GetMapping("/admin/applications/{applicationId}")
    public String getCafeApplicationDetail(
            @PathVariable Long applicationId,
            Model model,
            RedirectAttributes redirectAttributes) {

        CafeApplicationResponseDTO applicationDTO = cafeService.getCafeApplicationDetail(applicationId);


        if (applicationDTO == null) {
            redirectAttributes.addFlashAttribute("error", "해당 신청서를 찾을 수 없습니다.");
            return "redirect:/cafe/admin/applications";
        }

        model.addAttribute("name", applicationDTO.getName());
        model.addAttribute("description", applicationDTO.getDescription());
        model.addAttribute("category", applicationDTO.getCategory());
        model.addAttribute("applicantId", applicationDTO.getApplicantId());
        model.addAttribute("regDate", applicationDTO.getRegDate());

        return "admin/applicationDetail";
    }


    @PostMapping("/admin/approve")
    public String approveCafe(@RequestParam Long applicationId, RedirectAttributes redirectAttributes, Principal principal) {
        Long adminId = getLoggedInUserId(principal); // 로그인된 관리자 ID 사용
        if (adminId == null) {
            return "redirect:/login";
        }

        try {
            CafeApplicationResponseDTO approvedCafe = cafeService.approveCafe(applicationId, adminId);
            redirectAttributes.addFlashAttribute("message", approvedCafe.getName() + " 카페 개설 신청이 승인되었습니다.");
            // TODO: 사용자에게 승인 알림 (이메일, 알림 시스템, 마이페이지에 링크 등) 로직 추가
            // 예: redirectAttributes.addFlashAttribute("finalRegistrationLink", "/cafe/my-applications/" + applicationId + "/complete");
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


    @GetMapping("/myApplications/{applicationId}/complete")
    public String showFinalRegistrationForm(@PathVariable Long applicationId, Model model, Principal principal) {
        Long userId = getLoggedInUserId(principal); // 로그인된 사용자 ID
        if (userId == null) {
            return "redirect:/login"; // 예시: 로그인 페이지로 리다이렉트
        }

        try {
            // 서비스에서 신청 정보를 가져와서 사용자 권한 확인
            CafeApplicationResponseDTO application = cafeService.getCafeApplicationDetail(applicationId);

            // 신청자가 본인인지, 그리고 이미 승인된 신청인지 확인
            if (!application.getApplicantId().equals(userId)) {
                model.addAttribute("error", "해당 카페 신청에 대한 권한이 없습니다.");
                return "error/accessDenied"; // TODO: 실제 오류 페이지 경로로 변경
            }
            // ✅ CafeApplicationStatus Enum 사용
            if (!application.getStatus().equals(CafeApplicationStatus.APPROVED)) { // Enum 객체 직접 비교 또는 .name() 비교
                model.addAttribute("error", "승인된 카페 신청만 최종 등록할 수 있습니다.");
                return "error/badRequest"; // TODO: 실제 오류 페이지 경로로 변경
            }

            model.addAttribute("applicationId", applicationId);

            model.addAttribute("name", application.getName());
            model.addAttribute("description", application.getDescription());
            model.addAttribute("category", application.getCategory());
            return "cafe/finalRegistrationForm"; // 최종 등록 폼 뷰
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error/notFound"; // TODO: 실제 오류 페이지 경로로 변경
        }
    }


    @PostMapping("/myApplications/{applicationId}/register")
    public String handleFinalRegistration(
            @PathVariable Long applicationId,
            @RequestParam("cafeImage") MultipartFile cafeImage,
            @RequestParam("cafeThumbnail") MultipartFile cafeThumbnail,
            RedirectAttributes redirectAttributes,
            Principal principal) {

        Long userId = getLoggedInUserId(principal); // 로그인된 사용자 ID
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            // ✅ 파일이 null이거나 비어있는 경우를 함께 체크합니다.
            if (cafeImage == null || cafeImage.isEmpty() || cafeThumbnail == null || cafeThumbnail.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "카페 대표 이미지와 썸네일은 필수입니다.");
                return "redirect:/myApplications/" + applicationId + "/complete";
            }
            log.info("Starting cafe registration for application ID: {}", applicationId);

            CafeResponseDTO newCafe = cafeService.registerCafeAfterApproval(applicationId, cafeImage, cafeThumbnail, userId);

            if (newCafe == null || newCafe.getId() == null) {
                redirectAttributes.addFlashAttribute("error", "카페 등록에 실패했습니다. 유효하지 않은 정보입니다.");
                log.error("Failed to register cafe. newCafe or newCafe.id is null.");
                return "redirect:/myApplications/" + applicationId + "/complete";
            }

            redirectAttributes.addFlashAttribute("message", "카페가 성공적으로 등록되었습니다!");
            log.info("Successfully registered cafe with ID: {}", newCafe.getId());
            return "redirect:/cafe/" + newCafe.getId();
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.error("Registration failed for application ID {}: {}", applicationId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/myApplications/" + applicationId + "/complete";
        }
    }

    @GetMapping("/list")
    public String listAllCafes(Model model, Principal principal) {
        // userId는 개인화된 정보가 필요할 때만 사용. 현재는 getAllCafes()가 userId를 받지 않으므로 제거.
        List<CafeResponseDTO> cafes = cafeService.getAllCafes();
        model.addAttribute("cafes", cafes);
        return "cafe/list";
    }

    @GetMapping("/{cafeId}")
    public String getCafe(@PathVariable Long cafeId, Model model, Principal principal) {
        Long userId = (principal != null) ? getLoggedInUserId(principal) : null; // 로그인된 사용자 ID (없으면 null)
        if (userId == null) {
            // 로그인 없이도 카페 상세 조회 가능하도록 처리하거나, 로그인 필요시 리다이렉트
            // 현재 로직은 userId가 null이더라도 서비스가 처리할 수 있다고 가정
        }
        CafeResponseDTO response = cafeService.getCafeById(cafeId, userId);
        model.addAttribute("cafe", response);
        return "cafe/detail";
    }

    @GetMapping("/{cafeId}/edit")
    public String showEditForm(@PathVariable Long cafeId, Model model, Principal principal) {
        Long userId = getLoggedInUserId(principal); // 로그인된 사용자 ID
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            CafeResponseDTO cafe = cafeService.getCafeById(cafeId, userId);
            if (!cafe.isOwner()) {
                model.addAttribute("error", "수정 권한이 없습니다.");
                return "error/access-denied"; // TODO: 실제 오류 페이지 경로로 변경
            }
            model.addAttribute("cafe", cafe);
            return "cafe/edit";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error/not-found"; // TODO: 실제 오류 페이지 경로로 변경
        }
    }

    @PostMapping("/{cafeId}/update")
    public String updateCafe(
            @PathVariable Long cafeId,
            @ModelAttribute CafeUpdateDTO updateDTO,
            RedirectAttributes redirectAttributes,
            Principal principal) {

        Long userId = getLoggedInUserId(principal); // 로그인된 사용자 ID
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            cafeService.updateCafe(cafeId, updateDTO, userId);
            redirectAttributes.addFlashAttribute("message", "카페 정보가 성공적으로 수정되었습니다.");
        } catch (IllegalAccessException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cafe/" + cafeId;
    }

    @PostMapping("/{cafeId}/delete")
    public String deleteCafe(
            @PathVariable Long cafeId,
            RedirectAttributes redirectAttributes,
            Principal principal) {

        Long userId = getLoggedInUserId(principal); // 로그인된 사용자 ID
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            cafeService.deleteCafe(cafeId, userId);
            redirectAttributes.addFlashAttribute("message", "카페가 성공적으로 삭제되었습니다.");
        } catch (IllegalAccessException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cafe/list";
    }

    @PostMapping("/{cafeId}/join")
    public String sendJoinRequest(@PathVariable Long cafeId, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        Long userId = getLoggedInUserId(principal);
        try {
            cafeService.sendJoinRequest(cafeId, userId);
            redirectAttributes.addFlashAttribute("message", "카페 가입 신청이 완료되었습니다.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cafe/" + cafeId;
    }

    @GetMapping("/my/cafe/{cafeId}/joinRequests")
    public String showJoinRequests(@PathVariable Long cafeId, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        Long userId = getLoggedInUserId(principal);

        // 권한 확인: 요청한 사용자가 해당 카페의 소유자인지 확인
        if (!cafeService.isCafeOwner(cafeId, userId)) {
            model.addAttribute("error", "해당 카페의 관리자만 접근할 수 있습니다.");
            return "error/accessDenied";
        }

        List<CafeJoinRequestResponseDTO> requests = cafeService.getPendingJoinRequests(cafeId);
        model.addAttribute("cafeId", cafeId);
        model.addAttribute("requests", requests);
        return "cafe/admin/joinRequestList";

    }

    @PostMapping("/my/cafe/joinRequests/approve/{requestId}")
    public String approveJoinRequest(@PathVariable Long requestId, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        Long adminId = getLoggedInUserId(principal);

        try {
            Long cafeId = cafeService.approveJoinRequest(requestId, adminId);
            redirectAttributes.addFlashAttribute("message", "가입 신청이 승인되었습니다.");

            return "redirect:/my/cafe/" + cafeId + "/joinRequests";
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            // 오류 발생 시에는 임시로 기존 URL로 리다이렉트
            return "redirect:/my/cafe/joinRequests";
        }
    }
}