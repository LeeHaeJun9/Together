package com.example.together.service;

import com.example.together.dto.cafe.CafeApplicationResponseDTO;
import com.example.together.dto.cafe.CafeCreateRequestDTO;
import com.example.together.dto.cafe.CafeResponseDTO;
import org.springframework.transaction.annotation.Transactional;

public interface CafeService {
    // 사용자가 카페 개설을 신청하는 메서드
    CafeApplicationResponseDTO applyForCafe(CafeCreateRequestDTO requestDTO, Long userId);

    // 관리자가 카페 개설 신청을 승인하는 메서드
    CafeResponseDTO approveCafe(Long applicationId, Long adminId);

    @Transactional
    CafeResponseDTO createCafe(CafeCreateRequestDTO requestDTO, Long userId);

    // 카페 정보 조회 메서드
    CafeResponseDTO getCafeById(Long cafeId);
}
