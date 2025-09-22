package com.example.together.controller.meeting;

import com.example.together.domain.MeetingReview;
import com.example.together.domain.RecruitingStatus;
import com.example.together.domain.User;
import com.example.together.dto.PageRequestDTO;
import com.example.together.dto.PageResponseDTO;
import com.example.together.dto.cafe.CafeResponseDTO;
import com.example.together.dto.meeting.MeetingDTO;
import com.example.together.dto.meeting.MeetingReviewDTO;
import com.example.together.dto.meeting.MeetingUserDTO;
import com.example.together.repository.UserRepository;
import com.example.together.service.UserService;
import com.example.together.service.cafe.CafeService;
import com.example.together.service.meeting.MeetingReviewService;
import com.example.together.service.meeting.MeetingUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/cafe/{cafeId}/meeting/review")
@Log4j2
@RequiredArgsConstructor
public class MeetingReviewController {

    private final MeetingReviewService meetingReviewService;
    private final UserService userService;
    private final CafeService cafeService;
    private final UserRepository userRepository;
    private final MeetingUserService meetingUserService;

    private User getUserFromPrincipal(Principal principal) {
        if (principal == null) return null;
        String userId = principal.getName(); // 로그인 아이디
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
    }


    @GetMapping("/list")
    public String mtReviewList(@PathVariable("cafeId") Long cafeId, PageRequestDTO pageRequestDTO, Model model, Principal principal) {
        PageResponseDTO<MeetingReviewDTO> responseDTO = meetingReviewService.list(pageRequestDTO);
        log.info(responseDTO);
        model.addAttribute("responseDTO", responseDTO);

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

        return "meeting/review/list";
    }


    @GetMapping("/register")
    public String  mtReviewRegisterGet(@PathVariable("cafeId") Long cafeId,
                                       @RequestParam(value = "meetingId", required = false) Long meetingId,
//                                       @RequestParam(value="location", required=false) String location,
                                       Model model, Principal principal, PageRequestDTO pageRequestDTO) {

        User user = getUserFromPrincipal(principal);
        CafeResponseDTO cafeResponse = cafeService.getCafeById(cafeId, user.getId());
        MeetingReviewDTO reviewDTO = new MeetingReviewDTO();

        if (meetingId != null && meetingId > 0) {
            // meetingId가 있을 때: MeetingDTO 가져와서 정보 채우기
            MeetingDTO meetingDTO = meetingReviewService.getMeetingDTOById(meetingId);

            reviewDTO.setMeetingId(meetingDTO.getId());
            reviewDTO.setMeetingDate(meetingDTO.getMeetingDate());
            reviewDTO.setMeetingLocation(meetingDTO.getLocation());
            reviewDTO.setMeetingAddress(meetingDTO.getAddress());

            // 리뷰어 정보는 로그인 유저 기준
            reviewDTO.setReviewerId(user.getId());
            reviewDTO.setReviewerNickname(user.getNickname());

        } else {
            // meetingId가 없을 때: 작성자 정보만 세팅하고, 나머지는 빈값
            reviewDTO.setReviewerId(user.getId());
            reviewDTO.setReviewerNickname(user.getNickname());
            reviewDTO.setReviewerUserId(user.getUserId());
            // meetingDate, meetingLocation, meetingAddress는 빈 값으로 사용자 입력 받음
        }

        model.addAttribute("dto", reviewDTO);
        model.addAttribute("cafeResponse", cafeResponse);
        model.addAttribute("userName", user.getUserId());
        model.addAttribute("pageRequestDTO", pageRequestDTO);


        return "meeting/review/register";
    }
    @PostMapping("/register")
    public String mtReviewRegisterPost(@PathVariable("cafeId") Long cafeId,
                                       @Valid MeetingReviewDTO meetingReviewDTO,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        log.info("mtReviewRegister Post.....");

        if(bindingResult.hasErrors()) {
            log.info("has errors..... mtReviewRegister Post....");
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            redirectAttributes.addAttribute("cafeId", cafeId);
//            redirectAttributes.addAttribute("cafeId", meetingReviewDTO.getCafeId());

            return "redirect:/cafe/{cafeId}/meeting/review/register";
        }

        // 로그인한 사용자 아이디 가져오기
        String userId = userDetails.getUsername();
        log.info(meetingReviewDTO);

        Long reviewId;

        // 서비스 호출 시 DTO + 로그인한 사용자 아이디 넘기기
        if (meetingReviewDTO.getMeetingId() != null) {
            // ✅ createReview (모임 리뷰 등록)
            MeetingReview review = meetingReviewService.createReview(
                    userId,
                    meetingReviewDTO.getMeetingId(),
                    meetingReviewDTO.getTitle(),
                    meetingReviewDTO.getContent()
            );
            reviewId = review.getId();
        } else {
            // ✅ writeReview (임의 리뷰 작성)
            MeetingReview review = meetingReviewService.writeReview(
                    userId,
                    meetingReviewDTO.getTitle(),
                    meetingReviewDTO.getContent()
            );
            reviewId = review.getId();
        }

        redirectAttributes.addFlashAttribute("result", reviewId);
        redirectAttributes.addAttribute("cafeId", cafeId);
//        redirectAttributes.addAttribute("cafeId", meetingReviewDTO.getCafeId());

        return "redirect:/cafe/{cafeId}/meeting/review/list";
    }

