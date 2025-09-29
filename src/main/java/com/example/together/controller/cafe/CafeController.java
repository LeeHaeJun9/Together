package com.example.together.controller.cafe;

import com.example.together.domain.Cafe;
import com.example.together.domain.CafeApplication;
import com.example.together.domain.CafeApplicationStatus;
import com.example.together.domain.CafeCategory;
import com.example.together.dto.PageRequestDTO;
import com.example.together.dto.PageResponseDTO;
import com.example.together.dto.cafe.*;
import com.example.together.dto.calendar.CalendarEventDTO;
import com.example.together.dto.meeting.MeetingDTO;
import com.example.together.dto.post.PostResponseDTO;
import com.example.together.service.UserService;
import com.example.together.service.cafe.CafeService;
import com.example.together.service.meeting.MeetingService;
import com.example.together.service.post.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final UserService userService;
    private final MeetingService meetingService;
    private final PostService postService;

    private Long getLoggedInUserId(Principal principal) {
        if (principal == null) {
            // 로그인되지 않은 사용자는 null을 반환하여 서비스 계층에서 처리하도록 합니다.
            return null;
        }
        String userIdString = principal.getName();
        // UserService의 findByUserId 메서드를 사용해 Long 타입의 고유 ID를 반환합니다.
        return userService.findByUserId(userIdString).getId();
    }

    @PostMapping("/apply")
    public String applyForCafe(@ModelAttribute CafeCreateRequestDTO cafeRequest, Principal principal) {
        Long userId = getLoggedInUserId(principal); // 로그인된 사용자 ID 사용
        cafeService.applyForCafe(cafeRequest, userId);
        // TODO: 신청 완료 페이지로 리다이렉트하거나 메시지를 표시하는 것이 좋습니다.
        return "redirect:/cafe/application-status"; // 가상의 신청 현황 페이지
    }

    @GetMapping("/application-status")
    public String showApplicationStatus() {
        return "cafe/applicationStatus";
    }

