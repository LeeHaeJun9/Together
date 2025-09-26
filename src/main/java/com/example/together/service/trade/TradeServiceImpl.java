package com.example.together.service.trade;

import com.example.together.common.FileStorageUtil;
import com.example.together.domain.Trade;
import com.example.together.domain.TradeCategory;
import com.example.together.domain.TradeImage;
import com.example.together.repository.TradeImageRepository;
import com.example.together.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {

  private final TradeRepository tradeRepository;
  private final TradeImageRepository tradeImageRepository;

  @Value("${org.zerock.upload.path:C:/upload}")
  private String uploadPath;

  @Value("${app.upload.url-prefix:/upload}")
  private String urlPrefix;

  @Override
  public Trade save(Trade trade) {
    return tradeRepository.save(trade);
  }

  @Override
  public Trade find(Long id) {
    return tradeRepository.findById(id).orElse(null);
  }

  @Override
  @Transactional
  public void remove(Long id) {
    Trade trade = tradeRepository.findById(id).orElse(null);
    if (trade == null) return;

    // 1) 삭제 대상 파일(썸네일 + 연결 이미지) 수집
    List<Path> toDelete = new ArrayList<>();

    if (trade.getThumbnail() != null && !trade.getThumbnail().isBlank()) {
      try {
        Path thumb = FileStorageUtil.toPhysicalPath(trade.getThumbnail(), urlPrefix, uploadPath);
        toDelete.add(thumb);
      } catch (Exception ignore) {}
    }

    List<TradeImage> imgs = tradeImageRepository.findByTrade_IdOrderBySortOrderAsc(trade.getId());
    for (TradeImage img : imgs) {
      try {
        Path p = FileStorageUtil.toPhysicalPath(img.getImageUrl(), urlPrefix, uploadPath);
        toDelete.add(p);
      } catch (Exception ignore) {}
    }

    // 2) DB 먼저 삭제
    tradeImageRepository.deleteByTrade_Id(trade.getId());
    tradeRepository.delete(trade);

    // 3) 트랜잭션 커밋 후 실제 파일 삭제
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override public void afterCommit() {
        toDelete.forEach(FileStorageUtil::deleteQuietly);
      }
    });
  }

  @Override
  public List<Trade> list() {
    return tradeRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
  }

  @Override
  public List<Trade> listByCategory(TradeCategory category) {
    return tradeRepository.findByCategory(category, Sort.by(Sort.Direction.DESC, "id"));
  }
}
