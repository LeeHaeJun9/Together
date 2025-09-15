package com.example.together.service.trade;

import com.example.together.dto.trade.TradeDTO;
import com.example.together.dto.trade.TradeReadDTO;
import com.example.together.dto.trade.TradeUploadDTO;

import java.util.List;

public interface TradeService {

  Long register(TradeUploadDTO dto, Long sellerId);
  void modify(Long id, TradeUploadDTO dto, Long sellerId);
  void remove(Long id, Long sellerId);

  // 메인 목록 (COMPLETED 제외 + 선택적 category/keyword, 기본 정렬: regdate desc)
  List<TradeDTO> listMain(String category, String keyword);

  // 상세 조회(READ)
  TradeReadDTO read(Long id);
}
