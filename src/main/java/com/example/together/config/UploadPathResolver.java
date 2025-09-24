package com.example.together.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 업로드 루트 경로를 통합적으로 결정해 주는 리졸버.
 * 우선순위: app.upload-path > org.zerock.upload.path > spring.servlet.multipart.location > 기본값(C:/upload)
 */
@Component
@Slf4j
public class UploadPathResolver {

  @Value("${app.upload-path:}")
  private String appUploadPath;

  @Value("${org.zerock.upload.path:}")
  private String legacyUploadPath;

  @Value("${spring.servlet.multipart.location:}")
  private String multipartLocation;

  public String resolvePath() {
    String raw = firstNonBlank(appUploadPath, legacyUploadPath, multipartLocation, "C:/upload");
    String normalized = raw.replace('\\', '/');
    String abs = Paths.get(normalized).toAbsolutePath().toString();
    log.debug("[UploadPathResolver] using upload path: {}", abs);
    return abs;
  }

  /** 정적 리소스 매핑 시 사용하는 file:// URI 형태 (끝에 / 포함) */
  public String resolveAsFileUri() {
    String uri = Paths.get(resolvePath()).toUri().toString();
    if (!uri.endsWith("/")) uri = uri + "/";
    return uri;
  }

  private static String firstNonBlank(String... arr) {
    for (String s : arr) {
      if (s != null && !s.isBlank()) return s.trim();
    }
    return null;
  }
}
