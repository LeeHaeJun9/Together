package com.example.together.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name = "chat_room")
public class ChatRoom {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "buyer_id")
  private Long buyerId;

  @Column(name = "seller_id")
  private Long sellerId;

  @Column(name = "trade_id")
  private Long tradeId;

  @CreationTimestamp
  @Column(name = "regdate")
  private LocalDateTime regDate;

  @UpdateTimestamp
  @Column(name = "moddate")
  private LocalDateTime modDate;
}
