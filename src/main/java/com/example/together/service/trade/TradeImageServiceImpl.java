package com.example.together.service.trade;

import com.example.together.domain.Trade;
import com.example.together.domain.TradeImage;
import com.example.together.repository.TradeImageRepository; // ← 경로 확인
import com.example.together.repository.TradeRepository;     // ← 경로 확인
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class TradeImageServiceImpl implements TradeImageService {

  private final TradeRepository tradeRepository;
  private final TradeImageRepository tradeImageRepository;

  private static final String UPLOAD_ROOT = "uploads";

  @Override
  @Transactional
  public List<String> saveImages(Long tradeId, List<MultipartFile> files) {
    List<String> urls = new ArrayList<>();
    if (files == null || files.isEmpty()) return urls;

    Trade trade = tradeRepository.findById(tradeId)
        .orElseThrow(() -> new IllegalArgumentException("Trade not found: " + tradeId));

    LocalDate today = LocalDate.now();
    Path dir = tradeDir(tradeId, today);
    try {
      Files.createDirectories(dir);
    } catch (IOException e) {
      throw new RuntimeException("업로드 디렉토리 생성 실패: " + dir, e);
    }

    int sort = 0;
    for (MultipartFile file : files) {
      if (file.isEmpty()) continue;

      String original = file.getOriginalFilename();
      String ext = (original != null && original.lastIndexOf('.') != -1)
          ? original.substring(original.lastIndexOf('.')) : "";
      String filename = UUID.randomUUID() + ext;

      Path savePath = dir.resolve(filename);
      try {
        file.transferTo(savePath.toFile());
      } catch (IOException e) {
        log.warn("파일 저장 실패: {}", savePath, e);
        continue;
      }

      String url = toWebUrl(tradeId, today, filename);
      urls.add(url);

      TradeImage img = new TradeImage();
      img.setTrade(trade);
      img.setImageUrl(url);
      img.setSortOrder(sort++);
      tradeImageRepository.save(img);
    }
    return urls;
  }

  @Override
  @Transactional(readOnly = true)
  public String loadFirstImageUrl(Long tradeId) {
    return tradeImageRepository
        .findFirstByTrade_IdOrderBySortOrderAsc(tradeId)
        .map(TradeImage::getImageUrl)
        .orElse(null);
  }

  private static Path tradeDir(Long tradeId, LocalDate date) {
    String datePath = String.format("%04d/%02d/%02d",
        date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    return Paths.get(UPLOAD_ROOT, "trade", String.valueOf(tradeId), datePath)
        .toAbsolutePath().normalize();
  }

  private static String toWebUrl(Long tradeId, LocalDate date, String filename) {
    String datePath = String.format("%04d/%02d/%02d",
        date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    return "/uploads/trade/" + tradeId + "/" + datePath + "/" + filename;
  }
}