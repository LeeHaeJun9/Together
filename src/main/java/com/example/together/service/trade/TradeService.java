package com.example.together.service.trade;

import com.example.together.dto.trade.TradeDTO;
import com.example.together.dto.trade.TradeReadDTO;
import com.example.together.dto.trade.TradeUploadDTO;

import java.util.List;

public interface TradeService {

  Long register(TradeUploadDTO dto, Long sellerId);

  TradeReadDTO read(Long id);

  void modify(Long id, TradeUploadDTO dto, Long sellerId);

  void remove(Long id, Long sellerId);

  List<TradeDTO> listMain(String category, String keyword);
}
