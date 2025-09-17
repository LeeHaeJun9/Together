// 1. ManagerRepository.java - 관리자 데이터 처리
package com.example.together.repository.manager;

import com.example.together.dto.manager.ManagerDTO;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ManagerRepository {

    // 로그인 - 이메일로 관리자 찾기
    ManagerDTO findByEmail(String email);

    // 관리자 정보 조회
    ManagerDTO findById(Long managerId);

    // 전체 관리자 목록
    List<ManagerDTO> findAll();

    // 관리자 등록
    int insertManager(ManagerDTO managerDTO);

    // 관리자 정보 수정
    int updateManager(ManagerDTO managerDTO);

    // 관리자 삭제
    int deleteManager(Long managerId);

    // 관리자 상태 변경
    int updateManagerStatus(Long managerId, String status);

    // 이메일 중복 체크
    int countByEmail(String email);
}
