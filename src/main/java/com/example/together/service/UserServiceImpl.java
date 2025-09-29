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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService, UserDetailsService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUserId(username)
        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
    if (user.getStatus() != Status.ACTIVE) {
      throw new UsernameNotFoundException("비활성 계정입니다: " + username);
    }
    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getUserId())
        .password(user.getPassword())
        .roles(user.getSystemRole().name())
        .build();
  }

  /** 실제 저장하는 오버로드 */
  @Override
  @Transactional
  public User register(memberRegisterDTO registerDTO) {
    if (isUserIdExists(registerDTO.getUserId())) {
      throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
    }
    if (isEmailExists(registerDTO.getEmail())) {
      throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
    }

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

    try {
      return userRepository.save(user);
    } catch (DataIntegrityViolationException e) {
      // UNIQUE/NOT NULL 위반 등 즉시 확인용
      log.error("회원가입 저장 실패(제약 위반) userId={}, email={}, err={}",
          registerDTO.getUserId(), registerDTO.getEmail(), e.getMessage(), e);
      throw e;
    }
  }

  /** 🔧 문제였던 void 오버로드 구현: 저장 + 사진(옵션) */
  @Override
  @Transactional
  public void register(memberRegisterDTO registerDTO, MultipartFile profilePhoto) {
    // 1) DB 저장
    User saved = register(registerDTO);

    // 2) 프로필 사진이 있으면 업로드 후 DB에 URL 반영
    if (profilePhoto != null && !profilePhoto.isEmpty()) {
      String url = uploadProfilePhoto(saved.getUserId(), profilePhoto);
      // uploadProfilePhoto 내부에서 userRepository.save 호출로 반영됨
      log.info("회원가입 프로필 사진 저장 완료: userId={}, url={}", saved.getUserId(), url);
    }
  }

  @Override
  @Transactional
  public User updateProfile(Long id, memberRegisterDTO registerDTO) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    if (registerDTO.getName() != null) user.setName(registerDTO.getName());
    if (registerDTO.getPhone() != null) user.setPhone(registerDTO.getPhone());
    if (registerDTO.getNickname() != null) user.setNickname(registerDTO.getNickname());
    return user;
  }

  @Override
  @Transactional
  public boolean changePassword(String userId, String currentPassword, String newPassword) {
    try {
      User user = findByUserId(userId);
      if (user == null) return false;
      if (!passwordEncoder.matches(currentPassword, user.getPassword())) return false;
      user.setPassword(passwordEncoder.encode(newPassword));
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

  @Override public boolean isUserIdExists(String userId) { return userRepository.findByUserId(userId).isPresent(); }
  @Override public boolean isEmailExists(String email) { return userRepository.findByEmail(email).isPresent(); }
  @Override public boolean isNameExists(String name) { return userRepository.findByName(name).isPresent(); }
  @Override public boolean isNicknameExists(String nickname) { return userRepository.findByNickname(nickname).isPresent(); }
  @Override public boolean isPhoneExists(String phone) { return userRepository.findByPhone(phone).isPresent(); }

  @Override public User findByUserId(String userId) { return userRepository.findByUserId(userId).orElse(null); }
  @Override public User findByEmail(String email) { return userRepository.findByEmail(email).orElse(null); }

  @Override
  public User authenticate(String userId, String password) {
    Optional<User> userOpt = userRepository.findByUserId(userId);
    if (userOpt.isPresent()) {
      User user = userOpt.get();
      if (user.getStatus() == Status.ACTIVE && passwordEncoder.matches(password, user.getPassword())) {
        return user;
      }
    }
    return null;
  }

  @Override
  public String findUserIdByNameAndEmail(String name, String email) {
    Optional<User> userOpt = userRepository.findByNameAndEmail(name, email);
    return userOpt.map(User::getUserId).orElse(null);
  }

  @Override
  public boolean isAdmin(Long adminId) {
    Optional<User> userOpt = userRepository.findById(adminId);
    return userOpt.isPresent() && userOpt.get().getSystemRole().equals(SystemRole.ADMIN);
  }

  @Transactional(readOnly = true)
  public String getUserNicknameById(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    return user.getNickname();
  }

  @Override
  @Transactional
  public boolean updateTempPassword(String userId, String tempPassword) {
    try {
      User user = findByUserId(userId);
      if (user == null) return false;
      user.setPassword(passwordEncoder.encode(tempPassword));
      userRepository.save(user);
      return true;
    } catch (Exception e) {
      log.error("임시 비밀번호 업데이트 실패: userId = {}, error = {}", userId, e.getMessage());
      return false;
    }
  }

  @Override
  @Transactional
  public void updateUserPassword(String userId, String newPassword) {
    Optional<User> userOpt = userRepository.findByUserId(userId);
    if (userOpt.isPresent()) {
      User user = userOpt.get();
      user.setPassword(passwordEncoder.encode(newPassword));
      userRepository.save(user);
    } else {
      log.warn("사용자를 찾을 수 없음: {}", userId);
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
      userRepository.save(user);

      return photoUrl;

    } catch (IOException e) {
      log.error("프로필 사진 업로드 실패: {}", e.getMessage(), e);
      throw new RuntimeException("파일 업로드에 실패했습니다.", e);
    }
  }

  @Override
  public boolean isNicknameAvailable(String nickname, String currentUserId) {
    try {
      Optional<User> currentUserOpt = userRepository.findByUserId(currentUserId);
      if (currentUserOpt.isEmpty()) return false;
      Optional<User> existingUserOpt = userRepository.findByNickname(nickname);
      if (existingUserOpt.isEmpty()) return true;
      return existingUserOpt.get().getId().equals(currentUserOpt.get().getId());
    } catch (Exception e) {
      log.error("닉네임 중복 확인 중 오류: {}", e.getMessage());
      return false;
    }
  }

  @Override
  public User findUserForPasswordReset(String userId, String email, String name) {
    return userRepository.findByUserIdAndEmailAndName(userId, email, name).orElse(null);
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
        case "email": {
          // 형식 검증
          if (value == null || !value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            log.warn("올바르지 않은 이메일 형식: {}", value);
            return false;
          }
          // 다른 사용자의 이메일인지 확인
          Optional<User> exists = userRepository.findByEmail(value);
          if (exists.isPresent() && !exists.get().getId().equals(user.getId())) {
            log.warn("이미 사용 중인 이메일: {}", value);
            return false;
          }
          user.setEmail(value);
          break;
        }
        case "nickname": {
          if (value == null) return false;
          String v = value.trim();
          if (v.length() < 2 || v.length() > 20 || !v.matches("^[가-힣a-zA-Z0-9]+$")) {
            log.warn("유효하지 않은 닉네임: {}", v);
            return false;
          }
          Optional<User> exists = userRepository.findByNickname(v);
          if (exists.isPresent() && !exists.get().getId().equals(user.getId())) {
            log.warn("이미 사용 중인 닉네임: {}", v);
            return false;
          }
          user.setNickname(v);
          break;
        }
        case "name": {
          if (value == null) return false;
          String v = value.trim();
          if (v.length() < 2 || v.length() > 10) {
            log.warn("유효하지 않은 이름 길이: {}", v);
            return false;
          }
          user.setName(v);
          break;
        }
        case "phone": {
          if (value != null && !value.matches("^010-\\d{4}-\\d{4}$")) {
            log.warn("유효하지 않은 전화번호 형식: {}", value);
            return false;
          }
          // 전화번호 중복 체크 (null 허용 시에는 null/빈문자열이면 스킵)
          if (value != null && !value.isBlank()) {
            Optional<User> exists = userRepository.findByPhone(value);
            if (exists.isPresent() && !exists.get().getId().equals(user.getId())) {
              log.warn("이미 사용 중인 전화번호: {}", value);
              return false;
            }
          }
          user.setPhone(value);
          break;
        }
        default:
          log.warn("지원하지 않는 필드: {}", field);
          return false;
      }

      userRepository.save(user);
      log.info("사용자 정보 업데이트 성공: userId={}, field={}", userId, field);
      return true;

    } catch (Exception e) {
      log.error("사용자 정보 업데이트 중 오류: userId={}, field={}, err={}", userId, field, e.getMessage(), e);
      return false;
    }
  }
}
