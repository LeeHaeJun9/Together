package com.example.together.controller.meeting;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
//@RequestMapping("/meeting")
@Log4j2
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final MeetingUserService meetingUserService;
    private final UserRepository userRepository;
    private final CafeService cafeService;
    private final UserService userService;

    /** ✅ Principal에서 로그인된 userId를 가져와 User 객체 반환 */
    private User getUserFromPrincipal(Principal principal) {
        if (principal == null) return null;
        String userId = principal.getName(); // 로그인 아이디
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
    }

    @GetMapping("/meeting/list")
    public void meetingListByRequestParam(@RequestParam("cafeId") Long cafeId, PageRequestDTO pageRequestDTO, Model model, Principal principal) {
        processMeetingList(cafeId, pageRequestDTO, model, principal);
    }

    // Case 2: 카페 상세 페이지의 "모임" 탭에서 클릭하여 /cafe/{cafeId}/meetings 형태로 요청할 때
    // 중요: @RequestMapping을 /cafe로 옮기거나, 여기만 특정 경로를 처리하도록 재정의합니다.
    // 여기서는 @RequestMapping("/meeting")을 유지하면서, 이 메서드만 다른 경로를 처리하도록 합니다.
    @GetMapping("/cafe/{cafeId}/meetings") // << 이 매핑을 추가합니다.
    public String meetingListByPathVariable(@PathVariable("cafeId") Long cafeId, PageRequestDTO pageRequestDTO, Model model, Principal principal) {
        // 실제 로직은 중복을 피하기 위해 별도의 private 메서드로 분리하는 것이 좋습니다.
        processMeetingList(cafeId, pageRequestDTO, model, principal);
        return "meeting/list"; // 템플릿 이름을 명시적으로 반환
    }


    // 두 메서드에서 공통으로 사용될 로직을 분리
    private void processMeetingList(Long cafeId, PageRequestDTO pageRequestDTO, Model model, Principal principal) {
        // 1. 특정 카페의 모임 목록을 조회합니다.
        PageResponseDTO<MeetingDTO> responseDTO = meetingService.listByCafeId(cafeId, pageRequestDTO);
        log.info(responseDTO);
        model.addAttribute("responseDTO", responseDTO);

        // 2. 로그인 여부에 따라 다른 getCafeById 메서드를 호출합니다.
        CafeResponseDTO cafeResponse;
        if (principal != null) {
            // 로그인된 사용자
            User user = getUserFromPrincipal(principal);
            cafeResponse = cafeService.getCafeById(cafeId, user.getId());
        } else {
            // 익명 사용자 (userId 없이 호출)
            cafeResponse = cafeService.getCafeById(cafeId);
        }

        model.addAttribute("cafeResponse", cafeResponse);
    }

    @GetMapping("/meeting/register")
    public String meetingRegisterGet(@RequestParam("cafeId") Long cafeId, Model model, Principal principal, PageRequestDTO pageRequestDTO) {
        User user = getUserFromPrincipal(principal);


        CafeResponseDTO cafeResponse = cafeService.getCafeById(cafeId, user.getId());

        MeetingDTO meetingDTO = new MeetingDTO();
        meetingDTO.setOrganizerId(user.getId());
        meetingDTO.setOrganizerName(user.getUserId());

        model.addAttribute("meetingDTO", meetingDTO);
        model.addAttribute("cafeResponse", cafeResponse);
        model.addAttribute("userName", user.getUserId());
        model.addAttribute("pageRequestDTO", pageRequestDTO);

        return "meeting/register";
    }

    @PostMapping("/meeting/register")
    public String meetingRegisterPost(@Valid MeetingDTO meetingDTO,
                                      BindingResult bindingResult,
                                      Principal principal,
                                      @RequestParam("cafeId") Long cafeId,
                                      RedirectAttributes redirectAttributes) {
        log.info("meetingRegister Post.....");

        User user = getUserFromPrincipal(principal);
        if (user == null) {
            log.warn("User is NOT authenticated on POST request. Redirecting to login.");
            // 리다이렉트 시 cafeId도 함께 전달하여 /meeting/register GET 요청 시 cafeId를 사용할 수 있게 합니다.
            redirectAttributes.addAttribute("cafeId", cafeId);
            return "redirect:/login";
        }

        meetingDTO.setUserId(user.getUserId());

        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(fe -> {
                log.info(fe.getField() + " : " + fe.getDefaultMessage());
            });
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            redirectAttributes.addAttribute("cafeId", cafeId); // 오류 시에도 cafeId 유지
            return "redirect:/meeting/register";
        }

        log.info(meetingDTO);
        Long id = meetingService.MeetingCreate(meetingDTO, cafeId);

        redirectAttributes.addFlashAttribute("result", id);
        redirectAttributes.addAttribute("cafeId", cafeId); // 성공 시에도 cafeId 유지
        return "redirect:/meeting/list";
    }

    @GetMapping({"/meeting/read", "/meeting/modify"})
    public void meetingRead(Long id, PageRequestDTO pageRequestDTO, Model model, Principal principal) {
        User user = getUserFromPrincipal(principal);

        MeetingDTO meetingDTO = meetingService.MeetingDetail(id);
        log.info(meetingDTO);
        model.addAttribute("dto", meetingDTO);

        List<MeetingUserDTO> meetingUser = meetingUserService.getMeetingUsersByMeetingId(id);
        model.addAttribute("meetingUser", meetingUser);
        model.addAttribute("loggedInUserId", user != null ? user.getId() : null);
    }

    @PostMapping("/meeting/modify")
    public String meetingModify(@Valid MeetingDTO meetingDTO,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Principal principal) {
        log.info("meetingModify Post....." + meetingDTO);

        User user = getUserFromPrincipal(principal);
        if (user == null) {
            log.warn("User is NOT authenticated on POST request. Redirecting to login.");
            return "redirect:/login";
        }

        meetingDTO.setOrganizerId(user.getId());

        if (bindingResult.hasErrors()) {
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

    @PostMapping("/meeting/remove")
    public String meetingRemove(Long id, RedirectAttributes redirectAttributes, Principal principal) {
        log.info("meetingRemove..." + id);

        User user = getUserFromPrincipal(principal);
        if (user == null) {
            log.warn("User is NOT authenticated on POST request. Redirecting to login.");
            return "redirect:/login";
        }

        meetingService.MeetingDelete(id);
        redirectAttributes.addFlashAttribute("result", "removed");
        return "redirect:/meeting/list";
    }

    @PostMapping("/meeting/apply")
    public String meetingApply(@RequestParam Long meetingId,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        User user = getUserFromPrincipal(principal);
        if (user == null) {
            return "redirect:/login";
        }

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
