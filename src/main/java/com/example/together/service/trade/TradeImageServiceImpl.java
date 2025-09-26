package com.example.together.service.trade;

import com.example.together.common.FileStorageUtil;
import com.example.together.domain.Trade;
import com.example.together.domain.TradeImage;
import com.example.together.repository.TradeImageRepository;
import com.example.together.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeImageServiceImpl implements TradeImageService {

  private final TradeRepository tradeRepository;
  private final TradeImageRepository tradeImageRepository;

  @Value("${org.zerock.upload.path:C:/upload}")
  private String uploadPath;

  /** 브라우저 접근 URL prefix (예: /upload) */
  @Value("${app.upload.url-prefix:/upload}")
  private String urlPrefix;

  @Override
  @Transactional(readOnly = true)
  public List<TradeImage> listByTradeId(Long tradeId) {
    return tradeImageRepository.findByTrade_IdOrderBySortOrderAsc(tradeId);
  }

  /**
   * 이미지 저장: 현재 보유 개수 기준으로 최대 10장까지만 추가 저장
   */
  @Override
  @Transactional
  public void saveImages(Long tradeId, List<MultipartFile> images) {
    if (images == null || images.isEmpty()) return;

    Trade trade = tradeRepository.findById(tradeId).orElseThrow();

    // 현재 보유 개수
    int currentCnt = tradeImageRepository.findByTrade_IdOrderBySortOrderAsc(tradeId).size();
    int remain = Math.max(0, 10 - currentCnt);
    if (remain <= 0) return; // 이미 10장 보유

    // 업로드할 목록 제한
    List<MultipartFile> toSave = images.stream()
        .filter(f -> f != null && !f.isEmpty())
        .limit(remain)
        .collect(Collectors.toList());
    if (toSave.isEmpty()) return;

    int nextOrder = tradeImageRepository.findMaxSortOrderByTradeId(tradeId).orElse(0) + 1;

    try {
      Path base = FileStorageUtil.uploadRoot(uploadPath);
      Files.createDirectories(base);

      for (MultipartFile f : toSave) {
        String ext = StringUtils.getFilenameExtension(f.getOriginalFilename());
        String storedName = UUID.randomUUID().toString().replace("-", "");
        if (StringUtils.hasText(ext)) storedName += "." + ext.toLowerCase();

        Path target = base.resolve(storedName).normalize();
        if (!target.startsWith(base)) throw new IllegalArgumentException("Invalid target path");
        Files.copy(f.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        TradeImage img = new TradeImage();
        img.setTrade(trade);

        String prefix = urlPrefix.endsWith("/") ? urlPrefix : (urlPrefix + "/");
        img.setImageUrl(prefix + storedName);      // DB에는 URL 형식으로 저장 (/upload/xxx.jpg)
        img.setStoredName(storedName);
        img.setOriginalName(f.getOriginalFilename());
        img.setSortOrder(nextOrder++);

        tradeImageRepository.save(img);
      }
    } catch (Exception e) {
      throw new RuntimeException("이미지 저장 실패", e);
    }
  }

  /**
   * 일부 이미지 삭제: DB 삭제 후, 커밋 완료되면 물리 파일 삭제
   */
  @Override
  @Transactional
  public void deleteByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) return;

    // 1) 삭제 대상 조회 → 물리 경로 확보
    List<TradeImage> targets = tradeImageRepository.findAllById(ids);
    if (targets.isEmpty()) return;

    List<Path> toDelete = targets.stream()
        .map(t -> FileStorageUtil.toPhysicalPath(t.getImageUrl(), urlPrefix, uploadPath))
        .collect(Collectors.toList());

    // 2) DB 삭제
    tradeImageRepository.deleteAllInBatch(targets);

    // 3) 트랜잭션 커밋 후 실제 파일 삭제
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override public void afterCommit() {
        toDelete.forEach(FileStorageUtil::deleteQuietly);
      }
    });
  }

  /**
   * 거래 단위 전체 이미지 삭제: DB 삭제 후, 커밋 완료되면 물리 파일 삭제
   */
  @Override
  @Transactional
  public void deleteByTradeId(Long tradeId) {
    // 1) 모든 이미지 조회 → 물리 경로 확보
    List<TradeImage> imgs = tradeImageRepository.findByTrade_IdOrderBySortOrderAsc(tradeId);
    List<Path> toDelete = imgs.stream()
        .map(t -> FileStorageUtil.toPhysicalPath(t.getImageUrl(), urlPrefix, uploadPath))
        .collect(Collectors.toList());

    // 2) DB 삭제
    tradeImageRepository.deleteByTrade_Id(tradeId);

    // 3) 트랜잭션 커밋 후 실제 파일 삭제
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override public void afterCommit() {
        toDelete.forEach(FileStorageUtil::deleteQuietly);
      }
    });
  }
}
