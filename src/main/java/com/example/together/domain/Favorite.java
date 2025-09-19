package com.example.together.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "favorite",
    // DB에 유니크가 없다면 JPA 레벨에서라도 중복 방지 (선택)
    uniqueConstraints = @UniqueConstraint(name = "uk_favorite_user_trade", columnNames = {"user_id", "trade_id"})
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Favorite {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @UpdateTimestamp
  private LocalDateTime moddate;

  @CreationTimestamp
  private LocalDateTime regdate;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "trade_id", foreignKey = @ForeignKey(name = "FKn9ec7rhkkwvk3jqam42hitvvi"))
  private Trade trade;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "FKh3f2dg11ibnht4fvnmx60jcif"))
  private User user;
}