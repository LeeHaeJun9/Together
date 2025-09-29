package com.example.together.controller;

import com.example.together.domain.ChatMessage;
import com.example.together.domain.ChatRoom;
import com.example.together.domain.Trade;
import com.example.together.domain.TradeCategory;
import com.example.together.domain.User;
import com.example.together.repository.ChatMessageRepository;
import com.example.together.repository.UserRepository;
import com.example.together.service.chat.ChatService;
import com.example.together.service.trade.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.security.Principal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;
  private final TradeService tradeService;
  private final UserRepository userRepository;
  private final ChatMessageRepository msgRepo;

  private Long me(Principal principal) {
    if (principal == null) return null;
    return userRepository.findByUserId(principal.getName())
        .map(User::getId)
        .orElse(null);
  }

  private String nickname(Long userId) {
    return userRepository.findById(userId).map(User::getNickname).orElse("알수없음");
  }

  private static final NumberFormat NF_KR = NumberFormat.getIntegerInstance(Locale.KOREA);

  private String priceText(Long price) {
    return (price == null) ? "가격문의" : NF_KR.format(price) + "원";
  }

  private String categoryLabel(TradeCategory c) {
    if (c == null) return "미분류";
    switch (c) {
      case ART:
        return "예술";
      case COOK:
        return "요리";
      case MUSIC:
        return "음악";
      case PET:
        return "반려동물";
      case SPORTS:
        return "운동";
      case STUDY:
        return "학습";
      case TRAVEL:
        return "여행";
      default:
        return "미분류";
    }
  }

  private String statusLabel(Object status) {
    if (status == null) return "미정";
    switch (String.valueOf(status)) {
      case "FOR_SALE":
        return "판매중";
      case "RESERVED":
        return "예약중";
      case "SOLD_OUT":
        return "판매완료";
      default:
        return String.valueOf(status);
    }
  }

  private String statusBadgeClass(Object status) {
    if (status == null) return "text-bg-secondary";
    switch (String.valueOf(status)) {
      case "FOR_SALE":
        return "text-bg-primary";

      case "RESERVED":
        return "text-bg-warning";

      case "SOLD_OUT":
        return "text-bg-secondary";

      default:
        return "text-bg-secondary";
    }
  }

  private String statusClass(Object status) {
    return statusBadgeClass(status);
  }

  @PostMapping("/start")
  public String start(@RequestParam Long tradeId, Principal principal) {
    Long myId = me(principal);
    if (myId == null) return "redirect:/member/login";

    Trade trade = tradeService.find(tradeId);
    if (trade == null) return "redirect:/trade/list";

    Long sellerId = trade.getSellerUserId();
    if (Objects.equals(sellerId, myId)) {
      return "redirect:/trade/read/" + tradeId;
    }

    ChatRoom room = chatService.start(tradeId, sellerId, myId);
    return "redirect:/chat/room/" + room.getId();
  }

  @GetMapping("/list")
  public String list(Model model, Principal principal) {
    Long myId = me(principal);
    if (myId == null) return "redirect:/member/login";

    List<ChatRoom> rooms = chatService.listRooms(myId);

    List<Map<String, Object>> items = new ArrayList<>(rooms.size());
    for (ChatRoom r : rooms) {
      Long counterpartId = Objects.equals(r.getSellerId(), myId) ? r.getBuyerId() : r.getSellerId();
      String counterpartNm = nickname(counterpartId);

      Trade trade = tradeService.find(r.getTradeId());
      String title = (trade != null && trade.getTitle() != null) ? trade.getTitle() : "(삭제됨)";
      String thumb = (trade != null) ? trade.getThumbnail() : null;
      String priceTxt = (trade != null) ? priceText(trade.getPrice()) : "가격문의";
      String cateLabel = (trade != null) ? categoryLabel(trade.getCategory()) : "미분류";
      String statLabel = (trade != null) ? statusLabel(trade.getStatus()) : "미정";
      String statClass = (trade != null) ? statusBadgeClass(trade.getStatus()) : "text-bg-secondary";

      ChatMessage last = msgRepo.findTop1ByChatRoomIdOrderByRegDateDesc(r.getId());
      String preview = (last != null) ? last.getContent() : "(대화 없음)";
      LocalDateTime when = (last != null) ? last.getRegDate()
          : (r.getModDate() != null ? r.getModDate() : r.getRegDate());

      Map<String, Object> row = new HashMap<>();
      row.put("roomId", r.getId());
      row.put("tradeId", r.getTradeId());
      row.put("tradeTitle", title);
      row.put("thumbnail", thumb);
      row.put("counterpartName", counterpartNm);
      row.put("lastContent", preview);
      row.put("lastTime", when);
      row.put("priceText", priceTxt);
      row.put("categoryLabel", cateLabel);
      row.put("statusLabel", statLabel);
      row.put("statusClass", statClass);
      items.add(row);
    }

    model.addAttribute("items", items);
    model.addAttribute("me", myId);
    return "chat/list";
  }

  @GetMapping("/room/{roomId}")
  public String room(@PathVariable Long roomId, Model model, Principal principal) {
    Long myId = me(principal);
    if (myId == null) return "redirect:/member/login";

    ChatRoom room = chatService.getRoomIfParticipant(roomId, myId);
    if (room == null) return "redirect:/trade/list";

    Trade trade = tradeService.find(room.getTradeId());

    String sellerName = nickname(room.getSellerId());
    String buyerName = nickname(room.getBuyerId());
    Map<Long, String> names = new HashMap<>();
    names.put(room.getSellerId(), sellerName);
    names.put(room.getBuyerId(), buyerName);

    String priceTxt = (trade != null) ? priceText(trade.getPrice()) : "가격문의";
    String cateLabel = (trade != null) ? categoryLabel(trade.getCategory()) : "미분류";
    String statLabel = (trade != null) ? statusLabel(trade.getStatus()) : "미정";
    String statClass = (trade != null) ? statusBadgeClass(trade.getStatus()) : "text-bg-secondary";

    // ★ 초기 메시지: ID 오름차순 50개
    List<ChatMessage> messages = msgRepo.findTop50ByChatRoomIdOrderByIdAsc(room.getId());

    model.addAttribute("room", room);
    model.addAttribute("trade", trade);
    model.addAttribute("me", myId);
    model.addAttribute("messages", messages);
    model.addAttribute("names", names);
    model.addAttribute("sellerName", sellerName);
    model.addAttribute("buyerName", buyerName);
    model.addAttribute("priceText", priceTxt);
    model.addAttribute("categoryLabel", cateLabel);
    model.addAttribute("statusLabel", statLabel);
    model.addAttribute("statusClass", statClass);
    return "chat/room";
  }

  // ★ 폴링: afterId(우선) → after(시간, 호환)
  @GetMapping("/messages")
  @ResponseBody
  public List<Map<String, Object>> poll(@RequestParam Long roomId,
                                        @RequestParam(required = false) Long afterId,
                                        @RequestParam(required = false) String after,
                                        Principal principal) {
    Long myId = me(principal);
    if (myId == null) return List.of();

    ChatRoom room = chatService.getRoomIfParticipant(roomId, myId);
    if (room == null) return List.of();

    List<ChatMessage> list;
    if (afterId != null) {
      list = msgRepo.findByChatRoomIdAndIdGreaterThanOrderByIdAsc(roomId, afterId);
    } else if (after != null && !after.isBlank()) {
      LocalDateTime ts = LocalDateTime.parse(after);
      list = msgRepo.findByChatRoomIdAndRegDateAfterOrderByRegDateAsc(roomId, ts);
    } else {
      list = msgRepo.findTop50ByChatRoomIdOrderByIdAsc(roomId);
    }

    List<Map<String, Object>> out = new ArrayList<>(list.size());
    for (ChatMessage m : list) {
      Map<String, Object> row = new HashMap<>();
      row.put("id", m.getId());
      row.put("senderId", m.getSenderId());
      row.put("senderName", nickname(m.getSenderId()));
      row.put("content", m.getContent());
      row.put("regDate", m.getRegDate() != null ? m.getRegDate().toString() : null);
      out.add(row);
    }
    return out;
  }

  @PostMapping("/send")
  @ResponseBody
  public ResponseEntity<?> send(@RequestParam Long roomId,
                                @RequestParam String content,
                                Principal principal) {
    Long myId = me(principal);
    if (myId == null) return ResponseEntity.status(401).build();

    ChatRoom room = chatService.getRoomIfParticipant(roomId, myId);
    if (room == null) return ResponseEntity.status(403).build();

    if (content == null || content.trim().isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "empty"));
    }

    ChatMessage saved = chatService.send(roomId, myId, content.trim());
    return ResponseEntity.ok(Map.of(
        "id", saved.getId(),
        "senderId", saved.getSenderId(),
        "senderName", nickname(saved.getSenderId()),
        "content", saved.getContent(),
        "regDate", saved.getRegDate() != null ? saved.getRegDate().toString() : null
    ));
  }

  @PostMapping("/room/{roomId}/delete")
  @ResponseBody
  public ResponseEntity<?> deleteRoom(@PathVariable Long roomId, Principal principal) {
    Long myId = me(principal);
    if (myId == null) return ResponseEntity.status(401).build();

    ChatRoom room = chatService.getRoomIfParticipant(roomId, myId);
    if (room == null) return ResponseEntity.status(403).build();

    chatService.deleteRoom(roomId, myId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/trade/status")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> updateTradeStatus(@RequestParam Long roomId,
                                                               @RequestParam String status,
                                                               Principal principal) {
    Long myId = me(principal);
    if (myId == null) return ResponseEntity.status(401).build();

    ChatRoom room = chatService.getRoomIfParticipant(roomId, myId);
    if (room == null) return ResponseEntity.status(403).build(); // 참여자 아님

    // 판매자만 변경 가능
    if (!Objects.equals(room.getSellerId(), myId)) return ResponseEntity.status(403).build();

    Trade trade = tradeService.find(room.getTradeId());
    if (trade == null) return ResponseEntity.status(404).build();

    String next = (status == null) ? "" : status.trim().toUpperCase(Locale.ROOT);
    if (!List.of("FOR_SALE", "RESERVED", "SOLD_OUT").contains(next)) {
      return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "invalid status"));
    }

    String cur = String.valueOf(trade.getStatus());

    // 한 번 SOLD_OUT이면 더 이상 변경 불가
    if ("SOLD_OUT".equalsIgnoreCase(cur) && !"SOLD_OUT".equals(next)) {
      return ResponseEntity.status(409).body(Map.of("ok", false, "msg", "locked"));
    }

    // 변경 없음
    if (cur.equalsIgnoreCase(next)) {
      return ResponseEntity.ok(Map.of(
          "ok", true,
          "status", cur,
          "statusLabel", statusLabel(cur),
          "statusClass", statusClass(cur)
      ));
    }

    // ★ 여기만 변경: status가 String 이므로 그대로 세팅
    trade.setStatus(next);

    tradeService.save(trade);

    return ResponseEntity.ok(Map.of(
        "ok", true,
        "status", next,
        "statusLabel", statusLabel(next),
        "statusClass", statusClass(next)
    ));
  }
}