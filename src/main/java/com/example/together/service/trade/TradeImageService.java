package com.example.together.service.trade;

import com.example.together.domain.Trade;
import com.example.together.domain.TradeImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TradeImageService {

  /** trade_id 기준 이미지 목록(정렬 보장) */
  List<TradeImage> listByTradeId(Long tradeId);

  /** 이미지 여러 개 삭제(물리 파일도 함께 정리) */
  void deleteByIds(List<Long> imageIds);

  /** 업로드된 파일들 저장 + DB(trade_image) insert */
  void saveImages(Long tradeId, List<MultipartFile> files);

  void deleteByTradeId(Long tradeId);

}
