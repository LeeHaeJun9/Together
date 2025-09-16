package com.example.together.service.trade;

import com.example.together.domain.*;
import com.example.together.dto.trade.TradeDTO;
import com.example.together.dto.trade.TradeReadDTO;
import com.example.together.dto.trade.TradeUploadDTO;
import com.example.together.repository.TradeImageRepository;
import com.example.together.repository.TradeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {

  private final TradeRepository tradeRepository;
  private final TradeImageRepository tradeImageRepository;

  @PersistenceContext
  private EntityManager entityManager;

  private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  @Override
  @Transactional
  public Long register(TradeUploadDTO dto, Long sellerId) {
    User sellerRef = entityManager.getReference(User.class, sellerId);

    Trade trade = new Trade();
    trade.setTitle(dto.getTitle());
    trade.setDescription(dto.getDescription());
    trade.setPrice(dto.getPrice());
    trade.setThumbnail(dto.getThumbnail());
    trade.setTradeCategory(TradeCategory.valueOf(dto.getTradeCategory()));
    trade.setTradeStatus(TradeStatus.valueOf(dto.getTradeStatus()));
    trade.setSeller(sellerRef);


    return tradeRepository.save(trade).getId();
  }

  @Override
  @Transactional(readOnly = true)
  public TradeReadDTO read(Long id) {
    Trade trade = tradeRepository.findByIdWithImages(id)
        .orElseThrow(() -> new IllegalArgumentException("trade not found"));

    List<String> imageUrls = (trade.getImages() != null)
        ? trade.getImages().stream().map(TradeImage::getImageUrl).toList()
        : List.of();

    String rd = (trade.getRegDate() != null) ? trade.getRegDate().format(DT) : null;
    String md = (trade.getModDate() != null) ? trade.getModDate().format(DT) : null;

    return TradeReadDTO.builder()
        .id(trade.getId())
        .title(trade.getTitle())
        .description(trade.getDescription())
        .price(trade.getPrice())
        .thumbnail(trade.getThumbnail())
        .tradeCategory(trade.getTradeCategory().name())
        .tradeStatus(trade.getTradeStatus().name())
        .sellerId(trade.getSeller() != null ? trade.getSeller().getId() : null)
        .regDate(rd)
        .modDate(md)
        .imageUrls(imageUrls)
        .build();
  }

  @Override
  @Transactional
  public void modify(Long id, TradeUploadDTO dto, Long sellerId) {
    Trade trade = tradeRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("trade not found"));

    if (trade.getSeller() == null || !trade.getSeller().getId().equals(sellerId)) {
      throw new IllegalStateException("no permission");
    }

    trade.modify(
        dto.getTitle(),
        dto.getDescription(),
        dto.getPrice(),
        TradeCategory.valueOf(dto.getTradeCategory()),
        TradeStatus.valueOf(dto.getTradeStatus()),
        dto.getThumbnail()
    );
  }

  @Override
  @Transactional
  public void remove(Long id, Long sellerId) {
    Trade trade = tradeRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("trade not found"));

    if (trade.getSeller() == null || !trade.getSeller().getId().equals(sellerId)) {
      throw new IllegalStateException("no permission");
    }

    tradeRepository.delete(trade);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TradeDTO> listMain(String category, String keyword) {

    TradeCategory cat = null;
    if (category != null && !category.isBlank()) {
      try {
        cat = TradeCategory.valueOf(category.trim());
      } catch (IllegalArgumentException ignore) {
      }
    }
    String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

    String jpql = """
      select t
      from Trade t
      where t.tradeStatus <> :completed
        and (:cat is null or t.tradeCategory = :cat)
        and (:kw is null or t.title like concat('%', :kw, '%')
                     or t.description like concat('%', :kw, '%'))
      order by t.regDate desc
      """;

    TypedQuery<Trade> query = entityManager.createQuery(jpql, Trade.class)
        .setParameter("completed", TradeStatus.COMPLETED)
        .setParameter("cat", cat)
        .setParameter("kw", kw);

    List<Trade> list = query.getResultList();

    // 3) 엔티티 -> DTO 매핑
    return list.stream()
        .map(t -> TradeDTO.builder()
            .id(t.getId())
            .title(t.getTitle())
            .description(t.getDescription())
            .price(t.getPrice())
            .thumbnail(t.getThumbnail())
            .tradeCategory(t.getTradeCategory() != null ? t.getTradeCategory().name() : null)
            .tradeStatus(t.getTradeStatus() != null ? t.getTradeStatus().name() : null)
            .sellerId(t.getSeller() != null ? t.getSeller().getId() : null)
            .regDate(t.getRegDate() != null ? t.getRegDate().toString() : null)
            .modDate(t.getModDate() != null ? t.getModDate().toString() : null)
            .build())
        .toList();
  }
}