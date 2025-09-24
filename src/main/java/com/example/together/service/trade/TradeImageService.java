package com.example.together.service.trade;

import com.example.together.domain.Trade;
import com.example.together.domain.TradeImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TradeImageService {

  List<TradeImage> listByTradeId(Long tradeId);

  void deleteByIds(List<Long> imageIds);

  void saveImages(Long tradeId, List<MultipartFile> files);

  void deleteByTradeId(Long tradeId);

}
