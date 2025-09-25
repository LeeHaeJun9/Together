package com.example.together.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Value("${org.zerock.upload.path:C:/upload}")
  private String uploadPath;


  @Value("${app.upload.url-prefix:/upload}")
  private String urlPrefix;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // OS 독립적으로 file:/// 형태로 변환
    String fileLocation = Paths.get(uploadPath).toAbsolutePath().normalize().toUri().toString();
    // 예) "/upload/**" URL로 들어오면 C:/upload/ 실제 파일을 서빙
    registry.addResourceHandler(urlPrefix.endsWith("/**") ? urlPrefix : (urlPrefix + "/**"))
        .addResourceLocations(fileLocation);
  }
}
