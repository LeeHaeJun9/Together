package com.example.together.controller;

import com.example.together.dto.trade.TradeDTO;
import com.example.together.dto.trade.TradeReadDTO;
import com.example.together.dto.trade.TradeUploadDTO;
import com.example.together.service.trade.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.List;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/trade")
public class TradeController {

  private final TradeService tradeService;

  @GetMapping("/list")
  public void list(String category, String q, Model model) {
    List<TradeDTO> list = tradeService.listMain(category, q);
    log.info("list size={}, category={}, q={}", list.size(), category, q);
    model.addAttribute("list", list);
    model.addAttribute("category", category);
    model.addAttribute("q", q);
  }

  @GetMapping("/register")
  public void registerGET() {
  }

  @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public String registerPost(@Valid TradeUploadDTO tradeUploadDTO,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
    log.info("trade POST register...");
    if (bindingResult.hasErrors()) {
      log.info("has errors...");
      redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
      return "redirect:/trade/register";
    }
    Long id = tradeService.register(tradeUploadDTO, 1L);
    redirectAttributes.addFlashAttribute("result", id);
    return "redirect:/trade/list";
  }

  @GetMapping({"/read", "/modify"})
  public void read(Long id, Model model) {
    TradeReadDTO tradeReadDTO = tradeService.read(id);
    log.info(tradeReadDTO);
    model.addAttribute("dto", tradeReadDTO);
  }

  @PostMapping(value = "/modify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public String modify(Long id,
                       @Valid TradeUploadDTO tradeUploadDTO,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes) {
    log.info("trade modify post... id={}, dto={}", id, tradeUploadDTO);
    if (bindingResult.hasErrors()) {
      log.info("has errors...");
      redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
      redirectAttributes.addAttribute("id", id);
      return "redirect:/trade/modify";
    }
    tradeService.modify(id, tradeUploadDTO, 1L);
    redirectAttributes.addFlashAttribute("result", "modified");
    redirectAttributes.addAttribute("id", id);
    return "redirect:/trade/read";
  }

  @PostMapping("/remove")
  public String remove(Long id, RedirectAttributes redirectAttributes) {
    log.info("remove post.. id={}", id);
    tradeService.remove(id, 1L);
    redirectAttributes.addFlashAttribute("result", "removed");
    return "redirect:/trade/list";
  }
}