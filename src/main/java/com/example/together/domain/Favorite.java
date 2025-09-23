package com.example.together.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "favorite",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_favorite_trade_user",
        columnNames = {"trade_id", "user_id"}
    )
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Favorite {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "trade_id", nullable = false)
  private Trade trade;

  // ⚠️ FK(Long)만 유지합니다. 문자열 컬럼 매핑 금지!
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @CreationTimestamp
  private LocalDateTime createdAt;
}
