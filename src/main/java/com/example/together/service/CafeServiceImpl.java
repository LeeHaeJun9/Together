package com.example.together.service;

import com.example.together.domain.*;
import com.example.together.dto.cafe.CafeApplicationResponseDTO;
import com.example.together.dto.cafe.CafeCreateRequestDTO;
import com.example.together.dto.cafe.CafeResponseDTO;
import com.example.together.repository.CafeApplicationRepository;
import com.example.together.repository.CafeRepository;
import com.example.together.repository.MembershipRepository;
import com.example.together.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CafeServiceImpl implements CafeService {

    private final CafeRepository cafeRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final CafeApplicationRepository  cafeApplicationRepository;

    @Transactional
    @Override
    public CafeApplicationResponseDTO applyForCafe(CafeCreateRequestDTO requestDTO, Long userId) {
        // 1. 중복된 이름의 카페가 있는지 확인 (승인 전에 미리 체크)
        if (cafeRepository.findByName(requestDTO.getName()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 카페 이름입니다.");
        }

        // 2. 신청자 User 엔티티 조회
        User applicant = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // **3. 카테고리 유효성 검증 및 Enum 변환**
        CafeCategory category;
        try {
            category = CafeCategory.valueOf(requestDTO.getCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("카페 카테고리를 찾을 수 없습니다.");
        }

        // 4. CafeApplication 엔티티 생성 (상태는 PENDING)
        CafeApplication application = CafeApplication.builder()
                .name(requestDTO.getName())
                .description(requestDTO.getDescription())
                .category(category)
                .applicant(applicant)
                .status(CafeApplicationStatus.PENDING)
                .build();

        // 5. 신청 정보를 데이터베이스에 저장
        CafeApplication savedApplication = cafeApplicationRepository.save(application);

        // 6. 응답 DTO 반환
        return new CafeApplicationResponseDTO(
                savedApplication.getId(),
                savedApplication.getName(),
                savedApplication.getDescription(),
                savedApplication.getCategory().name(),
                savedApplication.getStatus(),
                savedApplication.getRegDate(),
                savedApplication.getApplicant().getId());
    }

    @Transactional
    @Override
    public CafeResponseDTO approveCafe(Long applicationId, Long adminId) {
        // 1. 관리자 권한 확인 (실제로는 JWT 토큰 등으로 검증)
        // ...

        // 2. 승인할 카페 개설 신청 정보 조회 및 검증
        CafeApplication application = cafeApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카페 신청입니다."));
        if (application.getStatus() != CafeApplicationStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신청입니다.");
        }

        // 3. CafeApplication 정보를 기반으로 실제 Cafe 엔티티 생성
        User applicant = application.getApplicant();
        Cafe newCafe = Cafe.builder()
                .name(application.getName())
                .description(application.getDescription())
                .category(application.getCategory())
                .owner(applicant)
                .build();

        // 4. Cafe 엔티티를 데이터베이스에 저장
        Cafe savedCafe = cafeRepository.save(newCafe);

        // 5. 신청자에게 CAFE_ADMIN 역할을 가진 Membership 부여
        Membership ownerMembership = Membership.builder()
                .user(applicant)
                .cafe(savedCafe)
                .role(CafeRole.CAFE_ADMIN)
                .build();

        // 6. Membership 엔티티를 데이터베이스에 저장
        membershipRepository.save(ownerMembership);

        // 7. CafeApplication의 상태를 APPROVED로 변경
        application.approve();
        // 8. 상태 변경을 데이터베이스에 저장
        cafeApplicationRepository.save(application);

        // 9. 생성된 카페 정보를 DTO로 반환
        return new CafeResponseDTO(
                savedCafe.getId(),
                savedCafe.getName(),
                savedCafe.getDescription(),
                savedCafe.getCategory().name(),
                savedCafe.getRegDate(),
                savedCafe.getOwner().getId(),
                1); // 소유자 1명으로 시작
    }

    @Transactional
    @Override
    public CafeApplicationResponseDTO rejectCafe(Long applicationId) {
        CafeApplication application = cafeApplicationRepository.findByIdWithApplicant(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카페 신청입니다."));

        if (application.getStatus() != CafeApplicationStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신청입니다.");
        }

        application.reject();
        // **상태 변경을 데이터베이스에 저장**
        cafeApplicationRepository.save(application);

        return new CafeApplicationResponseDTO(application);
    }

    @Transactional
    @Override
    public CafeResponseDTO createCafe(CafeCreateRequestDTO requestDTO, Long userId) {
        // 1. 비즈니스 로직: 중복된 카페 이름이 있는지 확인
        if (cafeRepository.findByName(requestDTO.getName()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 카페 이름입니다.");
        }

        // 2. User 엔티티 조회 및 검증
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // **3. DTO의 문자열을 Enum으로 변환
        CafeCategory category;
        try {
            category = CafeCategory.valueOf(requestDTO.getCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("카페 카테고리를 찾을 수 없습니다.");
        }

        // 4. Cafe 엔티티 생성
        Cafe cafe = Cafe.builder()
                .name(requestDTO.getName())
                .description(requestDTO.getDescription())
                .category(category)
                .owner(owner)
                .build();

        // 5. 엔티티를 데이터베이스에 저장
        Cafe savedCafe = cafeRepository.save(cafe);

        // 6. 응답 DTO 반환
        return new CafeResponseDTO(
                savedCafe.getId(),
                savedCafe.getName(),
                savedCafe.getDescription(),
                savedCafe.getCategory().name(),
                savedCafe.getRegDate(),
                savedCafe.getOwner().getId(),
                1 // 생성자 본인 포함 1명으로 시작
        );
    }

    @Override
    public CafeResponseDTO getCafeById(Long cafeId) {
        // 1. Cafe 엔티티 조회
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카페입니다."));

        // 2. 동적으로 회원 수 계산
        Integer memberCount = membershipRepository.countByCafe(cafe);

        // 3. 응답 DTO 반환
        return new CafeResponseDTO(
                cafe.getId(),
                cafe.getName(),
                cafe.getDescription(),
                cafe.getCategory().name(),
                cafe.getRegDate(),
                cafe.getOwner().getId(),
                memberCount
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CafeApplication> getPendingApplications() {
        // 반환 타입을 DTO 리스트가 아닌 엔티티 리스트로 변경
        return cafeApplicationRepository.findByStatusWithApplicant(CafeApplicationStatus.PENDING);
    }

    @Override
    public CafeApplicationResponseDTO getCafeApplicationDetail(Long applicationId) {
        CafeApplication application = cafeApplicationRepository.findByIdWithApplicant(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신청입니다."));

        return new CafeApplicationResponseDTO(application);
    }

    @Transactional
    public CafeResponseDTO registerCafeAfterApproval(
            Long applicationId,
            MultipartFile cafeImage,
            MultipartFile cafeThumbnail,
            Long userId) {
        // This logic is the same as before
        CafeApplication application = cafeApplicationRepository.findByIdWithApplicant(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신청서를 찾을 수 없습니다."));

        if (application.getStatus() != CafeApplicationStatus.APPROVED) {
            throw new IllegalStateException("승인된 카페 신청만 등록할 수 있습니다.");
        }

        if (!application.getApplicant().getId().equals(userId)) {
            throw new IllegalStateException("해당 신청서의 소유자만 카페를 등록할 수 있습니다.");
        }

        String cafeImageUrl = "/images/" + cafeImage.getOriginalFilename();
        String cafeThumbnailUrl = "/thumbnails/" + cafeThumbnail.getOriginalFilename();


        Cafe newCafe = Cafe.builder()
                .name(application.getName())
                .description(application.getDescription())
                .category(application.getCategory())
                .owner(application.getApplicant())
                .cafeImage(cafeImageUrl)
                .cafeThumbnail(cafeThumbnailUrl)
                .memberCount(1) // Starts with 1 owner
                .build();


        cafeRepository.save(newCafe);

        // 최종 등록 후, 신청서를 데이터베이스에서 삭제
        cafeApplicationRepository.delete(application);

        return new CafeResponseDTO(
                newCafe.getId(),
                newCafe.getName(),
                newCafe.getDescription(),
                newCafe.getCategory().name(),
                newCafe.getRegDate(),
                newCafe.getOwner().getId(),
                newCafe.getMemberCount()
        );
    }
}
