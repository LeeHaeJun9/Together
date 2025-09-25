package com.example.together.service.trade;

import com.example.together.domain.Trade;
import com.example.together.domain.TradeImage;
import com.example.together.repository.TradeImageRepository;
import com.example.together.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TradeImageServiceImpl implements TradeImageService {

  private final TradeRepository tradeRepository;
  private final TradeImageRepository tradeImageRepository;

  @Value("${org.zerock.upload.path:C:/upload}")
  private String uploadPath;

  // 브라우저가 접근할 URL prefix (WebMvcConfig와 동일)
  @Value("${app.upload.url-prefix:/upload}")
  private String urlPrefix;

  @Override
  @Transactional(readOnly = true)
  public List<TradeImage> listByTradeId(Long tradeId) {
    return tradeImageRepository.findByTrade_IdOrderBySortOrderAsc(tradeId);
  }

  @Override
  @Transactional
  public void saveImages(Long tradeId, List<MultipartFile> images) {
    if (images == null || images.isEmpty()) return;

    Trade trade = tradeRepository.findById(tradeId).orElseThrow();

    int nextOrder = tradeImageRepository.findMaxSortOrderByTradeId(tradeId).orElse(0) + 1;

    try {
      Path base = Paths.get(uploadPath).toAbsolutePath().normalize();
      Files.createDirectories(base);

      for (MultipartFile f : images) {
        if (f.isEmpty()) continue;

        String ext = StringUtils.getFilenameExtension(f.getOriginalFilename());
        String storedName = UUID.randomUUID().toString().replace("-", "");
        if (ext != null && !ext.isBlank()) storedName += "." + ext.toLowerCase();

        Path target = base.resolve(storedName);
        Files.copy(f.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        TradeImage img = new TradeImage();
        img.setTrade(trade);

        String prefix = urlPrefix.endsWith("/") ? urlPrefix : (urlPrefix + "/");
        img.setImageUrl(prefix + storedName);
        img.setSortOrder(nextOrder++);

        tradeImageRepository.save(img);
      }
    } catch (Exception e) {
      throw new RuntimeException("이미지 저장 실패", e);
    }
  }

  @Override
  @Transactional
  public void deleteByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) return;
    tradeImageRepository.deleteByIdIn(ids);
  }

  @Override
  @Transactional
  public void deleteByTradeId(Long tradeId) {
    tradeImageRepository.deleteByTrade_Id(tradeId);
  }
}
