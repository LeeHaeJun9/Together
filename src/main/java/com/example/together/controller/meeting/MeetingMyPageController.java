package com.example.together.controller.meeting;

import com.example.together.domain.MeetingJoinStatus;
import com.example.together.dto.meeting.MyJoinedMeetingsDTO;
import com.example.together.service.UserService;
import com.example.together.service.meeting.MeetingMyPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequestMapping("/meeting")
@RequiredArgsConstructor
@Log4j2
public class MeetingMyPageController {

    private final MeetingMyPageService meetingMyPageService;
    private final UserService userService;

    private Long getLoggedInUserId(Principal principal) {
        if (principal == null) {
            // 로그인되지 않은 사용자는 null을 반환하여 서비스 계층에서 처리하도록 합니다.
            return null;
        }
        String userIdString = principal.getName();
        // UserService의 findByUserId 메서드를 사용해 Long 타입의 고유 ID를 반환합니다.
        return userService.findByUserId(userIdString).getId();
    }

    @GetMapping("/my/joined-meetings")
    public String getMyPageMeetings(@RequestParam(required = false, defaultValue = "meetingDate") String sortBy,
                                    Principal principal, Model model) {
        Long userId = getLoggedInUserId(principal);
        if (userId == null) {
            // 로그인 안 된 경우 로그인 페이지로 리다이렉트
            return "redirect:/login";
        }

        MyJoinedMeetingsDTO myJoinedMeetingsDTO =
                meetingMyPageService.getMyJoinedMeetings(userId, MeetingJoinStatus.ACCEPTED);

        model.addAttribute("myJoinedMeetings", myJoinedMeetingsDTO);

        model.addAttribute("upcomingMeetingList", myJoinedMeetingsDTO.getUpcomingMeetings());
        model.addAttribute("completedMeetingList", myJoinedMeetingsDTO.getCompletedMeetings());
        model.addAttribute("hostedMeetingList", myJoinedMeetingsDTO.getHostedMeetings());
        model.addAttribute("myReviewsList", myJoinedMeetingsDTO.getMyReviews());

        return "meeting/myMeetings"; // 뷰 파일 위치: templates/meeting/myMeetings.html
    }
}
