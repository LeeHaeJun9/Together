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
import com.example.together.service.cafe.CafeService;
import com.example.together.service.meeting.MeetingService;
import com.example.together.service.meeting.MeetingUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final UserEditor userEditor;
    private final MeetingUserService meetingUserService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(User.class, userEditor);
    }

    @GetMapping("/list")
    public void meetingList(PageRequestDTO pageRequestDTO, Model model) {
        PageResponseDTO<MeetingDTO> responseDTO = meetingService.list(pageRequestDTO);
        log.info(responseDTO);
        model.addAttribute("responseDTO", responseDTO);
    }


    @GetMapping("/register")
    public void meetingRegisterGet(@RequestParam(required = false) Long cafeId,
                                   @AuthenticationPrincipal User user,
                                   @ModelAttribute("pageRequestDTO") PageRequestDTO pageRequestDTO,
                                   Model model) {
        MeetingDTO meetingDTO = new MeetingDTO();

        if (cafeId != null) {
            CafeResponseDTO cafeResponse = cafeService.getCafeById(cafeId, Long.valueOf(user.getUserId()));
            meetingDTO.setCafe(cafeResponse);
        }

        model.addAttribute("dto", meetingDTO);
    }
    @PostMapping("/register")
    public String meetingRegisterPost(@Valid MeetingDTO meetingDTO,
                                      BindingResult bindingResult,
                                      @AuthenticationPrincipal User user,
                                      RedirectAttributes redirectAttributes) {
        log.info("meetingRegister Post.....");

        // 유효성 검사 오류 처리
        if (bindingResult.hasErrors()) {
            log.info("has errors..... meetingRegister Post....");
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            return "redirect:/meeting/register";
        }

        // 폼에서 cafeId를 hidden 필드로 받아오고, DTO에 CafeResponseDTO를 설정해야 함
        // (주의: HTML 폼에 <input type="hidden" name="cafe.id" ...>와 같이 필드를 추가해야 함)
        // 현재는 cafeId가 DTO에 직접 매핑되지 않으므로, 아래와 같은 처리가 필요

        if (meetingDTO.getCafe() != null && meetingDTO.getCafe().getId() != null) {
            CafeResponseDTO cafeResponse = cafeService.getCafeById(meetingDTO.getCafe().getId(), Long.valueOf(user.getUserId()));
            meetingDTO.setCafe(cafeResponse);
        }

        log.info(meetingDTO);
        Long id = meetingService.MeetingCreate(meetingDTO);

        redirectAttributes.addFlashAttribute("result", id);
        return "redirect:/meeting/list";
    }

    @GetMapping({"/read", "/modify"})
    public void meetingRead(Long id, PageRequestDTO pageRequestDTO, Model model) {
        MeetingDTO meetingDTO = meetingService.MeetingDetail(id);
        log.info(meetingDTO);
        model.addAttribute("dto", meetingDTO);

        List<MeetingUserDTO> meetingUser = meetingUserService.getMeetingUsersByMeetingId(id);
        model.addAttribute("meetingUser", meetingUser);
    }
    @PostMapping("/modify")
    public String meetingModify (@ModelAttribute MeetingDTO dto, PageRequestDTO pageRequestDTO, @Valid MeetingDTO meetingDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        log.info("meetingModify Post....." + meetingDTO);

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
    public String meetingRemove(Long id, RedirectAttributes redirectAttributes) {
        log.info("meetingRemove..." + id);
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
