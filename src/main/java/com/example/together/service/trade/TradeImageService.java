package com.example.together.service.trade;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface TradeImageService {
  List<String> saveImages(Long tradeId, List<MultipartFile> files);
  String loadFirstImageUrl(Long tradeId);
}
