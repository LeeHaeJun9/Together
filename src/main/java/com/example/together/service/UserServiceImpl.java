package com.example.together.service;

import com.example.together.domain.Status;
import com.example.together.domain.SystemRole;
import com.example.together.domain.User;
import com.example.together.dto.member.memberRegisterDTO;
import com.example.together.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService, UserDetailsService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /* ====================== 인증 (Spring Security) ====================== */


        if (user.getStatus() != Status.ACTIVE) {
            throw new UsernameNotFoundException("비활성 계정입니다: " + username);
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUserId())
                .password(user.getPassword())
                .roles(user.getSystemRole().name())
                .build();
    }

    @Override
    @Transactional
    public User register(memberRegisterDTO registerDTO) {
        // Delegate to the full method with null photo
        register(registerDTO, null);

        // Return the saved user
        return userRepository.findByUserId(registerDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 저장 실패"));
    }

    @Override
    @Transactional
    public void register(memberRegisterDTO registerDTO, MultipartFile profilePhoto) {
        log.info("회원가입 시작: userId = {}", registerDTO.getUserId());

        // 1️⃣ Duplicate validation
        if (isUserIdExists(registerDTO.getUserId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        if (isEmailExists(registerDTO.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 2️⃣ Create user entity
        User user = User.builder()
                .userId(registerDTO.getUserId())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .name(registerDTO.getName())
                .email(registerDTO.getEmail())
                .phone(registerDTO.getPhone())
                .nickname(registerDTO.getNickname())
                .systemRole(SystemRole.USER)
                .status(Status.ACTIVE)
                .build();

        log.info("User 엔티티 생성 완료: userId = {}", user.getUserId());

        // 3️⃣ Handle optional profile photo
        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            try {
                String photoUrl = uploadProfilePhotoForNewUser(user.getUserId(), profilePhoto);
                user.setProfilePhoto(photoUrl);
                log.info("프로필 사진 업로드 성공: userId = {}, photoUrl = {}", user.getUserId(), photoUrl);
            } catch (Exception e) {
                log.warn("프로필 사진 업로드 실패 (회원가입은 계속 진행): userId = {}, error = {}",
                        user.getUserId(), e.getMessage());
            }
        }

        // 4️⃣ ⭐ CRITICAL: Save user to database ⭐
        User savedUser = userRepository.save(user);
        log.info("✅ 회원가입 완료: userId = {}, DB ID = {}", savedUser.getUserId(), savedUser.getId());
    }

    /**
     * Helper method for uploading profile photo during registration
     */


    /**
     * Helper method for uploading profile photo during registration
     */
    private String uploadProfilePhotoForNewUser(String userId, MultipartFile photo) throws IOException {
        String currentDir = System.getProperty("user.dir");
        String uploadDir = currentDir + File.separator + "uploads" + File.separator + "profile" + File.separator;

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
            log.info("업로드 디렉토리 생성: {}", uploadDir);
        }

        String originalFilename = photo.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";

        String newFilename = userId + "_" + System.currentTimeMillis() + extension;
        File targetFile = new File(uploadDir, newFilename);
        photo.transferTo(targetFile);

        return "/uploads/profile/" + newFilename;
    }

    @Override
    @Transactional
    public User updateProfile(Long id, memberRegisterDTO registerDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (registerDTO.getName() != null) {
            user.setName(registerDTO.getName());
        }
        if (registerDTO.getPhone() != null) {
            user.setPhone(registerDTO.getPhone());
        }
        if (registerDTO.getNickname() != null) {
            user.setNickname(registerDTO.getNickname());
        }

        return user;
    }

    @Override
    @Transactional
    public boolean changePassword(String userId, String currentPassword, String newPassword) {
        try {
            User user = findByUserId(userId);
            if (user == null) {
                return false;
            }

            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                return false;
            }

            String encodedNewPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedNewPassword);

            userRepository.save(user);
            return true;

        } catch (Exception e) {
            log.error("비밀번호 변경 실패: userId = {}, error = {}", userId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isEmailExistsExcludeUser(String email, String excludeUserId) {
        try {
            User existingUser = userRepository.findByEmail(email).orElse(null);
            return existingUser != null && !existingUser.getUserId().equals(excludeUserId);
        } catch (Exception e) {
            log.error("이메일 중복 확인 실패: email = {}, error = {}", email, e.getMessage());
            return true;
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setStatus(Status.DELETED);
    }

    @Override
    public boolean isUserIdExists(String userId) {
        return userRepository.findByUserId(userId).isPresent();
    }

    @Override
    public boolean isEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public boolean isNameExists(String name) {
        return userRepository.findByName(name).isPresent();
    }

    @Override
    public boolean isNicknameExists(String nickname) {
        return userRepository.findByNickname(nickname).isPresent();
    }

    @Override
    public boolean isPhoneExists(String phone) {
        return userRepository.findByPhone(phone).isPresent();
    }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // username 파라미터에는 SecurityConfig.usernameParameter("userId")로 전달된 userId가 들어옵니다.
    final String userId = username;
    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));


    if (user.getStatus() != Status.ACTIVE) {
      throw new UsernameNotFoundException("비활성 계정입니다: " + userId);
    }

    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getUserId())
        .password(user.getPassword())
        .roles(user.getSystemRole().name()) // ROLE_ 접두사는 .roles()가 자동 부여
        .build();
  }

  /* ====================== 회원가입 ====================== */

  /** 실제 저장하는 오버로드 (프로필 사진 제외) */
  @Override
  @Transactional
  public User register(memberRegisterDTO registerDTO) {
    // 1) 입력 정규화
    final String userId   = safeTrim(registerDTO.getUserId());
    final String email    = toEmailCanonical(registerDTO.getEmail());
    final String name     = safeTrim(registerDTO.getName());
    final String phone    = safeTrim(registerDTO.getPhone());
    final String nickname = safeTrim(registerDTO.getNickname());
    final String rawPw    = registerDTO.getPassword();

    // 2) 필수값 검증(엔티티 제약과 동일선상)
    requireNonEmpty(userId,   "아이디는 필수입니다.");
    requireNonEmpty(email,    "이메일은 필수입니다.");
    requireNonEmpty(name,     "이름은 필수입니다.");
    requireNonEmpty(phone,    "전화번호는 필수입니다.");
    requireNonEmpty(nickname, "닉네임은 필수입니다.");
    requireNonEmpty(rawPw,    "비밀번호는 필수입니다.");

    // 3) 중복 체크 (사전 차단)
    if (isUserIdExists(userId)) {
      throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
    }
    if (isEmailExists(email)) {
      throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
    }
    if (isNicknameExists(nickname)) {
      throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
    }
    if (isPhoneExists(phone)) {
      throw new IllegalArgumentException("이미 존재하는 전화번호입니다.");
    }

    // 4) 저장
    User user = User.builder()
        .userId(userId)
        .password(passwordEncoder.encode(rawPw))
        .name(name)
        .email(email)
        .phone(phone)
        .nickname(nickname)
        .systemRole(SystemRole.USER)
        .status(Status.ACTIVE)
        .build();

    try {
      User saved = userRepository.save(user);
      // 필요 시 즉시 flush해서 DB 반영 보장 (등록 직후 로그인을 시도할 때 안정적)
      userRepository.flush();
      return saved;
    } catch (DataIntegrityViolationException e) {
      log.error("회원가입 저장 실패(제약 위반) userId={}, email={}, nickname={}, phone={}, err={}",
          userId, email, nickname, phone, e.getMessage(), e);
      throw e;
    }
  }

  /** 회원가입 + 프로필 사진(옵션) */
  @Override
  @Transactional
  public void register(memberRegisterDTO registerDTO, MultipartFile profilePhoto) {
    User saved = register(registerDTO); // DB 저장 + flush
    if (profilePhoto != null && !profilePhoto.isEmpty()) {
      String url = uploadProfilePhoto(saved.getUserId(), profilePhoto);
      log.info("회원가입 프로필 사진 저장: userId={}, url={}", saved.getUserId(), url);
    }


    @Override
    @Transactional
    public void updateUserPassword(String userId, String newPassword) {
        System.out.println("updateUserPassword 호출됨: userId=" + userId + ", newPassword=" + newPassword);

        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("사용자 찾음: " + user.getUserId());

            String encodedPassword = passwordEncoder.encode(newPassword);
            System.out.println("암호화된 비밀번호: " + encodedPassword);

            user.setPassword(encodedPassword);
            User savedUser = userRepository.save(user);
            System.out.println("저장 완료: " + savedUser.getUserId());
        } else {
            System.out.println("사용자를 찾을 수 없음: " + userId);
        }
    }

    @Override
    @Transactional
    public String uploadProfilePhoto(String userId, MultipartFile photo) {
        try {
            String currentDir = System.getProperty("user.dir");
            String uploadDir = currentDir + File.separator + "uploads" + File.separator + "profile" + File.separator;

            File directory = new File(uploadDir);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                log.info("업로드 디렉토리 생성: {}, 성공: {}", uploadDir, created);
            }

            String originalFilename = photo.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            } else {
                extension = ".jpg";
            }

            String newFilename = userId + "_" + System.currentTimeMillis() + extension;

            File targetFile = new File(uploadDir, newFilename);
            photo.transferTo(targetFile);

            String photoUrl = "/uploads/profile/" + newFilename;

            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

            user.setProfilePhoto(photoUrl);
            userRepository.save(user);

            log.info("프로필 사진 업로드 및 DB 저장 성공: userId = {}, photoUrl = {}", userId, photoUrl);

            return photoUrl;

        } catch (IOException e) {
            log.error("프로필 사진 업로드 중 파일 저장 실패: " + e.getMessage(), e);
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional
    public boolean updateUserField(String userId, String field, String value) {
        try {
            Optional<User> userOptional = userRepository.findByUserId(userId);
            if (userOptional.isEmpty()) {
                log.warn("사용자를 찾을 수 없습니다: userId = {}", userId);
                return false;
            }

            User user = userOptional.get();

            switch (field) {
                case "email":
                    if (isEmailExistsForOtherUser(value, user.getId())) {
                        log.warn("이미 사용 중인 이메일: email = {}", value);
                        return false;
                    }
                    user.setEmail(value);
                    break;

                case "nickname":
                    if (value == null || value.trim().length() < 2 || value.trim().length() > 10) {
                        log.warn("유효하지 않은 닉네임: nickname = {}", value);
                        return false;
                    }
                    if (!isNicknameAvailable(value.trim(), userId)) {
                        log.warn("이미 사용 중인 닉네임: nickname = {}", value);
                        return false;
                    }
                    user.setNickname(value.trim());
                    break;

                case "name":
                    if (value == null || value.trim().length() < 2 || value.trim().length() > 10) {
                        log.warn("유효하지 않은 이름: name = {}", value);
                        return false;
                    }
                    user.setName(value.trim());
                    break;

                case "phone":
                    if (value != null && !value.matches("^010-\\d{4}-\\d{4}$")) {
                        log.warn("유효하지 않은 전화번호 형식: phone = {}", value);
                        return false;
                    }
                    user.setPhone(value);
                    break;

                default:
                    log.warn("지원하지 않는 필드: field = {}", field);
                    return false;
            }

            userRepository.save(user);
            log.info("사용자 정보 업데이트 성공: userId = {}, field = {}", userId, field);
            return true;

        } catch (Exception e) {
            log.error("사용자 정보 업데이트 중 오류 발생: userId = {}, field = {}, error = {}",
                    userId, field, e.getMessage());
            return false;
        }
=======
  }

  /* ====================== 프로필/비밀번호 ====================== */

  @Override
  @Transactional
  public User updateProfile(Long id, memberRegisterDTO registerDTO) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    if (registerDTO.getName() != null)     user.setName(safeTrim(registerDTO.getName()));
    if (registerDTO.getPhone() != null)    user.setPhone(safeTrim(registerDTO.getPhone()));
    if (registerDTO.getNickname() != null) user.setNickname(safeTrim(registerDTO.getNickname()));
    // dirty checking 으로 반영
    return user;
  }

  @Override
  @Transactional
  public boolean changePassword(String userId, String currentPassword, String newPassword) {
    User user = findByUserId(userId);
    if (user == null) return false;
    if (!passwordEncoder.matches(currentPassword, user.getPassword())) return false;

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.saveAndFlush(user); // 즉시 반영
    return true;
  }

  @Override
  @Transactional
  public boolean updateTempPassword(String userId, String tempPassword) {
    User user = findByUserId(userId);
    if (user == null) return false;

    user.setPassword(passwordEncoder.encode(tempPassword));
    userRepository.saveAndFlush(user);
    return true;
  }

  @Override
  @Transactional
  public void updateUserPassword(String userId, String newPassword) {
    userRepository.findByUserId(userId).ifPresent(user -> {
      user.setPassword(passwordEncoder.encode(newPassword));
      userRepository.saveAndFlush(user);
    });
  }

  @Override
  @Transactional
  public String uploadProfilePhoto(String userId, MultipartFile photo) {
    try {
      String currentDir = System.getProperty("user.dir");
      String uploadDir = currentDir + File.separator + "uploads" + File.separator + "profile" + File.separator;

      File directory = new File(uploadDir);
      if (!directory.exists()) {
        boolean created = directory.mkdirs();
        log.info("업로드 디렉토리 생성: {}, 성공: {}", uploadDir, created);
      }

      String originalFilename = photo.getOriginalFilename();
      String extension = (originalFilename != null && originalFilename.contains("."))
          ? originalFilename.substring(originalFilename.lastIndexOf("."))
          : ".jpg";

      String newFilename = userId + "_" + System.currentTimeMillis() + extension;
      File targetFile = new File(uploadDir, newFilename);
      photo.transferTo(targetFile);

      String photoUrl = "/uploads/profile/" + newFilename;

      User user = userRepository.findByUserId(userId)
          .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
      user.setProfilePhoto(photoUrl);
      userRepository.saveAndFlush(user);

      return photoUrl;
    } catch (IOException e) {
      log.error("프로필 사진 업로드 실패: {}", e.getMessage(), e);
      throw new RuntimeException("파일 업로드에 실패했습니다.", e);
    }
  }

  /* ====================== 조회/중복체크/삭제 ====================== */

  @Override public boolean isUserIdExists(String userId) { return userRepository.findByUserId(safeTrim(userId)).isPresent(); }
  @Override public boolean isEmailExists(String email) { return userRepository.findByEmail(toEmailCanonical(email)).isPresent(); }
  @Override public boolean isNameExists(String name) { return userRepository.findByName(safeTrim(name)).isPresent(); }
  @Override public boolean isNicknameExists(String nickname) { return userRepository.findByNickname(safeTrim(nickname)).isPresent(); }
  @Override public boolean isPhoneExists(String phone) { return userRepository.findByPhone(safeTrim(phone)).isPresent(); }

  @Override public User findByUserId(String userId) { return userRepository.findByUserId(safeTrim(userId)).orElse(null); }
  @Override public User findByEmail(String email) { return userRepository.findByEmail(toEmailCanonical(email)).orElse(null); }

  @Override
  public User authenticate(String userId, String password) {
    return userRepository.findByUserId(safeTrim(userId))
        .filter(u -> u.getStatus() == Status.ACTIVE && passwordEncoder.matches(password, u.getPassword()))
        .orElse(null);
  }

  @Override
  public String findUserIdByNameAndEmail(String name, String email) {
    return userRepository.findByNameAndEmail(safeTrim(name), toEmailCanonical(email))
        .map(User::getUserId).orElse(null);
  }

  @Override
  public boolean isAdmin(Long adminId) {
    return userRepository.findById(adminId)
        .map(u -> u.getSystemRole() == SystemRole.ADMIN)
        .orElse(false);
  }

  @Transactional(readOnly = true)
  public String getUserNicknameById(Long userId) {
    return userRepository.findById(userId)
        .map(User::getNickname)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
  }

  @Override
  @Transactional
  public void deleteUser(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    user.setStatus(Status.DELETED);
  }

  @Override
  public boolean isEmailExistsExcludeUser(String email, String excludeUserId) {
    return userRepository.findByEmail(toEmailCanonical(email))
        .filter(u -> !u.getUserId().equals(safeTrim(excludeUserId)))
        .isPresent();
  }

  @Override
  public boolean isNicknameAvailable(String nickname, String currentUserId) {
    Optional<User> me = userRepository.findByUserId(safeTrim(currentUserId));
    if (me.isEmpty()) return false;

    Optional<User> other = userRepository.findByNickname(safeTrim(nickname));
    return other.isEmpty() || other.get().getId().equals(me.get().getId());
  }

  @Override
  public User findUserForPasswordReset(String userId, String email, String name) {
    return userRepository.findByUserIdAndEmailAndName(safeTrim(userId), toEmailCanonical(email), safeTrim(name))
        .orElse(null);
  }

  @Override
  @Transactional
  public boolean updateUserField(String userId, String field, String value) {
    Optional<User> userOptional = userRepository.findByUserId(safeTrim(userId));
    if (userOptional.isEmpty()) {
      log.warn("사용자를 찾을 수 없습니다: userId = {}", userId);
      return false;

    }
    User user = userOptional.get();

    switch (field) {
      case "email" -> {
        String v = toEmailCanonical(value);
        if (v == null || !v.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) return false;
        if (isEmailExistsExcludeUser(v, user.getUserId())) return false;
        user.setEmail(v);
      }
      case "nickname" -> {
        String v = safeTrim(value);
        if (v.length() < 2 || v.length() > 20 || !v.matches("^[가-힣a-zA-Z0-9]+$")) return false;
        if (!isNicknameAvailable(v, user.getUserId())) return false;
        user.setNickname(v);
      }
      case "name" -> {
        String v = safeTrim(value);
        if (v.length() < 2 || v.length() > 10) return false;
        user.setName(v);
      }
      case "phone" -> {
        String v = safeTrim(value);
        if (v != null && !v.isBlank() && !v.matches("^010-\\d{4}-\\d{4}$")) return false;
        if (v != null && !v.isBlank()) {
          Optional<User> exists = userRepository.findByPhone(v);
          if (exists.isPresent() && !exists.get().getId().equals(user.getId())) return false;
        }
        user.setPhone(v);
      }
      default -> {
        log.warn("지원하지 않는 필드: {}", field);
        return false;
      }
    }

    userRepository.saveAndFlush(user); // 즉시 반영
    return true;
  }

  /* ====================== 유틸 ====================== */

  private static String safeTrim(String s) {
    return s == null ? null : s.trim();
  }

  private static String toEmailCanonical(String email) {
    return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
  }


            return existingUser.getId().equals(currentUser.getId());

        } catch (Exception e) {
            log.error("닉네임 중복 확인 중 오류: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public User findUserForPasswordReset(String userId, String email, String name) {
        return userRepository.findByUserIdAndEmailAndName(userId, email, name).orElse(null);
    }
}
=======
  private static void requireNonEmpty(String v, String msg) {
    if (v == null || v.isBlank()) throw new IllegalArgumentException(msg);
  }
}
