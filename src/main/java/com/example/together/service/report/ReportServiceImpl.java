// com.example.together.service.report.ReportServiceImpl
package com.example.together.service.report;

import com.example.together.domain.*;
import com.example.together.dto.report.ReportAdminRow;
import com.example.together.dto.report.ReportCreateDTO;
import com.example.together.repository.CafeRepository;
import com.example.together.repository.ReportRepository;
import com.example.together.repository.TradeRepository;
import com.example.together.repository.UserRepository;
import com.example.together.repository.MembershipRepository; // ✅ 추가
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

  private final ReportRepository reportRepository;
  private final UserRepository userRepository;
  private final CafeRepository cafeRepository;
  private final TradeRepository tradeRepository;
  private final MembershipRepository membershipRepository; // ✅ 추가

  @Transactional
  @Override
  public Long create(Long reporterUserPk, ReportCreateDTO dto) {
    Report r = Report.builder()
        .reason(dto.getReason())
        .reportType(dto.getReportType())
        .targetId(dto.getTargetId())
        .reporterId(reporterUserPk)
        .build();
    reportRepository.save(r);
    return r.getId();
  }

  @Override
  public Page<ReportAdminRow> listAdmin(ReportType type, Pageable pageable) {
    final Page<Report> page = (type == null)
        ? reportRepository.findAll(pageable)
        : reportRepository.findByReportType(type, pageable);

    final List<Report> reports = page.getContent();

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
          targetLabel = "카페 게시물 #" + r.getTargetId();
          // 실제 PostRepo 연결 전까지는 버튼 숨김(다음 단계에서 연결)
          canDelete   = false;
          deleteLabel = "카페 게시물 삭제";
        }
        case USER, CHAT, COMMENT -> {
          User tu = targetUserMap.get(r.getTargetId());
          if (tu != null) {
            targetUserNickname = tu.getNickname();
            targetUserLoginId  = tu.getUserId();
            targetLabel        = (tu.getNickname() != null ? tu.getNickname() : tu.getUserId());
            targetUrl          = "/member/profile/" + tu.getId(); // 필요시 수정
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
        // 다음 단계에서 PostRepository 연결 후 활성화
        throw new IllegalStateException("카페 게시물 삭제는 다음 단계에서 연결합니다.");
      }
      default -> throw new IllegalStateException("이 유형은 '대상 삭제'가 아닌 '회원 정지' 대상입니다.");
    }

    // 신고 레코드도 함께 제거
    reportRepository.delete(r);
  }

  // ✅ FK 제약 대응: membership(자식) -> cafe(부모) 순서로 삭제
  private void deleteCafeWithChildren(Long cafeId) {
    Cafe cafe = cafeRepository.findById(cafeId)
        .orElseThrow(() -> new EntityNotFoundException("카페가 존재하지 않습니다. id=" + cafeId));

    // 1) 자식 먼저 삭제
    membershipRepository.deleteByCafe(cafe);

    // TODO: 다른 자식 테이블이 있다면 여기서 같이 정리
    // postRepository.deleteByCafe(cafe);
    // imageRepository.deleteByCafe(cafe);
    // ...

    // 2) 부모 삭제
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
