package com.example.together.trade;

import com.example.together.domain.*;
import com.example.together.dto.trade.TradeDTO;
import com.example.together.dto.trade.TradeReadDTO;
import com.example.together.dto.trade.TradeUploadDTO;
import com.example.together.repository.TradeRepository;
import com.example.together.service.trade.TradeService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;



import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
@Log4j2
class TradeServiceTest {

  @Autowired
  private TradeService tradeService;

  @PersistenceContext
  private EntityManager entityManager;

  private Long sellerId; // 더미 유저 PK

  private Long insertDummyUserAndGetId() {
    entityManager.createNativeQuery("""
      INSERT INTO `user`
        (cafe_role, email, name, nickname, password, phone, status, system_role, user_id, regdate, moddate)
      VALUES
        ('CAFE_USER','tester01@example.com','홍길동','tester1','1234','010-1234-5678','ACTIVE','USER','tester01', NOW(), NOW())
      """)
        .executeUpdate();

    Long id = ((Number) entityManager.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult()).longValue();
    log.info("[SETUP] dummy user inserted: id={}", id);
    return id;
  }

  @BeforeEach
  void setUp() {
    // 테스트마다 sellerId 새로 확보 (롤백되므로 서로 영향 없음)
    sellerId = insertDummyUserAndGetId();
    assertThat(sellerId).isNotNull();
  }

  @Test
  void testRegister() {
    log.info("-----------Register Test-----------");

    TradeUploadDTO dto = TradeUploadDTO.builder()
        .title("샘플 상품")
        .description("테스트용 설명")
        .price(50_000)
        .thumbnail("/img/sample.jpg")
        .tradeCategory(TradeCategory.ELECTRONICS.name())
        .tradeStatus(TradeStatus.FOR_SALE.name())
        .build();

    log.info("등록 DTO = {}", dto);
    Long id = tradeService.register(dto, sellerId);
    log.info("거래 등록 완료 -> tradeId={}", id);

    assertThat(id).isNotNull();
    assertThat(id).isPositive();
    log.info("tradeId가 정상 생성됨 (id={})", id);

  }

  @Test
  void testModifyAndRemove() {
    log.info("---------------modifyAndRemove---------------");

    Long id = tradeService.register(
        TradeUploadDTO.builder()
            .title("원래 제목")
            .description("원래 설명")
            .price(10000)
            .thumbnail("/img/old.jpg")
            .tradeCategory(TradeCategory.BOOKS.name())
            .tradeStatus(TradeStatus.FOR_SALE.name())
            .build(),
        sellerId
    );
    log.info("수정/삭제 대상 등록 tradeId={}", id);

    tradeService.modify(
        id,
        TradeUploadDTO.builder()
            .title("수정된 제목")
            .description("수정된 설명")
            .price(12000)
            .thumbnail("/img/new.jpg")
            .tradeCategory(TradeCategory.BOOKS.name())
            .tradeStatus(TradeStatus.RESERVED.name())
            .build(),
        sellerId
    );
    log.info("거래 수정 완료 -> tradeId={}", id);

    tradeService.remove(id, sellerId);
    log.info("거래 삭제 완료 -> tradeId={}", id);

  }
}




