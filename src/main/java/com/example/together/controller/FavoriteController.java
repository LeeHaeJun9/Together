package com.example.together.controller;

import com.example.together.domain.Favorite;
import com.example.together.domain.TradeImage;
import com.example.together.domain.User;
import com.example.together.repository.UserRepository;
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
import java.util.*;

@Slf4j
@Controller
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class FavoriteController {

  private final FavoriteService favoriteService;
  private final TradeImageService tradeImageService;
  private final UserRepository userRepository;

  /** Principal(userId 문자열) → User PK(Long) */
  private Long currentUserPk(Principal principal) {
    if (principal == null) return null;
    return userRepository.findByUserId(principal.getName())
        .map(User::getId)
        .orElse(null);
  }

  @PostMapping("/toggle")
  @ResponseBody
  public Map<String, Object> toggle(@RequestParam Long tradeId, Principal principal) {
    Long me = currentUserPk(principal);
    if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

    boolean liked = favoriteService.toggle(tradeId, me);
    long count = favoriteService.count(tradeId);

    log.info("favorite/toggle userPk={} tradeId={} -> liked={}, count={}", me, tradeId, liked, count);
    return Map.of("liked", liked, "count", count);
  }

  @GetMapping("/list")
  public String favorites(Model model, Principal principal) {
    Long mePk = currentUserPk(principal);
    if (mePk == null) return "redirect:/member/login?redirect=/favorite/list";

    // 1) PK 기반
    List<Favorite> byPk = Optional.ofNullable(favoriteService.listMine(mePk)).orElseGet(List::of);
    // 2) 로그인ID 기반(레거시)
    String loginId = principal.getName();
    List<Favorite> byLogin = Optional.ofNullable(favoriteService.listMineByLoginId(loginId)).orElseGet(List::of);

    // trade 널 제외 + 같은 trade 중복 제거
    Map<Long, Favorite> merged = new LinkedHashMap<>();
    for (Favorite f : byPk)    if (f != null && f.getTrade() != null) merged.putIfAbsent(f.getTrade().getId(), f);
    for (Favorite f : byLogin) if (f != null && f.getTrade() != null) merged.putIfAbsent(f.getTrade().getId(), f);
    List<Favorite> favorites = new ArrayList<>(merged.values());

    // 썸네일/하트/가격 보강
    Map<Long, String> thumbnails = new HashMap<>();
    Map<Long, Long>   favoriteCounts = new HashMap<>();
    Map<Long, String> priceTexts = new HashMap<>();
    var nf = java.text.NumberFormat.getIntegerInstance(java.util.Locale.KOREA);

    favorites.forEach(f -> {
      var t = f.getTrade();
      String thumb = t.getThumbnail();
      if (thumb == null || thumb.isBlank()) {
        var imgs = tradeImageService.listByTradeId(t.getId());
        if (!imgs.isEmpty()) thumb = imgs.get(0).getImageUrl();
      }
      if (thumb != null) thumbnails.put(t.getId(), thumb);

      favoriteCounts.put(t.getId(), favoriteService.count(t.getId()));
      if (t.getPrice() != null) priceTexts.put(t.getId(), nf.format(t.getPrice()) + "원");
    });

    boolean hasFavorites = !favorites.isEmpty();
    log.info("favorite/list size={} hasFavorites={} thumbs={} prices={}",
        favorites.size(), hasFavorites, thumbnails.size(), priceTexts.size());

    model.addAttribute("favorites", favorites);
    model.addAttribute("hasFavorites", hasFavorites); // ← 템플릿은 이것만 본다
    model.addAttribute("thumbnails", thumbnails);
    model.addAttribute("favoriteCounts", favoriteCounts);
    model.addAttribute("priceTexts", priceTexts);

    // 디버그용 숫자(보이진 않지만 혹시 필요하면 템플릿에서 찍어 볼 수 있음)
    model.addAttribute("favoritesSize", favorites.size());

    return "favorite/list";
  }
}