    @GetMapping("/read")
    public String mtReviewRead(@PathVariable Long cafeId, Long id, PageRequestDTO pageRequestDTO, Model model, Principal principal) {
        User user = getUserFromPrincipal(principal);

        MeetingReviewDTO meetingReviewDTO = meetingReviewService.MeetingReviewDetail(id);
        log.info(meetingReviewDTO);
        model.addAttribute("dto", meetingReviewDTO);

//        List<MeetingUserDTO> meetingUser = meetingUserService.getMeetingUsersByMeetingId(id);
//        model.addAttribute("meetingUser", meetingUser);
//        model.addAttribute("loggedInUserId", user != null ? user.getId() : null);

        CafeResponseDTO cafeResponse;
        if (user != null) {
            cafeResponse = cafeService.getCafeById(cafeId, user.getId());
        } else {
            cafeResponse = cafeService.getCafeById(cafeId);
        }
        model.addAttribute("cafeResponse", cafeResponse);

        return "meeting/review/read";
    }

    @GetMapping("/modify")
    public String mtReviewModifyGet(@PathVariable Long cafeId, Long id, PageRequestDTO pageRequestDTO, Model model, Principal principal) {
        User user = getUserFromPrincipal(principal);

        MeetingReviewDTO meetingReviewDTO = meetingReviewService.MeetingReviewDetail(id);
        log.info(meetingReviewDTO);
        model.addAttribute("dto", meetingReviewDTO);

//        List<MeetingUserDTO> meetingUser = meetingUserService.getMeetingUsersByMeetingId(id);
//        model.addAttribute("meetingUser", meetingUser);
//        model.addAttribute("loggedInUserId", user != null ? user.getId() : null);

        CafeResponseDTO cafeResponse;
        if (user != null) {
            cafeResponse = cafeService.getCafeById(cafeId, user.getId());
        } else {
            cafeResponse = cafeService.getCafeById(cafeId);
        }
        model.addAttribute("cafeResponse", cafeResponse);

        return "meeting/review/modify";
    }
    @PostMapping("/modify")
    public String mtReviewModifyPost (@PathVariable Long cafeId, PageRequestDTO pageRequestDTO, @Valid MeetingReviewDTO meetingReviewDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        log.info("mtReviewModify Post....." + meetingReviewDTO);

        if(bindingResult.hasErrors()) {
            log.info("has errors..... mtReviewModify Post....");
            String link = pageRequestDTO.getLink();
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            redirectAttributes.addAttribute("id", meetingReviewDTO.getId());
            return "redirect:/cafe/{cafeId}/meeting/review/modify?"+ link;
        }

        meetingReviewService.MeetingReviewModify(meetingReviewDTO);
        redirectAttributes.addFlashAttribute("result", "modified");
        redirectAttributes.addAttribute("id", meetingReviewDTO.getId());
        return "redirect:/cafe/{cafeId}/meeting/review/read";
    }

    @PostMapping("/remove")
    public String mtReviewRemove(Long id, RedirectAttributes redirectAttributes) {
        log.info("mtReviewRemove..." + id);
        meetingReviewService.MeetingReviewDelete(id);
        redirectAttributes.addFlashAttribute("result", "removed");
        return "redirect:/cafe/{cafeId}/meeting/review/list";
    }
}
