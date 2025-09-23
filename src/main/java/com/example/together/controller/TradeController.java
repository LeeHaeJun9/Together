package com.example.together.controller;

import com.example.together.domain.Trade;
import com.example.together.domain.TradeImage;
import com.example.together.domain.TradeCategory;
import com.example.together.domain.User;
import com.example.together.repository.UserRepository;
import com.example.together.service.trade.FavoriteService;
import com.example.together.service.trade.TradeImageService;
import com.example.together.service.trade.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyEditorSupport;
import java.security.Principal;
import java.util.*;
import java.util.Locale;

@Controller
@RequestMapping("/trade")
@RequiredArgsConstructor
public class TradeController {

  private final TradeService tradeService;
  private final TradeImageService tradeImageService;
  private final FavoriteService favoriteService;
  private final UserRepository userRepository;

  // 환경변수/프로퍼티 우선 → 기본값(사용자 홈 하위)
  @Value("${app.upload-path:${TOGETHER_UPLOAD_PATH:${user.home}/together/uploads}}")
  private String uploadPath;

  // 카테고리 셀렉트용(뷰 라벨)
  private static final List<String> CATEGORIES = Arrays.asList(
      "운동", "예술", "음악", "반려동물", "수집", "언어", "요리"
  );

  // ★ 한글 라벨 ↔ Enum 변환기
  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(TradeCategory.class, new PropertyEditorSupport() {

      private TradeCategory tryNames(String... names) {
        for (String n : names) {
          try { return TradeCategory.valueOf(n); } catch (Exception ignore) {}
        }
        for (TradeCategory c : TradeCategory.values()) {
          for (String n : names) {
            if (c.name().equalsIgnoreCase(n)) return c;
          }
        }
        return null;
      }

      @Override
      public void setAsText(String text) throws IllegalArgumentException {
        if (text == null) { setValue(null); return; }
        String t = text.trim();
        if (t.isEmpty()) { setValue(null); return; }

        // enum 이름 그대로 들어온 경우 (예: MUSIC)
        try {
          setValue(TradeCategory.valueOf(t.toUpperCase(Locale.ROOT)));
          return;
        } catch (Exception ignore) {}

        // 한글 라벨 매핑
        TradeCategory mapped = switch (t) {
          case "운동"     -> tryNames("SPORTS", "EXERCISE", "SPORT");
          case "예술"     -> tryNames("ART", "ARTS");
          case "음악"     -> tryNames("MUSIC");
          case "반려동물" -> tryNames("PETS", "PET");
          case "수집"     -> tryNames("COLLECTION", "COLLECTING", "COLLECTIONS");
          case "언어"     -> tryNames("LANGUAGE", "LANGUAGES");
          case "요리"     -> tryNames("COOKING", "FOOD", "CUISINE");
          default -> null;
        };

        setValue(mapped); // 모르겠으면 null(필요 시 required 검증으로 처리)
      }
    });
  }

  /** 현재 로그인한 계정의 로그인 ID(userId, 문자열) */
  private String currentLoginId(Principal principal) {
    return principal == null ? null : principal.getName();
  }

  /** 현재 로그인한 사용자 PK(Long) */
  private Long currentUserPk(Principal principal) {
    if (principal == null) return null;
    return userRepository.findByUserId(principal.getName())
        .map(User::getId)
        .orElse(null);
  }

  /** 목록 */
  @GetMapping("/list")
  public String list(Model model, Principal principal) {
    List<Trade> trades = tradeService.list();

    // 썸네일 보강
    Map<Long, String> thumbnails = new HashMap<>();
    for (Trade t : trades) {
      String thumb = t.getThumbnail();
      if (thumb == null || thumb.isBlank()) {
        List<TradeImage> imgs = tradeImageService.listByTradeId(t.getId());
        if (!imgs.isEmpty()) thumb = imgs.get(0).getImageUrl();
      }
      if (thumb != null) thumbnails.put(t.getId(), thumb);
    }

    // 각 게시물 찜 개수 맵
    Map<Long, Long> favoriteCounts = new HashMap<>();
    for (Trade t : trades) {
      favoriteCounts.put(t.getId(), favoriteService.count(t.getId()));
    }

    // 가격 문자열(서버에서 포맷 → 템플릿은 단순 출력)
    java.text.NumberFormat nf = java.text.NumberFormat.getIntegerInstance(java.util.Locale.KOREA);
    Map<Long, String> priceTexts = new HashMap<>();
    for (Trade t : trades) {
      if (t.getPrice() != null) {
        priceTexts.put(t.getId(), nf.format(t.getPrice()) + "원");
      }
    }

    model.addAttribute("trades", trades);
    model.addAttribute("thumbnails", thumbnails);
    model.addAttribute("favoriteCounts", favoriteCounts);
    model.addAttribute("priceTexts", priceTexts);  // ★ 템플릿에서 사용
    model.addAttribute("principalUserId", currentUserPk(principal));
    return "trade/list";
  }

  /** 등록 폼 */
  @GetMapping("/register")
  public String registerForm(Model model, Principal principal) {
    model.addAttribute("categories", CATEGORIES);

    // 닉네임 자동 입력(로그인 ID로 조회)
    String nickname = "";
    String loginId = currentLoginId(principal);
    if (loginId != null) {
      nickname = userRepository.findByUserId(loginId)
          .map(User::getNickname)
          .orElse(loginId);
    }
    model.addAttribute("nickname", nickname);
    return "trade/register";
  }

  /** 등록 처리 */
  @PostMapping("/register")
  public String register(@ModelAttribute Trade trade,
                         @RequestParam(value = "images", required = false) List<MultipartFile> images,
                         @RequestParam(value = "files", required = false) List<MultipartFile> files, // ★ 양쪽 이름 모두 수용
                         Principal principal) {

    // 업로드 파라미터 통합
    List<MultipartFile> uploaded = (images != null && !images.isEmpty()) ? images : files;

    // 판매자 식별 정보 주입 (PK + 보여줄 닉네임)
    Long mePk = currentUserPk(principal);
    if (mePk != null) {
      trade.setSellerUserId(mePk); // ★ Trade 엔티티에 sellerUserId(Long) 필드 필요
      String nickname = userRepository.findById(mePk)
          .map(User::getNickname)
          .orElse(currentLoginId(principal));
      trade.setSellerNickname(nickname);
    }

    Trade saved = tradeService.save(trade);

    // 이미지 저장 및 썸네일 보강
    if (uploaded != null && !uploaded.isEmpty()) {
      tradeImageService.saveImages(saved.getId(), uploaded);
      if (saved.getThumbnail() == null || saved.getThumbnail().isBlank()) {
        List<TradeImage> after = tradeImageService.listByTradeId(saved.getId());
        if (!after.isEmpty()) {
          saved.setThumbnail(after.get(0).getImageUrl());
          saved = tradeService.save(saved);
        }
      }
    }
    return "redirect:/trade/read/" + saved.getId();
  }

  /** 읽기 */
  @GetMapping("/read/{id}")
  public String read(@PathVariable Long id,
                     @RequestParam(value = "from", required = false) String from, // favorite에서 진입 여부
                     Model model,
                     Principal principal) {
    Trade trade = tradeService.find(id);
    if (trade == null) return "redirect:/trade/list";

    List<TradeImage> images = tradeImageService.listByTradeId(id);

    Long mePk = currentUserPk(principal);
    boolean liked = (mePk != null) && favoriteService.isLiked(id, mePk);
    long likeCount = favoriteService.count(id);

    // 작성자 여부: User PK(Long) 비교
    boolean isOwner = mePk != null && trade.getSellerUserId() != null
        && mePk.equals(trade.getSellerUserId());

    // 뒤로가기 URL — favorite에서 들어왔으면 favorite/list로
    String backUrl = "favorite".equalsIgnoreCase(from) ? "/favorite/list" : "/trade/list";

    // 템플릿 호환을 위해 trade/dto 둘 다 넣어둠
    model.addAttribute("trade", trade);
    model.addAttribute("dto", trade);

    model.addAttribute("images", images);
    model.addAttribute("liked", liked);
    model.addAttribute("likeCount", likeCount);
    model.addAttribute("favoriteCount", likeCount);
    model.addAttribute("principalUserId", mePk);
    model.addAttribute("isOwner", isOwner);
    model.addAttribute("backUrl", backUrl);

    return "trade/read";
  }

  /** 수정 폼 */
  @GetMapping("/modify/{id}")
  public String modifyForm(@PathVariable Long id, Model model, Principal principal) {
    Trade trade = tradeService.find(id);
    if (trade == null) return "redirect:/trade/list";

    List<TradeImage> images = tradeImageService.listByTradeId(id);

    String nickname = "";
    String loginId = currentLoginId(principal);
    if (loginId != null) {
      nickname = userRepository.findByUserId(loginId)
          .map(User::getNickname)
          .orElse(loginId);
    }

    model.addAttribute("dto", trade);
    model.addAttribute("images", images);
    model.addAttribute("categories", CATEGORIES);
    model.addAttribute("nickname", nickname);
    return "trade/modify";
  }

  /** 수정 처리 */
  @PostMapping("/modify/{id}")
  public String modify(@PathVariable Long id,
                       @ModelAttribute Trade trade,
                       @RequestParam(value = "images", required = false) List<MultipartFile> newImages,
                       @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds,
                       Principal principal) {

    trade.setId(id);

    // 판매자 식별 정보 유지/갱신
    Long mePk = currentUserPk(principal);
    if (mePk != null) {
      trade.setSellerUserId(mePk);
      String nickname = userRepository.findById(mePk)
          .map(User::getNickname)
          .orElse(currentLoginId(principal));
      trade.setSellerNickname(nickname);
    }

    Trade saved = tradeService.save(trade);

    if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
      tradeImageService.deleteByIds(deleteImageIds);
    }

    if (newImages != null && !newImages.isEmpty()) {
      tradeImageService.saveImages(id, newImages);

      if (saved.getThumbnail() == null || saved.getThumbnail().isBlank()) {
        List<TradeImage> after = tradeImageService.listByTradeId(id);
        if (!after.isEmpty()) {
          saved.setThumbnail(after.get(0).getImageUrl());
          saved = tradeService.save(saved);
        }
      }
    }
    return "redirect:/trade/read/" + saved.getId();
  }

  /** 삭제 */
  @PostMapping("/remove/{id}")
  public String remove(@PathVariable Long id) {
    tradeImageService.deleteByTradeId(id);
    tradeService.remove(id);
    return "redirect:/trade/list";
  }
}
