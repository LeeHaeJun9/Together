package com.example.together.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(nullable = false, length = 255)
  private String description;

  @Column(nullable = false)
  private Integer price;

  @Column(nullable = false, length = 255)
  private String thumbnail;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TradeStatus tradeStatus = TradeStatus.FOR_SALE;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private TradeCategory tradeCategory;


  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private User seller;

  @OneToMany(mappedBy = "trade", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("sortOrder ASC, id ASC")
  @Builder.Default
  private List<TradeImage> images = new ArrayList<>();

  public void modify(String title,
                     String description,
                     Integer price,
                     TradeCategory category,
                     TradeStatus status,
                     String thumbnail) {
    this.title = title;
    this.description = description;
    this.price = price;
    this.tradeCategory = category;
    this.tradeStatus = status;
    this.thumbnail = thumbnail;
  }

  public void clearImages() {
    if (this.images != null) this.images.clear();
  }

  public void addImage(String imageUrl, Integer sortOrder) {
    TradeImage img = TradeImage.builder()
        .imageUrl(imageUrl)
        .sortOrder(sortOrder)
        .trade(this)
        .build();
    this.images.add(img);
  }
}
