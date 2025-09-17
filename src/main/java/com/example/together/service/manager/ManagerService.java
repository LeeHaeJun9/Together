//package com.example.together.service.manager;
//
//import com.example.together.dto.manager.ManagerDTO;
//import com.example.together.dto.manager.ManagerLoginDTO;
//
//import java.util.List;
//
//public interface ManagerService {
//
//    // 로그인
//    ManagerDTO login(ManagerLoginDTO loginDTO);
//
//    // 관리자 정보 조회
//    ManagerDTO getManagerInfo(Long managerId);
//
//    // 관리자 목록 조회
//    List<ManagerDTO> getAllManagers();
//
//    // 관리자 등록
//    boolean registerManager(ManagerDTO managerDTO);
//
//    // 관리자 정보 수정
//    boolean updateManager(ManagerDTO managerDTO);
//
//    // 관리자 삭제
//    boolean deleteManager(Long managerId);
//
//    // 관리자 상태 변경 (활성/비활성)
//    boolean changeManagerStatus(Long managerId, String status);
//
////    // 대시보드 통계 정보
////    DashboardDTO getDashboardInfo();
//}
