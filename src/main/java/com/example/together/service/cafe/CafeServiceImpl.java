package com.example.together.service.cafe;

import com.example.together.domain.*;
import com.example.together.dto.PageRequestDTO;
import com.example.together.dto.PageResponseDTO;
import com.example.together.dto.cafe.*;
import com.example.together.dto.calendar.CalendarEventDTO;
import com.example.together.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Log4j2
public class CafeServiceImpl implements CafeService {

    private final CafeRepository cafeRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final CafeApplicationRepository  cafeApplicationRepository;
    private final CafeJoinRequestRepository  cafeJoinRequestRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CafeCalendarRepository cafeCalendarRepository;


    @Value("${org.zerock.upload.path}")
    private String uploadPath;


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
    public CafeApplicationResponseDTO approveCafe(Long applicationId, Long adminId) {
        // 1. 관리자 권한 확인 (이 부분은 실제 구현에 따라 달라질 수 있습니다.)
        // ...

        // 2. 승인할 카페 개설 신청 정보 조회 및 검증
        CafeApplication application = cafeApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카페 신청입니다."));
        if (application.getStatus() != CafeApplicationStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신청입니다.");
        }

        // 3. CafeApplication의 상태를 APPROVED로 변경
        application.approve();

        // 4. 상태 변경을 데이터베이스에 저장
        CafeApplication savedApplication = cafeApplicationRepository.save(application);

        // 5. 응답 DTO 반환
        return new CafeApplicationResponseDTO(savedApplication);
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
                1 ,
                null,
                null,
                true,
                true
        );
    }

    @Override
    public CafeResponseDTO getCafeInfoWithMembership(Long cafeId, Long userId) {
        // 1. Cafe 엔티티 조회
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카페입니다."));

        // 2. 동적으로 회원 수 계산
        Integer memberCount = membershipRepository.countByCafe(cafe);

        boolean isOwner = cafe.getOwner().getId().equals(userId); // userId는 여기에서 null이 아님을 보장
        boolean isMember = membershipRepository.existsByCafeAndUser(cafe, userRepository.getReferenceById(userId));


        // 3. 응답 DTO 반환
        return new CafeResponseDTO(
                cafe.getId(),
                cafe.getName(),
                cafe.getDescription(),
                cafe.getCategory().name(),
                cafe.getRegDate(),
                cafe.getOwner().getId(),
                memberCount,
                cafe.getCafeImage(),
                cafe.getCafeThumbnail(),
                isOwner,
                isMember
        );
    }

    @Override
    public CafeResponseDTO getBasicCafeInfo(Long cafeId) {
        // userId가 없는 경우를 위한 메서드
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카페입니다."));
        Integer memberCount = membershipRepository.countByCafe(cafe);

        // isOwner와 isMember는 항상 false로 설정
        return new CafeResponseDTO(
                cafe.getId(),
                cafe.getName(),
                cafe.getDescription(),
                cafe.getCategory().name(),
                cafe.getRegDate(),
                cafe.getOwner().getId(),
                memberCount,
                cafe.getCafeImage(),
                cafe.getCafeThumbnail(),
                false, // isOwner
                false  // isMember
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
    @Override
    public CafeResponseDTO registerCafeAfterApproval(
            Long applicationId,
            MultipartFile cafeImage,
            MultipartFile cafeThumbnail,
            Long userId) {

        // 1. 승인된 신청서와 신청자 정보 조회 및 유효성 검증
        CafeApplication application = cafeApplicationRepository.findByIdWithApplicant(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신청서를 찾을 수 없습니다."));

        if (application.getStatus() != CafeApplicationStatus.APPROVED) {
            throw new IllegalStateException("승인된 카페 신청만 등록할 수 있습니다.");
        }

        if (!application.getApplicant().getId().equals(userId)) {
            throw new IllegalStateException("해당 신청서의 소유자만 카페를 등록할 수 있습니다.");
        }

        if (cafeImage.isEmpty() || cafeThumbnail.isEmpty()) {
            throw new IllegalArgumentException("카페 이미지와 썸네일은 필수입니다.");
        }

        String uuid = UUID.randomUUID().toString();
        String cafeImageFileName = uuid + "_" + cafeImage.getOriginalFilename();
        String cafeThumbnailFileName = uuid + "_" + cafeThumbnail.getOriginalFilename();

        // 파일 저장
        try {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs(); // 디렉터리가 없으면 생성
            }
            cafeImage.transferTo(new File(uploadPath, cafeImageFileName));
            cafeThumbnail.transferTo(new File(uploadPath, cafeThumbnailFileName));
        } catch (Exception e) {
            throw new RuntimeException("파일 저장에 실패했습니다.", e);
        }

        // DB에 저장할 경로 설정 (예: /upload/고유파일명.jpg)
        String cafeImageUrl = "/upload/" + cafeImageFileName;
        String cafeThumbnailUrl = "/upload/" + cafeThumbnailFileName;

        // 3. CafeApplication 정보를 기반으로 실제 Cafe 엔티티 생성
        User applicant = application.getApplicant();
        Cafe newCafe = Cafe.builder()
                .name(application.getName())
                .description(application.getDescription())
                .category(application.getCategory())
                .owner(applicant)
                .cafeImage(cafeImageUrl)
                .cafeThumbnail(cafeThumbnailUrl)
                .memberCount(1) // 소유자 1명으로 시작
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

        // 7. 최종 등록이 완료되었으므로, CafeApplication을 데이터베이스에서 삭제
        cafeApplicationRepository.delete(application);

        // 8. 생성된 카페 정보를 DTO로 반환
        return new CafeResponseDTO(
                savedCafe.getId(),
                savedCafe.getName(),
                savedCafe.getDescription(),
                savedCafe.getCategory().name(),
                savedCafe.getRegDate(),
                savedCafe.getOwner().getId(),
                savedCafe.getMemberCount(),
                savedCafe.getCafeImage(),
                savedCafe.getCafeThumbnail(),
                true,
                true
        );
    }

    @Transactional(readOnly = true)
    public List<CafeResponseDTO> getAllCafes() {
        // 1. 데이터베이스에서 모든 Cafe 엔티티를 가져옵니다.
        List<Cafe> cafes = cafeRepository.findAll();

        // 2. 각 Cafe 엔티티를 DTO로 변환하여 리스트로 반환합니다.
        return cafes.stream()
                .map(cafe -> new CafeResponseDTO(
                        cafe.getId(),
                        cafe.getName(),
                        cafe.getDescription(),
                        cafe.getCategory().name(),
                        cafe.getRegDate(),
                        cafe.getOwner().getId(),
                        cafe.getMemberCount(),
                        cafe.getCafeImage(),
                        cafe.getCafeThumbnail(),
                        false,
                        false
                ))
                .collect(Collectors.toList());
    }


    @Transactional
    @Override
    public void updateCafe(Long cafeId, CafeUpdateDTO updateDTO, Long userId) throws IllegalAccessException {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카페입니다."));

        // 권한 확인: 소유자 ID와 요청한 사용자 ID가 다르면 예외 발생
        if (!cafe.getOwner().getId().equals(userId)) {
            throw new IllegalAccessException("카페를 수정할 권한이 없습니다.");
        }

        // 1. DTO의 텍스트 정보로 엔티티 업데이트
        cafe.setName(updateDTO.getName());
        cafe.setDescription(updateDTO.getDescription());
        cafe.setCategory(updateDTO.getCategory());

        // 2. 이미지 파일 처리
        if (updateDTO.getCafeImage() != null && !updateDTO.getCafeImage().isEmpty()) {
            // 기존 이미지 파일 삭제 (선택 사항이지만 좋은 습관)
            // File oldFile = new File(uploadPath, cafe.getCafeImage());
            // if (oldFile.exists()) { oldFile.delete(); }

            // 새로운 이미지 파일 저장
            String uuid = UUID.randomUUID().toString();
            String originalFilename = updateDTO.getCafeImage().getOriginalFilename();
            String newFileName = uuid + "_" + originalFilename;

            try {
                updateDTO.getCafeImage().transferTo(new File(uploadPath, newFileName));
                cafe.setCafeImage("/upload/" + newFileName);
            } catch (Exception e) {
                throw new RuntimeException("카페 이미지 저장에 실패했습니다.", e);
            }
        }

        // 3. 썸네일 이미지 파일 처리 (위와 동일한 로직 적용)
        if (updateDTO.getCafeThumbnail() != null && !updateDTO.getCafeThumbnail().isEmpty()) {
            // 기존 썸네일 파일 삭제
            // ... (위와 동일한 로직)

            // 새로운 썸네일 파일 저장
            String uuid = UUID.randomUUID().toString();
            String originalFilename = updateDTO.getCafeThumbnail().getOriginalFilename();
            String newFileName = uuid + "_" + originalFilename;

            try {
                updateDTO.getCafeThumbnail().transferTo(new File(uploadPath, newFileName));
                cafe.setCafeThumbnail("/upload/" + newFileName);
            } catch (Exception e) {
                throw new RuntimeException("카페 썸네일 저장에 실패했습니다.", e);
            }
        }

        // 4. 최종적으로 엔티티 저장
        cafeRepository.save(cafe);
    }

    @Transactional
    @Override
    public void deleteCafe(Long cafeId, Long userId) throws IllegalAccessException {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카페입니다."));

        // 권한 확인: 소유자 ID와 요청한 사용자 ID가 다르면 예외 발생
        if (!cafe.getOwner().getId().equals(userId)) {
            throw new IllegalAccessException("카페를 삭제할 권한이 없습니다.");
        }

        commentRepository.deleteByPostIn(postRepository.findByCafe(cafe));

        postRepository.deleteByCafe(cafe);

        cafeJoinRequestRepository.deleteByCafe(cafe);

        membershipRepository.deleteByCafe(cafe);

        // 데이터베이스에서 카페 삭제
        cafeRepository.delete(cafe);
    }

    @Override
    @Transactional
    public void sendJoinRequest(Long cafeId, Long userId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 카페를 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 이미 가입했거나 신청 중인지 확인
        if (membershipRepository.findByCafeAndUser(cafe, user).isPresent() ||
                cafeJoinRequestRepository.existsByCafeAndUserAndStatus(cafe, user, CafeJoinRequestStatus.PENDING)) {
            throw new IllegalStateException("이미 가입된 회원이거나 가입 신청이 처리 중입니다.");
        }

        // CafeJoinRequest 엔티티 생성 및 저장
        CafeJoinRequest joinRequest = CafeJoinRequest.builder()
                .cafe(cafe)
                .user(user)
                .status(CafeJoinRequestStatus.PENDING)
                .build();
        cafeJoinRequestRepository.save(joinRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CafeJoinRequestResponseDTO> getPendingJoinRequests(Long cafeId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 카페를 찾을 수 없습니다."));

        List<CafeJoinRequest> requests = cafeJoinRequestRepository.findByCafeAndStatus(cafe, CafeJoinRequestStatus.PENDING);

        // DTO로 변환하여 반환
        return requests.stream()
                .map(CafeJoinRequestResponseDTO::new)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public Long approveJoinRequest(Long requestId, Long adminId) {
        CafeJoinRequest joinRequest = cafeJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("가입 신청을 찾을 수 없습니다."));

        // 1. 관리자 권한 확인 (요청 받은 카페의 소유자인지)
        if (!joinRequest.getCafe().getOwner().getId().equals(adminId)) {
            throw new IllegalStateException("승인 권한이 없습니다.");
        }

        // 2. 신청 상태 확인
        if (joinRequest.getStatus() != CafeJoinRequestStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신청입니다.");
        }

        // 3. Membership 엔티티 생성 및 저장 (사용자에게 일반 멤버 역할 부여)
        Membership newMember = Membership.builder()
                .cafe(joinRequest.getCafe())
                .user(joinRequest.getUser())
                .role(CafeRole.CAFE_USER) // 기본 역할은 CAFE_USER
                .build();
        membershipRepository.save(newMember);

        // 4. CafeJoinRequest 상태를 APPROVED로 변경
        joinRequest.setStatus(CafeJoinRequestStatus.APPROVED);
        cafeJoinRequestRepository.save(joinRequest);

        // 5. 카페 회원수 증가
        joinRequest.getCafe().setMemberCount(joinRequest.getCafe().getMemberCount() + 1);
        cafeRepository.save(joinRequest.getCafe());

        // ✅ 6. 승인된 카페의 ID를 반환
        return joinRequest.getCafe().getId();
    }

    @Override
    @Transactional
    public void rejectJoinRequest(Long requestId, Long adminId) {
        CafeJoinRequest joinRequest = cafeJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("가입 신청을 찾을 수 없습니다."));

        if (!joinRequest.getCafe().getOwner().getId().equals(adminId)) {
            throw new IllegalStateException("거절 권한이 없습니다.");
        }

        if (joinRequest.getStatus() != CafeJoinRequestStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신청입니다.");
        }

        joinRequest.setStatus(CafeJoinRequestStatus.REJECTED);
        cafeJoinRequestRepository.save(joinRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCafeOwner(Long cafeId, Long userId) {
        return cafeRepository.findById(cafeId)
                .map(cafe -> cafe.getOwner().getId().equals(userId))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<CafeApplicationResponseDTO> getApplicationsByUserId(Long userId) {
        // CafeApplicationRepository를 사용하여 특정 사용자 ID에 해당하는 신청서 목록을 찾습니다.
        List<CafeApplication> applications = cafeApplicationRepository.findByApplicantId(userId);

        // 조회된 엔티티 목록을 DTO 목록으로 변환하여 반환합니다.
        return applications.stream()
                .map(CafeApplicationResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MyJoinedCafesDTO getMyJoinedCafes(Long userId) {
        List<Membership> memberships = membershipRepository.findByUserId(userId);

        long totalJoinedCafes = memberships.size();
        long totalOwnedCafes = memberships.stream()
                .filter(m -> m.getRole() == CafeRole.CAFE_ADMIN)
                .count();

        // 💡 최근 7일 내 가입한 카페 수 계산 (regDate 기준)
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        long recentlyJoinedCount = memberships.stream()
                .filter(m -> m.getRegDate().isAfter(oneWeekAgo))
                .count();

        // 💡 1. 모든 카테고리별 개수 맵 계산
        Map<String, Long> categoryCounts = memberships.stream()
                .collect(
                        Collectors.groupingBy(
                                m -> m.getCafe().getCategory().getKoreanName(),
                                Collectors.counting()
                        )
                );

        // 💡 2. 개수를 기준으로 내림차순 정렬하여 상위 1개만 추출
        List<Map.Entry<String, Long>> sortedCategories = categoryCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(1) // 상위 1개만 선택
                .collect(Collectors.toList());

        String cat1Name = "가입 카테고리"; // 기본값
        long cat1Count = 0;

        if (sortedCategories.size() > 0) {
            cat1Name = sortedCategories.get(0).getKey();
            cat1Count = sortedCategories.get(0).getValue();
        }


        // 3. DTO에 담아 반환
        return MyJoinedCafesDTO.builder()
                .memberships(memberships)
                .totalJoinedCafes(totalJoinedCafes)
                .totalOwnedCafes(totalOwnedCafes)
                .selectedCategory1Name(cat1Name)
                .selectedCategory1Count(cat1Count)
                .recentlyJoinedCount(recentlyJoinedCount) // 💡 새 필드 추가
                .build();
    }

    @Transactional
    public void leaveCafe(Long cafeId, Long userId) {
        // 1. 카페 및 사용자 존재 여부 확인
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카페입니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 해당 카페의 소유자인지 확인 (소유자는 탈퇴할 수 없음)
        if (cafe.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("카페 소유자는 탈퇴할 수 없습니다. 카페를 삭제하거나 소유권을 이전하세요.");
        }

        // 3. 해당 사용자의 멤버십 정보 찾기
        Membership membership = membershipRepository.findByCafeAndUser(cafe, user)
                .orElseThrow(() -> new IllegalArgumentException("해당 카페의 회원이 아닙니다."));

        // 4. 멤버십 정보 삭제
        membershipRepository.delete(membership);
    }

    @Override
    public String getCafeNameById(Long cafeId) {
        return cafeRepository.findById(cafeId)
                .map(Cafe::getName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카페입니다."));
    }

    @Override
    public List<Cafe> getSimilarCafes(Long cafeId, int limit) {
        Cafe currentCafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("카페를 찾을 수 없습니다."));

        return cafeRepository.findByCategoryAndIdNot(
                currentCafe.getCategory(),
                currentCafe.getId(),
                PageRequest.of(0, limit)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventDTO> getCalendarEvents(Long cafeId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("카페를 찾을 수 없습니다."));

        List<CafeCalendar> events = cafeCalendarRepository.findByCafe(cafe);

        return events.stream()
                .map(this::convertToCalendarEventDTO)
                .collect(Collectors.toList());
    }

    private CalendarEventDTO convertToCalendarEventDTO(CafeCalendar calendar) {
        // Meeting 엔티티가 없는 경우를 고려하여 예외 처리
        String title = (calendar.getMeeting() != null) ? calendar.getMeeting().getTitle() : "제목 없음";
        String start = (calendar.getMeeting() != null) ? calendar.getMeeting().getMeetingDate().toString() : null;
        String url = null;
        if (calendar.getMeeting() != null) {
            url = "/cafe/" + calendar.getCafe().getId() + "/meeting/read?id=" + calendar.getMeeting().getId() + "&page=1&size=10";
        }

        return CalendarEventDTO.builder()
                .id(calendar.getId())
                .title(title)
                .start(start)
                .url(url)
                .build();
    }

    @Override
    public List<CafeCategory> getAllCategories() {
        return List.of(CafeCategory.values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CafeResponseDTO> getRecommendedCafes(int limit) {
        // ✅ 수정: `findTop`을 `findBy`로 변경
        List<Cafe> recommendedCafes = cafeRepository.findByOrderByMemberCountDesc(PageRequest.of(0, limit));

        // 2. 엔티티 리스트를 DTO 리스트로 변환하여 반환합니다.
        return recommendedCafes.stream()
                .map(cafe -> new CafeResponseDTO(
                        cafe.getId(),
                        cafe.getName(),
                        cafe.getDescription(),
                        cafe.getCategory().getKoreanName(),
                        cafe.getRegDate(),
                        cafe.getOwner().getId(),
                        cafe.getMemberCount(),
                        cafe.getCafeImage(),
                        cafe.getCafeThumbnail(),
                        false,
                        false
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<CafeResponseDTO> getCafesByCategory(CafeCategory category) {
        // 1. Repository를 사용하여 해당 카테고리의 카페 엔티티 목록을 조회합니다.
        List<Cafe> cafes = cafeRepository.findByCategory(category);

        // 2. 조회된 엔티티 목록을 DTO 목록으로 변환하여 반환합니다.
        return cafes.stream()
                .map(cafe -> new CafeResponseDTO(
                        cafe.getId(),
                        cafe.getName(),
                        cafe.getDescription(),
                        cafe.getCategory().name(), // 카테고리 이름 (Enum)
                        cafe.getRegDate(),
                        cafe.getOwner().getId(),
                        cafe.getMemberCount(),
                        cafe.getCafeImage(),
                        cafe.getCafeThumbnail(),
                        false, // isOwner
                        false  // isMember
                ))
                .collect(Collectors.toList());
    }

//    @Override
//    @Transactional(readOnly = true)
//    public PageResponseDTO<CafeResponseDTO> getCafeList(PageRequestDTO pageRequestDTO) {
//
//        // 1. DTO에서 Pageable 객체 생성
//        Pageable pageable = pageRequestDTO.getPageable("regDate");
//
//        // 2. Repository의 searchAll 메서드를 호출하여 키워드 검색 수행
//        //    키워드는 DTO에서 가져오고, 카테고리 목록은 null로 전달합니다.
//        Page<Cafe> result = cafeRepository.searchAll(
//                pageRequestDTO.getKeyword(),
//                null, // 카테고리 검색이 아니므로 null 전달
//                pageable
//        );
//
//        // 3. Page<Cafe>를 DTO 리스트로 변환
//        List<CafeResponseDTO> dtoList = result.getContent().stream()
//                .map(this::convertToCafeResponseDTO)
//                .collect(Collectors.toList());
//
//        // 4. PageResponseDTO 생성 및 반환
//        return PageResponseDTO.<CafeResponseDTO>withAll()
//                .pageRequestDTO(pageRequestDTO)
//                .dtoList(dtoList)
//                .total((int) result.getTotalElements())
//                .build();
//    }
//
//    /**
//     * 특정 카테고리로 필터링된 카페 목록을 페이징하여 반환합니다.
//     */
//    @Override
//    @Transactional(readOnly = true)
//    public PageResponseDTO<CafeResponseDTO> getCafeListByCategory(CafeCategory cafeCategory, PageRequestDTO pageRequestDTO) {
//
//        // 1. DTO에서 Pageable 객체 생성
//        Pageable pageable = pageRequestDTO.getPageable("regDate");
//
//        // 2. Repository의 searchAll 메서드를 호출하여 카테고리 검색 수행
//        //    키워드는 null로 전달하고, 카테고리 목록을 전달합니다.
//        Page<Cafe> result = cafeRepository.searchAll(
//                null, // 카테고리 검색이므로 키워드는 null
//                Arrays.asList(cafeCategory), // 카테고리 목록 전달
//                pageable
//        );
//
//        // 3. Page<Cafe>를 DTO 리스트로 변환
//        List<CafeResponseDTO> dtoList = result.getContent().stream()
//                .map(this::convertToCafeResponseDTO)
//                .collect(Collectors.toList());
//
//        // 4. PageResponseDTO 생성 및 반환
//        return PageResponseDTO.<CafeResponseDTO>withAll()
//                .pageRequestDTO(pageRequestDTO)
//                .dtoList(dtoList)
//                .total((int) result.getTotalElements())
//                .build();
//    }

    @Override
    public PageResponseDTO<CafeResponseDTO> getCafeListWithFilters(PageRequestDTO pageRequestDTO, CafeCategory category) {

        String keyword = pageRequestDTO.getKeyword();
        String[] typeArr = pageRequestDTO.getTypes();

        Pageable pageable = pageRequestDTO.getPageable("regDate");
        if (typeArr == null) {
            keyword = null;
        }
        List<CafeCategory> categoryList = (category != null) ? List.of(category) : null;

        // CafeRepository의 searchAll 메서드를 호출하여 검색
        Page<Cafe> result = cafeRepository.searchAll(
                pageRequestDTO.getKeyword(),
                categoryList,
                pageable
        );

        // DTO 변환 및 PageResponseDTO 반환 로직...
        List<CafeResponseDTO> dtoList = result.getContent().stream()
                .map(this::convertToCafeResponseDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<CafeResponseDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int) result.getTotalElements())
                .build();
    }

    // DTO 변환을 위한 헬퍼 메서드 추가
    private CafeResponseDTO convertToCafeResponseDTO(Cafe cafe) {
        return new CafeResponseDTO(
                cafe.getId(),
                cafe.getName(),
                cafe.getDescription(),
                cafe.getCategory().name(),
                cafe.getRegDate(),
                cafe.getOwner().getId(),
                cafe.getMemberCount(),
                cafe.getCafeImage(),
                cafe.getCafeThumbnail(),
                false,
                false
        );
    }
}
