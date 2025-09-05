package com.example.together.repository;

import com.example.together.domain.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    boolean existsByZipcode(String zipcode); // 중복 저장 방지용

    Optional<Address> findByZipcode(String zipcode);
}