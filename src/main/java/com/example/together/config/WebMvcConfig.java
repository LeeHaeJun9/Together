package com.example.together.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Value("${app.upload-path:file:./uploads}")
  private String uploadPath;

  @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
      String root = "file:///C:/upload/";
      registry.addResourceHandler("/upload/**")
          .addResourceLocations(root);
    }
}


