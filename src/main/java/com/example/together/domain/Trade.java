package com.example.together.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "trade")
public class Trade {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;


  private String title;


  @Column(length = 2000)
  private String description;


  private Long price;

  private String sellerNickname;

  @Column(name = "seller_user_id")
  private Long sellerUserId;

  private String status;

  private String thumbnail;

  @Enumerated(EnumType.STRING)
  @Column(name = "trade_category",  nullable = false)
  private TradeCategory category;

  @CreationTimestamp
  private LocalDateTime regdate;
  @UpdateTimestamp
  private LocalDateTime moddate;
}
