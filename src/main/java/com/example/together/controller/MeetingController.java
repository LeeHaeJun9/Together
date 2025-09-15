package com.example.together.controller;


import com.example.together.dto.meeting.MeetingDTO;
import com.example.together.service.MeetingService;
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
@RequestMapping("/meeting")
@Log4j2
@RequiredArgsConstructor
public class MeetingController {
    private final MeetingService meetingService;

    @GetMapping("/list")
    public void meetingList() {

    }


    @GetMapping("/register")
    public void meetingRegisterGet() {

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

    @GetMapping("/read")
    public void meetingRead(Long id, Model model) {
        MeetingDTO meetingDTO = meetingService.MeetingDetail(id);
        log.info(meetingDTO);
        model.addAttribute("dto", meetingDTO);
    }
}
