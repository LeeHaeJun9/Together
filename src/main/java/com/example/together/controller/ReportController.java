// src/main/java/com/example/together/controller/ReportController.java
package com.example.together.controller;

import com.example.together.domain.User;
import com.example.together.dto.report.ReportCreateDTO;
import com.example.together.repository.UserRepository;
import com.example.together.service.report.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    @PostMapping("/report")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody ReportCreateDTO dto, Authentication auth) {
        User user = userRepository.findByUserId(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + auth.getName()));
        Long id = reportService.create(user.getId(), dto);

        Map<String, Object> body = new HashMap<>();
        body.put("id", id);
        body.put("ok", true);
        body.put("message", "신고가 접수되었습니다.");
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}