//package com.example.together.trade;
//
//import com.example.together.domain.*;
//import com.example.together.dto.trade.*;
//import com.example.together.repository.TradeRepository;
//import com.example.together.service.trade.TradeService;
//import com.example.together.service.trade.TradeServiceImpl;
//import lombok.extern.log4j.Log4j2;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.context.TestPropertySource;
//
//
//import java.math.BigDecimal;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//@Log4j2
//@DataJpaTest
//@Import(TradeServiceImpl.class)
//@TestPropertySource(properties = {
//    "spring.jpa.hibernate.ddl-auto=create-drop",
//    "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
//    "spring.jpa.show-sql=true",
//    "spring.jpa.properties.hibernate.format_sql=true",
//    "logging.level.com.example.together=DEBUG",
//    "logging.level.org.hibernate.SQL=DEBUG",
//    "logging.level.org.hibernate.orm.jdbc.bind=TRACE"
//})
//class TradeServiceSimpleTest {
//
//  @Autowired private TradeService tradeService;
//  @Autowired private TradeRepository tradeRepository;
//
//  @Test
//  @DisplayName("등록 → 읽기")
//  void register_and_read() {
//    log.info("=== register_and_read ===");
//    Long id = tradeService.register(
//        TradeSaveRequest.builder()
//            .title("아이폰 15")
//            .content("상태 A, 영수증 O")
//            .price(new BigDecimal("990000"))
//            .status("판매중")
//            .build(),
//        /* sellerPk */ 1L, /* sellerNickname */ "판매자"
//    );
//
//    TradeDTO dto = tradeService.read(id);
//    log.info("savedId={}, readTitle={}, favCount={}", id, dto.getTitle(), dto.getFavoriteCount());
//
//    assertThat(id).isNotNull();
//    assertThat(dto).isNotNull();
//    assertThat(dto.getTitle()).isEqualTo("아이폰 15");
//  }
//
//  @Test
//  @DisplayName("수정 → 삭제")
//  void modify_then_remove() {
//    log.info("=== modify_then_remove ===");
//    Long id = tradeRepository.save(Trade.builder()
//        .title("원본 제목")
//        .content("원본 내용")
//        .price(new BigDecimal("1000"))
//        .status("판매중")
//        .sellerNickname("닉")
//        .build()).getId();
//
//    tradeService.modify(id, TradeUpdateRequest.builder()
//        .title("수정 제목")
//        .content("수정 내용")
//        .price(new BigDecimal("2000"))
//        .status("예약")
//        .build(), /* editorPk */ 1L);
//
//    Trade after = tradeRepository.findById(id).orElseThrow();
//    log.info("afterModify: title={}, status={}, price={}", after.getTitle(), after.getStatus(), after.getPrice());
//    assertThat(after.getTitle()).isEqualTo("수정 제목");
//    assertThat(after.getStatus()).isEqualTo("예약");
//
//    tradeService.remove(id, /* requesterPk */ 1L);
//    boolean exists = tradeRepository.findById(id).isPresent();
//    log.info("existsAfterDelete={}", exists);
//    assertThat(exists).isFalse();
//  }
//}
//
//
//
//
