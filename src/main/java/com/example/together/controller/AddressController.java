package com.example.together.controller;

import com.example.together.domain.Address;
import com.example.together.repository.AddressRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/address")
public class AddressController {

    private final RestTemplate restTemplate;
    private final AddressRepository addressRepository;

    @Value("${juso.api.key}")
    private String apiKey;

    /**
     * 주소 API 호출을 위한 헬퍼 메소드
     * UriComponentsBuilder를 사용해 URL을 안전하게 구축합니다.
     * @param keyword 검색어
     * @return API 응답의 JSON 노드
     * @throws JsonProcessingException JSON 파싱 오류 시
     */
    private JsonNode callJusoApi(String keyword) throws JsonProcessingException {
        // UriComponentsBuilder가 쿼리 파라미터를 자동으로 인코딩하므로,
        // 수동으로 URLEncoder.encode()를 호출할 필요가 없습니다.
        URI uri = UriComponentsBuilder.fromHttpUrl("https://www.juso.go.kr/addrlink/addrLinkApi.do")
                .queryParam("confmKey", apiKey)
                .queryParam("currentPage", 1)
                .queryParam("countPerPage", 10)
                .queryParam("keyword", keyword)
                .queryParam("resultType", "json")
                .build()
                .toUri();

        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(response.getBody());
    }

    // 단순 API 호출 (응답 JSON 확인용)
    @GetMapping
    public ResponseEntity<String> getAddress(@RequestParam String keyword) {
        try {
            JsonNode root = callJusoApi(keyword);
            return ResponseEntity.ok(root.toString());
        } catch (Exception e) {
            // 실제 서비스에서는 로깅 시스템을 활용하는 것이 좋습니다.
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("주소 조회 중 오류 발생: " + e.getMessage());
        }
    }

    // 주소 저장 API (DB에 저장 후 반환)
    @PostMapping("/save")
    public ResponseEntity<?> saveAddress(@RequestParam String keyword) {
        try {
            JsonNode root = callJusoApi(keyword);
            JsonNode jusoArray = root.path("results").path("juso");

            // 검색 결과가 없는 경우 404 Not Found 응답
            if (!jusoArray.isArray() || jusoArray.size() == 0) {
                return ResponseEntity.status(404).body("주소를 찾을 수 없습니다.");
            }

            JsonNode juso = jusoArray.get(0);
            String zipcode = juso.path("zipNo").asText();

            // 이미 존재하는 주소면 기존 주소 반환
            Optional<Address> existingAddress = addressRepository.findByZipcode(zipcode);
            if (existingAddress.isPresent()) {
                return ResponseEntity.ok(existingAddress.get());
            }

            Address address = Address.builder()
                    .zipcode(zipcode)
                    .city(juso.path("siNm").asText())
                    .district(juso.path("sggNm").asText())
                    .neighborhood(juso.path("emdNm").asText())
                    .street(juso.path("roadAddrPart1").asText())
                    .build();

            return ResponseEntity.ok(addressRepository.save(address));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("주소 저장 중 오류 발생: " + e.getMessage());
        }
    }
}