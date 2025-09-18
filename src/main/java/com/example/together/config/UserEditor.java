package com.example.together.config;

import com.example.together.domain.User;
import com.example.together.service.UserService;
import org.springframework.stereotype.Component;

import java.beans.PropertyEditorSupport;

@Component
public class UserEditor extends PropertyEditorSupport {

    private final UserService userService;

    public UserEditor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || text.isBlank()) {
            setValue(null);
        } else {
            User user = userService.findByUserId(text);
            setValue(user);
        }
    }
}

