package com.example.together.service;

import com.example.together.domain.User;
import com.example.together.domain.SystemRole;
import com.example.together.domain.Status;
import com.example.together.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        if ("kakao".equals(registrationId)) {
            return processKakaoUser(attributes);
        } else if ("naver".equals(registrationId)) {
            return processNaverUser(attributes);
        }

        // 기본 처리 (지원하지 않는 provider)
        return oAuth2User;
    }

    // ✅ 카카오 처리
    private OAuth2User processKakaoUser(Map<String, Object> attributes) {
        String kakaoId = "kakao_" + attributes.get("id").toString();

        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
        @SuppressWarnings("unchecked")
        Map<String, Object> profile = kakaoAccount != null ? (Map<String, Object>) kakaoAccount.get("profile") : null;
        String nickname = profile != null ? (String) profile.get("nickname") : "카카오사용자";

        if (email == null) {
            email = kakaoId + "@kakao.local"; // 임시 이메일
        }

        log.info("카카오 사용자: ID={}, 닉네임={}, 이메일={}", kakaoId, nickname, email);

        Optional<User> optionalUser = userRepository.findByUserId(kakaoId);
        User user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            log.info("기존 카카오 사용자 로그인: {}", user.getUserId());
        } else {
            user = User.builder()
                    .userId(kakaoId)
                    .name(nickname)
                    .email(email)
                    .nickname(nickname)
                    .phone("N/A") // ❗ 필수 컬럼 기본값
                    .password(UUID.randomUUID().toString())
                    .systemRole(SystemRole.USER)
                    .status(Status.ACTIVE)
                    .build();
            user = userRepository.save(user);
            log.info("새 카카오 사용자 등록: {}", user.getUserId());
        }

        Map<String, Object> modifiedAttributes = new HashMap<>();
        modifiedAttributes.put("id", kakaoId);
        modifiedAttributes.put("name", nickname);
        modifiedAttributes.put("email", email);
        modifiedAttributes.put("provider", "kakao");

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getSystemRole().name())),
                modifiedAttributes,
                "id"
        );
    }

    // ✅ 네이버 처리
    private OAuth2User processNaverUser(Map<String, Object> attributes) {
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        String naverId = "naver_" + response.get("id").toString();
        String name = (String) response.get("name");
        String email = (String) response.get("email");
        String nickname = name != null ? name : "네이버사용자";

        if (email == null) {
            email = naverId + "@naver.local"; // 임시 이메일
        }

        log.info("네이버 사용자: ID={}, 닉네임={}, 이메일={}", naverId, nickname, email);

        Optional<User> optionalUser = userRepository.findByUserId(naverId);
        User user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            log.info("기존 네이버 사용자 로그인: {}", user.getUserId());
        } else {
            user = User.builder()
                    .userId(naverId)
                    .name(nickname)
                    .email(email)
                    .nickname(nickname)
                    .phone("N/A") // ❗ 필수 컬럼 기본값
                    .password(UUID.randomUUID().toString())
                    .systemRole(SystemRole.USER)
                    .status(Status.ACTIVE)
                    .build();
            user = userRepository.save(user);
            log.info("새 네이버 사용자 등록: {}", user.getUserId());
        }

        Map<String, Object> modifiedAttributes = new HashMap<>();
        modifiedAttributes.put("id", naverId);
        modifiedAttributes.put("name", nickname);
        modifiedAttributes.put("email", email);
        modifiedAttributes.put("provider", "naver");

        // ✅ return 빠졌던 부분 추가
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getSystemRole().name())),
                modifiedAttributes,
                "id"
        );
    }
}
