package com.example.together.controller;

import com.example.together.domain.Favorite;
import com.example.together.domain.User;
import com.example.together.repository.UserRepository;
import com.example.together.service.chat.ChatService;
import com.example.together.service.trade.FavoriteService;
import com.example.together.service.trade.TradeImageService;
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

@Slf4j
@Controller
@RequiredArgsConstructor
public class FavoriteController {

  private final FavoriteService favoriteService;
  private final TradeImageService tradeImageService;
  private final UserRepository userRepository;
  private final ChatService chatService;


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

  /** 내 찜 목록 - 최종 경로: /member/favorites */
  @GetMapping("/member/favorites")
  public String favorites(Model model, Principal principal) {
    Long mePk = currentUserPk(principal);
    if (mePk == null) return "redirect:/member/login?redirect=/member/favorites";

    // 1) PK 기반
    List<Favorite> byPk = Optional.ofNullable(favoriteService.listMine(mePk)).orElseGet(List::of);
    // 2) 로그인ID 기반(레거시 겸용)
    String loginId = principal.getName();
    List<Favorite> byLogin = Optional.ofNullable(favoriteService.listMineByLoginId(loginId)).orElseGet(List::of);

    // trade 널 제외 + 같은 trade 중복 제거(보존 순서: PK → 로그인ID)
    Map<Long, Favorite> merged = new LinkedHashMap<>();
    for (Favorite f : byPk)    if (f != null && f.getTrade() != null) merged.putIfAbsent(f.getTrade().getId(), f);
    for (Favorite f : byLogin) if (f != null && f.getTrade() != null) merged.putIfAbsent(f.getTrade().getId(), f);
    List<Favorite> favorites = new ArrayList<>(merged.values());

    // 썸네일/찜수/가격 텍스트
    Map<Long, String> thumbnails = new HashMap<>();
    Map<Long, Long>   favoriteCounts = new HashMap<>();
    Map<Long, String> priceTexts = new HashMap<>();
    NumberFormat nf = NumberFormat.getIntegerInstance(Locale.KOREA);

    favorites.forEach(f -> {
      var t = f.getTrade();
      if (t == null) return;

      String thumb = t.getThumbnail();
      if (thumb == null || thumb.isBlank()) {
        var imgs = tradeImageService.listByTradeId(t.getId());
        if (!imgs.isEmpty()) thumb = imgs.get(0).getImageUrl();
      }
      if (thumb != null && !thumb.isBlank()) thumbnails.put(t.getId(), thumb);

      favoriteCounts.put(t.getId(), favoriteService.count(t.getId()));
      if (t.getPrice() != null) priceTexts.put(t.getId(), nf.format(t.getPrice()) + "원");
    });

    boolean hasFavorites = !favorites.isEmpty();
    log.info("[member/favorites] size={} hasFavorites={} thumbs={} prices={}",
        favorites.size(), hasFavorites, thumbnails.size(), priceTexts.size());

    model.addAttribute("favorites", favorites);
    model.addAttribute("hasFavorites", hasFavorites);
    model.addAttribute("thumbnails", thumbnails);
    model.addAttribute("favoriteCounts", favoriteCounts);
    model.addAttribute("priceTexts", priceTexts);
    model.addAttribute("totalFavorites", favorites.size()); // 상단 카드에서 사용

    int chatCount = chatService.countRooms(mePk);
    model.addAttribute("chatCount", chatCount);

    return "member/favorites";
  }
}
