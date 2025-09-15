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

  private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yy-MM-dd");
  private final TradeRepository tradeRepository;
  private final TradeImageRepository tradeImageRepository;

  @PersistenceContext
  private EntityManager entityManager;

  // register/modify/remove

  // register
  @Override
  @Transactional
  public Long register(TradeUploadDTO dto, Long sellerId) {
    // User가 아직 완성 전이면 최소 엔티티라도 필요합니다(컴파일/런타임 안정화).
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

  // 게시물 수정(본인 게시물만)
  @Override
  @Transactional
  public void modify(Long id, TradeUploadDTO dto, Long sellerId) {
    Trade trade = tradeRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("거래 조회 불가능"));

    // 권한 체크
    if (!trade.getSeller().getId().equals(sellerId)) {
      throw new IllegalStateException("권한이 없습니다");
    }
    trade.setTitle(dto.getTitle());
    trade.setDescription(dto.getDescription());
    trade.setPrice(dto.getPrice());
    trade.setThumbnail(dto.getThumbnail());
    trade.setTradeCategory(TradeCategory.valueOf(dto.getTradeCategory()));
    trade.setTradeStatus(TradeStatus.valueOf(dto.getTradeStatus()));

  }

  // 게시물 삭제(본인 게시물만)
  @Override
  @Transactional
  public void remove(Long id, Long sellerId) {
    Trade trade = tradeRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("거래 조회 불가능"));

    if (!trade.getSeller().getId().equals(sellerId)) {
      throw new IllegalStateException("권한이 없습니다");
    }

    tradeRepository.delete(trade);
  }


  @Override
  @Transactional(readOnly = true)
  public List<TradeDTO> listMain(String category, String keyword) {
    String where = " where Trade.tradeStatus <> :completed ";
    if (category != null && !category.isBlank()) where += " and t.tradeCategory = :category ";
    if (keyword  != null && !keyword.isBlank())  where += " and lower(t.title) like lower(concat('%', :kw, '%')) ";

    String jpql = "select t from Trade t" + where + " order by t.regDate desc";

    TypedQuery<Trade> q = entityManager.createQuery(jpql, Trade.class)
        .setParameter("completed", TradeStatus.COMPLETED);

    if (category != null && !category.isBlank())
      q.setParameter("category", TradeCategory.valueOf(category));
    if (keyword != null && !keyword.isBlank())
      q.setParameter("kw", keyword);

    return q.getResultList().stream().map(this::toDTO).toList();
  }

  // 상세조회

  @Override
  @Transactional(readOnly = true)
  public TradeReadDTO read(Long id) {
    Trade trade = tradeRepository.findByIdWithImages(id)
        .orElseThrow(() -> new IllegalArgumentException("trade not found"));

    var imageUrls = trade.getImages() != null
        ? trade.getImages().stream().map(TradeImage::getImageUrl).toList(): List.<String>of();

    String rd = trade.getRegDate() != null ? trade.getRegDate().format(DT) : null;
    String md = trade.getModDate() != null ? trade.getModDate().format(DT) : null;

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

  private TradeDTO toDTO(Trade trade) {

    String rd = trade.getRegDate() != null ? trade.getRegDate().format(DT) : null;
    String md = trade.getModDate() != null ? trade.getModDate().format(DT) : null;

    return TradeDTO.builder()
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
        .build();
  }
}