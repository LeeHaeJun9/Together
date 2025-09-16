package com.example.together.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 1024)
  private String imageUrl;

  private Integer sortOrder;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Trade trade;
}
