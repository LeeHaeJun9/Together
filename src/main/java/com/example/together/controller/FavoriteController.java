package com.example.together.controller;

import com.example.together.service.favorite.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/favorite")
public class FavoriteController {

  private final FavoriteService favoriteService;

  // read 화면의 찜 버튼이 호출
  @PostMapping("/toggle")
  public ResponseEntity<?> toggle(@RequestParam("tradeId") Long tradeId) {
    String userId = currentUserId();
    if (userId == null) {
      return ResponseEntity.status(401).body("로그인이 필요합니다.");
    }
    boolean favored = favoriteService.toggle(tradeId, userId);
    long count = favoriteService.count(tradeId);
    return ResponseEntity.ok(new ToggleResponse(favored, count));
  }

  private String currentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName()))
        ? auth.getName()
        : null;
  }

  public record ToggleResponse(boolean favored, long favoriteCount) {
  }
}