package com.example.together.controller.meeting;

import com.example.together.dto.PageRequestDTO;
import com.example.together.dto.PageResponseDTO;
import com.example.together.dto.meeting.MeetingReviewDTO;
import com.example.together.service.meeting.MeetingReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/meeting/review")
@Log4j2
@RequiredArgsConstructor
public class MeetingReviewController {

    private final MeetingReviewService meetingReviewService;

    @GetMapping("/list")
    public void mtReviewList(PageRequestDTO pageRequestDTO, Model model) {
        PageResponseDTO<MeetingReviewDTO> responseDTO = meetingReviewService.list(pageRequestDTO);
        log.info(responseDTO);
        model.addAttribute("responseDTO", responseDTO);
    }


    @GetMapping("/register")
    public void mtReviewRegisterGet() {

    }
    @PostMapping("/register")
    public String mtReviewRegisterPost(@Valid MeetingReviewDTO meetingReviewDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        log.info("mtReviewRegister Post.....");

        if(bindingResult.hasErrors()) {
            log.info("has errors..... mtReviewRegister Post....");
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());

            return "redirect:/meeting/review/register";
        }

        log.info(meetingReviewDTO);
        Long id = meetingReviewService.MeetingReview(meetingReviewDTO);

        redirectAttributes.addFlashAttribute("result", id);
        return "redirect:/meeting/review/list";
    }

    @GetMapping({"/read", "/modify"})
    public void mtReviewRead(Long id, PageRequestDTO pageRequestDTO, Model model) {
        MeetingReviewDTO meetingReviewDTO = meetingReviewService.MeetingReviewDetail(id);
        log.info(meetingReviewDTO);
        model.addAttribute("dto", meetingReviewDTO);
    }
    @PostMapping("/modify")
    public String mtReviewModify (PageRequestDTO pageRequestDTO, @Valid MeetingReviewDTO meetingReviewDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        log.info("mtReviewModify Post....." + meetingReviewDTO);

        if(bindingResult.hasErrors()) {
            log.info("has errors..... mtReviewModify Post....");
            String link = pageRequestDTO.getLink();
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            redirectAttributes.addAttribute("id", meetingReviewDTO.getId());
            return "redirect:/meeting/review/modify?"+ link;
        }

        meetingReviewService.MeetingReviewModify(meetingReviewDTO);
        redirectAttributes.addFlashAttribute("result", "modified");
        redirectAttributes.addAttribute("id", meetingReviewDTO.getId());
        return "redirect:/meeting/review/read";
    }

    @PostMapping("/remove")
    public String mtReviewRemove(Long id, RedirectAttributes redirectAttributes) {
        log.info("mtReviewRemove..." + id);
        meetingReviewService.MeetingReviewDelete(id);
        redirectAttributes.addFlashAttribute("result", "removed");
        return "redirect:/meeting/review/list";
    }
}
