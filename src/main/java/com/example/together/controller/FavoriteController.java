package com.example.together.controller;

import com.example.together.domain.Favorite;
import com.example.together.domain.Trade;
import com.example.together.domain.User;
import com.example.together.repository.ChatRoomRepository;
import com.example.together.repository.TradeImageRepository;
import com.example.together.repository.TradeRepository;
import com.example.together.repository.UserRepository;
import com.example.together.service.chat.ChatService;
import com.example.together.service.trade.FavoriteService;
import com.example.together.service.trade.TradeImageService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class FavoriteController {

  private final FavoriteService favoriteService;
  private final TradeImageService tradeImageService;
  private final UserRepository userRepository;
  private final ChatService chatService;

  // ✅ 카운트 계산/썸네일 벌크 조회용
  private final TradeRepository tradeRepository;
  private final TradeImageRepository tradeImageRepository;
  private final ChatRoomRepository chatRoomRepository;

  /** Principal(userId 문자열) → User PK(Long) */
  private Long currentUserPk(Principal principal) {
    if (principal == null) return null;
    return userRepository.findByUserId(principal.getName())
        .map(User::getId)
        .orElse(null);
  }

  /** 찜 토글(AJAX) - 경로 유지 (/favorite/toggle) */
  @PostMapping("/favorite/toggle")
  @ResponseBody
  public Map<String, Object> toggle(@RequestParam Long tradeId, Principal principal) {
    Long me = currentUserPk(principal);
    if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

    boolean liked = favoriteService.toggle(tradeId, me);
    long count = favoriteService.count(tradeId);

    log.info("[favorite/toggle] userPk={} tradeId={} -> liked={}, count={}", me, tradeId, liked, count);
    return Map.of("liked", liked, "count", count);
  }

  /** 내 찜 목록 - 최종 경로: /member/favorites (LAZY 안전하게 DTO로 투영) */
  @GetMapping("/member/favorites")
  public String favorites(Model model, Principal principal) {
    Long mePk = currentUserPk(principal);
    if (mePk == null) return "redirect:/member/login?redirect=/member/favorites";

    // 1) 두 경로에서 모은 찜 목록 병합 (중복 tradeId 제거, 순서 보존)
    List<Favorite> byPk    = Optional.ofNullable(favoriteService.listMine(mePk)).orElseGet(List::of);
    String loginId         = principal.getName();
    List<Favorite> byLogin = Optional.ofNullable(favoriteService.listMineByLoginId(loginId)).orElseGet(List::of);

    Map<Long, Favorite> merged = new LinkedHashMap<>();
    for (Favorite f : byPk)    if (f != null && f.getTrade() != null) merged.putIfAbsent(f.getTrade().getId(), f);
    for (Favorite f : byLogin) if (f != null && f.getTrade() != null) merged.putIfAbsent(f.getTrade().getId(), f);

    List<Long> tradeIds = merged.values().stream()
        .map(f -> f.getTrade().getId())
        .filter(Objects::nonNull)
        .toList();

    // 2) Trade를 한번에 가져와서 LAZY 예외 방지
    Map<Long, Trade> tradeMap = tradeIds.isEmpty()
        ? Map.of()
        : tradeRepository.findAllById(tradeIds).stream()
        .collect(Collectors.toMap(Trade::getId, t -> t));

    // 3) 화면에 필요한 최소 정보만 담은 DTO 목록 생성
    List<FavItem> items = new ArrayList<>();
    for (Long tid : tradeIds) {
      Trade t = tradeMap.get(tid);
      if (t == null) continue;
      items.add(new FavItem(
          t.getId(),
          nullToEmpty(t.getTitle()),
          t.getStatus(),                       // 문자열: "판매중/예약중/판매완료"
          t.getCategory() != null ? t.getCategory().toString() : null,
          t.getSellerNickname()
      ));
    }

    // 4) 썸네일 / 찜수 / 가격 텍스트 벌크 구성
    Map<Long, String> thumbnails = buildThumbnails(new ArrayList<>(tradeMap.values()));
    Map<Long, Long>   favoriteCounts = bulkFavoriteCounts(new ArrayList<>(tradeMap.values()));
    Map<Long, String> priceTexts     = buildPriceTexts(new ArrayList<>(tradeMap.values()));

    // 5) 상단 카드용 카운트 (상태 문자열 컬렉션)
    var SOLD_STATUSES = List.of("판매완료", "SOLD_OUT"); // 과거 데이터 혼재 대비
    int  totalFavorites = items.size();
    long soldCount      = tradeRepository.countBySellerUserIdAndStatusIn(mePk, SOLD_STATUSES);
    long boughtCount    = chatRoomRepository.countCompletedByBuyer(mePk, SOLD_STATUSES);
    long chatCount      = chatService.countRooms(mePk);

    // 6) 모델 바인딩
    model.addAttribute("items", items);
    model.addAttribute("thumbnails", thumbnails);
    model.addAttribute("favoriteCounts", favoriteCounts);
    model.addAttribute("priceTexts", priceTexts);

    model.addAttribute("totalFavorites", totalFavorites);
    model.addAttribute("soldCount", soldCount);
    model.addAttribute("boughtCount", boughtCount);
    model.addAttribute("chatCount", chatCount);

    return "member/favorites";
  }

  // ===== 내부 유틸 =====

  private String nullToEmpty(String s){ return (s == null) ? "" : s; }

  private Map<Long, String> buildThumbnails(List<Trade> trades) {
    Map<Long, String> thumbnails = new HashMap<>();
    List<Long> ids = trades.stream().map(Trade::getId).toList();

    // 1) 엔티티 thumbnail 우선
    for (Trade t : trades) {
      if (t.getThumbnail() != null && !t.getThumbnail().isBlank()) {
        thumbnails.put(t.getId(), t.getThumbnail());
      }
    }
    // 2) 없는 것만 첫 이미지 벌크 조회
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

  // ===== 화면 투영용 DTO =====
  @Data
  @AllArgsConstructor
  public static class FavItem {
    private Long   tradeId;
    private String title;
    private String status;          // "판매중/예약중/판매완료" (문자열)
    private String category;        // enum.toString() 또는 라벨링은 템플릿에서 처리
    private String sellerNickname;
  }
}
