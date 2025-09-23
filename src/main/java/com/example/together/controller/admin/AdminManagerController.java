package com.example.together.controller.admin;

import com.example.together.dto.manager.ManagerDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/managers")  // CHANGED: Changed from /admin/manager to /admin/managers
public class AdminManagerController {

    // 관리자 목록 페이지
    @GetMapping("/list")  // This now becomes /admin/managers/list (no conflict!)
    public String managerList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "role", required = false) String role,
            Model model) {

        // 테스트용 더미 데이터 (실제로는 Service에서 가져옴)
        List<ManagerDTO> managers = getDummyManagers();

        // 검색/필터링 처리 (간단한 예시)
        if (search != null && !search.trim().isEmpty()) {
            managers = managers.stream()
                    .filter(m -> m.getManagerName().contains(search) ||
                            m.getEmail().contains(search))
                    .toList();
        }

        if (status != null && !status.isEmpty()) {
            managers = managers.stream()
                    .filter(m -> m.getStatus().equals(status))
                    .toList();
        }

        if (role != null && !role.isEmpty()) {
            managers = managers.stream()
                    .filter(m -> m.getRole().equals(role))
                    .toList();
        }

        // 통계 계산
        long totalCount = managers.size();
        long activeCount = managers.stream().filter(m -> "ACTIVE".equals(m.getStatus())).count();
        long inactiveCount = totalCount - activeCount;

        // 페이징 처리 (간단한 예시)
        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int start = page * pageSize;
        int end = Math.min(start + pageSize, (int) totalCount);

        List<ManagerDTO> pagedManagers = managers.subList(start, end);

        // 모델에 데이터 추가
        model.addAttribute("managers", pagedManagers);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", inactiveCount);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "admin/managerList";
    }

    // 관리자 등록 폼 페이지
    @GetMapping("/register")  // Now becomes /admin/managers/register
    public String registerForm(Model model) {
        model.addAttribute("managerDTO", new ManagerDTO());
        return "admin/register";
    }

    // 관리자 등록 처리
    @PostMapping("/register")  // Now becomes /admin/managers/register
    public String registerManager(
            @ModelAttribute ManagerDTO managerDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 유효성 검사 실패 시
        if (result.hasErrors()) {
            model.addAttribute("error", "입력 정보를 확인해주세요.");
            return "admin/register";
        }

        // 이메일 중복 체크 (예시)
        if ("admin@test.com".equals(managerDTO.getEmail())) {
            model.addAttribute("error", "이미 사용 중인 이메일입니다.");
            return "admin/register";
        }

        try {
            // 실제로는 Service를 통해 저장
            // managerService.createManager(managerDTO);

            // 테스트용 처리
            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            managerDTO.setCreateDate(currentDate);

            System.out.println("새 관리자 등록: " + managerDTO.getManagerName() +
                    " (" + managerDTO.getEmail() + ")");

            redirectAttributes.addFlashAttribute("message",
                    "관리자 '" + managerDTO.getManagerName() + "'이(가) 성공적으로 등록되었습니다.");

            return "redirect:/admin/managers/list";  // UPDATED: Changed redirect path

        } catch (Exception e) {
            model.addAttribute("error", "등록 중 오류가 발생했습니다: " + e.getMessage());
            return "admin/register";
        }
    }

    // 관리자 수정 폼 페이지
    @GetMapping("/edit/{id}")  // Now becomes /admin/managers/edit/{id}
    public String editForm(@PathVariable Long id, Model model) {
        // 실제로는 Service에서 ID로 조회
        ManagerDTO manager = findManagerById(id);

        if (manager == null) {
            model.addAttribute("error", "관리자를 찾을 수 없습니다.");
            return "redirect:/admin/managers/list";  // UPDATED: Changed redirect path
        }

        model.addAttribute("managerDTO", manager);
        return "admin/register"; // 같은 폼 재사용
    }

    // 관리자 삭제
    @DeleteMapping("/delete/{id}")  // Now becomes /admin/managers/delete/{id}
    @ResponseBody
    public String deleteManager(@PathVariable Long id) {
        try {
            // 실제로는 Service를 통해 삭제
            // managerService.deleteManager(id);

            System.out.println("관리자 삭제: ID = " + id);
            return "success";

        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    // =============== 테스트용 더미 데이터 메서드들 ===============

    private List<ManagerDTO> getDummyManagers() {
        List<ManagerDTO> managers = new ArrayList<>();

        managers.add(new ManagerDTO(1L, "김철수", "admin@test.com",
                "password123", "SUPER_ADMIN", "2024-01-15", "ACTIVE"));
        managers.add(new ManagerDTO(2L, "이영희", "manager1@test.com",
                "password123", "ADMIN", "2024-02-10", "ACTIVE"));
        managers.add(new ManagerDTO(3L, "박민수", "manager2@test.com",
                "password123", "ADMIN", "2024-03-01", "INACTIVE"));
        managers.add(new ManagerDTO(4L, "최정미", "manager3@test.com",
                "password123", "ADMIN", "2024-03-05", "ACTIVE"));
        managers.add(new ManagerDTO(5L, "홍길동", "manager4@test.com",
                "password123", "ADMIN", "2024-03-10", "ACTIVE"));

        return managers;
    }

    private ManagerDTO findManagerById(Long id) {
        List<ManagerDTO> managers = getDummyManagers();
        return managers.stream()
                .filter(m -> m.getManagerId().equals(id))
                .findFirst()
                .orElse(null);
    }
}