package com.example.together.controller;

import com.example.together.domain.User;
import com.example.together.dto.member.memberRegisterDTO;
import com.example.together.service.UserService;

import com.example.together.service.chat.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final UserService userService;


  // ==================== 회원가입 관련 ====================

    // 회원가입 페이지
    @GetMapping("/member/register")
    public String showRegisterForm(Model model) {
        log.info("GET /member/register - 회원가입 페이지 요청");
        model.addAttribute("memberRegisterDTO", new memberRegisterDTO());
        return "member/register";
    }

    // 회원가입 처리
    @PostMapping("/member/register")
    public String processRegistration(@Valid @ModelAttribute("memberRegisterDTO") memberRegisterDTO registerDTO,
                                      BindingResult bindingResult,
                                      @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {

        log.info("POST /member/register - 회원가입 시도: userId = {}", registerDTO.getUserId());

        // 비밀번호 일치 확인
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "비밀번호가 일치하지 않습니다.");
        }

        // 아이디 중복 확인
        if (userService.isUserIdExists(registerDTO.getUserId())) {
            bindingResult.rejectValue("userId", "userId.duplicate", "이미 사용 중인 아이디입니다.");
        }

        // 이메일 중복 확인
        if (userService.isEmailExists(registerDTO.getEmail())) {
            bindingResult.rejectValue("email", "email.duplicate", "이미 사용 중인 이메일입니다.");
        }

        // 유효성 검사 실패 시
        if (bindingResult.hasErrors()) {
            log.warn("회원가입 폼 유효성 검사 실패");
            return "member/register";
        }
        // 모든 검증 통과 후 회원가입 진행
        try {
            log.info("✅ userService.register() 호출 직전: userId = {}", registerDTO.getUserId());
            userService.register(registerDTO, profilePhoto);
            log.info("✅ userService.register() 호출 완료: userId = {}", registerDTO.getUserId());
            redirectAttributes.addFlashAttribute("message", "회원가입에 성공했습니다. 로그인해주세요.");
            return "redirect:/mainPage";
        } catch (IllegalArgumentException e) {
            log.error("❌ 회원가입 처리 중 IllegalArgumentException 발생: userId = {}, error = {}",
                    registerDTO.getUserId(), e.getMessage(), e);
            model.addAttribute("error", e.getMessage());
            return "member/register";
        } catch (Exception e) {
            log.error("❌ 회원가입 처리 중 예상치 못한 예외 발생: userId = {}, error = {}",
                    registerDTO.getUserId(), e.getMessage(), e);
            model.addAttribute("error", "회원가입 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "member/register";
        }

    }

    // ==================== 중복 확인 AJAX API ====================

    // 사용자 ID 중복 확인 AJAX
    @GetMapping("/api/member/check-id")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkIdDuplicate(@RequestParam String userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean isDuplicate = userService.isUserIdExists(userId);

            response.put("success", true);
            response.put("isDuplicate", isDuplicate);

            if (isDuplicate) {
                response.put("message", "이미 사용중인 ID입니다.");
            } else {
                response.put("message", "사용 가능한 ID입니다.");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("ID 중복 체크 중 오류 발생: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "ID 체크 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    // 이메일 중복 확인 AJAX
    @GetMapping("/api/member/check-email")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkEmailDuplicate(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean isDuplicate = userService.isEmailExists(email);

            response.put("success", true);
            response.put("isDuplicate", isDuplicate);

            if (isDuplicate) {
                response.put("message", "이미 사용중인 이메일입니다.");
            } else {
                response.put("message", "사용 가능한 이메일입니다.");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("이메일 중복 체크 중 오류 발생: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "이메일 체크 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }
    // 닉네임 중복 확인 AJAX (회원가입용)
    @GetMapping("/api/member/check-nickname")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkNicknameDuplicate(@RequestParam String nickname) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean isDuplicate = userService.isNicknameExists(nickname);

            response.put("success", true);
            response.put("isDuplicate", isDuplicate);

            if (isDuplicate) {
                response.put("message", "이미 사용중인 닉네임입니다.");
            } else {
                response.put("message", "사용 가능한 닉네임입니다.");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("닉네임 중복 체크 중 오류 발생: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "닉네임 체크 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }


    // 이름 중복 확인 AJAX
    @GetMapping("/api/member/check-name")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkNameDuplicate(@RequestParam String name) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean isDuplicate = userService.isNameExists(name);

            response.put("success", true);
            response.put("isDuplicate", isDuplicate);

            if (isDuplicate) {
                response.put("message", "이미 사용중인 이름입니다.");
            } else {
                response.put("message", "사용 가능한 이름입니다.");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("이름 중복 체크 중 오류 발생: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "이름 체크 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    // 닉네임 중복 확인 (프로필 수정용) - 수정된 버전
    @PostMapping("/member/profile/checkNickname")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkNickname(@RequestBody Map<String, String> request,
                                                             Principal principal) {
        Map<String, Object> response = new HashMap<>();

        try {
            String nickname = request.get("nickname");
            String currentUserId = principal.getName();

            if (nickname == null || nickname.trim().isEmpty()) {
                response.put("isAvailable", false);  // available → isAvailable 수정
                response.put("message", "닉네임을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (nickname.length() < 2 || nickname.length() > 20) {
                response.put("isAvailable", false);  // available → isAvailable 수정
                response.put("message", "닉네임은 2-20자 사이여야 합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            if (!nickname.matches("^[가-힣a-zA-Z0-9]+$")) {
                response.put("isAvailable", false);  // available → isAvailable 수정
                response.put("message", "닉네임은 한글, 영문, 숫자만 사용 가능합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            boolean isAvailable = userService.isNicknameAvailable(nickname, currentUserId);

            response.put("isAvailable", isAvailable);  // available → isAvailable 수정
            response.put("message", isAvailable ? "사용 가능한 닉네임입니다." : "이미 사용중인 닉네임입니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("닉네임 중복확인 중 오류 발생: {}", e.getMessage());
            response.put("isAvailable", false);  // available → isAvailable 수정
            response.put("message", "중복확인 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 전화번호 중복 확인 AJAX
    @GetMapping("/api/member/check-phone")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkPhoneDuplicate(@RequestParam String phone) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean isDuplicate = userService.isPhoneExists(phone);

            response.put("success", true);
            response.put("isDuplicate", isDuplicate);

            if (isDuplicate) {
                response.put("message", "이미 사용중인 전화번호입니다.");
            } else {
                response.put("message", "사용 가능한 전화번호입니다.");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("전화번호 중복 체크 중 오류 발생: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "전화번호 체크 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    // 이메일 중복 확인 AJAX
    @PostMapping("/member/register/check-email")
    @ResponseBody
    public String checkEmail(@RequestParam("email") String email) {
        log.info("이메일 중복 확인: email = {}", email);
        boolean exists = userService.isEmailExists(email);
        return exists ? "{\"available\": false}" : "{\"available\": true}";
    }

    // 이름 중복 확인 AJAX
    @PostMapping("/member/register/check-name")
    @ResponseBody
    public String checkName(@RequestParam("name") String name) {
        log.info("이름 중복 확인: name = {}", name);
        boolean exists = userService.isNameExists(name);
        return exists ? "{\"available\": false}" : "{\"available\": true}";
    }

    // 닉네임 중복 확인 AJAX
    @PostMapping("/member/register/check-nickname")
    @ResponseBody
    public String checkNicknameForRegister(@RequestParam("nickname") String nickname) {
        log.info("닉네임 중복 확인: nickname = {}", nickname);
        boolean exists = userService.isNicknameExists(nickname);
        return exists ? "{\"available\": false}" : "{\"available\": true}";
    }

    // 전화번호 중복 확인 AJAX
    @PostMapping("/member/register/check-phone")
    @ResponseBody
    public String checkPhone(@RequestParam("phone") String phone) {
        log.info("전화번호 중복 확인: phone = {}", phone);
        boolean exists = userService.isPhoneExists(phone);
        return exists ? "{\"available\": false}" : "{\"available\": true}";
    }

    // ==================== 로그인/아이디·비밀번호 찾기 ====================

    @GetMapping("/login")
    public String loginPage() {
        log.info("GET /login - 로그인 페이지 요청");
        return "member/login";
    }

    // 아이디 찾기 페이지 표시
    @GetMapping("/member/findId")
    public String findIdPage() {
        return "member/findId";
    }

    // 아이디 찾기 요청 처리

    @PostMapping("/member/findId")
    @ResponseBody
    public Map<String, Object> findId(@RequestBody Map<String, String> request) { // 👈 이 부분을 수정하세요.
        Map<String, Object> response = new HashMap<>();

        try {
            // request 맵에서 name과 email 값을 꺼냅니다.
            String name = request.get("name");
            String email = request.get("email");

            log.info("아이디 찾기 요청: name = {}, email = {}", name, email);

            String userId = userService.findUserIdByNameAndEmail(name, email);

            if (userId != null) {
                response.put("success", true);
                response.put("userId", userId);
                response.put("message", "아이디를 찾았습니다.");
                log.info("아이디 찾기 성공: userId = {}", userId);
            } else {
                response.put("success", false);
                response.put("message", "일치하는 회원 정보를 찾을 수 없습니다.");
                log.warn("아이디 찾기 실패: name = {}, email = {}", name, email);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            log.error("아이디 찾기 처리 중 오류: {}", e.getMessage());
        }

        return response;
    }

    // 비밀번호 찾기 페이지 표시
    @GetMapping("/member/findPw")
    public String findPwPage() {
        return "member/findPw";
    }

    // 비밀번호 찾기 요청 처리 (임시 비밀번호 발급)
    @PostMapping("/member/findPw")
    @ResponseBody
    public Map<String, Object> findPassword(@RequestParam String userId,
                                            @RequestParam String email,
                                            @RequestParam String name) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean userExists = userService.isUserIdExists(userId) && userService.isEmailExists(email);

            if (userExists) {
                String tempPassword = "temp" + System.currentTimeMillis() % 10000;
                userService.updateUserPassword(userId, tempPassword);

                response.put("success", true);
                response.put("tempPassword", tempPassword);
                response.put("message", "임시 비밀번호가 발급되었습니다.");

                log.info("임시 비밀번호 발급 성공: userId = {}", userId);
            } else {
                response.put("success", false);
                response.put("message", "입력하신 정보와 일치하는 회원을 찾을 수 없습니다.");
                log.warn("비밀번호 찾기 실패: 사용자 정보 불일치 - userId = {}", userId);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            log.error("비밀번호 찾기 처리 중 오류: {}", e.getMessage());
        }

        return response;
    }

    // ==================== 프로필 관리 ====================

    // 프로필 페이지 표시
    @GetMapping("/member/profile")
    public String profilePage(Model model, Principal principal) {
        if (principal != null) {
            String userId = principal.getName();
            User user = userService.findByUserId(userId);

            if (user != null) {
                model.addAttribute("user", user);
                String userName = user.getName() != null ? user.getName() : user.getUserId();
                model.addAttribute("userName", userName);
                log.info("프로필 페이지 요청: userId = {}, userName = {}", userId, userName);
            } else {
                log.warn("사용자 정보를 찾을 수 없습니다: userId = {}", userId);
                return "redirect:/login";
            }
        } else {
            log.warn("로그인하지 않은 사용자가 프로필 페이지에 접근");
            return "redirect:/login";
        }
        return "member/profile";
    }

    // 프로필 사진 업로드
    @PostMapping("/member/profile/photo")
    @ResponseBody
    public Map<String, Object> uploadProfilePhoto(@RequestParam("photo") MultipartFile photo,
                                                  Principal principal) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }

            if (photo.isEmpty()) {
                response.put("success", false);
                response.put("message", "파일을 선택해주세요.");
                return response;
            }

            if (photo.getSize() > 5 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "파일 크기는 5MB 이하여야 합니다.");
                return response;
            }

            String contentType = photo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "이미지 파일만 업로드 가능합니다.");
                return response;
            }

            String userId = principal.getName();
            String photoUrl = userService.uploadProfilePhoto(userId, photo);

            if (photoUrl != null) {
                response.put("success", true);
                response.put("photoUrl", photoUrl);
                response.put("message", "프로필 사진이 성공적으로 업데이트되었습니다.");
                log.info("프로필 사진 업로드 성공: userId = {}, photoUrl = {}", userId, photoUrl);
            } else {
                response.put("success", false);
                response.put("message", "프로필 사진 업로드에 실패했습니다.");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            log.error("프로필 사진 업로드 오류: {}", e.getMessage());
        }

        return response;
    }

    // 개별 필드 업데이트
    @PostMapping("/member/profile/update")
    @ResponseBody
    public Map<String, Object> updateProfile(@RequestBody Map<String, String> request,
                                             Principal principal) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }

            String userId = principal.getName();
            String field = request.get("field");
            String value = request.get("value");

            if (field == null || value == null) {
                response.put("success", false);
                response.put("message", "필수 정보가 누락되었습니다.");
                return response;
            }

            String validationError = validateField(field, value);
            if (validationError != null) {
                response.put("success", false);
                response.put("message", validationError);
                return response;
            }

            boolean success = userService.updateUserField(userId, field, value);

            if (success) {
                response.put("success", true);
                response.put("message", getSuccessMessage(field));
                log.info("프로필 수정 성공: userId = {}, field = {}", userId, field);
            } else {
                response.put("success", false);
                response.put("message", getErrorMessage(field));
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            log.error("프로필 수정 오류: {}", e.getMessage());
        }

        return response;
    }



    // 비밀번호 변경
    @PostMapping("/member/profile/password")
    @ResponseBody
    public Map<String, Object> changePassword(@RequestBody Map<String, String> request,
                                              Principal principal) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }

            String userId = principal.getName();
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            boolean success = userService.changePassword(userId, currentPassword, newPassword);

            if (success) {
                response.put("success", true);
                response.put("message", "비밀번호가 성공적으로 변경되었습니다.");
                log.info("비밀번호 변경 성공: userId = {}", userId);
            } else {
                response.put("success", false);
                response.put("message", "현재 비밀번호가 일치하지 않습니다.");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            log.error("비밀번호 변경 오류: {}", e.getMessage());
        }

        return response;
    }

    // ==================== 마이페이지 관련 ====================

    // 마이페이지 메인
    @GetMapping("/mypage")
    public String myPage(Model model, Principal principal) {
        if (principal != null) {
            String userId = principal.getName();
            User user = userService.findByUserId(userId);
            model.addAttribute("user", user);

            model.addAttribute("favoriteCount", 0);
            model.addAttribute("tradeCount", 0);
            model.addAttribute("cafeCount", 0);
            model.addAttribute("meetingCount", 0);

            log.info("마이페이지 요청: userId = {}", userId);
        }
        return "member/mypage";
    }

    @GetMapping("/member/myCafes")
    public String myCafes(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("cafes", new ArrayList<>());
        }
        return "member/myCafes";
    }

    @GetMapping("/member/myTrade")
    public String myTradePage(Model model, Principal principal) {
        if (principal != null) {
            String userId = principal.getName();
            model.addAttribute("sellCount", 0);
            model.addAttribute("soldCount", 0);
            model.addAttribute("favoriteCount", 0);
            model.addAttribute("chatCount", 0);
            log.info("거래 내역 페이지 요청: userId = {}", userId);
        }
        return "member/myTrade";
    }

    @GetMapping("/member/myMeetings")
    public String myMeetingsPage(Model model, Principal principal) {
        if (principal != null) {
            String userId = principal.getName();
            model.addAttribute("upcomingMeetings", 0);
            model.addAttribute("completedMeetings", 0);
            model.addAttribute("hostedMeetings", 0);
            model.addAttribute("reviewCount", 0);
            log.info("나의 모임 페이지 요청: userId = {}", userId);
        }
        return "member/myMeetings";
    }

//    @GetMapping("/member/favorites")
//    public String favoritesPage(Model model, Principal principal) {
//        if (principal != null) {
//            String userId = principal.getName();
//            model.addAttribute("totalFavorites", 0);
//            model.addAttribute("availableCount", 0);
//            model.addAttribute("soldOutCount", 0);
//            model.addAttribute("recentCount", 0);
//            model.addAttribute("favorites", new ArrayList<>());
//            log.info("찜한 상품 페이지 요청: userId = {}", userId);
//        }
//        return "member/favorites";
//    }

    @GetMapping("/member/settings")
    public String settingsPage() {
        log.info("설정 페이지 요청 -> 프로필로 리디렉션");
        return "redirect:/member/profile";
    }

    // ==================== 회원탈퇴 ====================

    @GetMapping("/member/deleteUser")
    public String deleteUserPage(Model model, Principal principal) {
        if (principal != null) {
            String userId = principal.getName();
            User user = userService.findByUserId(userId);
            model.addAttribute("user", user);
            log.info("회원탈퇴 페이지 요청: userId = {}", userId);
        }
        return "member/deleteUser";
    }

    @PostMapping("/member/deleteUser")
    public String deleteUser(Principal principal, RedirectAttributes redirectAttributes) {
        if (principal != null) {
            String userId = principal.getName();
            User user = userService.findByUserId(userId);
            if (user != null) {
                userService.deleteUser(user.getId());
                redirectAttributes.addFlashAttribute("message", "회원탈퇴가 완료되었습니다.");
                return "redirect:/login";
            }
        }
        redirectAttributes.addFlashAttribute("error", "회원탈퇴 처리 중 오류가 발생했습니다.");
        return "redirect:/member/profile";
    }

    // ==================== 유틸리티 메서드 ====================

    /**
     * 필드별 유효성 검사
     */
    private String validateField(String field, String value) {
        switch (field) {
            case "email":
                if (!value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                    return "올바른 이메일 형식이 아닙니다.";
                }
                break;
            case "nickname":
            case "name":
                if (value.trim().length() < 2 || value.trim().length() > 10) {
                    return "이름은 2-10자 사이여야 합니다.";
                }
                break;
            case "phone":
                if (!value.matches("^010-\\d{4}-\\d{4}$")) {
                    return "전화번호는 010-XXXX-XXXX 형식이어야 합니다.";
                }
                break;
        }
        return null;
    }

    /**
     * 성공 메시지 반환
     */
    private String getSuccessMessage(String field) {
        switch (field) {
            case "email":
                return "이메일이 성공적으로 변경되었습니다.";
            case "nickname":
                return "닉네임이 성공적으로 변경되었습니다.";
            case "name":
                return "이름이 성공적으로 변경되었습니다.";
            case "phone":
                return "전화번호가 성공적으로 변경되었습니다.";
            default:
                return "정보가 성공적으로 수정되었습니다.";
        }
    }

    /**
     * 오류 메시지 반환
     */
    private String getErrorMessage(String field) {
        switch (field) {
            case "email":
                return "이메일 변경에 실패했습니다. 이미 사용 중인 이메일일 수 있습니다.";
            case "nickname":
                return "닉네임 변경에 실패했습니다.";
            case "name":
                return "이름 변경에 실패했습니다.";
            case "phone":
                return "전화번호 변경에 실패했습니다. 이미 사용 중인 전화번호일 수 있습니다.";
            default:
                return "정보 수정에 실패했습니다.";
        }
    }
}