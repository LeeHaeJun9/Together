package com.example.together.config;


import com.example.together.controller.meeting.StringToUserConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${org.zerock.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:///" + uploadPath + "/");
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