//    @GetMapping("/apply-form")
//    public String showApplyForm(Model model) {
//        model.addAttribute("cafeCreateRequestDTO", new CafeCreateRequestDTO());
//        return "cafe/applyForm"; // cafe/applyForm.html 뷰
//    }

    @GetMapping("/admin/applications")
    public String getPendingApplications(Model model) {
        // CafeService에서 엔티티 리스트를 가져와서 DTO로 변환
        List<CafeApplication> applications = cafeService.getPendingApplications();
        List<CafeApplicationResponseDTO> applicationDTOs = applications.stream()
                .map(CafeApplicationResponseDTO::fromEntity) // ✅ fromEntity 사용
                .collect(Collectors.toList());

        model.addAttribute("applications", applicationDTOs);
        return "/cafe/admin/applicationList";
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

        return "/cafe/admin/applicationDetail";
    }


    @PostMapping("/admin/approve")
    public String approveCafe(@RequestParam Long applicationId, RedirectAttributes redirectAttributes, Principal principal) {
        Long adminId = getLoggedInUserId(principal); // 로그인된 사용자 ID
        if (adminId == null) {
            return "redirect:/login"; // 로그인되지 않았으면 로그인 페이지로 리다이렉트
        }

        if (!userService.isAdmin(adminId)) {
            redirectAttributes.addFlashAttribute("error", "카페 신청 승인 권한이 없습니다.");
            return "redirect:/cafe/admin/applications";
        }

        try {
            CafeApplicationResponseDTO approvedCafe = cafeService.approveCafe(applicationId, adminId);
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
    public String listAllCafes(
            @RequestParam(value = "category", required = false) String categoryName,
            @RequestParam(value = "size", defaultValue = "6", required = false) int size,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model,
            PageRequestDTO pageRequestDTO) {

        log.info("GET /cafe/list 요청. 카테고리: {}", categoryName);

        // 요청받은 size 값을 pageRequestDTO에 설정
        pageRequestDTO.setSize(size);

        // type과 keyword를 PageRequestDTO에 직접 설정
        pageRequestDTO.setType(type);
        pageRequestDTO.setKeyword(keyword);

        PageResponseDTO<CafeResponseDTO> responseDTO;
        CafeCategory cafeCategory = null;

        if (categoryName != null && !categoryName.equalsIgnoreCase("ALL")) {
            try {
                cafeCategory = CafeCategory.valueOf(categoryName.toUpperCase());
                // 카테고리 필터링 시 type과 keyword는 사용하지 않으므로 null로 초기화
                pageRequestDTO.setType(null);
                pageRequestDTO.setKeyword(null);
            } catch (IllegalArgumentException e) {
                log.error("잘못된 카테고리 이름: {}", categoryName);
                // 잘못된 카테고리일 경우 필터링 조건을 모두 제거
                cafeCategory = null;
                pageRequestDTO.setType(null);
                pageRequestDTO.setKeyword(null);
            }
        }

        // 통합된 서비스 메서드 사용
        responseDTO = cafeService.getCafeListWithFilters(pageRequestDTO, cafeCategory);

        model.addAttribute("pageResponseDTO", responseDTO);
        model.addAttribute("categories", CafeCategory.values());

        return "cafe/list";
    }

    @GetMapping("/{cafeId}")
    public String getCafe(@PathVariable Long cafeId, Model model, Principal principal) {
        Long userId = (principal != null) ? getLoggedInUserId(principal) : null; // 로그인된 사용자 ID (없으면 null)
        String userNickname = null;

        List<PostResponseDTO> latestNotices = postService.getLatestNotices(cafeId, userId);
        List<PostResponseDTO> popularPosts = postService.getPopularPosts(cafeId, 5, userId);
        List<Cafe> similarCafes = cafeService.getSimilarCafes(cafeId, 3);
        List<CalendarEventDTO> events = cafeService.getCalendarEvents(cafeId);

        if (userId != null) {
            userNickname = userService.getUserNicknameById(userId);
        }
        CafeResponseDTO response = cafeService.getCafeInfoWithMembership(cafeId, userId);
        model.addAttribute("cafe", response);
        model.addAttribute("userNickname", userNickname);
        model.addAttribute("latestNotices", latestNotices);
        model.addAttribute("popularPosts", popularPosts);
        model.addAttribute("similarCafes", similarCafes);
        model.addAttribute("calendarEvents", events);
        return "cafe/detail";
    }

    @GetMapping("/{cafeId}/edit")
    public String showEditForm(@PathVariable Long cafeId, Model model, Principal principal) {
        Long userId = getLoggedInUserId(principal); // 로그인된 사용자 ID
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            CafeResponseDTO cafe = cafeService.getCafeInfoWithMembership(cafeId, userId);
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
    @ResponseBody
    public ResponseEntity<String> deleteCafeAjax(@PathVariable Long cafeId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        Long userId = getLoggedInUserId(principal);
        try {
            cafeService.deleteCafe(cafeId, userId);
            return ResponseEntity.ok("카페가 삭제되었습니다.");
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("카페 삭제 중 오류 발생");
        }
    }

    @PostMapping("/{cafeId}/join")
    @ResponseBody // 💡 @ResponseBody를 추가하여 JSON/텍스트 응답을 반환하도록 변경
    public ResponseEntity<?> sendJoinRequest(@PathVariable Long cafeId, Principal principal) {
        if (principal == null) {
            // 로그인 필요 시 401 Unauthorized 반환
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        Long userId = getLoggedInUserId(principal);
        try {
            cafeService.sendJoinRequest(cafeId, userId);
            // 💡 성공 시 200 OK와 성공 메시지 반환
            return ResponseEntity.ok("카페 가입 신청이 완료되었습니다.");
        } catch (IllegalStateException e) {
            // 이미 가입/신청 대기 중인 경우 409 Conflict 반환 (프론트엔드에서 409 처리 가능)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            // 유효하지 않은 cafeId 등인 경우 400 Bad Request 반환
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/my/cafe/{cafeId}/joinRequests")
    public String showJoinRequests(@PathVariable Long cafeId, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        Long userId = getLoggedInUserId(principal);

        CafeResponseDTO cafe = cafeService.getCafeInfoWithMembership(cafeId, userId);

        // 권한 확인: 요청한 사용자가 해당 카페의 소유자인지 확인
        if (!cafeService.isCafeOwner(cafeId, userId)) {
            model.addAttribute("error", "해당 카페의 관리자만 접근할 수 있습니다.");
            return "error/accessDenied";
        }

        List<CafeJoinRequestResponseDTO> requests = cafeService.getPendingJoinRequests(cafeId);
        model.addAttribute("cafeId", cafeId);
        model.addAttribute("requests", requests);
        model.addAttribute("cafe", cafe);
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

            return "redirect:/cafe/my/cafe/" + cafeId + "/joinRequests";
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            // 오류 발생 시에는 임시로 기존 URL로 리다이렉트
            return "redirect:/cafe/my/cafe/joinRequests";
        }
    }

    @GetMapping("/myApplications")
    public String showMyApplications(Model model, Principal principal) {
        if (principal == null) {
            // 로그인하지 않은 사용자는 로그인 페이지로 리다이렉트
            return "redirect:/login";
        }

        Long userId = getLoggedInUserId(principal);

        // CafeService를 통해 사용자의 모든 신청 내역을 가져옵니다.
        List<CafeApplicationResponseDTO> myApplications = cafeService.getApplicationsByUserId(userId);

        // 뷰(Thymeleaf)로 전달하기 위해 모델에 추가
        model.addAttribute("userApplications", myApplications);

        // 신청 목록을 보여줄 뷰 템플릿의 경로를 반환합니다.
        return "cafe/myApplicationList"; // 예시: cafe/myApplicationList.html
    }

    @GetMapping("/my/joined-cafes")
    public String showMyJoinedCafes(Model model, Principal principal) {
        // ... (로그인 및 ID 가져오기 로직 유지) ...

        Long userId = getLoggedInUserId(principal);
        MyJoinedCafesDTO myCafesData = cafeService.getMyJoinedCafes(userId);

        // 1. 필수 통계
        model.addAttribute("totalCafes", myCafesData.getTotalJoinedCafes());
        model.addAttribute("ownedCafes", myCafesData.getTotalOwnedCafes());

        // 2. 상위 1개 카테고리 통계
        model.addAttribute("cat1Name", myCafesData.getSelectedCategory1Name());
        model.addAttribute("cat1Count", myCafesData.getSelectedCategory1Count());

        // 💡 3. 최근 가입 카페 수
        model.addAttribute("recentlyJoinedCount", myCafesData.getRecentlyJoinedCount());

        model.addAttribute("cafes", myCafesData.getMemberships());

        return "cafe/myCafes";
    }

    @PostMapping("/{cafeId}/leave")
    @ResponseBody
    public ResponseEntity<String> leaveCafeAjax(@PathVariable Long cafeId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        Long userId = getLoggedInUserId(principal);
        try {
            cafeService.leaveCafe(cafeId, userId);
            return ResponseEntity.ok("카페 탈퇴가 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("카페 탈퇴 중 오류 발생");
        }
    }
}