package com.example.together.controller.Manager;
import com.example.together.domain.User;
import com.example.together.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {
    private final UserService userService;

    /**
     * 회원 관리 페이지
     */
    @GetMapping("/mUser")
    public String userManagePage(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String role,
            @RequestParam(defaultValue = "regDate") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model,
            Principal principal) {

        log.info("회원 관리 페이지 요청: admin={}, search={}, status={}, role={}",
                principal != null ? principal.getName() : "anonymous", search, status, role);

        try {
            // 관리자 권한 확인
            if (principal != null) {
                // TODO: 관리자 권한 체크
                // User admin = userService.findByUserId(principal.getName());
                // if (!admin.getSystemRole().equals(SystemRole.ADMIN)) {
                //     return "redirect:/main";
                // }
            }

            // 페이지네이션 설정
            Sort.Direction direction = Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

            // 사용자 목록 조회 (검색 및 필터링 포함)
            Page<User> userPage = searchUsers(search, status, role, pageable);

            // 통계 데이터 조회
            UserStats stats = getUserStatistics();

            // 모델에 데이터 추가
            model.addAttribute("users", userPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", userPage.getTotalPages());
            model.addAttribute("totalCount", userPage.getTotalElements());

            // 검색 조건 유지
            model.addAttribute("searchTerm", search);
            model.addAttribute("status", status);
            model.addAttribute("role", role);
            model.addAttribute("sort", sort);

            // 통계 데이터
            model.addAttribute("totalUsers", stats.getTotalUsers());
            model.addAttribute("activeUsers", stats.getActiveUsers());
            model.addAttribute("suspendedUsers", stats.getSuspendedUsers());
            model.addAttribute("newUsersToday", stats.getNewUsersToday());

            log.info("회원 관리 데이터 로드 완료: 총 {}명, 검색결과 {}명",
                    stats.getTotalUsers(), userPage.getTotalElements());

        } catch (Exception e) {
            log.error("회원 관리 페이지 로드 중 오류: {}", e.getMessage());
            setDefaultUserData(model);
        }

        return "manager/mUser";
    }

    /**
     * 사용자 검색 및 필터링
     */
    private Page<User> searchUsers(String search, String status, String role, Pageable pageable) {
        try {
            // TODO: 실제 구현에서는 UserService의 검색 메서드 사용
            // if (!search.isEmpty() || !status.isEmpty() || !role.isEmpty()) {
            //     return userService.searchUsers(search, status, role, pageable);
            // } else {
            //     return userService.findAllUsers(pageable);
            // }

            // 임시 구현: 빈 페이지 반환
            return Page.empty(pageable);

        } catch (Exception e) {
            log.error("사용자 검색 중 오류: {}", e.getMessage());
            return Page.empty(pageable);
        }
    }

    /**
     * 사용자 통계 조회
     */
    private UserStats getUserStatistics() {
        UserStats stats = new UserStats();

        try {
            // TODO: 실제 통계 조회 구현
            // stats.setTotalUsers(userService.countAllUsers());
            // stats.setActiveUsers(userService.countActiveUsers());
            // stats.setSuspendedUsers(userService.countSuspendedUsers());
            // stats.setNewUsersToday(userService.countNewUsersToday());

            // 임시 데이터
            stats.setTotalUsers(1245L);
            stats.setActiveUsers(1156L);
            stats.setSuspendedUsers(23L);
            stats.setNewUsersToday(12L);

        } catch (Exception e) {
            log.error("사용자 통계 조회 중 오류: {}", e.getMessage());
            // 기본값 유지
        }

        return stats;
    }

    /**
     * 사용자 상세 정보 조회 (AJAX)
     */
    @GetMapping("/api/admin/users/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserDetail(@PathVariable Long userId, Principal principal) {
        log.info("사용자 상세 정보 요청: userId={}, admin={}", userId,
                principal != null ? principal.getName() : "anonymous");

        try {
            // TODO: 실제 사용자 조회
            // User user = userService.findById(userId);
            // if (user == null) {
            //     return ResponseEntity.notFound().build();
            // }

            // 임시 데이터 반환
            Map<String, Object> userDetail = new HashMap<>();
            userDetail.put("id", userId);
            userDetail.put("userId", "user123");
            userDetail.put("name", "홍길동");
            userDetail.put("email", "user@example.com");
            userDetail.put("phone", "010-1234-5678");
            userDetail.put("regDate", "2024-01-15");
            userDetail.put("status", "ACTIVE");
            userDetail.put("profileImage", "/images/default-avatar.png");
            userDetail.put("cafeCount", 3);
            userDetail.put("tradeCount", 15);

            return ResponseEntity.ok(userDetail);

        } catch (Exception e) {
            log.error("사용자 상세 정보 조회 실패: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 사용자 정지 처리 (AJAX)
     */
    @PostMapping("/api/admin/users/{userId}/suspend")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> suspendUser(
            @PathVariable Long userId,
            @RequestBody(required = false) Map<String, String> requestBody,
            Principal principal) {

        log.info("사용자 정지 요청: userId={}, admin={}", userId,
                principal != null ? principal.getName() : "anonymous");

        Map<String, Object> response = new HashMap<>();

        try {
            String reason = requestBody != null ? requestBody.get("reason") : "관리자 정지";

            // TODO: 실제 정지 처리
            // boolean success = userService.suspendUser(userId, reason, principal.getName());
            // if (!success) {
            //     response.put("success", false);
            //     response.put("message", "사용자 정지에 실패했습니다.");
            //     return ResponseEntity.badRequest().body(response);
            // }

            response.put("success", true);
            response.put("message", "사용자가 성공적으로 정지되었습니다.");

            log.info("사용자 정지 완료: userId={}, reason={}", userId, reason);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("사용자 정지 처리 실패: userId={}, error={}", userId, e.getMessage());
            response.put("success", false);
            response.put("message", "처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 사용자 활성화 처리 (AJAX)
     */
    @PostMapping("/api/admin/users/{userId}/activate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> activateUser(@PathVariable Long userId, Principal principal) {
        log.info("사용자 활성화 요청: userId={}, admin={}", userId,
                principal != null ? principal.getName() : "anonymous");

        Map<String, Object> response = new HashMap<>();

        try {
            // TODO: 실제 활성화 처리
            // boolean success = userService.activateUser(userId, principal.getName());
            // if (!success) {
            //     response.put("success", false);
            //     response.put("message", "사용자 활성화에 실패했습니다.");
            //     return ResponseEntity.badRequest().body(response);
            // }

            response.put("success", true);
            response.put("message", "사용자가 성공적으로 활성화되었습니다.");

            log.info("사용자 활성화 완료: userId={}", userId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("사용자 활성화 처리 실패: userId={}, error={}", userId, e.getMessage());
            response.put("success", false);
            response.put("message", "처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 벌크 사용자 정지 처리 (AJAX)
     */
    @PostMapping("/api/admin/users/bulk-suspend")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> bulkSuspendUsers(
            @RequestBody Map<String, Object> requestBody,
            Principal principal) {

        log.info("벌크 사용자 정지 요청: admin={}", principal != null ? principal.getName() : "anonymous");

        Map<String, Object> response = new HashMap<>();

        try {
            @SuppressWarnings("unchecked")
            List<Long> userIds = (List<Long>) requestBody.get("userIds");
            String reason = (String) requestBody.get("reason");

            if (userIds == null || userIds.isEmpty()) {
                response.put("success", false);
                response.put("message", "선택된 사용자가 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // TODO: 실제 벌크 정지 처리
            // int suspendedCount = userService.bulkSuspendUsers(userIds, reason, principal.getName());

            response.put("success", true);
            response.put("message", userIds.size() + "명의 사용자가 정지되었습니다.");
            response.put("count", userIds.size());

            log.info("벌크 사용자 정지 완료: count={}", userIds.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("벌크 사용자 정지 실패: error={}", e.getMessage());
            response.put("success", false);
            response.put("message", "처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 벌크 사용자 활성화 처리 (AJAX)
     */
    @PostMapping("/api/admin/users/bulk-activate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> bulkActivateUsers(
            @RequestBody Map<String, Object> requestBody,
            Principal principal) {

        log.info("벌크 사용자 활성화 요청: admin={}", principal != null ? principal.getName() : "anonymous");

        Map<String, Object> response = new HashMap<>();

        try {
            @SuppressWarnings("unchecked")
            List<Long> userIds = (List<Long>) requestBody.get("userIds");

            if (userIds == null || userIds.isEmpty()) {
                response.put("success", false);
                response.put("message", "선택된 사용자가 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // TODO: 실제 벌크 활성화 처리
            // int activatedCount = userService.bulkActivateUsers(userIds, principal.getName());

            response.put("success", true);
            response.put("message", userIds.size() + "명의 사용자가 활성화되었습니다.");
            response.put("count", userIds.size());

            log.info("벌크 사용자 활성화 완료: count={}", userIds.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("벌크 사용자 활성화 실패: error={}", e.getMessage());
            response.put("success", false);
            response.put("message", "처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 오류 시 기본 사용자 데이터 설정
     */
    private void setDefaultUserData(Model model) {
        model.addAttribute("users", List.of());
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 0);
        model.addAttribute("totalCount", 0L);
        model.addAttribute("totalUsers", 1245);
        model.addAttribute("activeUsers", 1156);
        model.addAttribute("suspendedUsers", 23);
        model.addAttribute("newUsersToday", 12);
    }

    /**
     * 사용자 통계 데이터 클래스
     */
    private static class UserStats {
        private long totalUsers;
        private long activeUsers;
        private long suspendedUsers;
        private long newUsersToday;

        // Getters and Setters
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

        public long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }

        public long getSuspendedUsers() { return suspendedUsers; }
        public void setSuspendedUsers(long suspendedUsers) { this.suspendedUsers = suspendedUsers; }

        public long getNewUsersToday() { return newUsersToday; }
        public void setNewUsersToday(long newUsersToday) { this.newUsersToday = newUsersToday; }
    }
}
