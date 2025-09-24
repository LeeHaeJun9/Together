package com.example.together.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trade_image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // create table 의 image_url
  @Column(name = "image_url", nullable = false, length = 500)
  private String imageUrl;

  // create table 의 sort_order
  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "trade_id")
  private Trade trade;

  @Column(name = "original_name")
  private String originalName;

  @Column(name = "stored_name")
  private String storedName;
}
