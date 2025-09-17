

// 2. AdminCafeRepository.java - 카페 데이터 처리
package com.example.together.repository.manager;

import com.example.together.dto.manager.AdminCafeDTO;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdminCafeRepository {

    // 전체 카페 목록
    List<AdminCafeDTO> findAllCafes();

    // 카페 이름으로 검색
    List<AdminCafeDTO> findByNameContaining(String cafeName);

    // 카페 ID로 조회
    AdminCafeDTO findByCafeId(Long cafeId);

    // 카페 상태 변경
    int updateCafeStatus(Long cafeId, String status);

    // 카페 삭제
    int deleteCafe(Long cafeId);

    // 상태별 카페 목록
    List<AdminCafeDTO> findByStatus(String status);

    // 카페 운영자 변경
    int updateCafeOwner(Long cafeId, Long newOwnerId);

    // 전체 카페 수 조회
    int getTotalCafeCount();

    // 활성 카페 수 조회
    int getActiveCafeCount();
}
