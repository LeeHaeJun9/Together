package com.example.together.controller;

import com.example.together.domain.Trade;
import com.example.together.domain.TradeCategory;
import com.example.together.domain.TradeImage;
import com.example.together.domain.User;
import com.example.together.repository.ChatRoomRepository;
import com.example.together.repository.TradeImageRepository;
import com.example.together.repository.TradeRepository;
import com.example.together.repository.UserRepository;
import com.example.together.service.trade.FavoriteService;
import com.example.together.service.trade.TradeImageService;
import com.example.together.service.trade.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyEditorSupport;
import java.security.Principal;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

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
  private final TradeRepository tradeRepository;
  private final ChatRoomRepository chatRoomRepository;


  private static final Set<String> SOLD_SET = Set.of("SOLD_OUT");

  private static final Map<TradeCategory, String> ENUM_TO_KO = Map.of(
      TradeCategory.ART, "예술",
      TradeCategory.COOK, "요리",
      TradeCategory.MUSIC, "음악",
      TradeCategory.PET, "반려동물",
      TradeCategory.SPORTS, "스포츠",
      TradeCategory.STUDY, "스터디",
      TradeCategory.TRAVEL, "여행"
  );
  private static final Map<String, TradeCategory> KO_TO_ENUM;
  static {
    Map<String, TradeCategory> m = new HashMap<>();
    ENUM_TO_KO.forEach((k, v) -> m.put(v, k));
    KO_TO_ENUM = Collections.unmodifiableMap(m);
  }
  private static final List<String> CATEGORIES = List.of("예술", "요리", "음악", "반려동물", "스포츠", "스터디", "여행");

  // ===== 바인더: 한글 카테고리도 허용 =====
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
    return userRepository.findByUserId(principal.getName()).map(User::getId).orElse(null);
  }

  /* ===== 목록 (카테고리 + 검색) ===== */
  @GetMapping("/list")
  public String list(
      @RequestParam(value = "category", required = false) String categoryParam,
      @RequestParam(value = "query", required = false) String query,
      @RequestParam(value = "scope", required = false, defaultValue = "BOTH") String scope,
      Model model, Principal principal) {

    TradeCategory filter = parseCategory(categoryParam);
    Sort sort = Sort.by(Sort.Direction.DESC, "id");

    List<Trade> trades;
    String q = (query == null) ? null : query.trim();
    boolean hasQ = (q != null && !q.isEmpty());

    if (!hasQ) {
      trades = (filter == null) ? tradeService.list() : tradeService.listByCategory(filter);
    } else {
      switch (scope.toUpperCase(Locale.ROOT)) {
        case "TITLE":
          trades = (filter == null)
              ? tradeRepository.findByTitleContainingIgnoreCase(q, sort)
              : tradeRepository.findByCategoryAndTitleContainingIgnoreCase(filter, q, sort);
          break;
        case "CONTENT":
          trades = (filter == null)
              ? tradeRepository.findByDescriptionContainingIgnoreCase(q, sort)
              : tradeRepository.findByCategoryAndDescriptionContainingIgnoreCase(filter, q, sort);
          break;
        default: // BOTH
          trades = (filter == null)
              ? tradeRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(q, q, sort)
              : tradeRepository.findByCategoryAndTitleContainingIgnoreCaseOrCategoryAndDescriptionContainingIgnoreCase(
              filter, q, filter, q, sort);
      }
    }

    bindCommonListModel(model, trades, categoryParam, q, scope, currentUserPk(principal));
    return "trade/list";
  }

  /* ===== 내가 작성한 글 (trade/list 스타일) ===== */
  @GetMapping("/my")
  public String myTrades(
      @RequestParam(value = "category", required = false) String categoryParam,
      @RequestParam(value = "query", required = false) String query,
      @RequestParam(value = "scope", required = false, defaultValue = "BOTH") String scope,
      Model model, Principal principal) {

    Long mePk = currentUserPk(principal);
    if (mePk == null) return "redirect:/member/login?redirect=/trade/my";

    TradeCategory filter = parseCategory(categoryParam);
    Sort sort = Sort.by(Sort.Direction.DESC, "id");
    String q = (query == null) ? null : query.trim();
    boolean hasQ = (q != null && !q.isEmpty());

    List<Trade> trades;
    if (!hasQ) {
      // 전용 메서드가 있으면 아래 주석으로 교체 권장
      // trades = (filter == null)
      //     ? tradeRepository.findBySellerUserId(mePk, sort)
      //     : tradeRepository.findBySellerUserIdAndCategory(mePk, filter, sort);
      List<Trade> base = (filter == null) ? tradeService.list() : tradeService.listByCategory(filter);
      trades = base.stream()
          .filter(t -> Objects.equals(t.getSellerUserId(), mePk))
          .sorted(Comparator.comparing(Trade::getId).reversed())
          .toList();
    } else {
      switch (scope.toUpperCase(Locale.ROOT)) {
        case "TITLE":
          trades = ((filter == null)
              ? tradeRepository.findByTitleContainingIgnoreCase(q, sort)
              : tradeRepository.findByCategoryAndTitleContainingIgnoreCase(filter, q, sort))
              .stream().filter(t -> Objects.equals(t.getSellerUserId(), mePk)).toList();
          break;
        case "CONTENT":
          trades = ((filter == null)
              ? tradeRepository.findByDescriptionContainingIgnoreCase(q, sort)
              : tradeRepository.findByCategoryAndDescriptionContainingIgnoreCase(filter, q, sort))
              .stream().filter(t -> Objects.equals(t.getSellerUserId(), mePk)).toList();
          break;
        default:
          trades = tradeRepository
              .findByCategoryAndTitleContainingIgnoreCaseOrCategoryAndDescriptionContainingIgnoreCase(
                  filter, q, filter, q, sort)
              .stream().filter(t -> Objects.equals(t.getSellerUserId(), mePk)).toList();
      }
    }

    bindCommonListModel(model, trades, categoryParam, q, scope, mePk);
    return "trade/my";
  }

  /* ===== 등록 ===== */
  @GetMapping("/register")
  public String registerForm(Model model, Principal principal) {
    model.addAttribute("categories", CATEGORIES);
    String nickname = "";
    String loginId = currentLoginId(principal);
    if (loginId != null) {
      nickname = userRepository.findByUserId(loginId).map(User::getNickname).orElse(loginId);
    }
    model.addAttribute("nickname", nickname);
    return "trade/register";
  }

  @PostMapping("/register")
  public String register(@ModelAttribute Trade trade,
                         @RequestParam(value = "images", required = false) List<MultipartFile> images,
                         @RequestParam(value = "files", required = false) List<MultipartFile> files,
                         Principal principal) {
    List<MultipartFile> uploaded = (images != null && !images.isEmpty()) ? images : files;
    Long mePk = currentUserPk(principal);
    if (mePk != null) {
      trade.setSellerUserId(mePk);
      String nickname = userRepository.findById(mePk).map(User::getNickname).orElse(currentLoginId(principal));
      trade.setSellerNickname(nickname);
    }
    Trade saved = tradeService.save(trade);

    if (uploaded != null && !uploaded.isEmpty()) {
      tradeImageService.saveImages(saved.getId(), uploaded);
      if (saved.getThumbnail() == null || saved.getThumbnail().isBlank()) {
        tradeImageRepository.findFirstByTrade_IdOrderBySortOrderAsc(saved.getId())
            .map(TradeImage::getImageUrl)
            .ifPresent(url -> { saved.setThumbnail(url); tradeService.save(saved); });
      }
    }
    return "redirect:/trade/read/" + saved.getId();
  }

  /* ===== 읽기 ===== */
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

  /* ===== 수정 ===== */
  @GetMapping("/modify/{id}")
  public String modifyForm(@PathVariable Long id, Model model, Principal principal) {
    Trade trade = tradeService.find(id);
    if (trade == null) return "redirect:/trade/list";
    List<TradeImage> images = tradeImageService.listByTradeId(id);

    String nickname = "";
    String loginId = currentLoginId(principal);
    if (loginId != null) {
      nickname = userRepository.findByUserId(loginId).map(User::getNickname).orElse(loginId);
    }

    model.addAttribute("dto", trade);
    model.addAttribute("images", images);
    model.addAttribute("categories", CATEGORIES);
    model.addAttribute("nickname", nickname);
    return "trade/modify";
  }

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
      String nickname = userRepository.findById(mePk).map(User::getNickname).orElse(currentLoginId(principal));
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
            .ifPresent(url -> { saved.setThumbnail(url); tradeService.save(saved); });
      }
    }
    return "redirect:/trade/read/" + saved.getId();
  }

  /* ===== 삭제 ===== */
  @PostMapping("/remove/{id}")
  public String remove(@PathVariable Long id) {
    tradeImageService.deleteByTradeId(id);
    tradeService.remove(id);
    return "redirect:/trade/list";
  }

  /* ===== 판매한 상품(판매완료만) ===== */
  @GetMapping("/sold")
  public String mySold(Model model, Principal principal) {
    Long mePk = currentUserPk(principal);
    if (mePk == null) return "redirect:/member/login?redirect=/trade/sold";

    Sort sort = Sort.by(Sort.Direction.DESC, "id");
    List<String> soldParams = List.of("SOLD_OUT");

    List<Trade> trades = tradeRepository.findBySellerUserIdAndStatusIn(mePk, soldParams, sort);

    Map<Long, String> thumbnails = buildThumbnails(trades);
    Map<Long, Long> favoriteCounts = bulkFavoriteCounts(trades);
    Map<Long, String> priceTexts = buildPriceTexts(trades);
    Map<Long, String> categoryLabels = buildCategoryLabels(trades);

    long soldCount = trades.size();
    long boughtCount = chatRoomRepository.countCompletedByBuyer(mePk, soldParams);
    long totalFavorites = 0L; // 필요시 계산
    long chatCount = 0L;      // 필요시 서비스로 계산

    model.addAttribute("trades", trades);
    model.addAttribute("thumbnails", thumbnails);
    model.addAttribute("favoriteCounts", favoriteCounts);
    model.addAttribute("priceTexts", priceTexts);
    model.addAttribute("categoryLabels", categoryLabels);

    model.addAttribute("soldCount", soldCount);
    model.addAttribute("boughtCount", boughtCount);
    model.addAttribute("totalFavorites", totalFavorites);
    model.addAttribute("chatCount", chatCount);

    return "trade/sold";
  }

  /* ===== 구매한 상품(채팅에서 거래 완료된 건) ===== */
  @GetMapping("/bought")
  public String myBought(Model model, Principal principal) {
    Long mePk = currentUserPk(principal);
    if (mePk == null) return "redirect:/member/login?redirect=/trade/bought";

    List<String> soldParams = List.of("SOLD_OUT");

    List<Long> tradeIds = chatRoomRepository.findCompletedTradeIdsByBuyer(mePk, soldParams);
    List<Trade> trades = tradeIds.isEmpty() ? List.of() : tradeRepository.findAllById(tradeIds);
    trades = trades.stream().sorted(Comparator.comparing(Trade::getId).reversed()).toList();

    Map<Long, String> thumbnails = buildThumbnails(trades);
    Map<Long, Long> favoriteCounts = bulkFavoriteCounts(trades);
    Map<Long, String> priceTexts = buildPriceTexts(trades);
    Map<Long, String> categoryLabels = buildCategoryLabels(trades);

    long boughtCount = tradeIds.size();
    long soldCount = tradeRepository.countBySellerUserIdAndStatusIn(mePk, soldParams);
    long totalFavorites = 0L;
    long chatCount = 0L;

    model.addAttribute("trades", trades);
    model.addAttribute("thumbnails", thumbnails);
    model.addAttribute("favoriteCounts", favoriteCounts);
    model.addAttribute("priceTexts", priceTexts);
    model.addAttribute("categoryLabels", categoryLabels);

    model.addAttribute("soldCount", soldCount);
    model.addAttribute("boughtCount", boughtCount);
    model.addAttribute("totalFavorites", totalFavorites);
    model.addAttribute("chatCount", chatCount);

    return "trade/bought";
  }

  /* ===== 유틸 ===== */
  private TradeCategory parseCategory(String categoryParam) {
    if (categoryParam == null || categoryParam.isBlank()) return null;
    String key = categoryParam.trim();
    TradeCategory byKo = KO_TO_ENUM.get(key);
    if (byKo != null) return byKo;
    try { return TradeCategory.valueOf(key.toUpperCase(Locale.ROOT)); }
    catch (Exception ignore) { return null; }
  }

  private void bindCommonListModel(Model model, List<Trade> trades,
                                   String selectedCategory, String q, String scope, Long principalUserId) {
    Map<Long, String> thumbnails = new HashMap<>();
    List<Long> ids = trades.stream().map(Trade::getId).collect(Collectors.toList());
    if (!ids.isEmpty()) {
      for (Trade t : trades) {
        if (t.getThumbnail() != null && !t.getThumbnail().isBlank()) {
          thumbnails.put(t.getId(), t.getThumbnail());
        }
      }
      List<Long> needFirst = ids.stream().filter(id -> !thumbnails.containsKey(id)).collect(Collectors.toList());
      if (!needFirst.isEmpty()) {
        for (TradeImageRepository.ThumbProj p : tradeImageRepository.findFirstImageUrlsByTradeIds(needFirst)) {
          thumbnails.put(p.getTradeId(), p.getImageUrl());
        }
      }
    }

    Map<Long, Long> favoriteCounts = new HashMap<>();
    if (!ids.isEmpty()) {
      favoriteService.countByTradeIds(ids).forEach(p -> favoriteCounts.put(p.getTradeId(), p.getCnt()));
    }

    NumberFormat nf = NumberFormat.getIntegerInstance(Locale.KOREA);
    Map<Long, String> priceTexts = new HashMap<>();
    for (Trade t : trades) {
      if (t.getPrice() != null) priceTexts.put(t.getId(), nf.format(t.getPrice()) + "원");
    }

    Map<Long, String> categoryLabels = new HashMap<>();
    for (Trade t : trades) {
      categoryLabels.put(t.getId(),
          t.getCategory() != null ? ENUM_TO_KO.getOrDefault(t.getCategory(), "미분류") : "미분류");
    }

    model.addAttribute("trades", trades);
    model.addAttribute("thumbnails", thumbnails);
    model.addAttribute("favoriteCounts", favoriteCounts);
    model.addAttribute("priceTexts", priceTexts);
    model.addAttribute("categoryLabels", categoryLabels);

    model.addAttribute("categories", CATEGORIES);
    model.addAttribute("selectedCategory", selectedCategory);
    model.addAttribute("q", q);
    model.addAttribute("scope", scope);
    model.addAttribute("principalUserId", principalUserId);
  }

  private Map<Long, String> buildThumbnails(List<Trade> trades) {
    Map<Long, String> thumbnails = new HashMap<>();
    List<Long> ids = trades.stream().map(Trade::getId).toList();
    for (Trade t : trades) {
      if (t.getThumbnail() != null && !t.getThumbnail().isBlank()) {
        thumbnails.put(t.getId(), t.getThumbnail());
      }
    }
    List<Long> needFirst = ids.stream().filter(id -> !thumbnails.containsKey(id)).toList();
    if (!needFirst.isEmpty()) {
      for (TradeImageRepository.ThumbProj p : tradeImageRepository.findFirstImageUrlsByTradeIds(needFirst)) {
        thumbnails.put(p.getTradeId(), p.getImageUrl());
      }
    }
    return thumbnails;
  }

  private Map<Long, Long> bulkFavoriteCounts(List<Trade> trades) {
    Map<Long, Long> map = new HashMap<>();
    List<Long> ids = trades.stream().map(Trade::getId).toList();
    if (!ids.isEmpty()) favoriteService.countByTradeIds(ids).forEach(p -> map.put(p.getTradeId(), p.getCnt()));
    return map;
  }

  private Map<Long, String> buildPriceTexts(List<Trade> trades) {
    Map<Long, String> priceTexts = new HashMap<>();
    var nf = NumberFormat.getIntegerInstance(Locale.KOREA);
    for (Trade t : trades) {
      if (t.getPrice() != null) priceTexts.put(t.getId(), nf.format(t.getPrice()) + "원");
    }
    return priceTexts;
  }

  private Map<Long, String> buildCategoryLabels(List<Trade> trades) {
    Map<Long, String> labels = new HashMap<>();
    for (Trade t : trades) {
      labels.put(t.getId(),
          t.getCategory() != null ? ENUM_TO_KO.getOrDefault(t.getCategory(), "미분류") : "미분류");
    }
    return labels;
  }
}
