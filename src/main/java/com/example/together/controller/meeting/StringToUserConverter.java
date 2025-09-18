package com.example.together.controller.meeting;

import com.example.together.domain.User;
import com.example.together.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class StringToUserConverter implements Converter<String, User> {

    private final UserService userService;

    public StringToUserConverter(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        return userService.findByUserId(source);
    }
}

