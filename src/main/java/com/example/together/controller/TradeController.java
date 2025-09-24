package com.example.together.controller;

import com.example.together.domain.Trade;
import com.example.together.domain.TradeCategory;
import com.example.together.domain.TradeImage;
import com.example.together.domain.User;
import com.example.together.repository.TradeImageRepository;
import com.example.together.repository.UserRepository;
import com.example.together.service.trade.FavoriteService;
import com.example.together.service.trade.TradeImageService;
import com.example.together.service.trade.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyEditorSupport;
import java.security.Principal;
import java.util.*;
import java.util.Locale;

@Slf4j
@Controller
@RequestMapping("/trade")
@RequiredArgsConstructor
public class TradeController {

  private final TradeService tradeService;
  private final TradeImageService tradeImageService;
  private final FavoriteService favoriteService;
  private final UserRepository userRepository;
  private final TradeImageRepository tradeImageRepository; // ★ 첫 장 빠르게 조회용

  // 카테고리 셀렉트용(뷰 라벨)
  private static final List<String> CATEGORIES = Arrays.asList(
      "운동", "예술", "음악", "반려동물", "수집", "언어", "요리"
  );

  // ★ 한글 라벨 ↔ Enum 변환기 (폼 바인딩용)
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

  /** 목록 화면용: 한글 라벨 → Enum 매핑 */
  private TradeCategory mapLabelToEnum(String t) {
    if (t == null) return null;
    t = t.trim();
    if (t.isEmpty()) return null;

    // enum 이름 그대로 들어온 경우
    try {
      return TradeCategory.valueOf(t.toUpperCase(Locale.ROOT));
    } catch (Exception ignore) {}

    try {
      return switch (t) {
        case "운동"     -> TradeCategory.valueOf("SPORTS");
        case "예술"     -> TradeCategory.valueOf("ART");
        case "음악"     -> TradeCategory.valueOf("MUSIC");
        case "반려동물" -> TradeCategory.valueOf("PETS");
        case "수집"     -> TradeCategory.valueOf("COLLECTION");
        case "언어"     -> TradeCategory.valueOf("LANGUAGE");
        case "요리"     -> TradeCategory.valueOf("COOKING");
        default -> null;
      };
    } catch (Exception e) {
      return null;
    }
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

  /** 목록 (카테고리 필터 지원) */
  @GetMapping("/list")
  public String list(@RequestParam(value = "category", required = false) String categoryLabel,
                     Model model, Principal principal) {

    List<Trade> trades = tradeService.list();
    log.info("[list] all={}", trades.size());

    // 카테고리 필터 (있으면 in-memory 필터링)
    TradeCategory filterEnum = mapLabelToEnum(categoryLabel);
    if (filterEnum != null) {
      trades = trades.stream()
          .filter(t -> t.getCategory() == filterEnum)  // ★ 여기
          .toList();
      log.info("[list] filtered by {} -> {}", categoryLabel, trades.size());
    }

    // 썸네일 보강(엔티티 thumbnail 우선, 없으면 이미지 첫 장)
    Map<Long, String> thumbnails = new HashMap<>();
    for (Trade t : trades) {
      String thumb = t.getThumbnail();

      if (thumb == null || thumb.isBlank()) {
        tradeImageRepository.findFirstByTrade_IdOrderBySortOrderAsc(t.getId())
            .map(TradeImage::getImageUrl)
            .ifPresent(url -> {
              thumbnails.put(t.getId(), url);
              log.info("[list] trade={} thumb(firstImage)={}", t.getId(), url);
            });
      } else {
        thumbnails.put(t.getId(), thumb);
        log.info("[list] trade={} thumb(entity)={}", t.getId(), thumb);
      }
    }

    // 각 게시물 찜 개수 맵
    Map<Long, Long> favoriteCounts = new HashMap<>();
    for (Trade t : trades) {
      favoriteCounts.put(t.getId(), favoriteService.count(t.getId()));
    }

    // 가격 문자열(서버에서 포맷 → 템플릿 단순 출력)
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
    model.addAttribute("priceTexts", priceTexts);

    // ▼ 리스트 상단 UI용
    model.addAttribute("categories", CATEGORIES);
    model.addAttribute("selectedCategory", (categoryLabel != null && !categoryLabel.isBlank()) ? categoryLabel : null);

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
                         @RequestParam(value = "files", required = false) List<MultipartFile> files,
                         Principal principal) {

    // 업로드 파라미터 통합
    List<MultipartFile> uploaded = (images != null && !images.isEmpty()) ? images : files;

    // 업로드 유입 상태 로그
    log.info("[register] images.size={} files.size={}",
        images == null ? null : images.size(),
        files == null ? null : files.size());
    if (uploaded != null) {
      for (MultipartFile f : uploaded) {
        log.info("  - upload: name={} size={}", f.getOriginalFilename(), f.getSize());
      }
    }

    // 판매자 식별 정보 주입 (PK + 보여줄 닉네임)
    Long mePk = currentUserPk(principal);
    if (mePk != null) {
      trade.setSellerUserId(mePk); // Trade 엔티티에 sellerUserId(Long) 필요
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
        tradeImageRepository.findFirstByTrade_IdOrderBySortOrderAsc(saved.getId())
            .map(TradeImage::getImageUrl)
            .ifPresent(url -> {
              saved.setThumbnail(url);
              tradeService.save(saved);
              log.info("[register] set thumbnail={}", url);
            });
      }
    } else {
      log.info("[register] no images received -> skip image save");
    }
    return "redirect:/trade/read/" + saved.getId();
  }

  /** 읽기 */
  @GetMapping("/read/{id}")
  public String read(@PathVariable Long id,
                     @RequestParam(value = "from", required = false) String from,
                     Model model,
                     Principal principal) {
    Trade trade = tradeService.find(id);
    if (trade == null) return "redirect:/trade/list";

    List<TradeImage> images = tradeImageService.listByTradeId(id);
    log.info("[read] trade={} images={}", id, images.size());
    if (!images.isEmpty()) {
      log.info("[read] firstImageUrl={}", images.get(0).getImageUrl());
    }

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
    log.info("[modifyForm] trade={} images={}", id, images.size());

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
      log.info("[modify] newImages.size={}", newImages.size());
      tradeImageService.saveImages(id, newImages);

      if (saved.getThumbnail() == null || saved.getThumbnail().isBlank()) {
        tradeImageRepository.findFirstByTrade_IdOrderBySortOrderAsc(id)
            .map(TradeImage::getImageUrl)
            .ifPresent(url -> {
              saved.setThumbnail(url);
              tradeService.save(saved);
              log.info("[modify] set thumbnail={}", url);
            });
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
