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
import java.text.NumberFormat;
import java.util.*;

@Slf4j
@Controller
@RequestMapping("/trade")
@RequiredArgsConstructor
public class TradeController {

  private final TradeService tradeService;
  private final TradeImageService tradeImageService;
  private final FavoriteService favoriteService;
  private final UserRepository userRepository;
  private final TradeImageRepository tradeImageRepository;

  // Enum ↔ 한글 매핑
  private static final Map<TradeCategory, String> ENUM_TO_KO = Map.of(
      TradeCategory.ART,     "예술",
      TradeCategory.COOK,    "요리",
      TradeCategory.MUSIC,   "음악",
      TradeCategory.PET,     "반려동물",
      TradeCategory.SPORTS,  "운동",
      TradeCategory.STUDY,   "학습",
      TradeCategory.TRAVEL,  "여행"
  );
  private static final Map<String, TradeCategory> KO_TO_ENUM;
  static {
    Map<String, TradeCategory> m = new HashMap<>();
    ENUM_TO_KO.forEach((k, v) -> m.put(v, k));
    KO_TO_ENUM = Collections.unmodifiableMap(m);
  }
  // 뷰 선택용(라벨)
  private static final List<String> CATEGORIES = List.of(
      "예술", "요리", "음악", "반려동물", "운동", "학습", "여행"
  );

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(TradeCategory.class, new PropertyEditorSupport() {
      @Override
      public void setAsText(String text) throws IllegalArgumentException {
        if (text == null) { setValue(null); return; }
        String t = text.trim();
        if (t.isEmpty()) { setValue(null); return; }

        TradeCategory byKo = KO_TO_ENUM.get(t);
        if (byKo != null) { setValue(byKo); return; }

        try { setValue(TradeCategory.valueOf(t.toUpperCase(Locale.ROOT))); }
        catch (Exception e) { setValue(null); }
      }
    });
  }

  private String currentLoginId(Principal principal) {
    return principal == null ? null : principal.getName();
  }
  private Long currentUserPk(Principal principal) {
    if (principal == null) return null;
    return userRepository.findByUserId(principal.getName())
        .map(User::getId)
        .orElse(null);
  }

  // 목록
  @GetMapping("/list")
  public String list(@RequestParam(value = "category", required = false) String categoryParam,
                     Model model, Principal principal) {

    // 필터 파싱(한글/영문 모두 허용)
    TradeCategory filter = null;
    if (categoryParam != null && !categoryParam.isBlank()) {
      String key = categoryParam.trim();
      TradeCategory byKo = KO_TO_ENUM.get(key);
      if (byKo != null) filter = byKo;
      else {
        try { filter = TradeCategory.valueOf(key.toUpperCase(Locale.ROOT)); }
        catch (Exception ignore) {}
      }
    }

    List<Trade> trades = (filter == null) ? tradeService.list() : tradeService.listByCategory(filter);

    // 썸네일: 엔티티 thumbnail 우선, 없으면 첫 이미지
    Map<Long, String> thumbnails = new HashMap<>();
    for (Trade t : trades) {
      String thumb = t.getThumbnail();
      if (thumb == null || thumb.isBlank()) {
        tradeImageRepository.findFirstByTrade_IdOrderBySortOrderAsc(t.getId())
            .map(TradeImage::getImageUrl)
            .ifPresent(url -> thumbnails.put(t.getId(), url));
      } else {
        thumbnails.put(t.getId(), thumb);
      }
    }

    // 찜 개수
    Map<Long, Long> favoriteCounts = new HashMap<>();
    for (Trade t : trades) {
      favoriteCounts.put(t.getId(), favoriteService.count(t.getId()));
    }

    // 가격 텍스트
    NumberFormat nf = NumberFormat.getIntegerInstance(Locale.KOREA);
    Map<Long, String> priceTexts = new HashMap<>();
    for (Trade t : trades) {
      if (t.getPrice() != null) priceTexts.put(t.getId(), nf.format(t.getPrice()) + "원");
    }

    // 카테고리 라벨
    Map<Long, String> categoryLabels = new HashMap<>();
    for (Trade t : trades) {
      categoryLabels.put(
          t.getId(),
          t.getCategory() != null ? ENUM_TO_KO.getOrDefault(t.getCategory(), "미분류") : "미분류"
      );
    }

    model.addAttribute("trades", trades);
    model.addAttribute("thumbnails", thumbnails);
    model.addAttribute("favoriteCounts", favoriteCounts);
    model.addAttribute("priceTexts", priceTexts);
    model.addAttribute("categoryLabels", categoryLabels);

    model.addAttribute("categories", CATEGORIES);
    model.addAttribute("selectedCategory", categoryParam);
    model.addAttribute("principalUserId", currentUserPk(principal));
    return "trade/list";
  }

  // 등록 폼
  @GetMapping("/register")
  public String registerForm(Model model, Principal principal) {
    model.addAttribute("categories", CATEGORIES);
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

  // 등록 처리
  @PostMapping("/register")
  public String register(@ModelAttribute Trade trade,
                         @RequestParam(value = "images", required = false) List<MultipartFile> images,
                         @RequestParam(value = "files", required = false) List<MultipartFile> files,
                         Principal principal) {
    List<MultipartFile> uploaded = (images != null && !images.isEmpty()) ? images : files;

    Long mePk = currentUserPk(principal);
    if (mePk != null) {
      trade.setSellerUserId(mePk);
      String nickname = userRepository.findById(mePk)
          .map(User::getNickname)
          .orElse(currentLoginId(principal));
      trade.setSellerNickname(nickname);
    }

    Trade saved = tradeService.save(trade);

    if (uploaded != null && !uploaded.isEmpty()) {
      tradeImageService.saveImages(saved.getId(), uploaded);
      if (saved.getThumbnail() == null || saved.getThumbnail().isBlank()) {
        tradeImageRepository.findFirstByTrade_IdOrderBySortOrderAsc(saved.getId())
            .map(TradeImage::getImageUrl)
            .ifPresent(url -> {
              saved.setThumbnail(url);
              tradeService.save(saved);
            });
      }
    }
    return "redirect:/trade/read/" + saved.getId();
  }

  // 읽기
  @GetMapping("/read/{id}")
  public String read(@PathVariable Long id,
                     @RequestParam(value = "from", required = false) String from,
                     Model model, Principal principal) {
    Trade trade = tradeService.find(id);
    if (trade == null) return "redirect:/trade/list";

    List<TradeImage> images = tradeImageService.listByTradeId(id);

    Long mePk = currentUserPk(principal);
    boolean liked = (mePk != null) && favoriteService.isLiked(id, mePk);
    long likeCount = favoriteService.count(id);
    boolean isOwner = mePk != null && Objects.equals(trade.getSellerUserId(), mePk);

    String backUrl = "favorite".equalsIgnoreCase(from) ? "/favorite/list" : "/trade/list";

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

  // 수정 폼
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

  // 수정 처리
  @PostMapping("/modify/{id}")
  public String modify(@PathVariable Long id,
                       @ModelAttribute Trade trade,
                       @RequestParam(value = "images", required = false) List<MultipartFile> newImages,
                       @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds,
                       Principal principal) {

    trade.setId(id);

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
        tradeImageRepository.findFirstByTrade_IdOrderBySortOrderAsc(id)
            .map(TradeImage::getImageUrl)
            .ifPresent(url -> {
              saved.setThumbnail(url);
              tradeService.save(saved);
            });
      }
    }
    return "redirect:/trade/read/" + saved.getId();
  }

  // 삭제
  @PostMapping("/remove/{id}")
  public String remove(@PathVariable Long id) {
    tradeImageService.deleteByTradeId(id);
    tradeService.remove(id);
    return "redirect:/trade/list";
  }
}
