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
@Table(name = "chat_message")
public class ChatMessage {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "chat_room_id")
  private Long chatRoomId;

  @Column(name = "sender_id")
  private Long senderId;

  // DB 컬럼명이 message 이므로 name 지정
  @Column(name = "message", length = 2000, nullable = false)
  private String content;

  @CreationTimestamp
  @Column(name = "regdate")
  private LocalDateTime regDate;

  @UpdateTimestamp
  @Column(name = "moddate")
  private LocalDateTime modDate;
}
