package com.example.together.controller.meeting;


import com.example.together.config.UserEditor;
import com.example.together.domain.Cafe;
import com.example.together.domain.MeetingUser;
import com.example.together.domain.User;
import com.example.together.dto.PageRequestDTO;
import com.example.together.dto.PageResponseDTO;
import com.example.together.dto.cafe.CafeResponseDTO;
import com.example.together.dto.meeting.MeetingDTO;
import com.example.together.dto.meeting.MeetingUserDTO;
import com.example.together.repository.UserRepository;
import com.example.together.service.UserService;
import com.example.together.service.cafe.CafeService;
import com.example.together.service.meeting.MeetingService;
import com.example.together.service.meeting.MeetingUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/meeting")
@Log4j2
@RequiredArgsConstructor
public class MeetingController {
    private final MeetingService meetingService;
    private final UserRepository userRepository;
    private final CafeService cafeService;
    private final UserService userService;

    // `UserEditor`와 `@InitBinder`는 더 이상 필요하지 않아 제거했습니다.
    // `MeetingDTO`가 `organizerId`와 `organizerName`을 사용하기 때문에,
    // 컨트롤러에서 직접 `User` 객체를 바인딩할 필요가 없습니다.

    // Principal 객체에서 사용자 ID를 안전하게 가져오는 헬퍼 메서드
    private Long getUserIdFromPrincipal(Principal principal) {
        if (principal == null) {
            return null;
        }
        String email = principal.getName();
        Optional<User> userOptional = userRepository.findByEmail(email);
        return userOptional.map(User::getId).orElse(null);
    }

    @GetMapping("/list")
    public void meetingList(PageRequestDTO pageRequestDTO, Model model) {
        PageResponseDTO<MeetingDTO> responseDTO = meetingService.list(pageRequestDTO);
        log.info(responseDTO);
        model.addAttribute("responseDTO", responseDTO);
    }


    @GetMapping("/register")
    public String meetingRegisterGet(@RequestParam("cafeId") Long cafeId, Model model, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);

        if (userId == null) {
            return "redirect:/login";
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        CafeResponseDTO cafeResponse = cafeService.getCafeById(cafeId, user.getId());

        MeetingDTO meetingDTO = new MeetingDTO();
        // 모임 생성 페이지에서 주최자 정보를 미리 DTO에 채워줍니다.
        meetingDTO.setOrganizerId(user.getId());
        meetingDTO.setOrganizerName(user.getUserId());

        model.addAttribute("meetingDTO", meetingDTO);
        model.addAttribute("cafeResponse", cafeResponse);
        model.addAttribute("userName", user.getUserId());

        return "meeting/register";
    }
    @PostMapping("/register")
    public String meetingRegisterPost(@Valid MeetingDTO meetingDTO,
                                      BindingResult bindingResult,
                                      Principal principal,
                                      @RequestParam(required = false) Long cafeId,
                                      RedirectAttributes redirectAttributes) {
        log.info("meetingRegister Post.....");

        Long userId = getUserIdFromPrincipal(principal);

        if (userId == null) {
            log.warn("User is NOT authenticated on POST request. Redirecting to login.");
            return "redirect:/login";
        }

        // 폼 제출 시 주최자 정보가 누락될 경우를 대비해 다시 설정
        meetingDTO.setOrganizerId(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        meetingDTO.setOrganizerName(user.getUserId());


        if (bindingResult.hasErrors()) {
            log.info("has errors..... meetingRegister Post....");
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            redirectAttributes.addAttribute("cafeId", cafeId);
            return "redirect:/meeting/register";
        }

        if (cafeId != null) {
            CafeResponseDTO cafeResponse = cafeService.getCafeById(cafeId, userId);
            meetingDTO.setCafe(cafeResponse);
        } else {
            log.error("cafeId is null");
            redirectAttributes.addFlashAttribute("errors", "카페 정보가 누락되었습니다.");
            return "redirect:/meeting/register";
        }

        log.info(meetingDTO);
        Long id = meetingService.MeetingCreate(meetingDTO, cafeId);

        redirectAttributes.addFlashAttribute("result", id);
        return "redirect:/meeting/list";
    }

    @GetMapping({"/read", "/modify"})
    public void meetingRead(Long id, PageRequestDTO pageRequestDTO, Model model, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);

        MeetingDTO meetingDTO = meetingService.MeetingDetail(id);
        log.info(meetingDTO);
        model.addAttribute("dto", meetingDTO);

        List<MeetingUserDTO> meetingUser = meetingUserService.getMeetingUsersByMeetingId(id);
        model.addAttribute("meetingUser", meetingUser);
        model.addAttribute("loggedInUserId", userId);
    }
    @PostMapping("/modify")
    public String meetingModify (@Valid MeetingDTO meetingDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes, Principal principal) {
        log.info("meetingModify Post....." + meetingDTO);

        Long userId = getUserIdFromPrincipal(principal);
        if (userId == null) {
            log.warn("User is NOT authenticated on POST request. Redirecting to login.");
            return "redirect:/login";
        }

        // 주최자 정보가 누락될 경우를 대비해 다시 설정
        meetingDTO.setOrganizerId(userId);

        if(bindingResult.hasErrors()) {
            log.info("has errors..... meetingModify Post....");
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            redirectAttributes.addAttribute("id", meetingDTO.getId());
            return "redirect:/meeting/modify?id=" + meetingDTO.getId();
        }

        meetingService.MeetingModify(meetingDTO);
        redirectAttributes.addFlashAttribute("result", "modified");
        redirectAttributes.addAttribute("id", meetingDTO.getId());
        return "redirect:/meeting/read";
    }


    @PostMapping("/remove")
    public String meetingRemove(Long id, RedirectAttributes redirectAttributes, Principal principal) {
        log.info("meetingRemove..." + id);
        Long userId = getUserIdFromPrincipal(principal);

        if (userId == null) {
            log.warn("User is NOT authenticated on POST request. Redirecting to login.");
            return "redirect:/login";
        }

        meetingService.MeetingDelete(id);
        redirectAttributes.addFlashAttribute("result", "removed");
        return "redirect:/meeting/list";
    }

    @PostMapping("/apply")
    public String meetingApply(@RequestParam Long meetingId,
                               Principal principal,  // <-- Principal로 변경
                               RedirectAttributes redirectAttributes) {

        String userId = principal.getName();  // 로그인한 아이디 얻기
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자 정보가 없습니다."));

        // user로 신청 처리
        try {
            meetingService.applyToMeeting(user, meetingId);
            redirectAttributes.addFlashAttribute("message", "모임 신청이 완료되었습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        redirectAttributes.addAttribute("id", meetingId);
        return "redirect:/meeting/read?id=" + meetingId;
    }
}
