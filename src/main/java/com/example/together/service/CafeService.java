package com.example.together.service;

import com.example.together.domain.CafeApplication;
import com.example.together.dto.cafe.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CafeService {
    // 사용자가 카페 개설을 신청하는 메서드
    CafeApplicationResponseDTO applyForCafe(CafeCreateRequestDTO requestDTO, Long userId);

    // 관리자가 카페 개설 신청을 승인하는 메서드
    CafeApplicationResponseDTO approveCafe(Long applicationId, Long adminId);

    // 관리자가 카페 개설 신청을 거절하는 메서드
    CafeApplicationResponseDTO rejectCafe(Long applicationId);

    // 카페 생성 (관리자용)
    CafeResponseDTO createCafe(CafeCreateRequestDTO requestDTO, Long userId);

    // 특정 ID의 카페 조회
//    CafeResponseDTO getCafeById(Long cafeId);

    CafeResponseDTO getCafeById(Long cafeId, Long userId);

    // 검토 대기 중인 모든 신청 목록 조회
    List<CafeApplication> getPendingApplications();

    // 특정 신청 상세 정보 조회
    CafeApplicationResponseDTO getCafeApplicationDetail(Long applicationId);


    CafeResponseDTO registerCafeAfterApproval(Long applicationId, MultipartFile cafeImage, MultipartFile cafeThumbnail, Long userId);

    List<CafeResponseDTO> getAllCafes();

    void updateCafe(Long cafeId, CafeUpdateDTO updateDTO, Long userId) throws IllegalAccessException;

    void deleteCafe(Long cafeId, Long userId) throws IllegalAccessException;

    // 카페 가입 신청
    void sendJoinRequest(Long cafeId, Long userId);

    // 대기 중인 가입 신청 목록 조회
    List<CafeJoinRequestResponseDTO> getPendingJoinRequests(Long cafeId);

    // 가입 신청 승인
    Long approveJoinRequest(Long requestId, Long adminId);

    // 가입 신청 거절
    void rejectJoinRequest(Long requestId, Long adminId);

    // 사용자가 카페 운영자인지 확인하는 메서드
    boolean isCafeOwner(Long cafeId, Long userId);
}
