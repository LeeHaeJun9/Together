package com.example.together.config;


import com.example.together.controller.meeting.StringToUserConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.format.FormatterRegistry;


@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Value("${app.upload-path:file:./uploads}")
  private String uploadPath;
  @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
      String root = "file:///C:/upload/";
      registry.addResourceHandler("/upload/**")
          .addResourceLocations(root);

    registry.addResourceHandler("/files/**")
        .addResourceLocations(uploadPath.endsWith("/")? uploadPath : uploadPath + "/");


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:///" + uploadPath + "/");
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/");
        registry.addResourceHandler("/lib/**")
                .addResourceLocations("classpath:/static/lib/");
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/");
    }

    private final StringToUserConverter stringToUserConverter;

    public WebMvcConfig(StringToUserConverter stringToUserConverter) {
        this.stringToUserConverter = stringToUserConverter;

    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToUserConverter);
    }

}


