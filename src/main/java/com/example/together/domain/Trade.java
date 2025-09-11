package com.example.together.domain;

import jakarta.persistence.*;
import lombok.*;

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

    private String title;

    private String description;

    private Integer price;

    private String thumbnail;

    @Enumerated(EnumType.STRING)
    private TradeStatus tradeStatus = TradeStatus.FOR_SALE;

    @Enumerated(EnumType.STRING)
    private TradeCategory tradeCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    private User seller;

}
