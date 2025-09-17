package com.example.together.controller.meeting;


import com.example.together.config.UserEditor;
import com.example.together.domain.Cafe;
import com.example.together.domain.User;
import com.example.together.dto.PageRequestDTO;
import com.example.together.dto.PageResponseDTO;
import com.example.together.dto.cafe.CafeResponseDTO;
import com.example.together.dto.meeting.MeetingDTO;
import com.example.together.repository.UserRepository;
import com.example.together.service.cafe.CafeService;
import com.example.together.service.meeting.MeetingService;
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

@Controller
@RequestMapping("/meeting")
@Log4j2
@RequiredArgsConstructor
public class MeetingController {
    private final MeetingService meetingService;
    private final UserRepository userRepository;
    private final CafeService cafeService;
    private final UserEditor userEditor;

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
                                   Model model) {
        MeetingDTO meetingDTO = new MeetingDTO();

//        if (cafeId != null) {
//            CafeResponseDTO cafeResponse = cafeService.getCafeById(cafeId);
//            meetingDTO.setCafe(cafeResponse);  // meetingDTO에 cafeResponse 설정
//        }

        model.addAttribute("dto", meetingDTO);
    }
    @PostMapping("/register")
    public String meetingRegisterPost(@Valid MeetingDTO meetingDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        log.info("meetingRegister Post.....");

        if(bindingResult.hasErrors()) {
            log.info("has errors..... meetingRegister Post....");
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());

            return "redirect:/meeting/register";
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
}
