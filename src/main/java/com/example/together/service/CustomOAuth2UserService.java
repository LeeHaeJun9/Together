package com.example.together.service;

import com.example.together.domain.User;
import com.example.together.repository.UserRepository;
import com.example.together.domain.SystemRole;
import com.example.together.domain.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();

        log.info("OAuth2 로그인 시도: registrationId = {}", registrationId);
        log.info("OAuth2 사용자 정보: {}", attributes);

        // 네이버 사용자 정보 처리
//        if ("naver".equals(registrationId)) {
//            return processNaverUser(attributes);
//        }

        // 카카오 사용자 정보 처리
        if ("kakao".equals(registrationId)) {
            return processKakaoUser(attributes);
        }

        // 지원하지 않는 소셜 로그인
        return oauth2User;
    }

//    private OAuth2User processNaverUser(Map<String, Object> attributes) {
//        // 네이버 사용자 정보는 'response' 키 아래에 중첩되어 있습니다.
//        @SuppressWarnings("unchecked")
//        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
//        String naverId = "naver_" + response.get("id").toString(); // 네이버 고유 ID
//        String name = (String) response.get("name");
//        String email = (String) response.get("email");
//
//        log.info("네이버 사용자: ID = {}, 이름 = {}, 이메일 = {}", naverId, name, email);
//
//        // DB에서 사용자 조회
//        Optional<User> optionalUser = userRepository.findByUserId(naverId);
//        User user;
//
//        if (optionalUser.isPresent()) {
//            user = optionalUser.get();
//            log.info("기존 네이버 사용자 로그인: {}", user.getUserId());
//        } else {
//            // 새 사용자 등록
//            user = User.builder()
//                    .userId(naverId)
//                    .name(name)
//                    .email(email)
//                    .nickname(name) // 네이버에서 제공하는 이름으로 닉네임 설정
//                    .password(UUID.randomUUID().toString()) // 소셜 로그인은 비밀번호가 필요 없음
//                    .systemRole(SystemRole.USER)
//                    .status(Status.ACTIVE)
//                    .build();
//            user = userRepository.save(user);
//            log.info("새 네이버 사용자 등록: {}", user.getUserId());
//        }
//
//        // OAuth2User 객체에 필요한 속성 구성
//        Map<String, Object> modifiedAttributes = new HashMap<>();
//        modifiedAttributes.put("id", naverId);
//        modifiedAttributes.put("name", name);
//        modifiedAttributes.put("email", email);
//        modifiedAttributes.put("provider", "naver");
//
//        // Spring Security 세션에 저장할 OAuth2User 객체 생성
//        return new DefaultOAuth2User(
//                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getSystemRole().name())),
//                modifiedAttributes,
//                "id"
//        );
//    }

    private OAuth2User processKakaoUser(Map<String, Object> attributes) {
        // 카카오 사용자 정보 구조: {id=123456789, kakao_account={email=..., profile={nickname=...}}}
        String kakaoId = "kakao_" + attributes.get("id").toString();
        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        String email = (String) kakaoAccount.get("email");
        @SuppressWarnings("unchecked")
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String nickname = (String) profile.get("nickname");

        log.info("카카오 사용자: ID = {}, 닉네임 = {}, 이메일 = {}", kakaoId, nickname, email);

        // DB에서 사용자 조회
        Optional<User> optionalUser = userRepository.findByUserId(kakaoId);
        User user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            log.info("기존 카카오 사용자 로그인: {}", user.getUserId());
        } else {
            // 새 사용자 등록
            user = User.builder()
                    .userId(kakaoId)
                    .name(nickname)
                    .email(email)
                    .nickname(nickname)
                    .password(UUID.randomUUID().toString())
                    .systemRole(SystemRole.USER)
                    .status(Status.ACTIVE)
                    .build();
            user = userRepository.save(user);
            log.info("새 카카오 사용자 등록: {}", user.getUserId());
        }

        // OAuth2User 객체에 필요한 속성 구성
        Map<String, Object> modifiedAttributes = new HashMap<>();
        modifiedAttributes.put("id", kakaoId);
        modifiedAttributes.put("name", nickname);
        modifiedAttributes.put("email", email);
        modifiedAttributes.put("provider", "kakao");

        // Spring Security 세션에 저장할 OAuth2User 객체 생성
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getSystemRole().name())),
                modifiedAttributes,
                "id"
        );
    }
}