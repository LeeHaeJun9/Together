package com.example.together.controller.admin;

import com.example.together.domain.ReportType;
import com.example.together.service.report.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
@Slf4j
public class AdminReportController {

  private final ReportService reportService;

  /** 신고 리스트 (타입 필터 optional) */
  @GetMapping
  public String list(@RequestParam(value = "type", required = false) ReportType type,
                     @PageableDefault(size = 20, sort = "id") Pageable pageable,
                     Model model) {
    model.addAttribute("page", reportService.listAdmin(type, pageable));
    model.addAttribute("currentType", type);
    return "admin/report/list";
  }

  /** 콘텐츠 삭제(하드삭제): CAFE/TRADE/POST 만 허용 */
  @PostMapping("/{reportId}/content-delete")
  public String contentDelete(@PathVariable Long reportId,
                              RedirectAttributes ra) {
    try {
      reportService.deleteTarget(reportId);
      ra.addFlashAttribute("ok", "대상 콘텐츠를 삭제하고 해당 신고를 처리했습니다.");
    } catch (IllegalStateException e) {
      ra.addFlashAttribute("err", e.getMessage());
    } catch (EmptyResultDataAccessException e) {
      ra.addFlashAttribute("err", "이미 삭제된 대상입니다.");
    } catch (Exception e) {
      log.error("콘텐츠 삭제 실패", e);
      ra.addFlashAttribute("err", "콘텐츠 삭제 중 오류가 발생했습니다.");
    }
    return "redirect:/admin/reports";
  }

  /** 유저 제재(정지): USER/CHAT/COMMENT 만 허용 */
  @PostMapping("/{reportId}/user-ban")
  public String userBan(@PathVariable Long reportId,
                        RedirectAttributes ra) {
    try {
      reportService.banUser(reportId);
      ra.addFlashAttribute("ok", "대상 사용자를 정지하고 해당 신고를 처리했습니다.");
    } catch (IllegalStateException e) {
      ra.addFlashAttribute("err", e.getMessage());
    } catch (Exception e) {
      log.error("유저 제재 실패", e);
      ra.addFlashAttribute("err", "유저 제재 중 오류가 발생했습니다.");
    }
    return "redirect:/admin/reports";
  }

  /** 신고만 무시/종결 */
  @PostMapping("/{reportId}/ignore")
  public String ignore(@PathVariable Long reportId, RedirectAttributes ra) {
    try {
      // 서비스에 ignoreReport(reportId) 있으면 호출, 없으면 repo 직접 호출해도 OK
      // 여기서는 간단히: 처리 후 신고 레코드만 삭제
      // reportService.ignore(reportId); // 있다면 이걸 사용
      reportService.removeReportOnly(reportId); // 아래 서비스에 추가해둔 헬퍼(없으면 생성)
      ra.addFlashAttribute("ok", "신고를 종결 처리했습니다.");
    } catch (Exception e) {
      log.error("신고 종결 실패", e);
      ra.addFlashAttribute("err", "신고 종결 처리 중 오류가 발생했습니다.");
    }
    return "redirect:/admin/reports";
  }
}
