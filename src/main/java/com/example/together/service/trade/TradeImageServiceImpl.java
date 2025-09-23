package com.example.together.service.trade;


import com.example.together.domain.Trade;
import com.example.together.domain.TradeImage;
import com.example.together.repository.TradeImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class TradeImageServiceImpl implements TradeImageService {

  private final TradeImageRepository tradeImageRepository;

  /** WebMvcConfig의 /images/** 매핑과 동일한 루트 폴더 */
  @Value("${app.upload-path:${TOGETHER_UPLOAD_PATH:${user.home}/together/uploads}}")
  private String uploadRoot; // 예: C:/upload

  @Override
  @Transactional(readOnly = true)
  public List<TradeImage> listByTradeId(Long tradeId) {
    return tradeImageRepository.findAllByTradeIdOrderBySortOrderAsc(tradeId);
  }

  @Override
  public void deleteByIds(List<Long> imageIds) {
    if (imageIds == null || imageIds.isEmpty()) return;

    // 물리 파일 삭제
    List<TradeImage> images = tradeImageRepository.findAllById(imageIds);
    for (TradeImage img : images) {
      if (img.getStoredName() != null) {
        Path p = Paths.get(uploadRoot, "trade", img.getStoredName());
        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
      }
    }
    // DB 삭제
    tradeImageRepository.deleteAllByIdInBatch(imageIds);
  }

  @Override
  public void saveImages(Long tradeId, List<MultipartFile> files) {
    if (files == null || files.isEmpty()) return;

    Path dir = Paths.get(uploadRoot, "trade");
    try { Files.createDirectories(dir); } catch (IOException e) { throw new RuntimeException(e); }

    // 현재 마지막 sort_order 이후부터 붙이기
    List<TradeImage> existing = tradeImageRepository.findAllByTradeIdOrderBySortOrderAsc(tradeId);
    int order = existing.isEmpty() ? 0 : existing.get(existing.size() - 1).getSortOrder() + 1;

    for (MultipartFile file : files) {
      if (file == null || file.isEmpty()) continue;

      String original = Optional.ofNullable(file.getOriginalFilename()).orElse("");
      String ext = "";
      int dot = original.lastIndexOf('.');
      if (dot > -1) ext = original.substring(dot);

      String stored = UUID.randomUUID().toString().replace("-", "") + ext;
      Path target = dir.resolve(stored);
      try {
        file.transferTo(target.toFile());
      } catch (IOException e) {
        throw new RuntimeException("이미지 업로드 실패", e);
      }

      Trade tradeRef = new Trade(); // 프록시 레퍼런스
      tradeRef.setId(tradeId);

      TradeImage entity = new TradeImage();
      entity.setTrade(tradeRef);
      entity.setOriginalName(original);
      entity.setStoredName(stored);
      entity.setImageUrl("/images/trade/" + stored); // ★ read/list에서 바로 사용
      entity.setSortOrder(order++);

      tradeImageRepository.save(entity);
    }
  }
  @Override
  public void deleteByTradeId(Long tradeId) {
    List<TradeImage> images = tradeImageRepository.findAllByTradeIdOrderBySortOrderAsc(tradeId);
    // 파일 삭제
    for (TradeImage img : images) {
      if (img.getStoredName() != null) {
        Path p = Paths.get(uploadRoot, "trade", img.getStoredName());
        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
      }
    }
    // DB 삭제
    tradeImageRepository.deleteByTradeId(tradeId);
  }
}
