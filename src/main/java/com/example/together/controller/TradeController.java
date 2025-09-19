package com.example.together.controller;

import com.example.together.domain.Trade;
import com.example.together.dto.trade.*;
import com.example.together.service.favorite.FavoriteService;
import com.example.together.service.trade.TradeImageService;
import com.example.together.service.trade.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.Arrays;
import java.util.List;


@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/trade")
public class TradeController {

  private final TradeService tradeService;
  private final FavoriteService favoriteService;
  private final TradeImageService tradeImageService;

  @GetMapping
  public String root() {
    return "redirect:/trade/list";
  }

  @GetMapping("/list")
  public String list(@RequestParam(defaultValue = "0") int page,
                     @RequestParam(defaultValue = "10") int size,
                     @RequestParam(required = false) String q,
                     Model model) {

    Page<Trade> result = tradeService.findList(PageRequest.of(page, size), q);

    var dtoList = result.getContent().stream()
        .map(t -> TradeDTO.builder()
            .id(t.getId())
            .title(t.getTitle())
            .content(t.getContent())
            .price(t.getPrice())
            .status(t.getStatus() != null ? t.getStatus().name() : "FOR_SALE")
            .sellerNickname(t.getSellerNickname())
            .regdate(t.getRegdate())
            .moddate(t.getModdate())
            .favoriteCount((int) favoriteService.count(t.getId()))
            .thumbnail(t.getThumbnail())
            .build())
        .toList();

    model.addAttribute("trades", dtoList);
    model.addAttribute("page", result);
    return "trade/list";
  }

  @GetMapping("/read")
  public String read(@RequestParam("id") Long id, Model model) {
    Trade trade = tradeService.findOne(id)
        .orElseThrow(() -> new IllegalArgumentException("해당 게시물이 없습니다: " + id));

    String userId = currentUserId();
    long favoriteCount = favoriteService.count(id);
    boolean favored = favoriteService.isFavorited(id, userId);

    model.addAttribute("trade", trade);
    model.addAttribute("favoriteCount", favoriteCount);
    model.addAttribute("favored", favored);
    return "trade/read";
  }

  @GetMapping("/register")
  public String createForm(Model model) {
    model.addAttribute("trade", new Trade());
    return "trade/register";
  }

  @PostMapping("/register")
  public String create(@ModelAttribute("trade") Trade trade,
                       BindingResult bindingResult,
                       @RequestParam("writer") String writer,
                       @RequestParam(name = "files", required = false) MultipartFile[] files) {
    if (bindingResult.hasErrors()) {
      return "trade/register";
    }

    if (trade.getThumbnail() == null || trade.getThumbnail().isBlank()) {
      trade.setThumbnail("/images/no-image.png");
    }

    trade.setSellerNickname(writer);

    // 1) 글 저장
    Trade saved = tradeService.create(trade);

    // 2) 이미지 저장 → URL 반환 → 첫 장을 썸네일로 덮어쓰기
    List<String> urls = List.of();
    if (files != null && files.length > 0) {
      urls = tradeImageService.saveImages(saved.getId(), Arrays.asList(files));
    }
    if (!urls.isEmpty()) {
      saved.setThumbnail(urls.get(0));
      tradeService.update(saved.getId(), saved);
    }

    return "redirect:/trade/read?id=" + saved.getId();
  }

  @PostMapping("/delete")
  public String delete(@RequestParam("id") Long id) {
    tradeService.delete(id);
    return "redirect:/trade/list";
  }

  private String currentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName()))
        ? auth.getName()
        : null;
  }
}


