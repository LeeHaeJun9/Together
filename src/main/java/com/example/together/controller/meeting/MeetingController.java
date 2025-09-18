package com.example.together.controller.meeting;


import com.example.together.config.UserEditor;
import com.example.together.domain.Cafe;
import com.example.together.domain.User;
import com.example.together.dto.PageRequestDTO;
import com.example.together.dto.PageResponseDTO;
import com.example.together.dto.cafe.CafeResponseDTO;
import com.example.together.dto.meeting.MeetingDTO;
import com.example.together.repository.UserRepository;
import com.example.together.service.UserService;
import com.example.together.service.cafe.CafeService;
import com.example.together.service.meeting.MeetingService;
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
import java.util.Optional;

@Controller
@RequestMapping("/meeting")
@Log4j2
@RequiredArgsConstructor
public class MeetingController {
    private final MeetingService meetingService;
    private final UserRepository userRepository;
    private final CafeService cafeService;
    private final UserEditor userEditor;
    private final UserService userService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(User.class, userEditor);
    }

    private Long getUserIdFromPrincipal(Principal principal) {
        if (principal == null) {
            return null; // ✅ Principal이 null이면 null 반환
        }
        String userIdString = principal.getName();
        // userIdString으로 User를 찾고, 해당 User의 고유 ID(Long)를 반환
        return userService.findByUserId(userIdString).getId();
    }

    @GetMapping("/list")
    public void meetingList(PageRequestDTO pageRequestDTO, Model model) {
        PageResponseDTO<MeetingDTO> responseDTO = meetingService.list(pageRequestDTO);
        log.info(responseDTO);
        model.addAttribute("responseDTO", responseDTO);
    }


    @GetMapping("/register")
    public String meetingRegisterGet(@RequestParam("cafeId") Long cafeId, Model model, Principal principal) { // ✅ Principal 추가
        Long userId = getUserIdFromPrincipal(principal); // ✅ 메소드 사용
    public void meetingRegisterGet(@RequestParam(required = false) Long cafeId,
                                   @AuthenticationPrincipal User user,
                                   @ModelAttribute("pageRequestDTO") PageRequestDTO pageRequestDTO,
                                   Model model) {
        MeetingDTO meetingDTO = new MeetingDTO();

        if (userId == null) {
            return "redirect:/login";
        }

        Optional<User> userOptional = userRepository.findById(userId);

        if (!userOptional.isPresent()) {
            return "redirect:/login";
        }

        User user = userOptional.get();

        CafeResponseDTO cafeResponse = cafeService.getCafeById(cafeId, user.getId());

        model.addAttribute("meetingDTO", new MeetingDTO());
        model.addAttribute("cafeResponse", cafeResponse);
        model.addAttribute("userName", user.getUserId());


        return "meeting/register";
    }
    @PostMapping("/register")
    public String meetingRegisterPost(@Valid MeetingDTO meetingDTO,
                                      BindingResult bindingResult,
                                      Principal principal, // ✅ @AuthenticationPrincipal 대신 Principal 객체 사용
                                      @RequestParam(required = false) Long cafeId,
                                      RedirectAttributes redirectAttributes) {
        log.info("meetingRegister Post.....");

        Long userId = getUserIdFromPrincipal(principal); // ✅ Principal을 통해 사용자 ID 가져오기

        if (userId == null) {
            log.warn("User is NOT authenticated on POST request. Redirecting to login.");
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            log.info("has errors..... meetingRegister Post....");
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            redirectAttributes.addAttribute("cafeId", cafeId);
            return "redirect:/meeting/register";
        }

        if (cafeId != null) {
            CafeResponseDTO cafeResponse = cafeService.getCafeById(cafeId, userId); // ✅ userId 사용
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
    public void meetingRead(Long id, PageRequestDTO pageRequestDTO, Model model, Principal principal) { // ✅ Principal 추가
        Long userId = getUserIdFromPrincipal(principal);

        MeetingDTO meetingDTO = meetingService.MeetingDetail(id);
        log.info(meetingDTO);
        model.addAttribute("dto", meetingDTO);
        model.addAttribute("loggedInUserId", userId); // ✅ loggedInUserId 추가
    }
    @PostMapping("/modify")
    public String meetingModify (@ModelAttribute MeetingDTO dto, PageRequestDTO pageRequestDTO, @Valid MeetingDTO meetingDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes, Principal principal) { // ✅ Principal 추가
        log.info("meetingModify Post....." + meetingDTO);

        Long userId = getUserIdFromPrincipal(principal); // ✅ Principal을 통해 사용자 ID 가져오기
        if (userId == null) {
            log.warn("User is NOT authenticated on POST request. Redirecting to login.");
            return "redirect:/login";
        }

        if(bindingResult.hasErrors()) {
            log.info("has errors..... meetingModify Post....");
            String link = pageRequestDTO.getLink();
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            redirectAttributes.addAttribute("id", meetingDTO.getId());
            return "redirect:/meeting/modify?"+ link;
        }

        System.out.println("Organizer: " + dto.getOrganizer().getUserId());

        meetingService.MeetingModify(meetingDTO);
        redirectAttributes.addFlashAttribute("result", "modified");
        redirectAttributes.addAttribute("id", meetingDTO.getId());
        return "redirect:/meeting/read";
    }


    @PostMapping("/remove")
    public String meetingRemove(Long id, RedirectAttributes redirectAttributes, Principal principal) { // ✅ Principal 추가
        log.info("meetingRemove..." + id);
        Long userId = getUserIdFromPrincipal(principal); // ✅ Principal을 통해 사용자 ID 가져오기

        if (userId == null) {
            log.warn("User is NOT authenticated on POST request. Redirecting to login.");
            return "redirect:/login";
        }

        meetingService.MeetingDelete(id);
        redirectAttributes.addFlashAttribute("result", "removed");
        return "redirect:/meeting/list";
    }
}
