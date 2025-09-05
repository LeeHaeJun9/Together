package com.example.together.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String zipcode;       // 우편번호

    @Column(nullable = false, length = 50)
    private String city;          // 시/도

    @Column(nullable = false, length = 50)
    private String district;      // 시/군/구

    @Column(length = 50)
    private String neighborhood;  // 읍/면/동

    @Column(length = 100)
    private String street;        // 도로명
}