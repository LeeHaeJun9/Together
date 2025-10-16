// com.example.together.service.report.ReportServiceImpl
package com.example.together.service.report;

import com.example.together.domain.*;
import com.example.together.dto.report.ReportAdminRow;
import com.example.together.dto.report.ReportCreateDTO;
import com.example.together.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.together.domain.PostSubType.MEETING;
import static com.example.together.domain.PostSubType.REVIEW;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

  private final ReportRepository reportRepository;
  private final UserRepository userRepository;
  private final CafeRepository cafeRepository;
  private final TradeRepository tradeRepository;
  private final MembershipRepository membershipRepository; // ✅ 추가
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final  MeetingReviewRepository meetingReviewRepository;
  private final  MeetingRepository meetingRepository;

    @Transactional
    @Override
    public Long create(Long reporterUserPk, ReportCreateDTO dto) {
        PostSubType postSubType = null;

        if (dto.getReportType() == ReportType.POST) {
            // MEETING 우선
            Meeting meeting = meetingRepository.findById(dto.getTargetId()).orElse(null);
            if (meeting != null) {
                postSubType = meeting.getPostSubType();
            } else {
                // REVIEW 확인
                MeetingReview review = meetingReviewRepository.findById(dto.getTargetId()).orElse(null);
                if (review != null) {
                    postSubType = review.getPostSubType();
                } else {
                    // 일반 POST 확인
                    Post post = postRepository.findById(dto.getTargetId()).orElse(null);
                    if (post != null) {
                        postSubType = post.getPostSubType();
                    } else {
                        postSubType = null; // fallback
                    }
                }
            }
        } else {
            // USER, CHAT, COMMENT, CAFE, TRADE
            postSubType = null;
        }

        Report report = Report.builder()
                .reporterId(reporterUserPk)
                .reason(dto.getReason())
                .reportType(dto.getReportType())
                .targetId(dto.getTargetId())
                .postSubType(postSubType)
                .build();

        reportRepository.save(report);
        return report.getId();
    }

  @Override
  public Page<ReportAdminRow> listAdmin(ReportType type, Pageable pageable) {
    final Page<Report> page = (type == null)
        ? reportRepository.findAll(pageable)
        : reportRepository.findByReportType(type, pageable);

    final List<Report> reports = page.getContent();

      // 여기서 페이지 정보 가져오기
      int currentPage = pageable.getPageNumber(); // 0-based
      int pageSize = pageable.getPageSize();

    // 신고자 맵
    final Map<Long, User> reporterMap = reports.stream()
        .map(Report::getReporterId).filter(Objects::nonNull).distinct()
        .map(id -> userRepository.findById(id).orElse(null))
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(User::getId, u -> u, (a,b)->a));

    // 유저 제재형 대상 맵(USER/CHAT/COMMENT)
    final Map<Long, User> targetUserMap = reports.stream()
        .filter(r -> r.getReportType() == ReportType.USER
            || r.getReportType() == ReportType.CHAT
            || r.getReportType() == ReportType.COMMENT)
        .map(Report::getTargetId).filter(Objects::nonNull).distinct()
        .map(id -> userRepository.findById(id).orElse(null))
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(User::getId, u -> u, (a,b)->a));

    // 카페명 맵 (라벨 편의)
    final Map<Long, String> cafeNames = reports.stream()
        .filter(r -> r.getReportType() == ReportType.CAFE)
        .map(Report::getTargetId).filter(Objects::nonNull).distinct()
        .map(id -> cafeRepository.findById(id).orElse(null))
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(Cafe::getId, Cafe::getName, (a,b)->a));

    final List<ReportAdminRow> rows = reports.stream().map(r -> {
      final ReportType rt = r.getReportType();

      String targetLabel = "#" + r.getTargetId();
      String targetUrl   = null;
      String targetUserNickname = null;
      String targetUserLoginId  = null;

      boolean canDelete = false;
      boolean canBan    = false;
      String deleteLabel = null;

        switch (rt) {
            case CAFE -> {
                targetLabel = cafeNames.getOrDefault(r.getTargetId(), "#" + r.getTargetId());
                targetUrl   = "/cafe/" + r.getTargetId();
                canDelete   = true;
                deleteLabel = "카페 삭제";
            }

            case TRADE -> {
                targetLabel = "거래 #" + r.getTargetId();
                targetUrl   = "/trade/read/" + r.getTargetId();
                canDelete   = true;
                deleteLabel = "거래 삭제";
            }

            case POST -> {
                PostSubType pst = r.getPostSubType();

                if (pst == null || pst == PostSubType.GENERAL) {
                    targetLabel = "카페 게시글 #" + r.getTargetId();
                    Post post = postRepository.findById(r.getTargetId()).orElse(null);
                    if (post != null && post.getCafe() != null) {
                        targetUrl = "/cafe/" + post.getCafe().getId() + "/posts/" + post.getId();
                    } else {
                        targetUrl = "/posts/" + r.getTargetId(); // fallback
                    }
                } else if (pst == MEETING) {
                    targetLabel = "모임 게시글 #" + r.getTargetId();
                    Meeting meeting = meetingRepository.findById(r.getTargetId()).orElse(null);
                    if (meeting != null && meeting.getCafe() != null) {
                        targetUrl = "/cafe/" + meeting.getCafe().getId()
                                + "/meeting/read?id=" + meeting.getId()
                                + "&page=" + currentPage
                                + "&size=" + pageSize;
                    }
                } else if (pst == REVIEW) {
                    targetLabel = "후기 게시글 #" + r.getTargetId();
                    MeetingReview review = meetingReviewRepository.findById(r.getTargetId()).orElse(null);
                    if (review != null) {
                        Long cafeId = (review.getMeeting() != null && review.getMeeting().getCafe() != null)
                                ? review.getMeeting().getCafe().getId()
                                : review.getCafe().getId();
                        targetUrl = "/cafe/" + cafeId
                                + "/meeting/review/read?id=" + review.getId()
                                + "&page=" + currentPage
                                + "&size=" + pageSize;
                    }
                    canDelete = true;
                    deleteLabel = "게시글 삭제";
                }
            } // <- POST case 끝

            case USER, CHAT, COMMENT -> {
                User tu = targetUserMap.get(r.getTargetId());
                if (tu != null) {
                    targetUserNickname = tu.getNickname();
                    targetUserLoginId  = tu.getUserId();
                    targetLabel        = (tu.getNickname() != null ? tu.getNickname() : tu.getUserId());
                    targetUrl          = "/member/profile/" + tu.getId();
                } else {
                    targetLabel = "사용자 #" + r.getTargetId();
                }
                canBan = true;
            }

            default -> { /* no-op */ }
        }


        User reporter = reporterMap.get(r.getReporterId());
      String reporterNickname = (reporter != null && reporter.getNickname() != null)
          ? reporter.getNickname()
          : (reporter != null ? reporter.getUserId() : "알 수 없음");
      String reporterLoginId = (reporter != null) ? reporter.getUserId() : null;

      return ReportAdminRow.builder()
          .id(r.getId())
          .type(rt)
          .targetId(r.getTargetId())
          .targetLabel(targetLabel)
          .targetUrl(targetUrl)
          .targetUserNickname(targetUserNickname)
          .targetUserLoginId(targetUserLoginId)
          .reason(r.getReason())
          .regdate(r.getRegdate())
          .reporterNickname(reporterNickname)
          .reporterLoginId(reporterLoginId)
          .canDelete(canDelete)
          .canBan(canBan)
          .deleteLabel(deleteLabel)
          .build();
    }).toList();

    return new PageImpl<>(rows, pageable, page.getTotalElements());
  }

  @Transactional
  @Override
  public void deleteTarget(Long reportId) {
    final Report r = reportRepository.findById(reportId)
        .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다."));

    switch (r.getReportType()) {
      case CAFE -> {
        deleteCafeWithChildren(r.getTargetId());   // ✅ 자식 먼저 삭제 후 카페 삭제
      }
      case TRADE -> {
        // 필요 시 거래 관련 자식(댓글/이미지 등) 먼저 삭제하도록 보강
        tradeRepository.deleteById(r.getTargetId());  // 기존 동작 유지
      }
        case POST -> {
            if (r.getPostSubType() == null || r.getPostSubType() == PostSubType.GENERAL) {
                Post post = postRepository.findById(r.getTargetId())
                        .orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다."));
                commentRepository.deleteByPost(post);
                postRepository.deleteById(r.getTargetId());
            } else {
                switch (r.getPostSubType()) { // ✅ 여기서 PostSubType으로 switch
                    case MEETING -> deleteMeetingWithChildren(r.getTargetId());
                    case REVIEW -> meetingReviewRepository.deleteById(r.getTargetId());
                }
            }
        }
      default -> throw new IllegalStateException("이 유형은 '대상 삭제'가 아닌 '회원 정지' 대상입니다.");
    }

    // 신고 레코드도 함께 제거
    reportRepository.delete(r);
  }

    @Transactional
    public void deleteMeetingWithChildren(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("모임이 존재하지 않습니다"));

        // 후기, 이미지, 참여자 등 연관 엔티티 삭제
        meetingReviewRepository.deleteByMeeting(meeting);
        // meetingImageRepository.deleteByMeeting(meeting);
        // meetingJoinRepository.deleteByMeeting(meeting);

        meetingRepository.delete(meeting);
    }


    // ✅ FK 제약 대응: membership(자식) -> cafe(부모) 순서로 삭제
  @Transactional
  public void deleteCafeWithChildren(Long cafeId) {
      Cafe cafe = cafeRepository.findById(cafeId)
              .orElseThrow(() -> new EntityNotFoundException("카페가 존재하지 않습니다."));

      // 1) 멤버 삭제
      membershipRepository.deleteByCafe(cafe);

      // 2) 게시글 관련 댓글 삭제
      List<Post> posts = postRepository.findByCafe(cafe);
      for (Post post : posts) {
          commentRepository.deleteByPost(post);
      }

      // 3) 게시글 삭제
      postRepository.deleteByCafe(cafe);

      // 4) 카페 삭제
      cafeRepository.delete(cafe);
  }

  @Transactional
  @Override
  public void banUser(Long reportId) {
    final Report r = reportRepository.findById(reportId)
        .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다."));

    if (!(r.getReportType() == ReportType.USER
        || r.getReportType() == ReportType.CHAT
        || r.getReportType() == ReportType.COMMENT)) {
      throw new IllegalStateException("이 유형은 '회원 정지' 대상이 아닙니다.");
    }

    userRepository.findById(r.getTargetId())
        .ifPresent(u -> u.setStatus(Status.LOCKED));
    reportRepository.delete(r);
  }

  @Transactional
  @Override
  public void removeReportOnly(Long reportId) {
    reportRepository.findById(reportId).ifPresent(reportRepository::delete);
  }
}
