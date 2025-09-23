package com.example.together.controller.meeting;

import com.example.together.dto.meeting.MeetingReviewReplyDTO;
import com.example.together.service.meeting.MeetingReviewReplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Log4j2
@RequestMapping("/replies")
@RequiredArgsConstructor
public class MeetingReviewReplyController {
    private final MeetingReviewReplyService replyService;

    @PostMapping("/reviews/{reviewId}")
    public ResponseEntity<Long> register(@PathVariable Long reviewId,
                                         @RequestBody MeetingReviewReplyDTO replyDTO,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        replyDTO.setReplyer(userDetails.getUsername());
        replyDTO.setReviewId(reviewId);
        Long replyId = replyService.register(replyDTO);
        return new ResponseEntity<>(replyId, HttpStatus.CREATED);
    }

    // 댓글 목록 (GET)
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<List<MeetingReviewReplyDTO>> getList(@PathVariable Long reviewId) {
        List<MeetingReviewReplyDTO> replies = replyService.getList(reviewId);
        return new ResponseEntity<>(replies, HttpStatus.OK);
    }

    // 댓글 수정 (PUT)
    @PutMapping("/{replyId}")
    public ResponseEntity<String> modify(@PathVariable Long replyId, @RequestBody MeetingReviewReplyDTO replyDTO) {
        replyDTO.setId(replyId);
        replyService.modify(replyDTO);
        return new ResponseEntity<>("modified", HttpStatus.OK);
    }

    // 댓글 삭제 (DELETE)
    @DeleteMapping("/{replyId}")
    public ResponseEntity<String> remove(@PathVariable Long replyId) {
        replyService.remove(replyId);
        return new ResponseEntity<>("removed", HttpStatus.OK);
    }
}
