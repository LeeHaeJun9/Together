package com.example.together.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TradeTestController {

  @GetMapping("/trade/ping")
  public String ping() {
    return "trade/ping"; // templates/trade/ping.html 을 찾음
  }
}
