package com.example.together.service;

import com.example.together.domain.CafeApplication;
import com.example.together.dto.cafe.CafeApplicationResponseDTO;
import com.example.together.dto.cafe.CafeCreateRequestDTO;
import com.example.together.dto.cafe.CafeResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CafeService {
    // 사용자가 카페 개설을 신청하는 메서드
    CafeApplicationResponseDTO applyForCafe(CafeCreateRequestDTO requestDTO, Long userId);

    // 관리자가 카페 개설 신청을 승인하는 메서드
    CafeResponseDTO approveCafe(Long applicationId, Long adminId);

    // 관리자가 카페 개설 신청을 거절하는 메서드
    CafeApplicationResponseDTO rejectCafe(Long applicationId);

    // 카페 생성 (관리자용)
    CafeResponseDTO createCafe(CafeCreateRequestDTO requestDTO, Long userId);

    // 특정 ID의 카페 조회
    CafeResponseDTO getCafeById(Long cafeId);

    // 검토 대기 중인 모든 신청 목록 조회
    List<CafeApplication> getPendingApplications();

    // 특정 신청 상세 정보 조회
    CafeApplicationResponseDTO getCafeApplicationDetail(Long applicationId);
}
