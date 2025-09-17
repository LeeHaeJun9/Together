package com.example.together.service.manager;

import com.example.together.dto.manager.AdminCafeDTO;

import java.util.List;

public interface AdminCafeService {
    // 전체 카페 목록 조회
    List<AdminCafeDTO> getAllCafes();

    // 카페 검색 (이름으로)
    List<AdminCafeDTO> searchCafesByName(String cafeName);

    // 카페 상세 정보 조회
    AdminCafeDTO getCafeById(Long cafeId);

    // 카페 상태 변경 (활성/비활성/정지)
    boolean changeCafeStatus(Long cafeId, String status);

    // 카페 삭제
    boolean deleteCafe(Long cafeId);

//    // 카페 통계 정보
//    CafeStatisticsDTO getCafeStatistics(Long cafeId);

    // 상태별 카페 목록
    List<AdminCafeDTO> getCafesByStatus(String status);

    // 카페 운영자 변경
    boolean changeCafeOwner(Long cafeId, Long newOwnerId);
}
