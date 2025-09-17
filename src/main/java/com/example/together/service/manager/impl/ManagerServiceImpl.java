//// 1. ManagerServiceImpl.java - 관리자 기능 구현
//package com.example.together.service.manager.impl;
//
//import com.example.together.dto.manager.ManagerDTO;
//import com.example.together.dto.manager.ManagerLoginDTO;
//import com.example.together.repository.manager.ManagerRepository;
//import com.example.together.service.manager.ManagerService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import java.util.List;
//
//@Service
//public class ManagerServiceImpl implements ManagerService {
//
//    @Autowired
//    private ManagerRepository managerRepository;
//
//    @Override
//    public ManagerDTO login(ManagerLoginDTO loginDTO) {
//        // 1. 이메일로 관리자 찾기
//        ManagerDTO manager = managerRepository.findByEmail(loginDTO.getEmail());
//
//        // 2. 관리자가 없으면 null 반환
//        if (manager == null) {
//            return null;
//        }
//
//        // 3. 비밀번호 확인 (실제로는 암호화된 비밀번호와 비교해야 함)
//        if (manager.getPassword().equals(loginDTO.getPassword())) {
//            return manager;  // 로그인 성공
//        }
//
//        return null;  // 로그인 실패
//    }
//
//    @Override
//    public ManagerDTO getManagerInfo(Long managerId) {
//        return managerRepository.findById(managerId);
//    }
//
//    @Override
//    public List<ManagerDTO> getAllManagers() {
//        return managerRepository.findAll();
//    }
//
//    @Override
//    public boolean registerManager(ManagerDTO managerDTO) {
//        // 1. 이메일 중복 체크
//        int emailCount = managerRepository.countByEmail(managerDTO.getEmail());
//        if (emailCount > 0) {
//            return false;  // 이미 존재하는 이메일
//        }
//
//        // 2. 관리자 등록
//        int result = managerRepository.insertManager(managerDTO);
//        return result > 0;  // 성공하면 1 이상 반환
//    }
//
//    @Override
//    public boolean updateManager(ManagerDTO managerDTO) {
//        int result = managerRepository.updateManager(managerDTO);
//        return result > 0;
//    }
//
//    @Override
//    public boolean deleteManager(Long managerId) {
//        int result = managerRepository.deleteManager(managerId);
//        return result > 0;
//    }
//
//    @Override
//    public boolean changeManagerStatus(Long managerId, String status) {
//        int result = managerRepository.updateManagerStatus(managerId, status);
//        return result > 0;
//    }
//}
