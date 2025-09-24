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
import java.nio.file.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class TradeImageServiceImpl implements TradeImageService {

  private final TradeImageRepository tradeImageRepository;

  @Value("${org.zerock.upload.path}")
  private String uploadRoot;

  @Override
  @Transactional(readOnly = true)
  public List<TradeImage> listByTradeId(Long tradeId) {
    return tradeImageRepository.findAllByTrade_IdOrderBySortOrderAsc(tradeId);
  }

  @Override
  public void deleteByIds(List<Long> imageIds) {
    if (imageIds == null || imageIds.isEmpty()) return;

    // 물리 파일 삭제 (루트 경로와 일치)
    List<TradeImage> images = tradeImageRepository.findAllById(imageIds);
    for (TradeImage img : images) {
      String stored = img.getStoredName();
      if (stored != null && !stored.isBlank()) {
        Path p = Paths.get(uploadRoot, stored);
        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
      }
    }
    tradeImageRepository.deleteAllByIdInBatch(imageIds);
  }

  @Override
  public void saveImages(Long tradeId, List<MultipartFile> files) {
    if (files == null || files.isEmpty()) return;

    Path dir = Paths.get(uploadRoot);
    try { Files.createDirectories(dir); }
    catch (IOException e) { throw new RuntimeException("업로드 디렉토리 생성 실패: " + dir, e); }

    // 현재 마지막 sort_order 이후부터 붙이기
    List<TradeImage> existing =
        tradeImageRepository.findAllByTrade_IdOrderBySortOrderAsc(tradeId);
    int order = existing.isEmpty() ? 0 : existing.get(existing.size() - 1).getSortOrder() + 1;

    for (MultipartFile file : files) {
      if (file == null || file.isEmpty()) continue;

      String original = Optional.ofNullable(file.getOriginalFilename()).orElse("");
      String ext = "";
      int dot = original.lastIndexOf('.');
      if (dot > -1) ext = original.substring(dot);

      String stored = UUID.randomUUID().toString().replace("-", "") + ext;

      Path target = dir.resolve(stored);
      try { file.transferTo(target.toFile()); }
      catch (IOException e) { throw new RuntimeException("이미지 업로드 실패: " + original, e); }

      Trade tradeRef = new Trade();
      tradeRef.setId(tradeId);

      TradeImage entity = new TradeImage();
      entity.setTrade(tradeRef);
      entity.setOriginalName(original);
      entity.setStoredName(stored);
      entity.setImageUrl("/upload/" + stored); // 화면용 URL (WebMvcConfig와 1:1)
      entity.setSortOrder(order++);

      tradeImageRepository.save(entity);
    }
  }

  @Override
  public void deleteByTradeId(Long tradeId) {
    List<TradeImage> images =
        tradeImageRepository.findAllByTrade_IdOrderBySortOrderAsc(tradeId);

    for (TradeImage img : images) {
      String stored = img.getStoredName();
      if (stored != null && !stored.isBlank()) {
        Path p = Paths.get(uploadRoot, stored); // 루트 경로
        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
      }
    }
    tradeImageRepository.deleteByTrade_Id(tradeId);
  }
}
