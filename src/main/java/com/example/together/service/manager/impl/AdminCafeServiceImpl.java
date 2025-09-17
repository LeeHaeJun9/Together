// 2. AdminCafeServiceImpl.java - 카페 관리 기능 구현
package com.example.together.service.manager.impl;

import com.example.together.dto.manager.AdminCafeDTO;
import com.example.together.repository.manager.AdminCafeRepository;
import com.example.together.service.manager.AdminCafeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AdminCafeServiceImpl implements AdminCafeService {

    @Autowired
    private AdminCafeRepository adminCafeRepository;

    @Override
    public List<AdminCafeDTO> getAllCafes() {
        return adminCafeRepository.findAllCafes();
    }

    @Override
    public List<AdminCafeDTO> searchCafesByName(String cafeName) {
        // 카페 이름이 비어있으면 전체 목록 반환
        if (cafeName == null || cafeName.trim().isEmpty()) {
            return getAllCafes();
        }
        return adminCafeRepository.findByNameContaining(cafeName);
    }

    @Override
    public AdminCafeDTO getCafeById(Long cafeId) {
        return adminCafeRepository.findByCafeId(cafeId);
    }

    @Override
    public boolean changeCafeStatus(Long cafeId, String status) {
        // 유효한 상태인지 확인
        if (!isValidCafeStatus(status)) {
            return false;
        }

        int result = adminCafeRepository.updateCafeStatus(cafeId, status);
        return result > 0;
    }

    @Override
    public boolean deleteCafe(Long cafeId) {
        int result = adminCafeRepository.deleteCafe(cafeId);
        return result > 0;
    }

    @Override
    public List<AdminCafeDTO> getCafesByStatus(String status) {
        return adminCafeRepository.findByStatus(status);
    }

    @Override
    public boolean changeCafeOwner(Long cafeId, Long newOwnerId) {
        int result = adminCafeRepository.updateCafeOwner(cafeId, newOwnerId);
        return result > 0;
    }

    // 유효한 카페 상태인지 확인하는 메서드
    private boolean isValidCafeStatus(String status) {
        return "ACTIVE".equals(status) ||
                "INACTIVE".equals(status) ||
                "SUSPENDED".equals(status);
    }
}
