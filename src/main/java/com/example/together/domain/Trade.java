package com.example.together.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;



@Entity
@Table(name = "trade")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Trade {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 제목
  @Column(nullable = false, length = 255)
  private String title;

  // 본문(내용)
  @Lob
  @Column(name = "content")
  private String content;

  // 설명(별도 칼럼이 있다면 NOT NULL 회피 위해 기본값이라도 넣어주기)
  @Column(name = "description")
  private String description;

  // 가격
  @Column
  private BigDecimal price;

  // 상태
  public enum Status { FOR_SALE, RESERVED, SOLD_OUT }
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Status status = Status.FOR_SALE;

  // 판매자 닉네임
  @Column(name = "seller_nickname", length = 100)
  private String sellerNickname;

  // 썸네일 (nullable 허용 권장; NOT NULL이면 컨트롤러에서 기본값 세팅)
  @Column(name = "thumbnail", length = 1024)
  private String thumbnail;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime regdate;

  @UpdateTimestamp
  private LocalDateTime moddate;
}




