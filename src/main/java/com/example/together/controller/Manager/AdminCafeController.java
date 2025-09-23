//package com.example.together.controller.Manager;
//
//import com.example.together.service.UserService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.security.Principal;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Controller
//@RequestMapping("/manager")
//@RequiredArgsConstructor
//@Slf4j
//
//public class AdminCafeController {
//    private final UserService userService;
//    // private final CafeService cafeService; // 팀원 구현 시 추가
//
//    /**
//     * 카페 관리 페이지
//     */
//    @GetMapping("/mCafe")
//    public String cafeManagePage(
//            @RequestParam(defaultValue = "") String search,
//            @RequestParam(defaultValue = "") String status,
//            @RequestParam(defaultValue = "") String category,
//            @RequestParam(defaultValue = "regDate") String sort,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            Model model,
//            Principal principal) {
//
//        log.info("카페 관리 페이지 요청: admin={}, search={}, status={}, category={}",
//                principal != null ? principal.getName() : "anonymous", search, status, category);
//
//        try {
//            // 관리자 권한 확인
//            if (principal != null) {
//                // TODO: 관리자 권한 체크
//                // User admin = userService.findByUserId(principal.getName());
//                // if (!admin.getSystemRole().equals(SystemRole.ADMIN)) {
//                //     return "redirect:/main";
//                // }
//            }
//
//            // 페이지네이션 설정
//            Sort.Direction direction = Sort.Direction.DESC;
//            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
//
//            // 카페 목록 조회 (검색 및 필터링 포함)
//            Page<Object> cafePage = searchCafes(search, status, category, pageable);
//
//            // 통계 데이터 조회
//            CafeStats stats = getCafeStatistics();
//
//            // 모델에 데이터 추가
//            model.addAttribute("cafes", cafePage.getContent());
//            model.addAttribute("currentPage", page);
//            model.addAttribute("totalPages", cafePage.getTotalPages());
//            model.addAttribute("totalCount", cafePage.getTotalElements());
//
//            // 검색 조건 유지
//            model.addAttribute("searchTerm", search);
//            model.addAttribute("status", status);
//            model.addAttribute("category", category);
//            model.addAttribute("sort", sort);
//
//            // 통계 데이터
//            model.addAttribute("totalCafes", stats.getTotalCafes());
//            model.addAttribute("activeCafes", stats.getActiveCafes());
//            model.addAttribute("pendingCafes", stats.getPendingCafes());
//            model.addAttribute("newCafesToday", stats.getNewCafesToday());
//
//            log.info("카페 관리 데이터 로드 완료: 총 {}개, 검색결과 {}개",
//                    stats.getTotalCafes(), cafePage.getTotalElements());
//
//        } catch (Exception e) {
//            log.error("카페 관리 페이지 로드 중 오류: {}", e.getMessage());
//            setDefaultCafeData(model);
//        }
//
//        return "manager/mCafe";
//    }
//
//    /**
//     * 카페 검색 및 필터링
//     */
//    private Page<Object> searchCafes(String search, String status, String category, Pageable pageable) {
//        try {
//            // TODO: 실제 구현에서는 CafeService의 검색 메서드 사용
//            // if (!search.isEmpty() || !status.isEmpty() || !category.isEmpty()) {
//            //     return cafeService.searchCafes(search, status, category, pageable);
//            // } else {
//            //     return cafeService.findAllCafes(pageable);
//            // }
//
//            // 임시 구현: 빈 페이지 반환
//            return Page.empty(pageable);
//
//        } catch (Exception e) {
//            log.error("카페 검색 중 오류: {}", e.getMessage());
//            return Page.empty(pageable);
//        }
//    }
//
//    /**
//     * 카페 통계 조회
//     */
//    private CafeStats getCafeStatistics() {
//        CafeStats stats = new CafeStats();
//
//        try {
//            // TODO: 실제 통계 조회 구현
//            // stats.setTotalCafes(cafeService.countAllCafes());
//            // stats.setActiveCafes(cafeService.countActiveCafes());
//            // stats.setPendingCafes(cafeService.countPendingCafes());
//            // stats.setNewCafesToday(cafeService.countNewCafesToday());
//
//            // 임시 데이터
//            stats.setTotalCafes(89L);
//            stats.setActiveCafes(76L);
//            stats.setPendingCafes(8L);
//            stats.setNewCafesToday(3L);
//
//        } catch (Exception e) {
//            log.error("카페 통계 조회 중 오류: {}", e.getMessage());
//            // 기본값 유지
//        }
//
//        return stats;
//    }
//
//    /**
//     * 카페 상세 정보 조회 (AJAX)
//     */
//    @GetMapping("/api/admin/cafes/{cafeId}")
//    @ResponseBody
//    public ResponseEntity<Map<String, Object>> getCafeDetail(@PathVariable Long cafeId, Principal principal) {
//        log.info("카페 상세 정보 요청: cafeId={}, admin={}", cafeId,
//                principal != null ? principal.getName() : "anonymous");
//
//        try {
//            // TODO: 실제 카페 조회
//            // Cafe cafe = cafeService.findById(cafeId);
//            // if (cafe == null) {
//            //     return ResponseEntity.notFound().build();
//            // }
//
//            // 임시 데이터 반환
//            Map<String, Object> cafeDetail = new HashMap<>();
//            cafeDetail.put("id", cafeId);
//            cafeDetail.put("name", "독서 모임 카페");
//            cafeDetail.put("description", "함께 책을 읽고 토론하는 카페입니다.");
//            cafeDetail.put("category", "STUDY");
//            cafeDetail.put("thumbnail", "/images/default-cafe.png");
//            cafeDetail.put("memberCount", 25);
//            cafeDetail.put("regDate", "2024-01-15");
//            cafeDetail.put("status", "PENDING");
//
//            Map<String, Object> owner = new HashMap<>();
//            owner.put("name", "홍길동");
//            owner.put("userId", "user123");
//            cafeDetail.put("owner", owner);
//
//            return ResponseEntity.ok(cafeDetail);
//
//        } catch (Exception e) {
//            log.error("카페 상세 정보 조회 실패: cafeId={}, error={}", cafeId, e.getMessage());
//            return ResponseEntity.internalServerError().build();
//        }
//    }
//
//    /**
//     * 카페 승인 처리 (AJAX)
//     */
//    @PostMapping("/api/admin/cafes/{cafeId}/approve")
//    @ResponseBody
//    public ResponseEntity<Map<String, Object>> approveCafe(@PathVariable Long cafeId, Principal principal) {
//        log.info("카페 승인 요청: cafeId={}, admin={}", cafeId,
//                principal != null ? principal.getName() : "anonymous");
//
//        Map<String, Object> response = new HashMap<>();
//
//        try {
//            // TODO: 실제 승인 처리
//            // boolean success = cafeService.approveCafe(cafeId, principal.getName());
//            // if (!success) {
//            //     response.put("success", false);
//            //     response.put("message", "카페 승인에 실패했습니다.");
//            //     return ResponseEntity.badRequest().body(response);
//            // }
//
//            response.put("success", true);
//            response.put("message", "카페가 성공적으로 승인되었습니다.");
//
//            log.info("카페 승인 완료: cafeId={}", cafeId);
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            log.error("카페 승인 처리 실패: cafeId={}, error={}", cafeId, e.getMessage());
//            response.put("success", false);
//            response.put("message", "처리 중 오류가 발생했습니다.");
//            return ResponseEntity.internalServerError().body(response);
//        }
//    }
//
//    /**
//     * 카페 거부 처리 (AJAX)
//     */
//    @PostMapping("/api/admin/cafes/{cafeId}/reject")
//    @ResponseBody
//    public ResponseEntity<Map<String, Object>> rejectCafe(
//            @PathVariable Long cafeId,
//            @RequestBody Map<String, String> requestBody,
//            Principal principal) {
//
//        log.info("카페 거부 요청: cafeId={}, admin={}", cafeId,
//                principal != null ? principal.getName() : "anonymous");
//
//        Map<String, Object> response = new HashMap<>();
//
//        try {
//            String reason = requestBody.get("reason");
//            if (reason == null || reason.trim().isEmpty()) {
//                response.put("success", false);
//                response.put("message", "거부 사유를 입력해주세요.");
//                return ResponseEntity.badRequest().body(response);
//            }
//
//            // TODO: 실제 거부 처리
//            // boolean success = cafeService.rejectCafe(cafeId, reason, principal.getName());
//            // if (!success) {
//            //     response.put("success", false);
//            //     response.put("message", "카페 거부에 실패했습니다.");
//            //     return ResponseEntity.badRequest().body(response);
//            // }
//
//            response.put("success", true);
//            response.put("message", "카페가 거부되었습니다.");
//
//            log.info("카페 거부 완료: cafeId={}, reason={}", cafeId, reason);
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            log.error("카페 거부 처리 실패: cafeId={}, error={}", cafeId, e.getMessage());
//            response.put("success", false);
//            response.put("message", "처리 중 오류가 발생했습니다.");
//            return ResponseEntity.internalServerError().body(response);
//        }
//    }
//
//    /**
//     * 카페 정지 처리 (AJAX)
//     */
//    @PostMapping("/api/admin/cafes/{cafeId}/suspend")
//    @ResponseBody
//    public ResponseEntity<Map<String, Object>> suspendCafe(
//            @PathVariable Long cafeId,
//            @RequestBody Map<String, String> requestBody,
//            Principal principal) {
//
//        log.info("카페 정지 요청: cafeId={}, admin={}", cafeId,
//                principal != null ? principal.getName() : "anonymous");
//
//        Map<String, Object> response = new HashMap<>();
//
//        try {
//            String reason = requestBody.get("reason");
//            if (reason == null || reason.trim().isEmpty()) {
//                response.put("success", false);
//                response.put("message", "정지 사유를 입력해주세요.");
//                return ResponseEntity.badRequest().body(response);
//            }
//
//            // TODO: 실제 정지 처리
//            // boolean success = cafeService.suspendCafe(cafeId, reason, principal.getName());
//            // if (!success) {
//            //     response.put("success", false);
//            //     response.put("message", "카페 정지에 실패했습니다.");
//            //     return ResponseEntity.badRequest().body(response);
//            // }
//
//            response.put("success", true);
//            response.put("message", "카페가 정지되었습니다.");
//
//            log.info("카페 정지 완료: cafeId={}, reason={}", cafeId, reason);
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            log.error("카페 정지 처리 실패: cafeId={}, error={}", cafeId, e.getMessage());
//            response.put("success", false);
//            response.put("message", "처리 중 오류가 발생했습니다.");
//            return ResponseEntity.internalServerError().body(response);
//        }
//    }
//
//    /**
//     * 카페 재활성화 처리 (AJAX)
//     */
//    @PostMapping("/api/admin/cafes/{cafeId}/reactivate")
//    @ResponseBody
//    public ResponseEntity<Map<String, Object>> reactivateCafe(@PathVariable Long cafeId, Principal principal) {
//        log.info("카페 재활성화 요청: cafeId={}, admin={}", cafeId,
//                principal != null ? principal.getName() : "anonymous");
//
//        Map<String, Object> response = new HashMap<>();
//
//        try {
//            // TODO: 실제 재활성화 처리
//            // boolean success = cafeService.reactivateCafe(cafeId, principal.getName());
//            // if (!success) {
//            //     response.put("success", false);
//            //     response.put("message", "카페 재활성화에 실패했습니다.");
//            //     return ResponseEntity.badRequest().body(response);
//            // }
//
//            response.put("success", true);
//            response.put("message", "카페가 재활성화되었습니다.");
//
//            log.info("카페 재활성화 완료: cafeId={}", cafeId);
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            log.error("카페 재활성화 처리 실패: cafeId={}, error={}", cafeId, e.getMessage());
//            response.put("success", false);
//            response.put("message", "처리 중 오류가 발생했습니다.");
//            return ResponseEntity.internalServerError().body(response);
//        }
//    }
//
//    /**
//     * 벌크 카페 승인 처리 (AJAX)
//     */
//    @PostMapping("/api/admin/cafes/bulk-approve")
//    @ResponseBody
//    public ResponseEntity<Map<String, Object>> bulkApproveCafes(
//            @RequestBody Map<String, Object> requestBody,
//            Principal principal) {
//
//        log.info("벌크 카페 승인 요청: admin={}", principal != null ? principal.getName() : "anonymous");
//
//        Map<String, Object> response = new HashMap<>();
//
//        try {
//            @SuppressWarnings("unchecked")
//            List<Long> cafeIds = (List<Long>) requestBody.get("cafeIds");
//
//            if (cafeIds == null || cafeIds.isEmpty()) {
//                response.put("success", false);
//                response.put("message", "선택된 카페가 없습니다.");
//                return ResponseEntity.badRequest().body(response);
//            }
//
//            // TODO: 실제 벌크 승인 처리
//            // int approvedCount = cafeService.bulkApproveCafes(cafeIds, principal.getName());
//
//            response.put("success", true);
//            response.put("message", cafeIds.size() + "개의 카페가 승인되었습니다.");
//            response.put("count", cafeIds.size());
//
//            log.info("벌크 카페 승인 완료: count={}", cafeIds.size());
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            log.error("벌크 카페 승인 실패: error={}", e.getMessage());
//            response.put("success", false);
//            response.put("message", "처리 중 오류가 발생했습니다.");
//            return ResponseEntity.internalServerError().body(response);
//        }
//    }
//
//    /**
//     * 벌크 카페 거부 처리 (AJAX)
//     */
//    @PostMapping("/api/admin/cafes/bulk-reject")
//    @ResponseBody
//    public ResponseEntity<Map<String, Object>> bulkRejectCafes(
//            @RequestBody Map<String, Object> requestBody,
//            Principal principal) {
//
//        log.info("벌크 카페 거부 요청: admin={}", principal != null ? principal.getName() : "anonymous");
//
//        Map<String, Object> response = new HashMap<>();
//
//        try {
//            @SuppressWarnings("unchecked")
//            List<Long> cafeIds = (List<Long>) requestBody.get("cafeIds");
//            String reason = (String) requestBody.get("reason");
//
//            if (cafeIds == null || cafeIds.isEmpty()) {
//                response.put("success", false);
//                response.put("message", "선택된 카페가 없습니다.");
//                return ResponseEntity.badRequest().body(response);
//            }
//
//            if (reason == null || reason.trim().isEmpty()) {
//                response.put("success", false);
//                response.put("message", "거부 사유를 입력해주세요.");
//                return ResponseEntity.badRequest().body(response);
//            }
//
//            // TODO: 실제 벌크 거부 처리
//            // int rejectedCount = cafeService.bulkRejectCafes(cafeIds, reason, principal.getName());
//
//            response.put("success", true);
//            response.put("message", cafeIds.size() + "개의 카페가 거부되었습니다.");
//            response.put("count", cafeIds.size());
//
//            log.info("벌크 카페 거부 완료: count={}, reason={}", cafeIds.size(), reason);
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            log.error("벌크 카페 거부 실패: error={}", e.getMessage());
//            response.put("success", false);
//            response.put("message", "처리 중 오류가 발생했습니다.");
//            return ResponseEntity.internalServerError().body(response);
//        }
//    }
//
//    /**
//     * 오류 시 기본 카페 데이터 설정
//     */
//    private void setDefaultCafeData(Model model) {
//        model.addAttribute("cafes", List.of());
//        model.addAttribute("currentPage", 0);
//        model.addAttribute("totalPages", 0);
//        model.addAttribute("totalCount", 0L);
//        model.addAttribute("totalCafes", 89);
//        model.addAttribute("activeCafes", 76);
//        model.addAttribute("pendingCafes", 8);
//        model.addAttribute("newCafesToday", 3);
//    }
//
//    /**
//     * 카페 통계 데이터 클래스
//     */
//    private static class CafeStats {
//        private long totalCafes;
//        private long activeCafes;
//        private long pendingCafes;
//        private long newCafesToday;
//
//        // Getters and Setters
//        public long getTotalCafes() { return totalCafes; }
//        public void setTotalCafes(long totalCafes) { this.totalCafes = totalCafes; }
//
//        public long getActiveCafes() { return activeCafes; }
//        public void setActiveCafes(long activeCafes) { this.activeCafes = activeCafes; }
//
//        public long getPendingCafes() { return pendingCafes; }
//        public void setPendingCafes(long pendingCafes) { this.pendingCafes = pendingCafes; }
//
//        public long getNewCafesToday() { return newCafesToday; }
//        public void setNewCafesToday(long newCafesToday) { this.newCafesToday = newCafesToday; }
//    }
//}
